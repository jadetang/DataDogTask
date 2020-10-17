package com.datadog.task;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datadog.task.StatisticsCollector.StatisticReportThread;
import com.datadog.task.model.AggregatedStatistics;
import com.datadog.task.storage.StatisticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StaticReportThreadTest {

    private StatisticsRepository statisticsRepository;

    private StatisticReportThread reportThread;

    private final int timeRangeInSec = 10;

    @BeforeEach
    void setUp() {
        statisticsRepository = mock(StatisticsRepository.class);
        Console console = mock(Console.class);
        reportThread = new StatisticReportThread(statisticsRepository, console, timeRangeInSec, 5);

    }

    @Test
    void threadShouldReportMessage() {
        //given
        final long requestNumber = 999L;
        AggregatedStatistics aggregatedStatistics = new AggregatedStatistics();
        aggregatedStatistics.increaseRequests(requestNumber);
        when(statisticsRepository.getAggregatedStatics(timeRangeInSec)).thenReturn(aggregatedStatistics);
        //when
        final String message = reportThread.formatMessage(aggregatedStatistics);
        //then
        assertTrue(message.contains(String.valueOf(requestNumber)));
    }
}

