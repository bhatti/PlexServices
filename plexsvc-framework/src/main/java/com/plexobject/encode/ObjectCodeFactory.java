package com.plexobject.encode;

import com.plexobject.encode.json.JsonObjectCodec;

public class ObjectCodeFactory {
    private static final ObjectCodec JSON_CODEC = new JsonObjectCodec();

    public static ObjectCodec getObjectCodec(CodecType type) {
        if (type == CodecType.JSON) {
            return JSON_CODEC;
        } else {
            throw new IllegalArgumentException("Unsupported codec");
        }
    }
}
