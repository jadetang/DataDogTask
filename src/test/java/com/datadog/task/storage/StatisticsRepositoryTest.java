package com.datadog.task.storage;

import static com.datadog.task.TestUtil.randomLogRecord;
import static com.datadog.task.TestUtil.randomLogRecordNSecondAgo;
import static org.junit.jupiter.api.Assertions.*;

import com.datadog.task.model.AggregatedStatistics;
import com.datadog.task.model.LogRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatisticsRepositoryTest {

    private StatisticsRepository window;

    private final int windowSize = 120;

    @BeforeEach
    void setUp() {
        window = new StatisticsRepository(windowSize);
    }

    @Test
    void getAggregatedStatisticsShouldWork() {
        LogRecord logRecord = randomLogRecord();
        window.addRecord(logRecord);
        window.addRecord(logRecord);
        AggregatedStatistics aggregatedStatistics = window.getAggregatedStatics(1);
        assertEquals(2L, aggregatedStatistics.getTotalRequestCount());
        assertEquals(2, aggregatedStatistics.getAuthCounter().getAllItemCounts().get(logRecord.getAuth()));
        assertEquals(2, aggregatedStatistics.getClientIpCounter().getAllItemCounts().get(logRecord.getClientIp()));
        assertEquals(2, aggregatedStatistics.getSectionCounter().getAllItemCounts().get(logRecord.getSection()));
    }

    @Test
    void aggregatedStatisticsShouldNotContainOldRecord() {
        int kSecond = 10;
        LogRecord oldRecord = randomLogRecordNSecondAgo(kSecond + 1);
        window.addRecord(oldRecord);
        AggregatedStatistics aggregatedStatistics = window.getAggregatedStatics(kSecond);
        assertEquals(0L, aggregatedStatistics.getTotalRequestCount());
        assertTrue(aggregatedStatistics.getSectionCounter().getAllItemCounts().isEmpty());
    }

    @Test
    void aggregatedStatisticsShouldContainsValidRecord() {
        int kSecond = 10;
        for (int i = 0; i < kSecond; i++) {
            window.addRecord(randomLogRecordNSecondAgo(i));
        }
        AggregatedStatistics aggregatedStatistics = window.getAggregatedStatics(kSecond);
        assertEquals(kSecond, aggregatedStatistics.getTotalRequestCount());
    }

    @Test
    void slideWindowShouldOnlyContainsLastKSecondStatistics() {
        // add records number which is twice of window size
        for (int i = 0; i < windowSize * 2; i++) {
            window.addRecord(randomLogRecordNSecondAgo(i));
        }
        AggregatedStatistics aggregatedStatistics = window.getAggregatedStatics(windowSize);
        assertEquals(windowSize, aggregatedStatistics.getTotalRequestCount());
    }
}