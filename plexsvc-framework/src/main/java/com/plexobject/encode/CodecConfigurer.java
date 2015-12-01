package com.plexobject.encode;

/**
 * This interface allows configuration of underlying encoder
 * 
 * @author shahzad bhatti
 *
 */
public interface CodecConfigurer {
    void configureCodec(Object underlyingEncoder);
}
