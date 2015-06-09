package com.plexobject.domain;

/**
 * This class validates arguments
 * 
 * @author shahzad bhatti
 *
 */
public class Preconditions {
    public static void checkArgument(boolean pred, String message) {
        if (!pred) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkArgument(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkEmpty(String str, String message) {
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void requireNotNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <T> T checkNotNull(T obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }
}