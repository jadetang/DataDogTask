package com.datadog.task.storage;

import com.datadog.task.model.AggregatedStatistics;
import com.datadog.task.model.LogRecord;
import com.datadog.task.model.Statistic;
import java.time.Instant;

public class StatisticsRepository {

    private final Statistic[] statistics;

    private final int timeWindowSize;
    /**
     * Construct a repository to store the statics for a specific time window. Internally, it use an array as a ring buffer,
     * each slot of the buffer stores statistic data of one second.
     * @param timeWindowSize The size of the sliding window.
     */
    public StatisticsRepository(int timeWindowSize) {
        this.timeWindowSize = timeWindowSize;
        this.statistics = new Statistic[timeWindowSize];
    }

    /**
     * Add record to the sliding window and update the statistics accordingly.
     * If the record is older than the beginning of the sliding window, the record will be ignored.
     * If the record is newer than the current time, the record will also be ignored.
     * @param record The {@link LogRecord}.
     */
    public synchronized void addRecord(LogRecord record) {
        final long currentTime = Instant.now().getEpochSecond();
        if (!validRecord(record, currentTime)) {
            return;
        }
        final int index = toIndex(record.getTimestamp());
        if (statistics[index] == null || statistics[index].getTimestamp() != record.getTimestamp()) {
            statistics[index] = new Statistic(record.getTimestamp());
        }
        statistics[index].update(record);
    }

    /**
     * Return an {@link AggregatedStatistics} for last k seconds. If k is larger than the window size,
     * @param k Last k seconds.
     * @return an {@link AggregatedStatistics} contains statistics for last k seconds.
     */
    public synchronized AggregatedStatistics getAggregatedStatics(int k) {
        k = Math.min(timeWindowSize, k);
        long timeStamp = Instant.now().getEpochSecond();
        int index = toIndex(timeStamp);
        AggregatedStatistics aggregatedStatistics = new AggregatedStatistics();
        while (k > 0) {
            if (statistics[index] != null && statistics[index].getTimestamp() == timeStamp) {
                final Statistic statistic = statistics[index];
                aggregatedStatistics.increaseRequests(statistic.getRequestNumber());
                aggregatedStatistics.getSectionCounter().merge(statistic.getSectionCounter());
                aggregatedStatistics.getClientIpCounter().merge(statistic.getClientIpCounter());
                aggregatedStatistics.getAuthCounter().merge(statistic.getAuthCounter());
            }
            k--;
            index--;
            timeStamp--;
            // out side of the ring buffer, goes to the tail of array
            if (index < 0) {
                index += timeWindowSize;
            }
        }
        return aggregatedStatistics;
    }

    private int toIndex(long timestamp) {
        return (int) (timestamp % timeWindowSize);
    }

    private boolean validRecord(LogRecord record, long currentTime) {
        if (record == null || record.getTimestamp() == null) {
            return false;
        }
        return record.getTimestamp() <= currentTime && record.getTimestamp() > currentTime - timeWindowSize;
    }
}
