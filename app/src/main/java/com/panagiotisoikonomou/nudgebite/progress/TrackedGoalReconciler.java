package com.panagiotisoikonomou.nudgebite.progress;

import com.panagiotisoikonomou.nudgebite.data.entity.Goal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class TrackedGoalReconciler {

    private TrackedGoalReconciler() {
    }

    public static final class Plan {
        public final List<GoalMetricType> toInsert;
        public final List<Long> toDeleteIds;

        Plan(List<GoalMetricType> toInsert, List<Long> toDeleteIds) {
            this.toInsert = toInsert;
            this.toDeleteIds = toDeleteIds;
        }
    }

    public static Plan plan(List<Goal> allGoals) {
        List<String> selectedGoalLabels = new ArrayList<>();
        for (Goal goal : allGoals) {
            if (goal.metricType == null) {
                selectedGoalLabels.add(goal.name);
            }
        }
        // No stored goals yet means onboarding is still writing them: do not prune, or edited thresholds are lost.
        boolean onboardingGoalsKnown = !selectedGoalLabels.isEmpty();
        Set<GoalMetricType> required = OnboardingGoal.metricsFor(selectedGoalLabels);

        Set<GoalMetricType> alreadyStored = EnumSet.noneOf(GoalMetricType.class);
        List<Long> toDeleteIds = new ArrayList<>();
        for (Goal goal : allGoals) {
            if (goal.metricType == null) {
                continue;
            }
            GoalMetricType metricType = GoalMetricType.parseOrNull(goal.metricType);
            if (metricType == null) {
                toDeleteIds.add(goal.id);
            } else if (!required.contains(metricType)) {
                if (onboardingGoalsKnown) {
                    toDeleteIds.add(goal.id);
                }
            } else if (!alreadyStored.add(metricType)) {
                toDeleteIds.add(goal.id);
            }
        }

        List<GoalMetricType> toInsert = new ArrayList<>(required);
        toInsert.removeAll(alreadyStored);
        return new Plan(toInsert, toDeleteIds);
    }
}
