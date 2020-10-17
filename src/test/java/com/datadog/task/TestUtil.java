package com.datadog.task;

import com.datadog.task.model.LogRecord;
import com.datadog.task.model.LogRecord.LogRecordBuilder;
import java.time.Instant;
import java.util.Random;

public class TestUtil {

    private static final String[] SECTIONS = new String[] {"/api", "/datadog", "/user", "/news"};

    private static final String[] AUTHS = new String[] {"james", "john", "jane"};

    private static final String[] CLIENT_IPS = new String[] {"127.0.0.1", "255.255.255.255", "192.168.0.1"};

    private static final Random RANDOM = new Random();

    private TestUtil() {
    }

    public static LogRecord randomLogRecordNSecondAgo(long n) {
        LogRecordBuilder builder = LogRecordBuilder.aLogRecord();
        return builder.withTimestamp(Instant.now().getEpochSecond() - n)
                .withSection(random(SECTIONS)).withRequest(random(SECTIONS)).withClientIp(random(CLIENT_IPS))
                .withAuth(random(AUTHS)).build();
    }
    public static LogRecord randomLogRecord() {
        return randomLogRecordNSecondAgo(0L);
    }

    private static String random(String[] array) {
        return array[RANDOM.nextInt(array.length)];
    }
}
