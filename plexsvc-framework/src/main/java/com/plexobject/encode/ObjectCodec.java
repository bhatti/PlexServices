package com.plexobject.encode;

import java.util.Map;

public interface ObjectCodec {
    <T> String encode(T obj);

    <T> T decode(String text, Class<?> type, Map<String, Object> params);
}
