package com.plexobject.data;

/**
 * This interface defines a method for producing data fields given input
 * 
 * @author shahzad bhatti
 *
 */
public interface DataProvider extends DataProducer, Comparable<DataProvider> {
    /**
     * This method returns required request fields
     * 
     * @return
     */
    MetaFields getRequestFields();

    /**
     * This method returns response fields information
     * 
     * @return
     */
    MetaFields getResponseFields();

}
