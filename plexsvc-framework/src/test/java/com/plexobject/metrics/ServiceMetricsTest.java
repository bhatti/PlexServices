package com.plexobject.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServiceMetricsTest {
    private static final double EPSILON = 0.000001;
    private long started = System.currentTimeMillis();
    private ServiceMetrics metrics = new ServiceMetrics(null, "test");

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testName() throws Exception {
        assertEquals("test", metrics.getName());
        assertTrue(metrics.getStartTime() >= started);
    }

    @Test
    public void testGetAccumulatedResponseTime() throws Exception {
        for (int i = 0; i < 100; i++) {
            metrics.addResponseTime(10);
        }
        assertEquals(1000, metrics.getAccumulatedResponseTime());
        assertEquals(10, metrics.getLast());
    }

    @Test
    public void testGetSummary() throws Exception {
        for (int i = 1; i < 10000; i++) {
            metrics.addResponseTime(i % 10);
        }
        assertTrue(metrics.getSummary().contains("percentile"));
    }

    @Test
    public void testInvocations() throws Exception {
        for (int i = 0; i < 100; i++) {
            metrics.addResponseTime(1);
            if (i % 10 == 0) {
                metrics.incrementErrors();
            }
        }
        assertEquals(100, metrics.getSuccessInvocations());

        assertEquals(10, metrics.getErrorInvocations());

        assertTrue(metrics.getLastSuccessRequestTime() >= started);

        assertTrue(metrics.getLastErrorRequestTime() >= started);
    }

    @Test
    public void testGetPercentile() throws Exception {
        for (int i = 0; i < 10000; i++) {
            metrics.addResponseTime(10);
            if (i % 2 == 0) {
                metrics.addResponseTime(12);
            } else {
                metrics.addResponseTime(8);
            }
        }
        Map<Double, Sampleable> percentiles = metrics.getPercentile()
                .getPercentiles();
        String value = metrics.getPercentileValue();
        assertTrue(value.contains("99"));
        assertEquals(12.0, percentiles.get(99.0).sample(), EPSILON);
        assertEquals(12.0, percentiles.get(90.0).sample(), EPSILON);
        assertEquals(12.0, percentiles.get(80.0).sample(), EPSILON);
        assertEquals(10.0, percentiles.get(50.0).sample(), EPSILON);
    }

    @Test
    public void testGetTotalThroughput() throws Exception {
        for (int i = 0; i < 100; i++) {
            metrics.addResponseTime(1);
        }
        Thread.sleep(50);
        assertTrue(metrics.getTotalThroughput() > 1000);
    }

    @Test
    public void testMeans() throws Exception {
        for (int i = 0; i < 10000; i++) {
            metrics.addResponseTime(10);
            if (i % 2 == 0) {
                metrics.addResponseTime(12);
            } else {
                metrics.addResponseTime(8);
            }
        }
        assertEquals(10.0, metrics.getTotalMean(), EPSILON);
        assertEquals(10.0, metrics.getRunningMean(), EPSILON);
    }

    @Test
    public void testMinMax() throws Exception {
        metrics.addResponseTime(10);
        metrics.addResponseTime(100);
        assertEquals(10, metrics.getMin());
        assertEquals(90, metrics.getRange());
        assertEquals(100, metrics.getMax());
    }
}
