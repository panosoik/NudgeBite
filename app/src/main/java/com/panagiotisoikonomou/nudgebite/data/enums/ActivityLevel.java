package com.panagiotisoikonomou.nudgebite.data.enums;

public enum ActivityLevel {
    SEDENTARY("Sedentary"),
    LIGHT("Light"),
    MODERATE("Moderate"),
    ACTIVE("Active"),
    VERY_ACTIVE("Very active");

    public final String label;

    ActivityLevel(String label) {
        this.label = label;
    }
}
