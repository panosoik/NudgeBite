package com.panagiotisoikonomou.nudgebite.food;

/** Taxonomy: FoodGroup -> FoodSubgroup (optional) -> FoodItem. Each FoodItem bridges to the scoring FoodCategory. */
public enum FoodGroup {

    VEGETABLES("Vegetables"),
    FRUIT("Fruit"),
    GRAINS("Grains"),
    LEGUMES("Legumes"),
    NUTS_SEEDS("Nuts & seeds"),
    SEAFOOD("Seafood"),
    MEAT("Meat"),
    POULTRY("Poultry"),
    EGGS("Eggs"),
    DAIRY("Dairy"),
    FATS_OILS("Fats & oils"),
    BEVERAGES("Beverages"),
    SWEETS_BAKED("Sweets & baked goods"),
    SALTY_SNACKS("Salty & processed snacks");

    private final String label;

    FoodGroup(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
