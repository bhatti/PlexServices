package com.plexobject.data;


/**
 * This interface extends DataProvider and defines methods for
 * registering/unregistering as well as finding input data fields for output
 * fields
 * 
 * Note: When someone calls produce method, this DataProvider will use all
 * registered data providers to produce the output fields. The implementation
 * may execute necessary data providers serially or in parallel depending on the
 * available input fields.
 * 
 * @author shahzad bhatti
 *
 */
public interface DataProviders extends DataProducer {

    /**
     * This method will register data provider that requires input fields and
     * produces output fields
     * 
     * @param provider
     * @param input
     * @param output
     */
    void register(DataProvider provider);

    /**
     * This method will unregister data provider
     * 
     * @param provider
     */
    void unregister(DataProvider provider);
}
