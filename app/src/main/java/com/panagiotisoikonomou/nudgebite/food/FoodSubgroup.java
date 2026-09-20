package com.panagiotisoikonomou.nudgebite.food;

public enum FoodSubgroup {

    LEAFY_VEGETABLES(FoodGroup.VEGETABLES),
    FRUITING_VEGETABLES(FoodGroup.VEGETABLES),
    ROOT_VEGETABLES(FoodGroup.VEGETABLES),
    CRUCIFEROUS_VEGETABLES(FoodGroup.VEGETABLES),
    ALLIUM_VEGETABLES(FoodGroup.VEGETABLES),
    STARCHY_VEGETABLES(FoodGroup.VEGETABLES),
    OTHER_VEGETABLES(FoodGroup.VEGETABLES),

    BERRIES(FoodGroup.FRUIT),
    CITRUS_FRUIT(FoodGroup.FRUIT),
    POME_STONE_FRUIT(FoodGroup.FRUIT),
    TROPICAL_FRUIT(FoodGroup.FRUIT),
    MELONS(FoodGroup.FRUIT),
    DRIED_FRUIT(FoodGroup.FRUIT),

    RICE(FoodGroup.GRAINS),
    BREAD(FoodGroup.GRAINS),
    PASTA(FoodGroup.GRAINS),
    BREAKFAST_CEREAL(FoodGroup.GRAINS),
    OTHER_GRAINS(FoodGroup.GRAINS),

    FISH(FoodGroup.SEAFOOD),
    CEPHALOPODS(FoodGroup.SEAFOOD),
    CRUSTACEANS(FoodGroup.SEAFOOD),
    MOLLUSCS(FoodGroup.SEAFOOD),

    RED_MEAT(FoodGroup.MEAT),
    PROCESSED_MEAT(FoodGroup.MEAT),

    MILK(FoodGroup.DAIRY),
    YOGURT(FoodGroup.DAIRY),
    CHEESE(FoodGroup.DAIRY),

    UNSWEETENED_DRINKS(FoodGroup.BEVERAGES),
    SUGAR_SWEETENED_DRINKS(FoodGroup.BEVERAGES),
    DIET_DRINKS(FoodGroup.BEVERAGES),
    JUICE(FoodGroup.BEVERAGES);

    private final FoodGroup group;

    FoodSubgroup(FoodGroup group) {
        this.group = group;
    }

    public FoodGroup group() {
        return group;
    }
}
