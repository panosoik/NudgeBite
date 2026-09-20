package com.panagiotisoikonomou.nudgebite.quality;

import com.panagiotisoikonomou.nudgebite.data.enums.FoodCategory;

public final class ComponentUnitConverter {

    private ComponentUnitConverter() {
    }

    /** Servings for every component except whole grain, which is counted in grams. */
    public static double toComponentUnits(FoodCategory category, double grams) {
        switch (category) {
            case LEAFY_GREEN:
            case CRUCIFEROUS:
            case ROOT_VEGETABLE:
            case OTHER_VEGETABLE:
                return grams / Ahei2010Thresholds.VEGETABLE_SERVING_GRAMS;
            case FRUIT:
                return grams / Ahei2010Thresholds.FRUIT_SERVING_GRAMS;
            case WHOLE_GRAIN:
                return grams;
            case LEGUME:
                return grams / MvpAdaptationConstants.LEGUME_SERVING_GRAMS;
            case NUT_SEED:
                return grams / MvpAdaptationConstants.NUT_SEED_SERVING_GRAMS;
            case FISH:
            case SEAFOOD:
                return grams / MvpAdaptationConstants.FISH_SERVING_GRAMS;
            default:
                return 0.0;
        }
    }
}
