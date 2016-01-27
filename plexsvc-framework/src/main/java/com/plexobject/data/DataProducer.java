package com.plexobject.data;

/**
 * This interface defines method for producing collection of data fields given
 * input fields
 * 
 * @author shahzad bhatti
 *
 */
public interface DataProducer {
    /**
     * This method will produce set of data fields given input
     * 
     * @param requestFields
     *            - input parameter fields
     * @param responseFields
     *            - output fields
     * @param config
     *            - configuration parameters
     * @return
     * @throws DataProviderException
     */
    void produce(DataFieldRowSet requestFields, DataFieldRowSet responseFields,
            DataConfiguration config) throws DataProviderException;
}
