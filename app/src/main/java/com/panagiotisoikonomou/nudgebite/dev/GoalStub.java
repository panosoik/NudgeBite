package com.panagiotisoikonomou.nudgebite.dev;

import android.content.Context;

import com.panagiotisoikonomou.nudgebite.BuildConfig;
import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.dao.GoalDao;
import com.panagiotisoikonomou.nudgebite.data.entity.Goal;
import com.panagiotisoikonomou.nudgebite.progress.OnboardingGoal;

import java.util.List;

final class GoalStub {

    private GoalStub() {
    }

    static void setGoal(Context context, OnboardingGoal goal, boolean enabled) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            GoalDao goalDao = AppDatabase.getDatabase(context).goalDao();
            Goal existing = findFreeTextGoal(goalDao, goal);
            if (enabled && existing == null) {
                Goal row = new Goal();
                row.name = goal.label;
                row.metricType = null;
                goalDao.insert(row);
            } else if (!enabled && existing != null) {
                goalDao.deleteById(existing.id);
            }
        });
    }

    static boolean isSet(Context context, OnboardingGoal goal) {
        if (!BuildConfig.DEBUG) {
            return false;
        }
        return findFreeTextGoal(AppDatabase.getDatabase(context).goalDao(), goal) != null;
    }

    private static Goal findFreeTextGoal(GoalDao goalDao, OnboardingGoal goal) {
        List<Goal> allGoals = goalDao.getAllGoalsSync();
        if (allGoals == null) {
            return null;
        }
        for (Goal storedGoal : allGoals) {
            if (storedGoal.metricType == null && goal.label.equals(storedGoal.name)) {
                return storedGoal;
            }
        }
        return null;
    }
}
