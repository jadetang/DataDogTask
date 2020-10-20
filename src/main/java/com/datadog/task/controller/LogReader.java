package com.datadog.task.controller;

import com.datadog.task.model.LogRecord;
import com.datadog.task.model.NewLogReceivedEvent;
import com.datadog.task.storage.StatisticsRepository;
import com.datadog.task.util.LogParser;
import com.google.common.eventbus.EventBus;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogReader extends LifeCycle {

    private static final Logger log = LoggerFactory.getLogger(LogReader.class);

    private final String filePath;

    private final StatisticsRepository statisticsRepository;

    private final ExecutorService executorService;

    private final EventBus eventBus;

    private Tailer tailer;

    public LogReader(String filePath, StatisticsRepository statisticsRepository, EventBus eventBus) {
        this.filePath = filePath;
        this.statisticsRepository = statisticsRepository;
        this.executorService = Executors.newSingleThreadExecutor();
        this.eventBus = eventBus;
    }

    @Override
    void doInitialize() {
        log.info("Create log tailer for file {}", filePath);
        final File file = new File(filePath);
        tailer = new Tailer(file, new LogTailerListener(statisticsRepository, eventBus), 5L);
        executorService.execute(tailer);
    }

    @Override
    void doClose() {
        tailer.stop();
        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            log.error("Error while stopping the scheduler thread pool");
            Thread.currentThread().interrupt();
        }
    }

    public static class LogTailerListener extends TailerListenerAdapter {

        private final StatisticsRepository statisticsRepository;

        private final EventBus eventBus;

        public LogTailerListener(StatisticsRepository statisticsRepository, EventBus eventBus) {
            this.statisticsRepository = statisticsRepository;
            this.eventBus = eventBus;
        }

        @Override
        public void handle(String logLine) {
            final Optional<LogRecord> logRecord = LogParser.parse(logLine);
            logRecord.ifPresent(l -> {
                eventBus.post(new NewLogReceivedEvent());
                statisticsRepository.addRecord(l);
            });
        }
    }
}
