package com.datadog.task;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.datadog.task.model.AggregatedStatistics;
import com.datadog.task.model.NewLogReceivedEvent;
import com.datadog.task.storage.StatisticsRepository;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TrafficAlertTest {

    private TrafficAlert trafficAlert;

    private Console console;

    private StatisticsRepository statisticsRepository;

    final int thresholdPerSec = 10;

    final int timeWindowInSec = 10;


    @BeforeEach
    void setUp() {
        console = mock(Console.class);
        statisticsRepository = mock(StatisticsRepository.class);
        trafficAlert = new TrafficAlert(thresholdPerSec, timeWindowInSec, statisticsRepository, console);
        trafficAlert.initialize();
    }
    @Test
    void trafficBelowThresholdShouldNotTriggerAlarm() {
        //given
        AggregatedStatistics aggregatedStatistics = new AggregatedStatistics();
        aggregatedStatistics.increaseRequests(thresholdPerSec * timeWindowInSec - 1);
        when(statisticsRepository.getAggregatedStatics(timeWindowInSec)).thenReturn(aggregatedStatistics);

        //when
        trafficAlert.checkTraffic(new NewLogReceivedEvent());

        //then
        verify(console, never()).showStatistics(anyString());
    }

    @Test
    void trafficAboveThresholdShouldTriggerAlarmOnlyOnce() {
        //given
        AggregatedStatistics aggregatedStatistics = new AggregatedStatistics();
        aggregatedStatistics.increaseRequests(thresholdPerSec * timeWindowInSec);
        when(statisticsRepository.getAggregatedStatics(timeWindowInSec)).thenReturn(aggregatedStatistics);

        //when
        trafficAlert.checkTraffic(new NewLogReceivedEvent());
        trafficAlert.checkTraffic(new NewLogReceivedEvent());

        //then
        ArgumentCaptor<String> alertCaptor = ArgumentCaptor.forClass(String.class);
        verify(console, times(1)).showAlert(alertCaptor.capture());
        assertTrue(alertCaptor.getValue().startsWith("High traffic generated an alert"));
    }

    @Test
    void trafficAlertShouldEndWhenTrafficDrops() throws InterruptedException {
        //given
        AggregatedStatistics aggregatedStatisticsHighTraffic = new AggregatedStatistics();
        aggregatedStatisticsHighTraffic.increaseRequests(thresholdPerSec * timeWindowInSec);

        AggregatedStatistics aggregatedStatisticsLowTraffic = new AggregatedStatistics();
        aggregatedStatisticsLowTraffic.increaseRequests(thresholdPerSec * timeWindowInSec - 1);
        when(statisticsRepository.getAggregatedStatics(timeWindowInSec)).thenReturn(aggregatedStatisticsHighTraffic, aggregatedStatisticsLowTraffic);

        //when
        trafficAlert.checkTraffic(new NewLogReceivedEvent());

        TimeUnit.SECONDS.sleep(1L);

        ArgumentCaptor<String> alertCaptor = ArgumentCaptor.forClass(String.class);
        verify(console, times(1)).showAlert(alertCaptor.capture());
        assertTrue(alertCaptor.getValue().startsWith("High traffic generated an alert"));

        ArgumentCaptor<String> endAlertCaptor = ArgumentCaptor.forClass(String.class);
        verify(console, times(1)).endAlert(endAlertCaptor.capture());
        assertTrue(endAlertCaptor.getValue().startsWith("High traffic alert ended"));
    }

    @AfterEach
    void tearDown() {
        trafficAlert.close();
    }
}