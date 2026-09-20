package com.panagiotisoikonomou.nudgebite.dev;

enum DayStatusScenario {
    NOTHING_LOGGED("Nothing logged today"),
    LOGGED_NOT_GREEN("Logged, not green (your day so far)"),
    GREEN_TODAY_ONLY("Green today only (balanced start)"),
    GREEN_TODAY_AND_YESTERDAY("Green today + yesterday (building rhythm)");

    final String label;

    DayStatusScenario(String label) {
        this.label = label;
    }
}
