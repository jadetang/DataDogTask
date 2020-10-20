package com.datadog.task.controller;

import com.datadog.task.model.NewLogReceivedEvent;
import com.datadog.task.storage.StatisticsRepository;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Whenever there is a new log, this class check the total traffic for a time window. If the total traffic for the past
 * N seconds exceeds a threshold on average, it will generate an alert message. Whenever the total traffic drops again
 * below that threshold on average for the past N seconds, it will clear the alert message.
 */
public class TrafficAlert extends LifeCycle {

    private static final Logger log = LoggerFactory.getLogger(TrafficAlert.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final AtomicBoolean inAlert;

    private final AtomicReference<String> alertMessage;

    private final int thresholdPerSec;

    private final int timeWindowInSec;

    private final StatisticsRepository statisticsRepository;

    private final ScheduledExecutorService executorService;

    /**
     * Construct a new TrafficAlert.
     *
     * @param thresholdPerSec      traffic alter threshold per second
     * @param timeWindowInSec      the size of time window in second.
     * @param statisticsRepository the statistic repository stores all statistics
     */
    public TrafficAlert(int thresholdPerSec, int timeWindowInSec, StatisticsRepository statisticsRepository) {
        this.thresholdPerSec = thresholdPerSec;
        this.timeWindowInSec = timeWindowInSec;
        this.statisticsRepository = statisticsRepository;
        this.inAlert = new AtomicBoolean();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.alertMessage = new AtomicReference<>();
    }

    @Subscribe
    @AllowConcurrentEvents
    public void checkTraffic(NewLogReceivedEvent event) {
        if (inAlert.get()) {
            return;
        }
        final long totalRequests = statisticsRepository.getTotalRequests(timeWindowInSec);
        log.debug("total requests {}", totalRequests);
        if (totalRequests >= thresholdPerSec * timeWindowInSec && inAlert
                .compareAndSet(false, true)) {
            log.info("High traffic generated, total request {}", totalRequests);
            alertMessage.set(String.format("High traffic generated an alert - hits = %d, triggered at %s",
                    totalRequests,
                    DATE_TIME_FORMATTER.format(Instant.now())));
        }
    }

    public boolean inAlert() {
        return inAlert.get();
    }

    public String getAlertMessage() {
        return alertMessage.get();
    }

    @Override
    void doInitialize() {
        //launch a thread try to reset the alert every second.
        executorService.scheduleAtFixedRate(() -> {
            if (inAlert.get()) {
                final long totalRequests = statisticsRepository.getTotalRequests(timeWindowInSec);
                log.debug("total requests {}", totalRequests);
                if (totalRequests < thresholdPerSec * timeWindowInSec) {
                    log.info("High traffic ended, total request {}", totalRequests);
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
