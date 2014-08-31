package com.plexobject.metrics;

public interface ServiceMetricsMBean {
    /**
     * This method returns name of the service
     * 
     * @return
     */
    String getName();

    /**
     * This method returns number of times that this service was invoked
     * 
     * @return
     */
    long getSuccessInvocations();

    /**
     * This method returns number of times that this service had errors
     * 
     * @return
     */
    long getErrorInvocations();

    /**
     * This method returns accumulated value for this service
     * 
     * @return
     */
    long getAccumulatedResponseTime();

    /**
     * This method returns timestamp for last successful request
     * 
     * @return
     */
    long getLastSuccessRequestTime();

    /**
     * This method returns timestamp for last error request
     * 
     * @return
     */
    long getLastErrorRequestTime();

    /**
     * This method returns start time
     * 
     * @return
     */
    long getStartTime();

    /**
     * This method returns min value
     * 
     * @return
     */
    long getMin();

    /**
     * This method returns max value
     * 
     * @return
     */
    long getMax();

    /**
     * This method returns last value
     * 
     * @return
     */
    long getLast();

}
