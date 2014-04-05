package com.ssau.btc.sys;

import com.intelli.ray.core.ManagedComponent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author: Sergey42
 * Date: 05.04.14 17:04
 */
@ManagedComponent(name = "ThreadManager")
public class ThreadManager {

    protected ScheduledExecutorService service = Executors.newScheduledThreadPool(4);
    protected ExecutorService executorService = Executors.newFixedThreadPool(4);

    public void scheduleTask(Runnable runnable, long delay, TimeUnit timeUnit) {
        service.scheduleWithFixedDelay(runnable, 0, delay, timeUnit);
    }

    public void submitTask(Runnable runnable) {
        executorService.submit(runnable);
    }
}
