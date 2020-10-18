package com.datadog.task.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.datadog.task.model.LogRecord;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LogParserTest {

    @Test
    void parseRecordShouldWork() {
        final String logLine = "127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /api/users HTTP/1.0\" 200 123";
        final Optional<LogRecord> recordOpt = LogParser.parse(logLine);
        assertTrue(recordOpt.isPresent());
        final LogRecord record = recordOpt.get();
        assertEquals(1525881639L, record.getTimestamp());
        assertEquals("james", record.getAuth());
        assertEquals("127.0.0.1", record.getClientIp());
        assertEquals("/api/users", record.getRequest());
        assertEquals("/api", record.getSection());
    }

    @Test
    void parseRecordShouldWorksForRealAccessLog() throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(LogParserTest.class.getResourceAsStream("/access.log")))) {
            String line;
            while ((line = br.readLine()) != null) {
                final Optional<LogRecord> recordOpt = LogParser.parse(line);
                assertTrue(recordOpt.isPresent(), String.format("%s can not be parsed.", line));
            }
        }
    }

    @Test
    void parseRecordShouldReturnEmptyIfTheLogIsInvalid() {
        final String logLine = "127.0.0.1 - james [2020-10-15T12:55:49.357610Z]";
        final Optional<LogRecord> recordOpt = LogParser.parse(logLine);
        assertFalse(recordOpt.isPresent());
    }

    @Test
    void parseTimeStampShouldReturnRightValue() {
        final String timeString = "09/May/2018:16:00:39 +0000";
        assertEquals(1525881639L, LogParser.parseTimeStamp(timeString));
    }

    @Test
    void parseTimeShouldReturnNullForUnparsableTime() {
        final String timeString = "2020-10-15T12:55:49.357610Z";
        assertNull(LogParser.parseTimeStamp(timeString));
    }

    @Test
    void parseSectionShouldReturnRightValue() {
        assertEquals("/api", LogParser.parseSection("/api/user"));
        assertEquals("/api", LogParser.parseSection("/api/user/register"));
    }

    @Test
    void parseSectionShouldReturnFirstSectionIfOnlyOneSection() {
        assertEquals("/api", LogParser.parseSection("/api"));
    }

    @Test
    void parseSectionShouldReturnNullIfDataIsInvalid() {
        assertNull(LogParser.parseSection("api"));
    }

    @Test
    void parseSectionShouldReturnNullForEmptyString() {
        assertNull(LogParser.parseSection(""));
        assertNull(LogParser.parseSection(null));
    }
}