package com.panagiotisoikonomou.nudgebite.progress;

/** Green or amber only: there is no failing state. */
public final class GoalProgressStatus {

    public enum Level {
        GREEN,
        AMBER
    }

    public final String title;
    public final String message;
    public final String valueText;
    public final Level level;

    public GoalProgressStatus(String title, String message, String valueText, Level level) {
        this.title = title;
        this.message = message;
        this.valueText = valueText;
        this.level = level;
    }
}
