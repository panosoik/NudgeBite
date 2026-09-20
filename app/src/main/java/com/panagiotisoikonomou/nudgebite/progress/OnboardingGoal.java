package com.panagiotisoikonomou.nudgebite.progress;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Goals are stored as free text. A goal's metrics are stored as weekly metric rows for the export. */
public enum OnboardingGoal {

    ORGANIZE_MEALS("Plan my meals better"),
    FEWER_DELIVERIES("Order delivery less often",
            GoalMetricType.HOME_COOKED_MEALS, GoalMetricType.DELIVERIES_OR_FAST_FOOD),
    MORE_FRUIT_VEG("Eat more fruit and vegetables"),
    BETTER_CHOICES_OUT("Make more balanced choices when eating out", GoalMetricType.VEG_MEALS_OUT),
    QUICK_BALANCED("Find quick, balanced options when I'm short on time or energy"),
    DRINK_MORE_WATER("Drink more water");

    public final String label;
    private final List<GoalMetricType> metrics;

    OnboardingGoal(String label, GoalMetricType... metrics) {
        this.label = label;
        this.metrics = Arrays.asList(metrics);
    }

    public static List<String> allLabels() {
        List<String> labels = new ArrayList<>();
        for (OnboardingGoal goal : values()) {
            labels.add(goal.label);
        }
        return labels;
    }

    @Nullable
    public static OnboardingGoal fromLabelOrNull(@Nullable String label) {
        for (OnboardingGoal goal : values()) {
            if (goal.label.equals(label)) {
                return goal;
            }
        }
        return null;
    }

    /** In metric declaration order, whatever order the goals were picked in. */
    public static Set<GoalMetricType> metricsFor(Collection<String> selectedLabels) {
        Set<GoalMetricType> metrics = EnumSet.noneOf(GoalMetricType.class);
        for (String label : selectedLabels) {
            OnboardingGoal goal = fromLabelOrNull(label);
            if (goal != null) {
                metrics.addAll(goal.metrics);
            }
        }
        return metrics;
    }
}
