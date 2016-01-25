package com.plexobject.data;

import java.util.Collection;

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
     * @param input
     * @param expectedOutputFields
     *            - information about output fields
     * @return
     * @throws DataProviderException
     */
    Collection<DataFieldRow> produce(Collection<DataFieldRow> input,
            MetaFields expectedOutputFields) throws DataProviderException;
}
