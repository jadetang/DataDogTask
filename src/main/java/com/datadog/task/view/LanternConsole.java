package com.datadog.task.view;

import com.datadog.task.controller.HttpTrafficLogMonitor;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple console based on Lantern.
 */
public class LanternConsole {

    public static final String WELCOME_MESSAGE = ">_ SYSTEM ON，WELCOME";
    public static final String GOODBYE = ">_ GOODBYE";
    public static final String EXIT_MESSAGE = ">_ SYSTEM EXIT HOTKEY ON";
    public static final String QUIT_HINT = "Press F10 to quit";
    public static final int HEADER_START_ROW = 1;
    public static final int STATISTICS_START_ROW = 3;
    private static final Logger log = LoggerFactory.getLogger(LanternConsole.class);
    private static final Long REFRESH_INTERVAL = 500L;
    private final TerminalSize terminalSize;

    private final Screen screen;

    private final HttpTrafficLogMonitor httpTrafficLogMonitor;

    //the latest timestamp when the current alert is still on.
    private Long alertOnTime;

    /**
     * Construct a terminal with width and height.
     *
     * @param width                 The width of the terminal.
     * @param height                The height of the terminal.
     * @param httpTrafficLogMonitor
     * @throws IOException
     */
    public LanternConsole(int width, int height, HttpTrafficLogMonitor httpTrafficLogMonitor) throws IOException {
        Preconditions.checkArgument(width >= 100, "The minimal width is 100.");
        Preconditions.checkArgument(height >= 30, "The minimal height is 30.");
        this.terminalSize = new TerminalSize(width, height);
        this.screen = new TerminalScreen(
                new DefaultTerminalFactory().setInitialTerminalSize(terminalSize).createTerminalEmulator());
        this.httpTrafficLogMonitor = httpTrafficLogMonitor;
    }

    public void start() throws IOException, InterruptedException {
        screen.startScreen();
        typeMessage(WELCOME_MESSAGE, 0, 0);
        screenWait(REFRESH_INTERVAL * 2L);
        while (true) {
            screen.clear();
            // Monitoring keyboard input
            KeyStroke keyStroke = screen.pollInput();
            if (keyStroke != null && keyStroke.getKeyType() == KeyType.F10) {
                screen.clear();
                screenWait(REFRESH_INTERVAL);
                typeMessage(EXIT_MESSAGE, 0, 0);
                screenWait(REFRESH_INTERVAL * 2);
                typeMessage(GOODBYE, 0, 1);
                screenWait(REFRESH_INTERVAL * 2);
                break;
            }
            showHeader();
            showStatistics();
            screen.newTextGraphics()
                    .putString(1, terminalSize.getRows() * 4 / 5, Strings.repeat("-", terminalSize.getColumns() - 2));
            showAlert(terminalSize.getRows() * 4 / 5 + 1);
            screenWait(REFRESH_INTERVAL);
        }
        screen.close();
    }

    private void showAlert(int alertRow) {
        log.debug("Alert on time {}, now {}", alertOnTime, Instant.now().getEpochSecond());
        if (httpTrafficLogMonitor.inAlert()) {
            alertOnTime = Instant.now().getEpochSecond();
            TextGraphics alert = screen.newTextGraphics();
            alert.setForegroundColor(ANSI.RED);
            alert.putString(1, alertRow, httpTrafficLogMonitor.getAlertMessage(), SGR.BORDERED);
        } else {
            //If the alert is recovered, keep displaying recovered message for almost 10 seconds.
            if (alertOnTime != null && Instant.now().getEpochSecond() - alertOnTime <= 10) {
                TextGraphics alert = screen.newTextGraphics();
                alert.setForegroundColor(ANSI.GREEN);
                alert.putString(1, alertRow, "High traffic alert recovered.", SGR.CIRCLED);
            } else {
                // After 10 seconds, set alertOnTime to null so the recovered message is not displayed.
                alertOnTime = null;
            }
        }
    }

    private void showStatistics() {
        TextGraphics statisticsGraph = screen.newTextGraphics();
        statisticsGraph.setForegroundColor(ANSI.WHITE);
        String statisticMessage = httpTrafficLogMonitor.getStatisticsMessage();
        if (statisticMessage != null) {
            int i = 0;
            String[] messages = statisticMessage.split("\n");
            for (String message : messages) {
                statisticsGraph.putString(1, LanternConsole.STATISTICS_START_ROW + i, message);
                i++;
            }
        }
    }

    private void showHeader() {
        TextGraphics header = screen.newTextGraphics();
        header.setForegroundColor(ANSI.GREEN);
        header.putString(1, LanternConsole.HEADER_START_ROW, QUIT_HINT);
        header.putString(1, LanternConsole.HEADER_START_ROW + 1, Strings.repeat("-", terminalSize.getColumns() - 2));
    }

    public void screenWait(Long millis) throws IOException, InterruptedException {
        screen.refresh();
        TimeUnit.MILLISECONDS.sleep(millis);
    }

    public void typeMessage(String msg, int col, int row) throws IOException, InterruptedException {
        long interval = 11L;
        for (int i = 0; i < msg.length(); i++) {
            screen.setCursorPosition(new TerminalPosition(col + i + 1, row));
            screen.setCharacter((col + i), row, new TextCharacter(msg.charAt(i), TextColor.ANSI.GREEN, ANSI.BLACK));
            screenWait(ThreadLocalRandom.current().nextLong(interval * 3));
        }
        screenWait(ThreadLocalRandom.current().nextLong(interval * 3));
        screen.setCursorPosition(null);
        screen.refresh();
    }
}