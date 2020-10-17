package com.datadog.task;

import com.datadog.task.storage.StatisticsRepository;
import com.google.common.eventbus.EventBus;

public class HttpTrafficLogMonitor extends LifeCycle {

    private final StatisticsRepository statisticsRepository;

    private final Configuration configuration;

    private final LogReader logReader;

    private final StatisticsCollector statisticsCollector;

    private final TrafficAlert trafficAlert;

    private final EventBus eventBus;

    public HttpTrafficLogMonitor(Console console, Configuration configuration) {
        this.configuration = configuration;
        this.eventBus = new EventBus();
        this.statisticsRepository = new StatisticsRepository(configuration.getTimeWindowSizeInSec());
        this.logReader = new LogReader(configuration.getFilePath(), statisticsRepository, eventBus);
        this.statisticsCollector = new StatisticsCollector(configuration.getStatisticIntervalInSec(),
                configuration.getStatisticsTopK(), statisticsRepository, console);
        this.trafficAlert = new TrafficAlert(configuration.getThresholdPerSec(), configuration.getTimeWindowSizeInSec(),
                statisticsRepository, console);
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
