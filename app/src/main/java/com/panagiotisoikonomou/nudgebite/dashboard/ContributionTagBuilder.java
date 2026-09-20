package com.panagiotisoikonomou.nudgebite.dashboard;

import com.panagiotisoikonomou.nudgebite.data.entity.Ingredient;
import com.panagiotisoikonomou.nudgebite.food.well.WellComponent;
import com.panagiotisoikonomou.nudgebite.quality.Ahei2010Thresholds;
import com.panagiotisoikonomou.nudgebite.quality.well.WellIngredientMapper;
import com.panagiotisoikonomou.nudgebite.quality.well.WellPresenceGate;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ContributionTagBuilder {

    private static final String NOTHING_TO_NAME = "Meal logged.";

    private ContributionTagBuilder() {
    }

    public static String buildTag(List<Ingredient> ingredients) {
        int vegetables = wholeServingsOf(ingredients, WellComponent.VEGETABLES);
        int fruit = wholeServingsOf(ingredients, WellComponent.FRUIT);

        if (vegetables == 0 && fruit == 0) {
            return NOTHING_TO_NAME;
        }
        if (fruit == 0) {
            return "Logged — " + vegetables + " veg " + servingWord(vegetables);
        }
        if (vegetables == 0) {
            return "Logged — " + fruit + " fruit " + servingWord(fruit);
        }
        return "Logged — " + vegetables + " veg + " + fruit + " fruit " + servingWord(fruit);
    }

    public static boolean containsVegetables(List<Ingredient> ingredients) {
        return wholeServingsOf(ingredients, WellComponent.VEGETABLES) > 0;
    }

    private static int wholeServingsOf(List<Ingredient> ingredients, WellComponent group) {
        double grams = amountsByGroup(ingredients).getOrDefault(group, 0.0);
        if (!WellPresenceGate.isMeaningfullyPresent(group, grams)) {
            return 0;
        }
        return (int) Math.round(grams / servingGramsOf(group));
    }

    private static Map<WellComponent, Double> amountsByGroup(List<Ingredient> ingredients) {
        Map<WellComponent, Double> totals = new EnumMap<>(WellComponent.class);
        if (ingredients == null) {
            return totals;
        }
        for (Ingredient ingredient : ingredients) {
            for (WellComponent component : WellIngredientMapper.componentsOf(ingredient)) {
                if (component == WellComponent.VEGETABLES || component == WellComponent.FRUIT) {
                    totals.merge(component, ingredient.servingValue, Double::sum);
                }
            }
        }
        return totals;
    }

    private static double servingGramsOf(WellComponent group) {
        return group == WellComponent.FRUIT
                ? Ahei2010Thresholds.FRUIT_SERVING_GRAMS
                : Ahei2010Thresholds.VEGETABLE_SERVING_GRAMS;
    }

    private static String servingWord(int servings) {
        return servings == 1 ? "serving" : "servings";
    }
}
