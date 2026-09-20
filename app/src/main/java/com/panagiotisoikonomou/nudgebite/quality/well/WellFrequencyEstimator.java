package com.panagiotisoikonomou.nudgebite.quality.well;

import com.panagiotisoikonomou.nudgebite.data.entity.Ingredient;
import com.panagiotisoikonomou.nudgebite.data.entity.relation.MealWithIngredients;
import com.panagiotisoikonomou.nudgebite.data.enums.MealSource;
import com.panagiotisoikonomou.nudgebite.food.well.WellComponent;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Counts eating occasions, not amounts: a component counts once per meal. */
public final class WellFrequencyEstimator {

    private WellFrequencyEstimator() {
    }

    public static WellObservation observe(List<MealWithIngredients> allMeals, LocalDate from, LocalDate to,
                                          int expectedMealsPerDay) {
        Map<WellComponent, Integer> occurrences = new EnumMap<>(WellComponent.class);
        Set<LocalDate> daysWithLogs = new HashSet<>();
        int loggedMeals = 0;

        for (MealWithIngredients meal : allMeals) {
            LocalDate day = meal.meal.loggedAt.toLocalDate();
            if (day.isBefore(from) || day.isAfter(to)) {
                continue;
            }
            daysWithLogs.add(day);
            loggedMeals++;
            for (WellComponent component : componentsPresentIn(meal)) {
                occurrences.merge(component, 1, Integer::sum);
            }
        }
        return new WellObservation(occurrences, daysWithLogs.size(), loggedMeals, expectedMealsPerDay);
    }

    static Set<WellComponent> componentsPresentIn(MealWithIngredients meal) {
        Map<WellComponent, Double> amountByComponent = new EnumMap<>(WellComponent.class);
        if (meal.ingredients != null) {
            for (Ingredient ingredient : meal.ingredients) {
                for (WellComponent component : WellIngredientMapper.componentsOf(ingredient)) {
                    if (WellPresenceGate.isAmountBased(component)) {
                        amountByComponent.merge(component, ingredient.servingValue, Double::sum);
                    }
                }
            }
        }

        Set<WellComponent> present = EnumSet.noneOf(WellComponent.class);
        for (Map.Entry<WellComponent, Double> entry : amountByComponent.entrySet()) {
            if (WellPresenceGate.isMeaningfullyPresent(entry.getKey(), entry.getValue())) {
                present.add(entry.getKey());
            }
        }

        WellComponent fromSource = fromMealSource(meal.meal.mealSource);
        if (fromSource != null) {
            present.add(fromSource);
        }
        return present;
    }

    static WellComponent fromMealSource(MealSource source) {
        if (source == null) {
            return null;
        }
        switch (source) {
            case HOME_COOKED:
                return WellComponent.PREPARE_OWN_MEAL;
            case FAST_FOOD:
                return WellComponent.FAST_FOOD;
            default:
                return null;
        }
    }
}
