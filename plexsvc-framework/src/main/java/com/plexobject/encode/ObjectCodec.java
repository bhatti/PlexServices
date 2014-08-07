package com.plexobject.encode;

import java.util.Map;

/**
 * This interface defines methods to convert object to text and back to object
 * 
 * @author shahzad bhatti
 *
 */
public interface ObjectCodec {
    <T> String encode(T obj);

    <T> T decode(String text, Class<?> type, Map<String, Object> params);
}
