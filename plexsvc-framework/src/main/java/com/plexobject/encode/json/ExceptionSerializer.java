package com.plexobject.encode.json;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.plexobject.util.ExceptionUtils;

public class ExceptionSerializer extends JsonSerializer<Throwable> {
    @Override
    public void serialize(Throwable exception, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {
        Map<String, Object> props = ExceptionUtils.toErrors(exception);
        jgen.writeObject(props);
    }
}
