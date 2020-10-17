package com.datadog.task;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class HttpTrafficLogMonitorTest {

    @Test
    void test() throws Exception {
        LanternConsole console = new LanternConsole(100, 30);
        HttpTrafficLogMonitor httpTrafficLogMonitor = new HttpTrafficLogMonitor(
                console, new Configuration()
        );
        httpTrafficLogMonitor.initialize();
        console.start();
   //     TimeUnit.SECONDS.sleep(100000000L);
    }
}