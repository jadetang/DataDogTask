package com.datadog.task;

public interface Console {

    void showStatistics(String statistics);

    void showAlert(String alert);

    void endAlert(String alertEnd);
}
