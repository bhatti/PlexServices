package com.plexobject.metrics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plexobject.domain.Preconditions;

/**
 * This class creates percentile based on sampling of input data
 * 
 * @author shahzad bhatti
 * @param <T>
 */
public class Percentile<T extends Number & Comparable<T>> implements
        Serializable {
    private static final long serialVersionUID = 1L;

    private static final int MAX_BUFFER_SIZE = 10001;

    private final Sampler sampler;

    private final Map<Integer, Sampleable> statsByPercentile;
    private final LinkedList<T> samples = new LinkedList<>();

    private final LinkedBlockingDeque<ArrayList<T>> sampleQueue;
    private final ArrayList<T> allSamples = new ArrayList<T>();

    /**
     * Creates a new percentile tracker.
     * 
     * @param samplePercent
     *            The percent of events to sample [0, 100].
     * @param percentiles
     *            The percentiles to track.
     */
    public Percentile(float samplePercent, int... percentiles) {
        this(new Sampler(samplePercent), percentiles);
    }

    /**
     * Creates a new percentile tracker.
     * 
     * @param sampler
     *            The sampler to use for selecting recorded events.
     * @param percentiles
     *            The percentiles to track.
     */
    public Percentile(Sampler sampler, int... percentiles) {
        this(1, sampler, percentiles);
    }

    /**
     * Creates a new percentile tracker. A percentile tracker will randomly
     * sample recorded events with the given sampling rate, and will
     * automatically register variables to track the percentiles requested. When
     * allowFlushAfterSample is set to true, once the last percentile is
     * sampled, all recorded values are flushed in preparation for the next
     * window; otherwise, the percentile is calculated using the moving window
     * of the most recent values.
     * 
     * @param numSampleWindows
     *            How many sampling windows are used for calculation.
     * @param sampler
     *            The sampler to use for selecting recorded events. You may set
     *            sampler to null to sample all input.
     * @param percentiles
     *            The percentiles to track.
     */
    public Percentile(int numSampleWindows, Sampler sampler, int... percentiles) {
        Preconditions.checkArgument(numSampleWindows >= 1,
                "Must have one or more sample windows.");
        Preconditions.checkNotNull(percentiles, "null percentiles");
        Preconditions.checkArgument(percentiles.length > 0,
                "Must specify at least one percentile.");

        this.sampler = sampler;

        sampleQueue = new LinkedBlockingDeque<ArrayList<T>>(numSampleWindows);

        statsByPercentile = new HashMap<>();

        for (int i = 0; i < percentiles.length; i++) {
            boolean sortFirst = i == 0;
            PercentileVar stat = new PercentileVar(percentiles[i], sortFirst);
            statsByPercentile.put(percentiles[i], stat);
        }
    }

    /**
     * Get the variables associated with this percentile tracker.
     * 
     * @return A map from tracked percentile
     */
    public Map<Integer, Sampleable> getPercentiles() {
        return new HashMap<>(statsByPercentile);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");

        for (Map.Entry<Integer, Sampleable> perE : getPercentiles().entrySet()) {
            if (sb.length() > 1) {
                sb.append(",");
            }
            sb.append("\"P" + perE.getKey() + "\": "
                    + ServiceMetrics.format(perE.getValue().sample()));
        }
        sb.append("}");
        return sb.toString();
    }

    Sampleable getPercentile(double percentile) {
        return statsByPercentile.get(percentile);
    }

    /**
     * Records an event.
     * 
     * @param value
     *            The value to record if it is randomly selected based on the
     *            sampling rate.
     */
    public void record(T value) {
        if (sampler == null || sampler.select()) {
            synchronized (samples) {
                samples.addLast(value);
                while (samples.size() > MAX_BUFFER_SIZE) {
                    samples.removeFirst();
                }
            }
        }
    }

    public class PercentileVar implements Sampleable, Serializable {
        private static final long serialVersionUID = 1L;
        public final double percentile;
        @JsonIgnore
        public final boolean sortFirst;

        PercentileVar(double percentile, boolean sortFirst) {
            this.percentile = percentile;
            this.sortFirst = sortFirst;
        }

        @Override
        public double sample() {
            synchronized (samples) {
                if (sortFirst) {
                    sortSamples();
                }

                if (allSamples.isEmpty()) {
                    return 0d;
                }

                int maxIndex = allSamples.size() - 1;
                double selectIndex = maxIndex * percentile / 100;
                selectIndex = selectIndex < 0d ? 0d : selectIndex;
                selectIndex = selectIndex > maxIndex ? maxIndex : selectIndex;

                int indexLeft = (int) selectIndex;
                if (indexLeft == maxIndex) {
                    return allSamples.get(indexLeft).doubleValue();
                }

                double residue = selectIndex - indexLeft;
                return allSamples.get(indexLeft).doubleValue() * (1 - residue)
                        + allSamples.get(indexLeft + 1).doubleValue() * residue;
            }
        }

        private void sortSamples() {
            if (sampleQueue.remainingCapacity() == 0) {
                sampleQueue.removeFirst();
            }
            sampleQueue.addLast(new ArrayList<T>(samples));

            samples.clear();

            allSamples.clear();
            for (ArrayList<T> sample : sampleQueue) {
                allSamples.addAll(sample);
            }

            Collections.sort(allSamples);
        }

        @Override
        public String toString() {
            return percentile + "=" + sample();
        }

    }
}
