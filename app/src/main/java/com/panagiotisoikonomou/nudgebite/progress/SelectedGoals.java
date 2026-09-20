package com.panagiotisoikonomou.nudgebite.progress;

import com.panagiotisoikonomou.nudgebite.data.entity.Goal;

import java.util.ArrayList;
import java.util.List;

/** Rows with a metric type are internal and never shown as the user's goals. */
public final class SelectedGoals {

    private SelectedGoals() {
    }

    public static List<Goal> from(List<Goal> allGoals) {
        List<Goal> selected = new ArrayList<>();
        if (allGoals == null) {
            return selected;
        }
        for (Goal goal : allGoals) {
            if (goal != null && goal.metricType == null) {
                selected.add(goal);
            }
        }
        return selected;
    }
}
