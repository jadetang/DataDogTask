package com.datadog.task;

import com.datadog.task.controller.HttpTrafficLogMonitor;
import com.datadog.task.util.Config;
import com.datadog.task.view.LanternConsole;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, ConfigurationException, InterruptedException {
        Config config = null;
        if (args.length == 0) {
            log.info("use default properties");
            try(InputStream inputStream = Main.class.getResourceAsStream("/default.properties")) {
                config = new Config(inputStream);
            }
        } else {
            log.info("use provided properties file {}", args[0]);
            config = new Config(args[0]);
        }
        HttpTrafficLogMonitor httpTrafficLogMonitor = new HttpTrafficLogMonitor(config);
        httpTrafficLogMonitor.initialize();
        LanternConsole console = new LanternConsole(100, 30, httpTrafficLogMonitor);
        console.start();
        httpTrafficLogMonitor.close();
    }
}
