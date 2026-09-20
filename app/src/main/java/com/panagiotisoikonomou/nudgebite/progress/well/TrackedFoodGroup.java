package com.panagiotisoikonomou.nudgebite.progress.well;

import com.panagiotisoikonomou.nudgebite.food.well.WellComponent;

/** Eight of the eleven scored components: sugary drinks and sweets are not targets, cooking is not a food group. */
public enum TrackedFoodGroup {

    VEGETABLES(WellComponent.VEGETABLES, "Vegetables", TargetUnit.PER_DAY),
    FRUIT(WellComponent.FRUIT, "Fruit", TargetUnit.PER_DAY),
    WHOLE_GRAINS(WellComponent.WHOLE_GRAINS, "Whole grains", TargetUnit.PER_DAY),
    LEGUMES(WellComponent.BEANS_AND_LENTILS, "Legumes", TargetUnit.PER_DAY),
    NUTS_AND_SEEDS(WellComponent.NUTS_AND_SEEDS, "Nuts & seeds", TargetUnit.PER_DAY),
    FISH(WellComponent.FISH, "Fish", TargetUnit.PER_WEEK),
    RED_AND_PROCESSED_MEAT(WellComponent.RED_AND_PROCESSED_MEAT, "Red & processed meat", TargetUnit.PER_MONTH),
    FAST_FOOD(WellComponent.FAST_FOOD, "Fast food", TargetUnit.PER_MONTH);

    private final WellComponent component;
    private final String label;
    private final TargetUnit unit;

    TrackedFoodGroup(WellComponent component, String label, TargetUnit unit) {
        this.component = component;
        this.label = label;
        this.unit = unit;
    }

    public WellComponent component() {
        return component;
    }

    public String label() {
        return label;
    }

    public TargetUnit unit() {
        return unit;
    }
}
