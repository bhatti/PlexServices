package com.plexobject.domain;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.plexobject.handler.Handler;

/**
 * This class defines promise to set value or error of future interface, where
 * value or exception can be only set once.
 * 
 * @author shahzad bhatti
 *
 * @param <T>
 */
public class Promise<T> implements Future<T> {
    private boolean cancelled;
    private boolean timedout;
    private T value;
    private Exception exception;
    private Handler<Promise<T>> cancelHandler;
    private Handler<Promise<T>> timedoutHandler;

    public Promise() {
    }

    public Promise(T value) {
        this.value = value;
    }

    public Promise(Exception e) {
        this.exception = e;
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (cancelled || value != null || exception != null) {
            return false;
        }
        cancelled = true;
        notifyAll();
        if (cancelHandler != null) {
            cancelHandler.handle(this);
        }
        return true;
    }

    @Override
    public synchronized boolean isCancelled() {
        return cancelled;
    }

    public synchronized boolean isTimedout() {
        return timedout;
    }

    @Override
    public synchronized boolean isDone() {
        return value != null;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        try {
            return get(0, TimeUnit.MICROSECONDS);
        } catch (TimeoutException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public synchronized T get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        long started = System.nanoTime();
        while (value == null) {
            if (exception != null) {
                if (exception instanceof ExecutionException) {
                    throw (ExecutionException) exception;
                } else if (exception instanceof RuntimeException) {
                    throw (RuntimeException) exception;
                }
                throw new ExecutionException(exception);
            }
            if (cancelled) {
                throw new CancellationException("Operation cancelled");
            }
            wait(unit.toMillis(timeout));
            if (timeout > 0) {
                long elapsed = System.nanoTime() - started;
                if (elapsed > unit.toNanos(timeout)) {
                    timedout = true;
                    if (timedoutHandler != null) {
                        timedoutHandler.handle(this);
                    }
                    throw new TimeoutException("Request timed out for "
                            + unit.toMillis(timeout) + " milli-seconds");
                }
            }
        }
        return value;
    }

    /**
     * This method is used to set error. It can be only called once
     * 
     * @param e
     */
    public synchronized void setError(Exception e) {
        validateIfAlreadySet();
        this.exception = e;
        notifyAll();
    }

    /**
     * This method is used to set response value. It can be only called once
     * 
     * @param value
     */
    public synchronized void setValue(T value) {
        validateIfAlreadySet();

        this.value = value;
        notifyAll();
    }

    /**
     * This can be used to set callback when client code cancels future
     * operation
     * 
     * @param cancelHandler
     */
    public void setCancelHandler(Handler<Promise<T>> cancelHandler) {
        this.cancelHandler = cancelHandler;
    }

    /**
     * This callback is used to notify if request is times out
     * 
     * @param timedoutHandler
     */
    public void setTimedoutHandler(Handler<Promise<T>> timedoutHandler) {
        this.timedoutHandler = timedoutHandler;
    }

    private void validateIfAlreadySet() {
        if (exception != null) {
            throw new IllegalStateException(
                    "Future operation is already failed!");
        }
        if (value != null) {
            throw new IllegalStateException(
                    "Future operation is already completed!");
        }
    }

}
