package com.panagiotisoikonomou.nudgebite.progress;

import android.content.Context;

import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.dao.GoalDao;
import com.panagiotisoikonomou.nudgebite.data.entity.Goal;

public final class GoalBootstrap {

    private GoalBootstrap() {
    }

    public static void reconcileTrackedGoals(Context context) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            GoalDao goalDao = AppDatabase.getDatabase(context).goalDao();
            TrackedGoalReconciler.Plan plan = TrackedGoalReconciler.plan(goalDao.getAllGoalsSync());
            for (Long goalId : plan.toDeleteIds) {
                goalDao.deleteById(goalId);
            }
            for (GoalMetricType metricType : plan.toInsert) {
                Goal goal = new Goal();
                goal.name = metricType.displayLabel;
                goal.metricType = metricType.name();
                goal.targetValue = metricType.defaultTarget;
                goalDao.insert(goal);
            }
        });
    }
}
