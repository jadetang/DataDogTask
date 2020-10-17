package com.datadog.task;

public class SimpleConsole implements Console {

    @Override
    public void showStatistics(String statistics) {
        System.out.println(statistics);
    }

    @Override
    public void showAlert(String alert) {
        System.err.println(alert);
    }

    @Override
    public void endAlert(String alertEnd) {
        System.out.println(alertEnd);
    }
}
