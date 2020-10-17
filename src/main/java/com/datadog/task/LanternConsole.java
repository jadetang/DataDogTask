package com.datadog.task;

import com.google.common.base.Strings;
import com.googlecode.lanterna.SGR;
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
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanternConsole implements Console {

    private static final Logger log = LoggerFactory.getLogger(LanternConsole.class);

    private final TerminalSize terminalSize;

    private final Terminal terminal;

    private final Screen screen;

    private String statisticMessage;

    private String alertMessage;

    private String endAlertMessage;

    public LanternConsole(int width, int height) throws IOException {
        terminalSize = new TerminalSize(width, height);
        terminal = new DefaultTerminalFactory().setInitialTerminalSize(terminalSize).createTerminal();
        screen = new TerminalScreen(terminal);
    }

    public void start() throws IOException, InterruptedException {
        screen.startScreen();
        while (true) {
            screen.clear();
            // Monitoring keyboard input
            KeyStroke keyStroke = screen.pollInput();
            if (keyStroke != null && keyStroke.getKeyType() == KeyType.F10) {
                screen.clear();
/*                cursorWait(0, 2, 666);
                typeln(">_ SYSTEM EXIT HOTKEY ON", 0, 0);
                cursorWait(0, 2, 1111);
                typeln(">_ SESSION TERMINATED", 0, 1);
                cursorWait(0, 2, 1111);*/
                break;
            }
            TextGraphics head = screen.newTextGraphics();
            head.setForegroundColor(ANSI.GREEN);
            head.putString(1, 1, "Press F10 to quit");
            head.putString(1, 2, Strings.repeat("-", terminalSize.getColumns() - 1));

            TextGraphics statistic = screen.newTextGraphics();
            statistic.setForegroundColor(ANSI.WHITE);
            if (statisticMessage !=  null) {
                int i = 0;
                String[] messages = statisticMessage.split("\n");
                for (String message : messages) {
             //       log.info(message);
                    statistic.putString(1, 3 + i, message);
                    i++;
                }
                statistic.putString(1, 24, Strings.repeat("-", terminalSize.getColumns() - 1));
            }

            if (endAlertMessage != null) {
                log.info(endAlertMessage);
                TextGraphics alert = screen.newTextGraphics();
                alert.setForegroundColor(ANSI.GREEN);
                alert.putString(1, 26, endAlertMessage, SGR.CIRCLED);
            } else if (alertMessage != null) {
                log.info(alertMessage);
                TextGraphics alert = screen.newTextGraphics();
                alert.setForegroundColor(ANSI.RED);
                alert.putString(1, 26, alertMessage, SGR.BLINK);
            }


            TimeUnit.MILLISECONDS.sleep(500);
            screen.refresh();
        }

    }

    @Override
    public void showStatistics(String statistics) {
        log.info(statistics);
        this.statisticMessage = statistics;
    }

    @Override
    public void showAlert(String alert) {
        this.alertMessage = alert;
    }

    @Override
    public void endAlert(String alertEnd) {
        this.endAlertMessage = alertEnd;
    }
}
