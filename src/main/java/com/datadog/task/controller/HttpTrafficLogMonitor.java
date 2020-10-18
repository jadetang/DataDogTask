package com.datadog.task.controller;

import com.datadog.task.storage.StatisticsRepository;
import com.datadog.task.util.Config;
import com.google.common.eventbus.EventBus;

public class HttpTrafficLogMonitor extends LifeCycle {

    private final LogReader logReader;

    private final StatisticsCollector statisticsCollector;

    private final TrafficAlert trafficAlert;

    private final EventBus eventBus;

    public HttpTrafficLogMonitor(Config configuration) {
        this.eventBus = new EventBus();
        StatisticsRepository statisticsRepository = new StatisticsRepository(configuration.getTimeWindowSizeInSec());
        this.logReader = new LogReader(configuration.getFilePath(), statisticsRepository, eventBus);
        this.statisticsCollector = new StatisticsCollector(configuration.getStatisticIntervalInSec(),
                configuration.getStatisticsTopK(), statisticsRepository);
        this.trafficAlert = new TrafficAlert(configuration.getAlertThresholdPerSec(),
                configuration.getTimeWindowSizeInSec(),
                statisticsRepository);
    }

    public String getStatisticsMessage() {
        return statisticsCollector.getStatisticsMessage();
    }

    public boolean inAlert() {
        return trafficAlert.inAlert();
    }

    public String getAlertMessage() {
        return trafficAlert.getAlertMessage();
    }

    @Override
    void doInitialize() {
        logReader.initialize();
        statisticsCollector.initialize();
        trafficAlert.initialize();
        eventBus.register(trafficAlert);
    }

    @Override
    void doClose() {
        logReader.close();
        statisticsCollector.close();
        trafficAlert.close();
    }
}