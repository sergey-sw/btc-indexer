package com.ssau.btc.sys;

import com.intelli.ray.core.ManagedComponent;

import java.util.concurrent.*;

/**
 * Author: Sergey42
 * Date: 05.04.14 17:04
 */
@ManagedComponent(name = "ThreadManager")
public class ThreadManager {

    protected ScheduledExecutorService service = Executors.newScheduledThreadPool(8);
    protected ExecutorService executorService = Executors.newFixedThreadPool(8);

    public ScheduledFuture<?> scheduleTask(Runnable runnable, long delay, TimeUnit timeUnit) {
        return service.scheduleWithFixedDelay(runnable, 0, delay, timeUnit);
    }

    public Future<?> submitTask(Runnable runnable) {
        return executorService.submit(runnable);
    }
}
