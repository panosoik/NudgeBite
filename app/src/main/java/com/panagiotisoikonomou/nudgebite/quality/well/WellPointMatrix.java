package com.panagiotisoikonomou.nudgebite.quality.well;

import com.panagiotisoikonomou.nudgebite.food.well.WellComponent;

import java.util.EnumMap;
import java.util.Map;

/** From the published WELL Diet scoring appendix. WELL-inspired, not the WELL Diet Score. Sodium is unscored: max 110, never rescaled to 120. */
public final class WellPointMatrix {

    public static final int MAX_POINTS_PER_COMPONENT = 10;

    private static final Map<WellComponent, int[]> POINTS = new EnumMap<>(WellComponent.class);

    static {
        //                                             never 1/mo 2-3/mo 1-2/wk 3-4/wk 5-6/wk 1/day 2-3/day 4-5/day 6+/day
        POINTS.put(WellComponent.VEGETABLES,           new int[]{0,  0,   1,     2,     4,     6,    8,    9,     10,   10});
        POINTS.put(WellComponent.FRUIT,                new int[]{0,  0,   1,     2,     4,     6,    8,   10,     10,   10});
        POINTS.put(WellComponent.WHOLE_GRAINS,         new int[]{0,  0,   1,     2,     4,     6,    8,   10,     10,    8});
        POINTS.put(WellComponent.SUGARY_BEVERAGES_AND_JUICE,
                                                       new int[]{10, 9,   8,     6,     4,     1,    0,    0,      0,    0});
        POINTS.put(WellComponent.SWEETS_AND_BAKED_GOODS,
                                                       new int[]{10, 9,   8,     6,     4,     1,    0,    0,      0,    0});
        POINTS.put(WellComponent.BEANS_AND_LENTILS,    new int[]{0,  1,   2,     4,     6,     8,    9,   10,     10,   10});
        POINTS.put(WellComponent.NUTS_AND_SEEDS,       new int[]{0,  1,   2,     4,     6,     8,   10,   10,      8,    6});
        POINTS.put(WellComponent.RED_AND_PROCESSED_MEAT,
                                                       new int[]{10,10,   8,     6,     4,     2,    1,    0,      0,    0});
        POINTS.put(WellComponent.FISH,                 new int[]{0,  4,   7,    10,    10,    10,   10,   10,     10,   10});
        POINTS.put(WellComponent.FAST_FOOD,            new int[]{10, 8,   5,     2,     0,     0,    0,    0,      0,    0});
        POINTS.put(WellComponent.PREPARE_OWN_MEAL,     new int[]{0,  1,   1,     2,     4,     6,    8,   10,     10,   10});
    }

    public static final int MAX_TOTAL_POINTS = WellComponent.values().length * MAX_POINTS_PER_COMPONENT;

    private WellPointMatrix() {
    }

    public static int pointsFor(WellComponent component, WellFrequencyBand band) {
        return POINTS.get(component)[band.ordinal()];
    }

    public static boolean isNegative(WellComponent component) {
        return pointsFor(component, WellFrequencyBand.NEVER) > pointsFor(component, WellFrequencyBand.SIX_OR_MORE_A_DAY);
    }
}
