package com.datadog.task;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogGenerator {

    private static final Logger log = LoggerFactory.getLogger(LogGenerator.class);

    private static final ReentrantLock LOCK = new ReentrantLock();

    private static final Condition STOP = LOCK.newCondition();

    private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(5);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z")
            .withZone(ZoneId.systemDefault());

    private static final String[] IPS = new String[]{"192.168.0.110",
            "127.0.0.1",
            "60.242.26.14",
            "192.127.0.0",
            "50.18.212.157",
            "50.18.212.223",
            "52.25.214.31",
            "52.26.11.205",
            "52.26.14.11",
            "52.8.19.58",
            "52.8.8.189",
            "54.149.153.72",
            "54.187.182.230",
            "54.187.199.38",
            "54.187.208.163",
            "54.67.48.128",
            "54.67.52.245",
            "54.68.165.206",
            "54.68.183.151",
            "107.23.48.182",
            "107.23.48.232"};

    private static final String[] METHODS = new String[]{"GET", "GET", "GET", "POST", "POST", "PUT", "DELETE"};

    private static final String[] HTTP_CODE = new String[]{"200", "200", "200", "200", "200", "304", "403", "404"};

    public static void main(String[] args) {
        String filePath = "/tmp/access.log";
        int logPerSeconds = 100;
        if (args.length == 2) {
            filePath = args[0];
            logPerSeconds = Integer.parseInt(args[1]);
        } else if (args.length == 1) {
            filePath = args[0];
        }
        log.info("Generate {} logs to {} every second.", logPerSeconds, filePath);

        int finalLogPerSeconds = logPerSeconds;
        String finalFilePath = filePath;
        service.scheduleAtFixedRate(() -> {
            try {
                writeLog(finalFilePath, finalLogPerSeconds);
            } catch (IOException ioException) {
                log.error("error when writing log.", ioException);
            }
        }, 0L, 1L, TimeUnit.SECONDS);

        try {
            LOCK.lock();
            STOP.await();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted by other thread", e);
        } finally {
            LOCK.unlock();
        }
    }

    private static void writeLog(String fileName, int logSize) throws IOException {
        File file = new File(fileName);
        String timeStamp = timeStamp();
        for (int i = 0; i < logSize; i++) {
            FileUtils.writeStringToFile(
                    file, randomLog(timeStamp) + "\r\n", StandardCharsets.UTF_8, true);
        }
    }

    private static String randomLog(String timeStamp) {
        String logFormat = "%s - %s [%s] \"%s %s HTTP/1.1\" %s %d";
        return String
                .format(logFormat, randomItemFromArray(IPS), randomUser(), timeStamp, randomItemFromArray(METHODS),
                        randomSection(), randomItemFromArray(HTTP_CODE), ThreadLocalRandom.current().nextInt(0, 1000));
    }

    private static String randomSection() {
        return "/" + RandomStringUtils.randomAlphabetic(4) + "/" + RandomStringUtils.randomAlphabetic(3);
    }

    private static String randomItemFromArray(String[] array) {
        return array[ThreadLocalRandom.current().nextInt(0, array.length)];
    }

    private static String randomUser() {
        return RandomStringUtils.randomAlphabetic(4);
    }

    private static String timeStamp() {
        return DATE_TIME_FORMATTER.format(Instant.now());
    }

    private static void addHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            closeThreadPool();
            log.info("JVM exits, MetricsProducer stopped.");
            try {
                LOCK.lock();
                STOP.signal();
            } finally {
                LOCK.unlock();
            }
        }, "MetricsProducer-shutdown-hook"));
    }

    private static void closeThreadPool() {
        log.info("Closing scheduler thread pool producer.");
        service.shutdown();
        try {
            service.awaitTermination(10, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            log.error("Error while stopping the scheduler thread pool");
            Thread.currentThread().interrupt();
        }
    }
}
