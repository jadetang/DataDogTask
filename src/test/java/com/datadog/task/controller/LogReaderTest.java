package com.datadog.task.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.datadog.task.model.LogRecord;
import com.datadog.task.storage.StatisticsRepository;
import com.google.common.eventbus.EventBus;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogReaderTest {

    private LogReader logReader;

    private StatisticsRepository statisticsRepository;

    @BeforeEach
    void setUp() {
        statisticsRepository = mock(StatisticsRepository.class);
        final String filePath = LogReaderTest.class.getClassLoader().getResource("access.log").getPath();
        logReader = new LogReader(filePath, statisticsRepository, new EventBus());
    }

    @Test
    void shouldReadTheWholeRealFile() throws InterruptedException {
        logReader.initialize();
        //give the reader sometime to read the file.
        TimeUnit.SECONDS.sleep(1L);
        verify(statisticsRepository, times(511)).addRecord(any(LogRecord.class));
    }

    @AfterEach
    void tearDown() {
        logReader.close();
    }
}