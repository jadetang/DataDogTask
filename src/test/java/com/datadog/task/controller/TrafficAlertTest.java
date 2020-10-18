package com.datadog.task.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datadog.task.model.AggregatedStatistics;
import com.datadog.task.model.NewLogReceivedEvent;
import com.datadog.task.storage.StatisticsRepository;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrafficAlertTest {

    final int thresholdPerSec = 10;
    final int timeWindowInSec = 10;
    private TrafficAlert trafficAlert;
    private StatisticsRepository statisticsRepository;

    @BeforeEach
    void setUp() {
        statisticsRepository = mock(StatisticsRepository.class);
        trafficAlert = new TrafficAlert(thresholdPerSec, timeWindowInSec, statisticsRepository);
        trafficAlert.initialize();
    }

    @Test
    void trafficBelowThresholdShouldNotTriggerAlarm() {
        //given
        AggregatedStatistics aggregatedStatistics = new AggregatedStatistics(timeWindowInSec);
        aggregatedStatistics.increaseRequests(thresholdPerSec * timeWindowInSec - 1);
        when(statisticsRepository.getAggregatedStatics(timeWindowInSec)).thenReturn(aggregatedStatistics);

        //when
        trafficAlert.checkTraffic(new NewLogReceivedEvent());

        //then
        assertFalse(trafficAlert.inAlert());
        assertNull(trafficAlert.getAlertMessage());
    }

    @Test
    void trafficAboveThresholdShouldTriggerAlarmOnlyOnce() {
        //given
        when(statisticsRepository.getTotalRequests(timeWindowInSec)).thenReturn(
                (long) (thresholdPerSec * timeWindowInSec));

        //when
        trafficAlert.checkTraffic(new NewLogReceivedEvent());

        //then
        assertTrue(trafficAlert.inAlert());
        String alertMessage = trafficAlert.getAlertMessage();
        assertNotNull(alertMessage);

        //when receive another new message, the alert message should not change
        trafficAlert.checkTraffic(new NewLogReceivedEvent());
        assertTrue(trafficAlert.inAlert());
        String newAlertMessage = trafficAlert.getAlertMessage();
        assertEquals(alertMessage, newAlertMessage);
    }

    @Test
    void trafficAlertShouldEndWhenTrafficDrops() throws InterruptedException {
        //given
        long highTraffic = (long) thresholdPerSec * timeWindowInSec;
        when(statisticsRepository.getTotalRequests(timeWindowInSec))
                .thenReturn(highTraffic, highTraffic - 1L);

        //when
        trafficAlert.checkTraffic(new NewLogReceivedEvent());

        assertTrue(trafficAlert.inAlert());

        //give the background sometime to reset the alert
        TimeUnit.SECONDS.sleep(2L);
        assertFalse(trafficAlert.inAlert());
    }

    @AfterEach
    void tearDown() {
        trafficAlert.close();
    }
}