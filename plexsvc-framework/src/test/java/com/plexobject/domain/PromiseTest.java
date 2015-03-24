package com.plexobject.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.plexobject.handler.Handler;

public class PromiseTest {
    private boolean cancelHandlerInvoked;
    private boolean timedoutHandlerInvoked;
    private final Handler<Promise<Integer>> cancelHandler = new Handler<Promise<Integer>>() {
        @Override
        public void handle(Promise<Integer> request) {
            cancelHandlerInvoked = true;
        }
    };
    private final Handler<Promise<Integer>> timedoutHandler = new Handler<Promise<Integer>>() {
        @Override
        public void handle(Promise<Integer> request) {
            timedoutHandlerInvoked = true;
        }
    };

    @Test
    public void testDefaultConstructor() throws Exception {
        Promise<Integer> promise = new Promise<>();
        assertFalse(promise.isCancelled());
        assertFalse(promise.isTimedout());
        assertFalse(promise.isDone());
        promise.setValue(10);
        assertTrue(promise.isDone());
        assertEquals(new Integer(10), promise.get());
    }

    @Test(expected = IllegalStateException.class)
    public void testSetValueTwice() throws Exception {
        Promise<Integer> promise = new Promise<>();
        assertFalse(promise.isCancelled());
        assertFalse(promise.isTimedout());
        assertFalse(promise.isDone());
        promise.setValue(10);
        promise.setValue(10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetError() throws Exception {
        Promise<Integer> promise = new Promise<>();
        assertFalse(promise.isCancelled());
        assertFalse(promise.isTimedout());
        assertFalse(promise.isDone());
        promise.setError(new IllegalArgumentException());
        assertFalse(promise.isDone());
        promise.get();
    }

    @Test(expected = IllegalStateException.class)
    public void testSetErrorTwice() throws Exception {
        Promise<Integer> promise = new Promise<>();
        assertFalse(promise.isCancelled());
        assertFalse(promise.isTimedout());
        assertFalse(promise.isDone());
        promise.setError(new IllegalArgumentException());
        promise.setError(new IllegalArgumentException());
    }

    @Test
    public void testConstructor() throws Exception {
        Promise<Integer> promise = new Promise<>(1);
        assertFalse(promise.isCancelled());
        assertFalse(promise.isTimedout());
        assertTrue(promise.isDone());
        assertTrue(promise.isDone());
        assertEquals(new Integer(1), promise.get());
    }

    @Test(expected = ExecutionException.class)
    public void testErrorConstructor() throws Exception {
        Promise<Integer> promise = new Promise<>(new ExecutionException("msg",
                new IllegalArgumentException()));
        assertFalse(promise.isCancelled());
        assertFalse(promise.isTimedout());
        assertFalse(promise.isDone());
        Integer n = promise.get();
        System.out.println(n);
    }

    @Test(expected = CancellationException.class)
    public void testCancel() throws Exception {
        Promise<Integer> promise = new Promise<>();
        promise.setCancelHandler(cancelHandler);
        assertFalse(promise.isCancelled());
        assertFalse(promise.isTimedout());
        assertFalse(promise.isDone());
        assertTrue(promise.cancel(true));
        assertFalse(promise.cancel(true));
        assertTrue(promise.isCancelled());
        assertTrue(cancelHandlerInvoked);
        promise.get();
    }

    @Test(expected = TimeoutException.class)
    public void testBlockingGetWithoutValue() throws Exception {
        Promise<Integer> promise = new Promise<>();
        promise.setTimedoutHandler(timedoutHandler);
        try {
            promise.get(1, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            assertTrue(timedoutHandlerInvoked);
            throw e;
        }
    }

    @Test
    public void testBlockingGetWithValue() throws Exception {
        final Promise<Integer> promise = new Promise<>();
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10);
                    promise.setValue(20);
                } catch (Exception e) {
                }
            }
        }.start();
        Integer val = promise.get();
        assertEquals(new Integer(20), val);
    }
}
