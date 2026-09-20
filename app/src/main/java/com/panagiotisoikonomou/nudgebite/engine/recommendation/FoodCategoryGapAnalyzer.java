package com.panagiotisoikonomou.nudgebite.engine.recommendation;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.entity.Ingredient;
import com.panagiotisoikonomou.nudgebite.data.enums.FoodCategory;
import com.panagiotisoikonomou.nudgebite.data.enums.Sex;
import com.panagiotisoikonomou.nudgebite.quality.AheiScoring;
import com.panagiotisoikonomou.nudgebite.quality.ComponentUnitConverter;
import com.panagiotisoikonomou.nudgebite.quality.FoodCategoryComponentMapper;
import com.panagiotisoikonomou.nudgebite.quality.ScoreComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Ranks only positive AHEI components, so it never suggests more red meat or sugary drinks. */
public final class FoodCategoryGapAnalyzer {

    public static final int LOOKBACK_DAYS = 7;

    private FoodCategoryGapAnalyzer() {
    }

    /** Lowest-scoring component first; within one, the least-logged category first. */
    public static List<FoodCategory> rankUnderrepresented(List<Ingredient> loggedIngredients, @Nullable Sex sex) {
        Map<ScoreComponent, Double> meanIntake = meanDailyIntakeByComponent(loggedIngredients);
        Map<FoodCategory, Integer> timesLogged = timesLoggedByCategory(loggedIngredients);

        List<ScoreComponent> components = new ArrayList<>(AheiScoring.SCORED_COMPONENTS);
        components.sort(Comparator.comparingDouble(
                component -> AheiScoring.points(component, meanIntake.get(component), sex)));

        List<FoodCategory> ranked = new ArrayList<>();
        for (ScoreComponent component : components) {
            List<FoodCategory> members = FoodCategoryComponentMapper.membersOf(component);
            members.sort(Comparator.comparingInt(category -> timesLogged.getOrDefault(category, 0)));
            ranked.addAll(members);
        }
        return ranked;
    }

    public static boolean isAnyFoodGroupMissing(List<Ingredient> loggedIngredients) {
        return meanDailyIntakeByComponent(loggedIngredients).containsValue(0.0);
    }

    private static Map<ScoreComponent, Double> meanDailyIntakeByComponent(List<Ingredient> loggedIngredients) {
        Map<ScoreComponent, Double> totals = new EnumMap<>(ScoreComponent.class);
        for (ScoreComponent component : AheiScoring.SCORED_COMPONENTS) {
            totals.put(component, 0.0);
        }
        for (Ingredient ingredient : loggedIngredients) {
            FoodCategory category = FoodCategory.parseOrDefault(ingredient.foodCategory, FoodCategory.UNKNOWN);
            ScoreComponent component = FoodCategoryComponentMapper.componentOf(category);
            if (totals.containsKey(component)) {
                totals.merge(component, ComponentUnitConverter.toComponentUnits(category, ingredient.servingValue),
                        Double::sum);
            }
        }
        totals.replaceAll((component, total) -> total / LOOKBACK_DAYS);
        return totals;
    }

    private static Map<FoodCategory, Integer> timesLoggedByCategory(List<Ingredient> loggedIngredients) {
        Map<FoodCategory, Integer> counts = new EnumMap<>(FoodCategory.class);
        for (Ingredient ingredient : loggedIngredients) {
            counts.merge(FoodCategory.parseOrDefault(ingredient.foodCategory, FoodCategory.UNKNOWN), 1, Integer::sum);
        }
        return counts;
    }
}
