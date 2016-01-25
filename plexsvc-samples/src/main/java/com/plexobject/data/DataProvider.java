package com.plexobject.data;


/**
 * This interface defines a method for producing data fields given input
 * 
 * @author shahzad bhatti
 *
 */
public interface DataProvider extends DataProducer {
    /**
     * This method returns required input fields
     * 
     * @return
     */
    MetaFields getInputFields();

    /**
     * This method returns output fields information
     * 
     * @return
     */
    MetaFields getOutputFields();

}
