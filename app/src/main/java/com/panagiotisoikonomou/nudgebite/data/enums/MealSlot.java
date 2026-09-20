package com.panagiotisoikonomou.nudgebite.data.enums;

public enum MealSlot {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack");

    public final String label;

    MealSlot(String label) {
        this.label = label;
    }
}
