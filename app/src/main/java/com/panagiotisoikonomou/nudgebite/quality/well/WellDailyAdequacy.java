package com.panagiotisoikonomou.nudgebite.quality.well;

import com.panagiotisoikonomou.nudgebite.data.entity.Ingredient;
import com.panagiotisoikonomou.nudgebite.data.entity.relation.MealWithIngredients;
import com.panagiotisoikonomou.nudgebite.data.enums.FoodCategory;
import com.panagiotisoikonomou.nudgebite.food.well.WellComponent;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Amounts are summed over the whole day before the floor, unlike the frequency estimator, which sums per meal. */
public final class WellDailyAdequacy {

    private static final Set<WellComponent> ADEQUACY_GROUPS = EnumSet.of(
            WellComponent.VEGETABLES,
            WellComponent.FRUIT,
            WellComponent.WHOLE_GRAINS,
            WellComponent.BEANS_AND_LENTILS,
            WellComponent.NUTS_AND_SEEDS,
            WellComponent.FISH);

    private WellDailyAdequacy() {
    }

    public static int countOn(List<MealWithIngredients> allMeals, LocalDate day) {
        return presentOn(allMeals, day).size();
    }

    public static Set<WellComponent> presentOn(List<MealWithIngredients> allMeals, LocalDate day) {
        Map<WellComponent, Double> amountByGroup = adequacyAmountsOn(allMeals, day);

        Set<WellComponent> present = EnumSet.noneOf(WellComponent.class);
        for (Map.Entry<WellComponent, Double> entry : amountByGroup.entrySet()) {
            if (WellPresenceGate.isMeaningfullyPresent(entry.getKey(), entry.getValue())) {
                present.add(entry.getKey());
            }
        }
        return present;
    }

    private static Map<WellComponent, Double> adequacyAmountsOn(List<MealWithIngredients> allMeals, LocalDate day) {
        Map<WellComponent, Double> totals = new EnumMap<>(WellComponent.class);
        for (MealWithIngredients meal : allMeals) {
            if (meal.ingredients == null || !day.equals(meal.meal.loggedAt.toLocalDate())) {
                continue;
            }
            for (Ingredient ingredient : meal.ingredients) {
                for (WellComponent group : adequacyGroupsOf(ingredient)) {
                    totals.merge(group, ingredient.servingValue, Double::sum);
                }
            }
        }
        return totals;
    }

    /** Non-fish seafood counts here only: variety for the dot, but not WELL fish. */
    private static Set<WellComponent> adequacyGroupsOf(Ingredient ingredient) {
        Set<WellComponent> groups = EnumSet.noneOf(WellComponent.class);
        for (WellComponent component : WellIngredientMapper.componentsOf(ingredient)) {
            if (ADEQUACY_GROUPS.contains(component)) {
                groups.add(component);
            }
        }
        if (FoodCategory.SEAFOOD.name().equals(ingredient.foodCategory)) {
            groups.add(WellComponent.FISH);
        }
        return groups;
    }
}
