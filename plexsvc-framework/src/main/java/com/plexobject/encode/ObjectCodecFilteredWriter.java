package com.plexobject.encode;

/**
 * 
 * @author shahzad bhatti
 *
 */
public interface ObjectCodecFilteredWriter {
    /**
     * This method may perform additional filtering before writing value as
     * string
     * 
     * @param underlying
     *            encoder
     * @param value
     * @return stringified value
     */
    String writeString(Object underlyingEncoder, Object value);
}
