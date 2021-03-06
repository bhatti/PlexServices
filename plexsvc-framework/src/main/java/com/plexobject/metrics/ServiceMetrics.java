package com.plexobject.metrics;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class stores timing information for service metrics
 * 
 * @author shahzad bhatti
 * 
 * 
 */
public class ServiceMetrics implements ServiceMetricsMBean {
    private static final ThreadLocal<NumberFormat> NUMBER_FMT = new ThreadLocal<NumberFormat>() {
        @Override
        public NumberFormat initialValue() {
            return new DecimalFormat("#0.0");
        }
    };
    private static final ThreadLocal<DateFormat> DATE_FMT = new ThreadLocal<DateFormat>() {
        @Override
        public DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        }
    };

    private static final int SECONDS = 60;
    private static final float DEFAULT_SAMPLE_PERCENT = 10;
    private static final int[] DEFAULT_PERCENTILES = { 10, 25, 50, 80, 90, 99 };

    private final long started = System.currentTimeMillis();
    private final AtomicLong successInvocations = new AtomicLong();
    private final AtomicLong errorInvocations = new AtomicLong();
    private final AtomicLong accmulatedResponseValue = new AtomicLong();
    private long lastSuccessRequestTime;
    private long lastErrorRequestTime;
    private final AtomicInteger peakThroughput = new AtomicInteger();
    private final AtomicInteger lowestThoughput = new AtomicInteger();
    private final String name;
    private final Percentile<Long> percentile = new Percentile<Long>(
            DEFAULT_SAMPLE_PERCENT, DEFAULT_PERCENTILES);
    private final AtomicInteger[] transactionsPerSec = new AtomicInteger[SECONDS];
    private double accumulatedVariance = 0;
    private double runningMean = 0;
    private long minValue = Long.MAX_VALUE;
    private long maxValue = Long.MIN_VALUE;
    private long lastValue;
    private final StatsCollector statsd;

    public ServiceMetrics(StatsCollector statsd, String name) {
        this.statsd = statsd;
        this.name = name;
        for (int i = 0; i < SECONDS; i++) {
            transactionsPerSec[i] = new AtomicInteger();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getAccumulatedResponseTime() {
        return accmulatedResponseValue.get();
    }

    @Override
    public long getErrorInvocations() {
        return errorInvocations.get();
    }

    @Override
    public long getSuccessInvocations() {
        return successInvocations.get();
    }

    @Override
    public long getStartTime() {
        return started;
    }

    @Override
    public long getLastSuccessRequestTime() {
        return lastSuccessRequestTime;
    }

    @Override
    public long getLastErrorRequestTime() {
        return lastErrorRequestTime;
    }

    public void incrementErrors() {
        lastErrorRequestTime = System.currentTimeMillis();
        errorInvocations.incrementAndGet();
    }

    @SuppressWarnings("deprecation")
    public void addResponseTime(long value) {
        if (statsd != null) {
            statsd.incrementCounter(name + "Counter");
            statsd.recordExecutionTime(name + "ExecutionTime", value);
        }
        accmulatedResponseValue.addAndGet(value);
        successInvocations.incrementAndGet();
        lastSuccessRequestTime = System.currentTimeMillis();
        lastValue = value;
        percentile.record(value);
        long total = getSuccessInvocations();
        double delta = value - runningMean;
        runningMean += delta / total;
        accumulatedVariance += delta * (value - runningMean);

        // Update max/min.
        minValue = value < minValue ? value : minValue;
        maxValue = value > maxValue ? value : maxValue;
        final Date now = new Date();
        int tps = transactionsPerSec[now.getSeconds()].incrementAndGet();
        peakThroughput.set(Math.max(peakThroughput.get(), tps));
        lowestThoughput.set(Math.min(lowestThoughput.get(), tps));
        //
        transactionsPerSec[(now.getSeconds() + 1) % SECONDS].set(0);
    }

    public Percentile<Long> getPercentile() {
        return percentile;
    }

    public double getTotalThroughput() {
        long elapsed = System.currentTimeMillis() - started;
        return elapsed > 0 ? getSuccessInvocations() * 1000.0 / elapsed : 0;
    }

    public double getVariance() {
        return accumulatedVariance / getSuccessInvocations();
    }

    public double getStandardDeviation() {
        return Math.sqrt(getVariance());
    }

    public double getRunningMean() {
        return runningMean;
    }

    public double getTotalMean() {
        return runningMean;
    }

    @Override
    public long getMin() {
        return minValue;
    }

    @Override
    public long getMax() {
        return maxValue;
    }

    public long getRange() {
        return maxValue - minValue;
    }

    public String getPercentileValue() {
        return percentile.toString();
    }

    public String getSummary() {
        String lastErrorAt = getLastErrorRequestTime() > 0 ? "\", \"lastErrorAt\":\""
                + formatDate(getLastErrorRequestTime())
                : "";

        return "{\"name\":\"" + name + "\", \"startedAt\":\""
                + formatDate(getStartTime()) + "\", \"lastRequestAt\":\""
                + formatDate(getLastSuccessRequestTime()) + lastErrorAt
                + "\", \"successCount\":" + getSuccessInvocations()
                + ", \"errorsCount\":" + getErrorInvocations()
                + ", \"meanLatency\":" + format(getRunningMean())
                + ", \"min\": " + getMin() + ", \"maxLatency\":" + getMax()
                + ", \"percentile\":" + getPercentileValue() + "}";
    }

    @SuppressWarnings("deprecation")
    public double getLatestThroughput() {
        final Date now = new Date();
        int currentSec = now.getSeconds();
        int lastSec = currentSec > 0 ? currentSec - 1 : SECONDS - 1;
        return (transactionsPerSec[lastSec].get() > 0 ? transactionsPerSec[lastSec]
                .get() : transactionsPerSec[currentSec].get());
    }

    public double getPeakThroughput() {
        return peakThroughput.get();
    }

    public double getLowestThroughput() {
        return lowestThoughput.get();
    }

    @Override
    public long getLast() {
        return lastValue;
    }

    static final String format(double d) {
        return NUMBER_FMT.get().format(d);
    }

    static final String formatDate(long t) {
        return DATE_FMT.get().format(new Date(t));
    }

}
