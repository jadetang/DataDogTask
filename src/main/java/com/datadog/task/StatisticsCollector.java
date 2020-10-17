package com.datadog.task;

import com.datadog.task.model.AggregatedStatistics;
import com.datadog.task.storage.StatisticsRepository;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that collect statistics from {@link StatisticsRepository}
 * for last n seconds at fixed interval n seconds to {@link Console}
 */
public class StatisticsCollector extends LifeCycle {

    private static final Logger log = LoggerFactory.getLogger(StatisticsCollector.class);

    private final int intervalInSec;

    private final StatisticsRepository statisticsRepository;

    private final Console console;

    private final ScheduledExecutorService executorService;

    private final int topK;

    public StatisticsCollector(int intervalInSec, int topK, StatisticsRepository statisticsRepository, Console console) {
        this.intervalInSec = intervalInSec;
        this.statisticsRepository = statisticsRepository;
        this.console = console;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.topK = topK;
    }

    @Override
    public void doInitialize() {
        executorService.scheduleAtFixedRate(new StatisticReportThread(statisticsRepository, console, intervalInSec, topK), 1L, intervalInSec, TimeUnit.SECONDS);
    }

    @Override
    public void doClose() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            log.error("Error while stopping the scheduler thread pool");
            Thread.currentThread().interrupt();
        }
    }

    public static class StatisticReportThread implements Runnable {

        private final StatisticsRepository statisticsRepository;

        private final Console console;

        private final int timeRangeInSec;

        private final int topK;

        public StatisticReportThread(StatisticsRepository statisticsRepository, Console console, int timeRangeInSec, int topK) {
            this.statisticsRepository = statisticsRepository;
            this.console = console;
            this.timeRangeInSec = timeRangeInSec;
            this.topK = topK;
        }

        @Override
        public void run() {
            AggregatedStatistics statistics = statisticsRepository.getAggregatedStatics(timeRangeInSec);
            console.showStatistics(formatMessage(statistics));
        }

        @VisibleForTesting
        String formatMessage(AggregatedStatistics statistics) {
            final String messageTemplate = "Traffic statistic in last %d second:\n" +
                    "Total Requests: %d\n" +
                    "Top %d sections:\n%s" +
                    "Top %d auth:\n%s" +
                    "Top %d client IP:\n%s";
            return String.format(messageTemplate, timeRangeInSec, statistics.getTotalRequestCount(), topK,
                    formatEntries(statistics.getSectionCounter().topK(topK)), topK, formatEntries(statistics.getAuthCounter().topK(topK)),
                    topK, formatEntries(statistics.getClientIpCounter().topK(topK)));
        }

        private String formatEntries(List<Entry<String, Integer>> entries) {
            if (entries.size() == 0) {
                return "";
            }
            return entries.stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining("\n\t", "\t", "\n"));
        }
    }
}
