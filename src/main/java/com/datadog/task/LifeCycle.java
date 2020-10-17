package com.datadog.task;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class LifeCycle {

    private final AtomicBoolean initialized;

    public LifeCycle() {
        initialized = new AtomicBoolean(false);
    }

    public void initialize() {
        if (initialized.compareAndSet(false, true)) {
            doInitialize();
        }
    }

    abstract void doInitialize();

    abstract void doClose();

    public void close() {
        if (initialized.get()) {
            doClose();
        }
    }
}
