package com.plexobject.metrics;

/**
 * This interface allow hooking external stats collector such as statsD
 * 
 * @author shahzad bhatti
 *
 */
public interface StatsCollector {
    void incrementCounter(String name);

    void recordExecutionTime(String name, long value);

    void recordGaugeValue(String name, long value);

    void recordSetEvent(String name, String value);

}
