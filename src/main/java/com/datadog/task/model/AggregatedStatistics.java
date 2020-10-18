package com.datadog.task.model;

import java.util.StringJoiner;

/**
 * An class aggregates statistics for a continuous time range, provides these metrics
 * - total request
 * - QPS(request per second)
 * - top k sections
 * - top k client IP
 * - top k auths
 */
public class AggregatedStatistics {

    private final Counter sectionCounter;
    private final Counter clientIpCounter;
    private final Counter authCounter;
    private final int timeRangeLengthInSecond;
    private long totalRequest;

    public double getQps() {
        if (totalRequest == 0) {
            return 0.D;
        }
        return totalRequest / (double) timeRangeLengthInSecond;
    }

    public AggregatedStatistics(int timeRangeLengthInSecond) {
        this.totalRequest = 0L;
        this.sectionCounter = new Counter();
        this.clientIpCounter = new Counter();
        this.authCounter = new Counter();
        this.timeRangeLengthInSecond = timeRangeLengthInSecond;
    }

    public void increaseRequests(long requestNumber) {
        totalRequest += requestNumber;
    }

    public long getTotalRequest() {
        return totalRequest;
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
                .add("totalRequestCount=" + totalRequest)
                .add("sectionCounter=" + sectionCounter)
                .add("clientIpCounter=" + clientIpCounter)
                .add("authCounter=" + authCounter)
                .toString();
    }
}