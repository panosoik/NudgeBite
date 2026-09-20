package com.panagiotisoikonomou.nudgebite.quality.well;

import com.panagiotisoikonomou.nudgebite.data.entity.Ingredient;
import com.panagiotisoikonomou.nudgebite.data.enums.FoodCategory;
import com.panagiotisoikonomou.nudgebite.food.FoodItem;
import com.panagiotisoikonomou.nudgebite.food.well.FoodWellMappingRegistry;
import com.panagiotisoikonomou.nudgebite.food.well.WellComponent;

import java.util.EnumSet;
import java.util.Set;

/** Prefers the taxonomy item; the category fallback is coarser and invents nothing. */
public final class WellIngredientMapper {

    private WellIngredientMapper() {
    }

    public static Set<WellComponent> componentsOf(Ingredient ingredient) {
        FoodItem item = FoodItem.parseOrNull(ingredient.foodItem);
        if (item != null) {
            return FoodWellMappingRegistry.componentsOf(item);
        }
        WellComponent fromCategory = fromCategory(
                FoodCategory.parseOrDefault(ingredient.foodCategory, FoodCategory.UNKNOWN));
        return fromCategory == null ? EnumSet.noneOf(WellComponent.class) : EnumSet.of(fromCategory);
    }

    static WellComponent fromCategory(FoodCategory category) {
        switch (category) {
            case LEAFY_GREEN:
            case CRUCIFEROUS:
            case ROOT_VEGETABLE:
            case OTHER_VEGETABLE:
                return WellComponent.VEGETABLES;
            case FRUIT:
                return WellComponent.FRUIT;
            case WHOLE_GRAIN:
                return WellComponent.WHOLE_GRAINS;
            case LEGUME:
                return WellComponent.BEANS_AND_LENTILS;
            case NUT_SEED:
                return WellComponent.NUTS_AND_SEEDS;
            case FISH:
                return WellComponent.FISH;
            case SUGARY_DRINK:
                return WellComponent.SUGARY_BEVERAGES_AND_JUICE;
            case RED_MEAT:
            case PROCESSED_MEAT:
                return WellComponent.RED_AND_PROCESSED_MEAT;
            case SWEET_SNACK:
                return WellComponent.SWEETS_AND_BAKED_GOODS;
            default:
                return null;
        }
    }
}
