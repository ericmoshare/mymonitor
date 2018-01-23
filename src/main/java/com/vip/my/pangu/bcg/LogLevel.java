package com.vip.my.pangu.bcg;

public enum LogLevel {

    INFO(1),

    WARN(2);

    private final int value;

    LogLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
