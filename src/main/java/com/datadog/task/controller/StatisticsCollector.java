package com.datadog.task.controller;

import com.datadog.task.model.AggregatedStatistics;
import com.datadog.task.storage.StatisticsRepository;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that collect statistics from {@link StatisticsRepository} for last n seconds at fixed interval n seconds.
 */
public class StatisticsCollector extends LifeCycle {

    private static final Logger log = LoggerFactory.getLogger(StatisticsCollector.class);

    private final int intervalInSec;

    private final StatisticsRepository statisticsRepository;

    private final ScheduledExecutorService executorService;

    private final int topK;

    private final AtomicReference<String> statisticsMessage;

    public StatisticsCollector(int intervalInSec, int topK, StatisticsRepository statisticsRepository) {
        this.intervalInSec = intervalInSec;
        this.statisticsRepository = statisticsRepository;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.topK = topK;
        this.statisticsMessage = new AtomicReference<>();
    }

    @Override
    void doInitialize() {
        executorService.scheduleAtFixedRate(() -> {
            AggregatedStatistics statistics = statisticsRepository.getAggregatedStatics(intervalInSec);
            statisticsMessage.set(formatMessage(statistics));
        }, 0L, intervalInSec, TimeUnit.SECONDS);
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

    public String getStatisticsMessage() {
        return statisticsMessage.get();
    }

    @VisibleForTesting
    String formatMessage(AggregatedStatistics statistics) {
        final String messageTemplate = "Traffic statistic in last %d second:\n" +
                "Total Requests: %d, QPS: %.2f\n" +
                "Top %d sections:\n%s" +
                "Top %d auth:\n%s" +
                "Top %d client IP:\n%s";
        return String.format(messageTemplate, intervalInSec, statistics.getTotalRequest(), statistics.getQps(), topK,
                formatEntries(statistics.getSectionCounter().topK(topK)), topK,
                formatEntries(statistics.getAuthCounter().topK(topK)),
                topK, formatEntries(statistics.getClientIpCounter().topK(topK)));
    }

    private String formatEntries(List<Entry<String, Integer>> entries) {
        if (entries.size() == 0) {
            return "";
        }
        return entries.stream().map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n\t", "\t", "\n"));
    }
}
