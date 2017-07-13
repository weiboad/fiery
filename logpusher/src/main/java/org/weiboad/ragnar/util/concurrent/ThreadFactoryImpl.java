package org.weiboad.ragnar.util.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactoryImpl implements ThreadFactory {

    private AtomicInteger index = new AtomicInteger(0);

    private String prefix;

    public ThreadFactoryImpl(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        return new Thread(r, this.prefix + this.index.incrementAndGet());
    }
}
