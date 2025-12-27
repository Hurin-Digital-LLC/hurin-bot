package com.hurindigital.springgrokbot.discord;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class ThreadAutoCloseThreadFactory implements ThreadFactory {

    public static final String THREAD_NAME = "thread-auto-closer";

    public static ScheduledExecutorService scheduledExecutorService() {
        return Executors.newSingleThreadScheduledExecutor(new ThreadAutoCloseThreadFactory());
    }

    @Override
    public Thread newThread(@NonNull Runnable runnable) {
        Thread thread = new Thread(runnable, THREAD_NAME);
        thread.setDaemon(true);
        return thread;
    }

}
