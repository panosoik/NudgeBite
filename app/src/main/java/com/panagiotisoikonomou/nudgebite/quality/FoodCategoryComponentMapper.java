package com.panagiotisoikonomou.nudgebite.quality;

import com.panagiotisoikonomou.nudgebite.data.enums.FoodCategory;

import java.util.ArrayList;
import java.util.List;

/** Which AHEI component each category feeds: potato is not a vegetable, refined grain is not whole grain. */
public final class FoodCategoryComponentMapper {

    private FoodCategoryComponentMapper() {
    }

    public static ScoreComponent componentOf(FoodCategory category) {
        switch (category) {
            case LEAFY_GREEN:
            case CRUCIFEROUS:
            case ROOT_VEGETABLE:
            case OTHER_VEGETABLE:
                return ScoreComponent.VEGETABLE;
            case FRUIT:
                return ScoreComponent.FRUIT;
            case WHOLE_GRAIN:
                return ScoreComponent.WHOLE_GRAIN;
            case LEGUME:
            case NUT_SEED:
                return ScoreComponent.LEGUME_NUT;
            case FISH:
            case SEAFOOD:
                return ScoreComponent.FISH;
            default:
                return ScoreComponent.NONE;
        }
    }

    public static List<FoodCategory> membersOf(ScoreComponent component) {
        List<FoodCategory> members = new ArrayList<>();
        for (FoodCategory category : FoodCategory.values()) {
            if (componentOf(category) == component) {
                members.add(category);
            }
        }
        return members;
    }
}
