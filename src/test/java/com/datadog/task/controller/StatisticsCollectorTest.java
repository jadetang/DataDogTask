package com.datadog.task.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datadog.task.model.AggregatedStatistics;
import com.datadog.task.storage.StatisticsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StatisticsCollectorTest {

    private final int timeRangeInSec = 10;
    private StatisticsRepository statisticsRepository;
    private StatisticsCollector statisticsCollector;

    @BeforeEach
    void setUp() {
        statisticsRepository = mock(StatisticsRepository.class);
        statisticsCollector = new StatisticsCollector(timeRangeInSec, 5, statisticsRepository);
        statisticsCollector.initialize();
    }

    @Test
    void shouldFormatMessageCorrectly() {
        //given
        final long requestNumber = 999L;
        AggregatedStatistics aggregatedStatistics = new AggregatedStatistics(timeRangeInSec);
        aggregatedStatistics.increaseRequests(requestNumber);
        when(statisticsRepository.getAggregatedStatics(timeRangeInSec)).thenReturn(aggregatedStatistics);
        //when
        final String message = statisticsCollector.formatMessage(aggregatedStatistics);
        //then
        assertTrue(message.contains(String.valueOf(requestNumber)));
    }

    @AfterEach
    void tearDown() {
        statisticsCollector.close();
    }
}