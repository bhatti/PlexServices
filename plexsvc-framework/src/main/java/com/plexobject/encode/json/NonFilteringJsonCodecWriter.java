package com.plexobject.encode.json;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.plexobject.encode.ObjectCodecFilteredWriter;

public class NonFilteringJsonCodecWriter extends SimpleBeanPropertyFilter
        implements ObjectCodecFilteredWriter {

    private static final Logger logger = Logger
            .getLogger(NonFilteringJsonCodecWriter.class);
    private final SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
    private final PropertyFilter filter = new PropertyFilter() {
        @Override
        public void serializeAsField(Object pojo, JsonGenerator jgen,
                SerializerProvider prov, PropertyWriter writer)
                throws Exception {
            writer.serializeAsField(pojo, jgen, prov);
        }

        @Override
        public void serializeAsElement(Object elementValue, JsonGenerator jgen,
                SerializerProvider prov, PropertyWriter writer)
                throws Exception {
            writer.serializeAsElement(elementValue, jgen, prov);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void depositSchemaProperty(PropertyWriter writer,
                ObjectNode propertiesNode, SerializerProvider provider)
                throws JsonMappingException {
            writer.depositSchemaProperty(propertiesNode, provider);
        }

        @Override
        public void depositSchemaProperty(PropertyWriter writer,
                JsonObjectFormatVisitor objectVisitor,
                SerializerProvider provider) throws JsonMappingException {
            writer.depositSchemaProperty(objectVisitor);
        }
    };

    @Override
    public String writeString(Object underlyingEncoder, Object value) {
        if (!(underlyingEncoder instanceof ObjectMapper)) {
            throw new IllegalArgumentException(
                    "Unexpected value for object mapper");
        }
        ObjectMapper mapper = (ObjectMapper) underlyingEncoder;
        //
        if (logger.isDebugEnabled()) {
            logger.debug("Adding no filtering");
        }

        FilterProvider fProvider = simpleFilterProvider.addFilter(
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
