package com.plexobject.encode;

import java.util.Map;

/**
 * This interface defines methods to convert object to text and back to object
 * 
 * @author shahzad bhatti
 *
 */
public interface ObjectCodec {
    CodecType getType();

    /**
     * This method serializes given object into string using underlying encoder
     * 
     * @param obj
     * @return
     * @throws EncodingException
     */
    <T> String encode(T obj) throws EncodingException;

    /**
     * This method deserializes string into an object using underlying encoder
     * 
     * @param text
     * @param type
     * @param params
     * @return
     * @throws EncodingException
     */
    <T> T decode(String text, Class<?> type, Map<String, Object> params)
            throws EncodingException;
}
