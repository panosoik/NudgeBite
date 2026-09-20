package com.panagiotisoikonomou.nudgebite.quality;

public final class GreenDotCalibration {

    private GreenDotCalibration() {
    }

    /** A day needs two of the six WELL adequacy groups for the green dot. */
    public static final int MIN_DISTINCT_WELL_ADEQUACY_GROUPS = 2;

    /** The smallest amount that counts at all, so a garnish is not a food group. */
    public static final double MIN_SERVINGS_FOR_COMPONENT_TO_COUNT = 0.5;

    public static final double MIN_WHOLE_GRAIN_GRAMS_TO_COUNT = 15.0;
}
