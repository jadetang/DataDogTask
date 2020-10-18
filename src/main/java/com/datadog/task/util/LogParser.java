package com.datadog.task.util;

import com.datadog.task.model.LogRecord;
import com.datadog.task.model.LogRecord.LogRecordBuilder;
import com.google.common.annotations.VisibleForTesting;
import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

public class LogParser {

    public static final String AUTH = "auth";
    public static final String CLIENT_IP = "clientip";
    public static final String REQUEST = "request";
    public static final String TIMESTAMP = "timestamp";
    public static final String PATH_DELIMITER = "/";
    private static final Grok GROK;
    private static final DateTimeFormatter FORMATTER;

    static {
        GrokCompiler grokCompiler = GrokCompiler.newInstance();
        grokCompiler.registerDefaultPatterns();
        GROK = grokCompiler.compile("%{COMMONAPACHELOG}");
        FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy:H:m:s Z");
    }

    private LogParser() {

    }

    public static Optional<LogRecord> parse(String log) {
        final Map<String, Object> capture = GROK.capture(log);
        if (capture.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(parseMapToLogRecord(capture));
    }

    private static LogRecord parseMapToLogRecord(Map<String, Object> capture) {
        final LogRecordBuilder builder = LogRecordBuilder.aLogRecord();
        return builder.withAuth((String) capture.get(AUTH)).withClientIp((String) capture.get(CLIENT_IP))
                .withRequest((String) capture.get(REQUEST))
                .withSection(parseSection((String) capture.get(REQUEST)))
                .withTimestamp(parseTimeStamp((String) capture.get(TIMESTAMP)))
                .build();
    }

    @VisibleForTesting
    static Long parseTimeStamp(String timestamp) {
        try {
            return OffsetDateTime.parse(timestamp, FORMATTER).toEpochSecond();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @VisibleForTesting
    static String parseSection(String request) {
        if (request == null || request.length() == 0 || !request.startsWith(PATH_DELIMITER)) {
            return null;
        }
        final String[] paths = request.split(PATH_DELIMITER);
        if (paths.length >= 2) {
            return PATH_DELIMITER + paths[1];
        }
        return null;
    }
}
