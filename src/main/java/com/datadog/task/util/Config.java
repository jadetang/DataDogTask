package com.datadog.task.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class Config {

    private static final String TIME_WINDOW_LENGTH_IN_SEC = "time.window.sec";
    private static final String LOG_FILE_PATH = "logfile.path";
    private static final String TOP_K = "topk";
    private static final String DEFAULT_LOG_PATH = "/tmp/access.log";
    private static final String STATISTICS_REPORT_INTERVAL_IN_SEC = "statistics.interval.sec";
    private static final String ALERT_THRESHOLD_QPS = "alert.threshold.qps";
    private static final int DEFAULT_TIME_WINDOW_LENGTH_IN_SEC = 120;
    private static final int DEFAULT_TOP_K = 5;
    private static final int DEFAULT_STATISTICS_REPORT_INTERVAL_IN_SEC = 10;
    private static final int DEFAULT_THRESHOLD_QPS = 20;
    private final Configuration configuration;

    public Config(String propertiesFilePath) throws IOException, ConfigurationException {
        this.configuration = this.loadConfig(propertiesFilePath);
    }

    public Config(InputStream inputStream) throws IOException, ConfigurationException {
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.read(new InputStreamReader(inputStream));
        this.configuration = propertiesConfiguration;
    }

    private Configuration loadConfig(final String configFile) throws IOException, ConfigurationException {
        if (!Files.exists(Paths.get(configFile))) {
            throw new IOException(configFile + " not found.");
        }
        final Parameters params = new Parameters();
        final FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(
                PropertiesConfiguration.class);
        builder.configure(params.fileBased()
                .setFile(new File(configFile)));
        return builder.getConfiguration();
    }

    public int getTimeWindowSizeInSec() {
        return configuration.getInt(TIME_WINDOW_LENGTH_IN_SEC, DEFAULT_TIME_WINDOW_LENGTH_IN_SEC);
    }

    public String getFilePath() {
        return configuration.getString(LOG_FILE_PATH, DEFAULT_LOG_PATH);
    }

    public int getStatisticIntervalInSec() {
        return configuration.getInt(STATISTICS_REPORT_INTERVAL_IN_SEC, DEFAULT_STATISTICS_REPORT_INTERVAL_IN_SEC);
    }

    public int getStatisticsTopK() {
        return configuration.getInt(TOP_K, DEFAULT_TOP_K);
    }

    public int getAlertThresholdPerSec() {
        return configuration.getInt(ALERT_THRESHOLD_QPS, DEFAULT_THRESHOLD_QPS);
    }
}