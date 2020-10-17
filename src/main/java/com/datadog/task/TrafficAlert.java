package com.datadog.task;

import com.datadog.task.model.AggregatedStatistics;
import com.datadog.task.model.NewLogReceivedEvent;
import com.datadog.task.storage.StatisticsRepository;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Whenever there is a new log, this class check the total traffic for a time window.
 * If the total traffic for the past N seconds exceeds a threshold on average,
 * it will send an alert message to the {@link Console}.
 * Whenever the total traffic drops again below that threshold on average for the past N seconds,
 * it send an alert recovered message to the {@link Console}.
 */
public class TrafficAlert extends LifeCycle {

    private static final Logger log = LoggerFactory.getLogger(TrafficAlert.class);

    private final AtomicBoolean inAlert;

    private final int thresholdPerSec;

    private final int timeWindowInSec;

    private final Console console;

    private final StatisticsRepository statisticsRepository;

    private final ScheduledExecutorService executorService;

    /**
     * Construct a new TrafficAlert.
     * @param thresholdPerSec traffic alter threshold per second
     * @param timeWindowInSec the size of time window in second.
     * @param statisticsRepository the statistic repository stores all statistics
     * @param console the console displaying the message
     */
    public TrafficAlert(int thresholdPerSec, int timeWindowInSec, StatisticsRepository statisticsRepository,
            Console console) {
        this.thresholdPerSec = thresholdPerSec;
        this.timeWindowInSec = timeWindowInSec;
        this.console = console;
        this.statisticsRepository = statisticsRepository;
        this.inAlert = new AtomicBoolean();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Subscribe
    @AllowConcurrentEvents
    public void checkTraffic(NewLogReceivedEvent event) {
        if (inAlert.get()) {
            return;
        }
        final AggregatedStatistics statics = statisticsRepository.getAggregatedStatics(timeWindowInSec);
        if (statics.getTotalRequestCount() >= thresholdPerSec * timeWindowInSec && inAlert.compareAndSet(false, true)) {
            console.showAlert(String.format("High traffic generated an alert - hits = %d, triggered at %s",
                    statics.getTotalRequestCount(),
                    Instant.now()));
        }
    }

    @Override
    void doInitialize() {
        executorService.scheduleAtFixedRate(() -> {
            if (inAlert.get()) {
                final AggregatedStatistics statistics = statisticsRepository.getAggregatedStatics(timeWindowInSec);
                if (statistics.getTotalRequestCount() < thresholdPerSec * timeWindowInSec) {
                    console.endAlert(String.format("High traffic alert ended at %s", Instant.now()));
                    inAlert.set(false);
                }
            }
        }, 0, 1L, TimeUnit.SECONDS);
    }

    @Override
    void doClose() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            log.error("Error while stopping the scheduler thread pool");
            Thread.currentThread().interrupt();
        }
    }
}
