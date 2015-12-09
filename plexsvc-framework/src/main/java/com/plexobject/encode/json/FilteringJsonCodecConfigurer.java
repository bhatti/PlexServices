package com.plexobject.encode.json;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.plexobject.encode.CodecConfigurer;

public class FilteringJsonCodecConfigurer implements CodecConfigurer {
    private static final Logger logger = Logger
            .getLogger(FilteringJsonCodecConfigurer.class);
    static final String FILTERING_JSON_CODEC_CONFIGURER = "FilteringJsonCodecConfigurer";

    public static class FilteringIntrospector extends
            JacksonAnnotationIntrospector {
        private static final long serialVersionUID = 1L;

        @Override
        public Object findFilterId(Annotated a) {
            return FILTERING_JSON_CODEC_CONFIGURER;
        }
    }

    private final AnnotationIntrospector filterIntrospector = new FilteringIntrospector();

    @Override
    public void configureCodec(Object underlyingEncoder) {
        if (underlyingEncoder instanceof ObjectMapper) {
            ObjectMapper mapper = (ObjectMapper) underlyingEncoder;
            if (logger.isDebugEnabled()) {
                logger.debug("Adding custom annotation inspector for filtering fields from responses");
            }
            mapper.setAnnotationIntrospector(filterIntrospector);
        }
    }

}
