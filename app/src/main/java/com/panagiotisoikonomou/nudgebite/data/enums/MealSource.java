package com.panagiotisoikonomou.nudgebite.data.enums;

/** Only HOME_COOKED and FAST_FOOD carry a WELL component. Fast food is user-declared, never inferred. */
public enum MealSource {
    HOME_COOKED("Home-cooked"),
    DELIVERY_OR_TAKEOUT("Delivery / takeout"),
    FAST_FOOD("Fast food"),
    OTHER("Other");

    public final String label;

    MealSource(String label) {
        this.label = label;
    }
}
