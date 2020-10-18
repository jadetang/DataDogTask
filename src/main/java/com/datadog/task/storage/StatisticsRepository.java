package com.datadog.task.storage;

import com.datadog.task.model.AggregatedStatistics;
import com.datadog.task.model.LogRecord;
import com.datadog.task.model.Statistic;
import com.google.common.base.Preconditions;
import java.time.Instant;

/**
 * A repository holds statistics for a sliding time window, the time granularity is second. Internally, it use an array
 * as a ring buffer, each slot of the buffer stores statistic data of one second.
 */
public class StatisticsRepository {

    private final Statistic[] statistics;

    private final int timeWindowLengthInSec;

    /**
     * Construct a repository to store the statics for a limited sliding time window.
     *
     * @param timeWindowLengthInSec The length of the sliding time window in second.
     */
    public StatisticsRepository(int timeWindowLengthInSec) {
        Preconditions.checkArgument(timeWindowLengthInSec > 0, "Time window length should be a positive number.");
        this.timeWindowLengthInSec = timeWindowLengthInSec;
        this.statistics = new Statistic[timeWindowLengthInSec];
    }

    /**
     * Add record to the sliding window and update the statistic accordingly. If the record is older than the beginning
     * of the sliding window, the record will be ignored. If the record is newer than the current time, the record will
     * also be ignored.
     *
     * @param record The {@link LogRecord}.
     */
    public synchronized void addRecord(LogRecord record) {
        if (record == null || record.getTimestamp() == null) {
            return;
        }
        final long currentTime = Instant.now().getEpochSecond();
        if (!recordTimeInsideTheTimeWindow(record.getTimestamp(), currentTime)) {
            return;
        }
        final int index = toIndex(record.getTimestamp());
        if (statistics[index] == null || statistics[index].getTimestamp() != record.getTimestamp()) {
            statistics[index] = new Statistic(record.getTimestamp());
        }
        statistics[index].update(record);
    }

    /**
     * Return an {@link AggregatedStatistics} for last n seconds. If n is larger than the time window size, the time
     * window size will be taken as n.
     *
     * @param timeRangeInSec Last n seconds.
     * @return an {@link AggregatedStatistics} contains statistics for last timeRangeInSec seconds.
     */
    public synchronized AggregatedStatistics getAggregatedStatics(int timeRangeInSec) {
        timeRangeInSec = Math.min(timeWindowLengthInSec, timeRangeInSec);
        long timeStamp = Instant.now().getEpochSecond();
        int index = toIndex(timeStamp);
        AggregatedStatistics aggregatedStatistics = new AggregatedStatistics(timeRangeInSec);
        while (timeRangeInSec > 0) {
            if (statistics[index] != null && statistics[index].getTimestamp() == timeStamp) {
                final Statistic statistic = statistics[index];
                aggregatedStatistics.increaseRequests(statistic.getRequestNumber());
                aggregatedStatistics.getSectionCounter().merge(statistic.getSectionCounter());
                aggregatedStatistics.getClientIpCounter().merge(statistic.getClientIpCounter());
                aggregatedStatistics.getAuthCounter().merge(statistic.getAuthCounter());
            }
            timeRangeInSec--;
            index--;
            timeStamp--;
            // if index goes out side of the array, make the it goes to the tail of the array
            if (index < 0) {
                index += timeWindowLengthInSec;
            }
        }
        return aggregatedStatistics;
    }

    /**
     * Return total request number for last n seconds. If n is larger than the time window size, the time
     * window size will be taken as n. The performance is constant because it only collects the request number
     * not the other statistics such as {@link com.datadog.task.model.Counter}.
     * @param timeRangeInSec Last N seconds.
     * @return an {@link AggregatedStatistics} contains statistics for last timeRangeInSec seconds.
     */
    public synchronized Long getTotalRequests(int timeRangeInSec) {
        timeRangeInSec = Math.min(timeWindowLengthInSec, timeRangeInSec);
        long timeStamp = Instant.now().getEpochSecond();
        int index = toIndex(timeStamp);
        long totalRequests = 0L;
        while (timeRangeInSec > 0) {
            if (statistics[index] != null && statistics[index].getTimestamp() == timeStamp) {
                totalRequests += statistics[index].getRequestNumber();
            }
            timeRangeInSec--;
            index--;
            timeStamp--;
            // if index goes out side of the array, make the it goes to the tail of the array
            if (index < 0) {
                index += timeWindowLengthInSec;
            }
        }
        return totalRequests;
    }

    private int toIndex(long timestamp) {
        return (int) (timestamp % timeWindowLengthInSec);
    }

    private boolean recordTimeInsideTheTimeWindow(long recordTime, long currentTime) {
        return recordTime <= currentTime && recordTime > currentTime - timeWindowLengthInSec;
    }
}