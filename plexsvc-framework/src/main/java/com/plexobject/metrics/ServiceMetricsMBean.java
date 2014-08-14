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
	 * This method returns percentile values for this service
	 * 
	 * @return
	 */
	Percentile<Long> getPercentile();

	/**
	 * This method returns string value of percentile for this service
	 * 
	 * @return
	 */
	String getPercentileValue();

	/**
	 * This method returns throughput for requests/sec
	 * 
	 * @return
	 */
	double getTotalThroughput();

	/**
	 * This method returns peak throughput for requests/sec
	 * 
	 * @return
	 */
	double getPeakThroughput();

	/**
	 * This method returns lowest throughput for requests/sec
	 * 
	 * @return
	 */
	double getLowestThroughput();

	/**
	 * This method returns latest throughput for requests/sec
	 * 
	 * @return
	 */
	double getLatestThroughput();

	/**
	 * This method returns moving variance
	 * 
	 * @return
	 */
	double getVariance();

	/**
	 * This method returns moving standard deviation
	 * 
	 * @return
	 */
	double getStandardDeviation();

	/**
	 * This method returns moving average
	 * 
	 * @return
	 */
	double getRunningMean();

	/**
	 * This method returns total average since the service was created
	 * 
	 * @return
	 */
	double getTotalMean();

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

	/**
	 * This method returns difference between max and min
	 * 
	 * @return
	 */
	long getRange();

	/**
	 * This method returns stringified summary of service stats and performance
	 * information
	 * 
	 * @return
	 */
	String getSummary();
}
