package com.panagiotisoikonomou.nudgebite.water;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.entity.Goal;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.progress.OnboardingGoal;
import com.panagiotisoikonomou.nudgebite.progress.SelectedGoals;

import java.util.List;

/** Everything about water is gated on this goal. It feeds no score. */
public final class WaterGoal {

    public static final int DEFAULT_GLASSES_PER_DAY = 8;

    public static final int MIN_GLASSES_PER_DAY = 1;
    public static final int MAX_GLASSES_PER_DAY = 20;

    private WaterGoal() {
    }

    public static boolean isSelected(@Nullable List<Goal> allGoals) {
        if (allGoals == null) {
            return false;
        }
        for (Goal goal : SelectedGoals.from(allGoals)) {
            if (OnboardingGoal.DRINK_MORE_WATER.label.equals(goal.name)) {
                return true;
            }
        }
        return false;
    }

    public static int targetFor(@Nullable User user) {
        if (user == null || user.waterGlassesGoalPerDay == null) {
            return DEFAULT_GLASSES_PER_DAY;
        }
        return Math.max(MIN_GLASSES_PER_DAY,
                Math.min(MAX_GLASSES_PER_DAY, user.waterGlassesGoalPerDay));
    }
}
