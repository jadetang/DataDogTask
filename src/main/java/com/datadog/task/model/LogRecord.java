package com.datadog.task.model;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * A pojo holds one line of CLF HTTP access log.
 */
public class LogRecord {

    private final String request;

    private final Long timestamp;

    private final String section;

    private final String auth;

    private final String clientIp;

    private LogRecord(String request, long timestamp, String section, String auth, String clientIp) {
        this.request = request;
        this.timestamp = timestamp;
        this.section = section;
        this.auth = auth;
        this.clientIp = clientIp;
    }

    public String getRequest() {
        return request;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getSection() {
        return section;
    }

    public String getAuth() {
        return auth;
    }

    public String getClientIp() {
        return clientIp;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LogRecord.class.getSimpleName() + "[", "]")
                .add("request='" + request + "'")
                .add("timestamp=" + timestamp)
                .add("section='" + section + "'")
                .add("auth='" + auth + "'")
                .add("clientIp='" + clientIp + "'")
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogRecord logRecord = (LogRecord) o;
        return Objects.equals(timestamp, logRecord.timestamp) &&
                Objects.equals(request, logRecord.request) &&
                Objects.equals(section, logRecord.section) &&
                Objects.equals(auth, logRecord.auth) &&
                Objects.equals(clientIp, logRecord.clientIp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(request, timestamp, section, auth, clientIp);
    }

    public static final class LogRecordBuilder {

        private String request;
        private Long timestamp;
        private String section;
        private String auth;
        private String clientIp;

        private LogRecordBuilder() {
        }

        public static LogRecordBuilder aLogRecord() {
            return new LogRecordBuilder();
        }

        public LogRecordBuilder withRequest(String request) {
            this.request = request;
            return this;
        }

        public LogRecordBuilder withTimestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public LogRecordBuilder withSection(String section) {
            this.section = section;
            return this;
        }

        public LogRecordBuilder withAuth(String auth) {
            this.auth = auth;
            return this;
        }

        public LogRecordBuilder withClientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public LogRecord build() {
            return new LogRecord(request, timestamp, section, auth, clientIp);
        }
    }
}