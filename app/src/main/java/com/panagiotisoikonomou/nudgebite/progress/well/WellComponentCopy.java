package com.panagiotisoikonomou.nudgebite.progress.well;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.food.well.WellComponent;

import java.util.EnumMap;
import java.util.Map;

/** Adequacy groups can be praised or improved, negatives only improved, cooking only praised. */
public final class WellComponentCopy {

    private static final Map<WellComponent, String> POSITIVE = new EnumMap<>(WellComponent.class);
    private static final Map<WellComponent, String> IMPROVEMENT = new EnumMap<>(WellComponent.class);

    static {
        POSITIVE.put(WellComponent.VEGETABLES, "You've included vegetables in your meals this week.");
        POSITIVE.put(WellComponent.FRUIT, "You've included fruit in your meals this week.");
        POSITIVE.put(WellComponent.WHOLE_GRAINS, "You've included whole grains in your meals this week.");
        POSITIVE.put(WellComponent.FISH, "You've included fish or seafood in your meals this week.");
        POSITIVE.put(WellComponent.BEANS_AND_LENTILS, "You've included legumes in your meals this week.");
        POSITIVE.put(WellComponent.NUTS_AND_SEEDS, "You've included nuts or seeds in your meals this week.");
        POSITIVE.put(WellComponent.PREPARE_OWN_MEAL, "You've cooked for yourself most days this week.");

        IMPROVEMENT.put(WellComponent.VEGETABLES,
                "Try adding some vegetables to lunch or dinner more often this week.");
        IMPROVEMENT.put(WellComponent.FRUIT,
                "Adding some fruit to breakfast or having it as a snack could help this week.");
        IMPROVEMENT.put(WellComponent.WHOLE_GRAINS,
                "Try choosing a whole-grain option for one or two meals this week.");
        IMPROVEMENT.put(WellComponent.FISH,
                "Adding fish or seafood to one meal this week could help.");
        IMPROVEMENT.put(WellComponent.BEANS_AND_LENTILS,
                "Try a meal built around lentils, beans or chickpeas this week.");
        IMPROVEMENT.put(WellComponent.NUTS_AND_SEEDS,
                "A handful of nuts or seeds makes an easy snack to add this week.");
        IMPROVEMENT.put(WellComponent.SUGARY_BEVERAGES_AND_JUICE,
                "Swapping one sweet drink for water or tea could make a difference this week.");
        IMPROVEMENT.put(WellComponent.SWEETS_AND_BAKED_GOODS,
                "Fruit or yogurt can stand in for a sweet snack on a day or two this week.");
        IMPROVEMENT.put(WellComponent.RED_AND_PROCESSED_MEAT,
                "Fish, chicken or a meal built around legumes could take the place of one red-meat meal.");
        IMPROVEMENT.put(WellComponent.FAST_FOOD,
                "Having something ready at home makes the quick option easier on a busy day.");
    }

    private WellComponentCopy() {
    }

    public static boolean canBePositive(WellComponent component) {
        return POSITIVE.containsKey(component);
    }

    public static boolean canBeImprovement(WellComponent component) {
        return IMPROVEMENT.containsKey(component);
    }

    @Nullable
    public static String positiveObservation(WellComponent component) {
        return POSITIVE.get(component);
    }

    @Nullable
    public static String improvementNote(WellComponent component) {
        return IMPROVEMENT.get(component);
    }
}
