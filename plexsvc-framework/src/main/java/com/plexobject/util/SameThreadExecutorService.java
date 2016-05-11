package com.plexobject.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.plexobject.domain.Promise;

public class SameThreadExecutorService implements ExecutorService {

    @Override
    public void execute(Runnable command) {
        command.run();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public List<Runnable> shutdownNow() {
        return Collections.<Runnable> emptyList();
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        return false;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        try {
            T reply = task.call();
            return new Promise<T>(reply);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        task.run();
        return new Promise<T>(result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        task.run();
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        final List<Future<T>> result = new ArrayList<>();
        try {
            for (Callable<T> task : tasks) {
                T reply = task.call();
                Future<T> f = new Promise<T>(reply);
                result.add(f);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public <T> List<Future<T>> invokeAll(
            Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return invokeAll(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        try {
            for (Callable<T> task : tasks) {
                T reply = task.call();
                return reply;
            }
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
            long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        return invokeAny(tasks);
    }
};
