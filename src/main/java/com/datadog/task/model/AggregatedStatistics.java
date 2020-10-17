package com.datadog.task.model;

import java.util.StringJoiner;

public class AggregatedStatistics {

    private long totalRequestCount;

    private final Counter sectionCounter;

    private final Counter clientIpCounter;

    private final Counter authCounter;

    public AggregatedStatistics() {
        this.totalRequestCount = 0L;
        this.sectionCounter = new Counter();
        this.clientIpCounter = new Counter();
        this.authCounter = new Counter();
    }

    public void increaseRequests(long requestNumber) {
        totalRequestCount += requestNumber;
    }

    public long getTotalRequestCount() {
        return totalRequestCount;
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

    @Override
    public String toString() {
        return new StringJoiner(", ", AggregatedStatistics.class.getSimpleName() + "[", "]")
                .add("totalRequestCount=" + totalRequestCount)
                .add("sectionCounter=" + sectionCounter)
                .add("clientIpCounter=" + clientIpCounter)
                .add("authCounter=" + authCounter)
                .toString();
    }
}
