package com.panagiotisoikonomou.nudgebite.progress;

import com.panagiotisoikonomou.nudgebite.dashboard.ContributionTagBuilder;
import com.panagiotisoikonomou.nudgebite.data.entity.Ingredient;
import com.panagiotisoikonomou.nudgebite.data.entity.relation.MealWithIngredients;
import com.panagiotisoikonomou.nudgebite.data.enums.FoodCategory;
import com.panagiotisoikonomou.nudgebite.data.enums.MealSource;
import com.panagiotisoikonomou.nudgebite.quality.ComponentUnitConverter;
import com.panagiotisoikonomou.nudgebite.quality.FoodCategoryComponentMapper;
import com.panagiotisoikonomou.nudgebite.quality.ScoreComponent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GoalMetricCalculator {

    private GoalMetricCalculator() {
    }

    public static double weeklyValue(GoalMetricType metricType, List<MealWithIngredients> allMeals,
                                       LocalDate weekEnd) {
        List<MealWithIngredients> mealsInWeek = mealsInWeekEnding(allMeals, weekEnd);
        switch (metricType) {
            case HOME_COOKED_MEALS:
                return countMealsFrom(mealsInWeek, MealSource.HOME_COOKED);
            case DELIVERIES_OR_FAST_FOOD:
                return countMealsFrom(mealsInWeek, MealSource.DELIVERY_OR_TAKEOUT, MealSource.FAST_FOOD);
            case VEG_MEALS_OUT:
                return countMealsOutWithVegetables(mealsInWeek);
            default:
                throw new IllegalArgumentException("Unknown metric " + metricType);
        }
    }

    public static boolean meetsGoal(GoalMetricType metricType, double actual, double target) {
        return metricType.higherIsBetter ? actual >= target : actual <= target;
    }

    /** Averaged over days with a logged meal: a day with nothing logged is not a day without vegetables. */
    public static double vegetableServingsPerDay(List<MealWithIngredients> allMeals, LocalDate weekEnd) {
        Map<LocalDate, Double> servingsByLoggedDay = new HashMap<>();
        for (MealWithIngredients meal : mealsInWeekEnding(allMeals, weekEnd)) {
            servingsByLoggedDay.merge(meal.meal.loggedAt.toLocalDate(), vegetableServingsIn(meal), Double::sum);
        }
        double totalServings = 0.0;
        for (double servings : servingsByLoggedDay.values()) {
            totalServings += servings;
        }
        return servingsByLoggedDay.isEmpty() ? 0.0 : totalServings / servingsByLoggedDay.size();
    }

    private static double vegetableServingsIn(MealWithIngredients meal) {
        double servings = 0.0;
        if (meal.ingredients == null) {
            return servings;
        }
        for (Ingredient ingredient : meal.ingredients) {
            FoodCategory category = FoodCategory.parseOrDefault(ingredient.foodCategory, FoodCategory.UNKNOWN);
            if (FoodCategoryComponentMapper.componentOf(category) == ScoreComponent.VEGETABLE) {
                servings += ComponentUnitConverter.toComponentUnits(category, ingredient.servingValue);
            }
        }
        return servings;
    }

    private static List<MealWithIngredients> mealsInWeekEnding(List<MealWithIngredients> allMeals, LocalDate weekEnd) {
        LocalDate weekStart = weekEnd.minusDays(WeekPeriod.DAYS - 1L);
        List<MealWithIngredients> mealsInWeek = new ArrayList<>();
        for (MealWithIngredients meal : allMeals) {
            LocalDate day = meal.meal.loggedAt.toLocalDate();
            if (!day.isBefore(weekStart) && !day.isAfter(weekEnd)) {
                mealsInWeek.add(meal);
            }
        }
        return mealsInWeek;
    }

    /** Meals whose source was never asked have a null source and count on neither side. */
    private static int countMealsFrom(List<MealWithIngredients> meals, MealSource... sources) {
        int count = 0;
        for (MealWithIngredients meal : meals) {
            if (Arrays.asList(sources).contains(meal.meal.mealSource)) {
                count++;
            }
        }
        return count;
    }

    private static int countMealsOutWithVegetables(List<MealWithIngredients> meals) {
        int count = 0;
        for (MealWithIngredients meal : meals) {
            boolean eatenOut = meal.meal.mealSource == MealSource.DELIVERY_OR_TAKEOUT
                    || meal.meal.mealSource == MealSource.FAST_FOOD;
            if (eatenOut && ContributionTagBuilder.containsVegetables(meal.ingredients)) {
                count++;
            }
        }
        return count;
    }
}
