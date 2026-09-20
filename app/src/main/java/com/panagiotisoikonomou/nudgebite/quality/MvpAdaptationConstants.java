package com.panagiotisoikonomou.nudgebite.quality;

/** App-made estimates where AHEI-2010 does not apply. Never present them as the standard. */
public final class MvpAdaptationConstants {

    private MvpAdaptationConstants() {
    }

    // Unconfirmed: the USDA nut and seed serving.
    public static final double NUT_SEED_SERVING_GRAMS = 28.0;

    // Unconfirmed: roughly the USDA cooked legume serving.
    public static final double LEGUME_SERVING_GRAMS = 100.0;

    // Unconfirmed: a common cooked red meat serving.
    public static final double RED_PROCESSED_MEAT_SERVING_GRAMS = 100.0;

    // AHEI scores fish as omega-3 mg/day, which a photo cannot show; this uses 2 servings a week instead.
    public static final double FISH_TEN_POINT_SERVINGS_PER_DAY_PROXY = 2.0 / 7.0;

    public static final double FISH_SERVING_GRAMS = 140.0;
}
