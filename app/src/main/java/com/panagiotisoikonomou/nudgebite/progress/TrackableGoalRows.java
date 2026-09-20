package com.panagiotisoikonomou.nudgebite.progress;

import com.panagiotisoikonomou.nudgebite.data.entity.Goal;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.entity.relation.MealWithIngredients;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class TrackableGoalRows {

    private TrackableGoalRows() {
    }

    /** The veg card, then the delivery card, for whichever of the two goals the user picked. */
    public static List<GoalProgressStatus> forWeekEnding(List<Goal> allGoals, List<MealWithIngredients> allMeals,
                                                   LocalDate weekEnd, User user) {
        List<GoalProgressStatus> cards = new ArrayList<>();
        if (isSelected(allGoals, OnboardingGoal.MORE_FRUIT_VEG)) {
            double target = user.vegServingsGoalPerDay != null
                    ? user.vegServingsGoalPerDay : TrackableGoalThresholds.VEG_TARGET_SERVINGS_PER_DAY;
            cards.add(TrackableGoalEvaluator.evaluateVeg(
                    GoalMetricCalculator.vegetableServingsPerDay(allMeals, weekEnd), target));
        }
        if (isSelected(allGoals, OnboardingGoal.FEWER_DELIVERIES)) {
            int weeklyCount = (int) GoalMetricCalculator.weeklyValue(
                    GoalMetricType.DELIVERIES_OR_FAST_FOOD, allMeals, weekEnd);
            int acceptablePerWeek = user.deliveryAcceptablePerWeek != null
                    ? user.deliveryAcceptablePerWeek : TrackableGoalThresholds.DELIVERY_ACCEPTABLE_PER_WEEK;
            cards.add(TrackableGoalEvaluator.evaluateDelivery(weeklyCount, acceptablePerWeek));
        }
        return cards;
    }

    private static boolean isSelected(List<Goal> allGoals, OnboardingGoal goal) {
        for (Goal selectedGoal : SelectedGoals.from(allGoals)) {
            if (goal.label.equals(selectedGoal.name)) {
                return true;
            }
        }
        return false;
    }
}
