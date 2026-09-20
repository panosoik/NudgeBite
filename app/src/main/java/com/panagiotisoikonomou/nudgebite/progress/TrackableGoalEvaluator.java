package com.panagiotisoikonomou.nudgebite.progress;

public final class TrackableGoalEvaluator {

    private static final String VEG_TITLE = "Veg servings this week";
    private static final String DELIVERY_TITLE = GoalMetricType.DELIVERIES_OR_FAST_FOOD.displayLabel;

    private TrackableGoalEvaluator() {
    }

    public static GoalProgressStatus evaluateVeg(double servingsPerDay, double targetServingsPerDay) {
        String valueText = String.valueOf(weeklyTotal(servingsPerDay));
        String targetSentence = " Your goal works out at about " + weeklyTotal(targetServingsPerDay)
                + " servings a week.";
        if (servingsPerDay >= targetServingsPerDay) {
            return new GoalProgressStatus(VEG_TITLE,
                    "You're right on track with your vegetable goal this week." + targetSentence,
                    valueText, GoalProgressStatus.Level.GREEN);
        }
        if (servingsPerDay >= TrackableGoalThresholds.VEG_GOOD_TIER_SERVINGS_PER_DAY) {
            return new GoalProgressStatus(VEG_TITLE,
                    "You're close. Adding vegetables to one more meal could get you there." + targetSentence,
                    valueText, GoalProgressStatus.Level.GREEN);
        }
        return new GoalProgressStatus(VEG_TITLE,
                "Try adding some vegetables to lunch or dinner this week." + targetSentence,
                valueText, GoalProgressStatus.Level.AMBER);
    }

    /** The copy names counts literally, so an acceptable count above one still reads "One delivery". */
    public static GoalProgressStatus evaluateDelivery(int weeklyCount, int acceptablePerWeek) {
        String valueText = String.valueOf(weeklyCount);
        if (weeklyCount == 0) {
            return new GoalProgressStatus(DELIVERY_TITLE,
                    "No delivery or fast food this week. You're right on track with your goal.",
                    valueText, GoalProgressStatus.Level.GREEN);
        }
        if (weeklyCount <= acceptablePerWeek) {
            return new GoalProgressStatus(DELIVERY_TITLE,
                    "One delivery this week. You're within your goal.",
                    valueText, GoalProgressStatus.Level.GREEN);
        }
        return new GoalProgressStatus(DELIVERY_TITLE,
                "A few this week. Planning one easy meal ahead could help next time.",
                valueText, GoalProgressStatus.Level.AMBER);
    }

    private static int weeklyTotal(double perDay) {
        return (int) Math.round(perDay * WeekPeriod.DAYS);
    }
}
