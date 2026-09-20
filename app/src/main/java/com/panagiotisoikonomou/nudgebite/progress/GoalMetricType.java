package com.panagiotisoikonomou.nudgebite.progress;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.utils.EnumNames;

public enum GoalMetricType {

    HOME_COOKED_MEALS("Home-cooked meals", 5.0, true),
    DELIVERIES_OR_FAST_FOOD("Delivery and fast food", 3.0, false),
    VEG_MEALS_OUT("Meals out with veg", 2.0, true);

    public final String displayLabel;
    public final double defaultTarget;
    public final boolean higherIsBetter;

    GoalMetricType(String displayLabel, double defaultTarget, boolean higherIsBetter) {
        this.displayLabel = displayLabel;
        this.defaultTarget = defaultTarget;
        this.higherIsBetter = higherIsBetter;
    }

    @Nullable
    public static GoalMetricType parseOrNull(@Nullable String rawName) {
        return EnumNames.parseOrDefault(GoalMetricType.class, rawName, null);
    }
}
