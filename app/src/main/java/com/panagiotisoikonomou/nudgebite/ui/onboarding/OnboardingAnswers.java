package com.panagiotisoikonomou.nudgebite.ui.onboarding;

import com.panagiotisoikonomou.nudgebite.data.entity.Goal;
import com.panagiotisoikonomou.nudgebite.data.entity.User;

import java.util.ArrayList;
import java.util.List;

/** What the onboarding steps fill in, saved once the last step is done. */
public class OnboardingAnswers {

    public final User user = new User();
    public List<String> selectedGoals = new ArrayList<>();

    public OnboardingAnswers() {
        user.preferredFoods = new ArrayList<>();
        user.foodsToAvoid = new ArrayList<>();
    }

    public List<Goal> toGoalEntities() {
        List<Goal> goals = new ArrayList<>();
        for (String name : selectedGoals) {
            Goal goal = new Goal();
            goal.name = name;
            goals.add(goal);
        }
        return goals;
    }
}
