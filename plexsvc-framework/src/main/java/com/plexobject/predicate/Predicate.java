package com.plexobject.predicate;

public interface Predicate<T> {
    /**
     * This method checks if given object should be accepted
     * 
     * @param e
     * @return true if object should be accepted
     */
    boolean accept(T obj);
}
