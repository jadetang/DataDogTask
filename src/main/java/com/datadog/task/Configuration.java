package com.datadog.task;

public class Configuration {

    public int getTimeWindowSizeInSec() {
        return 10;
    }

    public String getFilePath() {
        return "/Users/stang/codebase/log-monitor/access.log";
    }

    public int getStatisticIntervalInSec() {
        return 5;
    }

    public int getStatisticsTopK() {
        return 5;
    }

    public int getThresholdPerSec() {
        return 2;
    }
}
