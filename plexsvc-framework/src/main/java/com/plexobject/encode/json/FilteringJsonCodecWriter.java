package com.plexobject.encode.json;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.plexobject.encode.ObjectCodecFilteredWriter;
import com.plexobject.handler.Request;

public class FilteringJsonCodecWriter extends SimpleBeanPropertyFilter
        implements ObjectCodecFilteredWriter {
    public static final String DEFAULT_FILTERED_NAMES_PARAM = "filteredFieldNames";

    private static final Logger logger = Logger
            .getLogger(FilteringJsonCodecWriter.class);
    private static final String RESPONSE_SUFFIX = "Response";
    private final Set<String> filteredFieldNames = new HashSet<>();
    private final SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();

    public FilteringJsonCodecWriter(Request request, String filteredFieldParam) {
        if (request != null) {
            String filteredFieldNamesValue = request
                    .getProperty(filteredFieldParam);
            if (filteredFieldNamesValue != null) {
                String[] fields = filteredFieldNamesValue.split("[,;\\s]");
                for (String field : fields) {
                    filteredFieldNames.add(field);
                }
                filteredFieldNames.add(request.getMethodName()
                        + RESPONSE_SUFFIX);
            }
        }
    }

    @Override
    public String writeString(Object underlyingEncoder, Object value) {
        if (!(underlyingEncoder instanceof ObjectMapper)) {
            throw new IllegalArgumentException(
                    "Unexpected value for object mapper");
        }
        ObjectMapper mapper = (ObjectMapper) underlyingEncoder;
        //
        if (filteredFieldNames.size() > 0) {
            SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter
                    .filterOutAllExcept(filteredFieldNames);
            if (logger.isDebugEnabled()) {
                logger.debug("Adding filter for all except"
                        + filteredFieldNames);
            }
            FilterProvider fProvider = simpleFilterProvider
                    .addFilter(
                            FilteringJsonCodecConfigurer.FILTERING_JSON_CODEC_CONFIGURER,
                            filter)
                    // setFailOnUnknownId: Ignore filtering the reference member
                    // fields
                    // Refer: https://jira.codehaus.org/browse/JACKSON-650
                    .setFailOnUnknownId(false);
            try {
                return mapper.writer(fProvider).writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse " + value, e);
            }
        } else {
            try {
                return mapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse " + value, e);
            }
        }
    }

    @Override
    protected boolean include(BeanPropertyWriter writer) {
        return true;
    }

    @Override
    protected boolean include(PropertyWriter writer) {
        return true;
    }
}