package com.panagiotisoikonomou.nudgebite.quality.well;

/** Floored at two: one meal a day is too little evidence to count as full coverage. */
public final class WellExpectedMeals {

    public static final int DEFAULT_EXPECTED_MEALS_PER_DAY = 3;

    public static final int MINIMUM_EXPECTED_MEALS_PER_DAY = 2;

    private WellExpectedMeals() {
    }

    /** All three false means the question was never asked, so it keeps the three-meal assumption. */
    public static int fromDeclaredMeals(boolean eatsBreakfast, boolean eatsLunch, boolean eatsDinner) {
        int declared = (eatsBreakfast ? 1 : 0) + (eatsLunch ? 1 : 0) + (eatsDinner ? 1 : 0);
        if (declared == 0) {
            return DEFAULT_EXPECTED_MEALS_PER_DAY;
        }
        return Math.max(MINIMUM_EXPECTED_MEALS_PER_DAY, declared);
    }
}
