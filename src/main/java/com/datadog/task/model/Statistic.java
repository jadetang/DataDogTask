package com.datadog.task.model;

/**
 * A class stores statistic for one second.
 */
public class Statistic {

    //The epoch second this class hold statistic for
    private final long timestamp;

    private final Counter sectionCounter;

    private final Counter clientIpCounter;

    private final Counter authCounter;

    private long requestNumber;

    public Statistic(Long timestamp) {
        this.timestamp = timestamp;
        this.sectionCounter = new Counter();
        this.clientIpCounter = new Counter();
        this.authCounter = new Counter();
        this.requestNumber = 0L;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void update(LogRecord record) {
        sectionCounter.increase(record.getSection());
        clientIpCounter.increase(record.getClientIp());
        authCounter.increase(record.getAuth());
        requestNumber++;
    }

    public long getRequestNumber() {
        return requestNumber;
    }

    public Counter getSectionCounter() {
        return sectionCounter;
    }

    public Counter getClientIpCounter() {
        return clientIpCounter;
    }

    public Counter getAuthCounter() {
        return authCounter;
    }
}