package com.plexobject.encode;

import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.encode.text.TextObjectCodec;

/**
 * This factory class provides implementation of code based on type
 * 
 * @author shahzad bhatti
 *
 */
public class ObjectCodeFactory {
    private static final ObjectCodec JSON_CODEC = new JsonObjectCodec();
    private static final ObjectCodec TEXT_CODEC = new TextObjectCodec();

    public static ObjectCodec getObjectCodec(CodecType type) {
        if (type == CodecType.JSON) {
            return JSON_CODEC;
        } else if (type == CodecType.TEXT) {
            return TEXT_CODEC;
        } else {
            throw new IllegalArgumentException("Unsupported codec");
        }
    }
}
