package com.plexobject.encode;

import java.util.Map;

public class NoneObjectCodec implements ObjectCodec {
    @Override
    public CodecType getType() {
        return CodecType.NONE;
    }

    @Override
    public <T> String encode(T obj) throws EncodingException {
        return null;
    }

    @Override
    public <T> T decode(String text, Class<?> type, Map<String, Object> params)
            throws EncodingException {
        return null;
    }
}
