package com.panagiotisoikonomou.nudgebite.quality.well;

import com.panagiotisoikonomou.nudgebite.food.well.WellComponent;
import com.panagiotisoikonomou.nudgebite.quality.Ahei2010Thresholds;
import com.panagiotisoikonomou.nudgebite.quality.GreenDotCalibration;
import com.panagiotisoikonomou.nudgebite.quality.MvpAdaptationConstants;

import java.util.EnumMap;
import java.util.Map;

/** The green dot's floors, applied per meal: a per-day gate would cap every component at one occasion a day. */
public final class WellPresenceGate {

    /** Unconfirmed: about a small pastry or a chocolate bar. */
    public static final double SWEETS_SERVING_GRAMS = 50.0;

    private static final Map<WellComponent, Double> SERVING_GRAMS = new EnumMap<>(WellComponent.class);

    static {
        SERVING_GRAMS.put(WellComponent.VEGETABLES, Ahei2010Thresholds.VEGETABLE_SERVING_GRAMS);
        SERVING_GRAMS.put(WellComponent.FRUIT, Ahei2010Thresholds.FRUIT_SERVING_GRAMS);
        SERVING_GRAMS.put(WellComponent.BEANS_AND_LENTILS, MvpAdaptationConstants.LEGUME_SERVING_GRAMS);
        SERVING_GRAMS.put(WellComponent.NUTS_AND_SEEDS, MvpAdaptationConstants.NUT_SEED_SERVING_GRAMS);
        SERVING_GRAMS.put(WellComponent.FISH, MvpAdaptationConstants.FISH_SERVING_GRAMS);
        SERVING_GRAMS.put(WellComponent.SUGARY_BEVERAGES_AND_JUICE, Ahei2010Thresholds.SSB_SERVING_GRAMS);
        SERVING_GRAMS.put(WellComponent.RED_AND_PROCESSED_MEAT, MvpAdaptationConstants.RED_PROCESSED_MEAT_SERVING_GRAMS);
        SERVING_GRAMS.put(WellComponent.SWEETS_AND_BAKED_GOODS, SWEETS_SERVING_GRAMS);
    }

    private WellPresenceGate() {
    }

    public static boolean isMeaningfullyPresent(WellComponent component, double rawAmountInMeal) {
        if (component == WellComponent.WHOLE_GRAINS) {
            return rawAmountInMeal >= GreenDotCalibration.MIN_WHOLE_GRAIN_GRAMS_TO_COUNT;
        }
        Double gramsPerServing = SERVING_GRAMS.get(component);
        if (gramsPerServing == null) {
            return false;
        }
        return rawAmountInMeal / gramsPerServing >= GreenDotCalibration.MIN_SERVINGS_FOR_COMPONENT_TO_COUNT;
    }

    public static boolean isAmountBased(WellComponent component) {
        return component == WellComponent.WHOLE_GRAINS || SERVING_GRAMS.containsKey(component);
    }
}
