package com.panagiotisoikonomou.nudgebite.water;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.utils.MealTimeText;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Due only when: goal on, inside the lunch+3h to dinner+3h window, 3h since the last glass and the last reminder, target not met. */
public final class WaterReminderRule {

    public static final Duration DRY_INTERVAL = Duration.ofHours(3);

    public static final Duration WINDOW_OFFSET_AFTER_MEAL = Duration.ofHours(3);

    public static final Duration REMINDER_COOLDOWN = Duration.ofHours(3);

    private WaterReminderRule() {
    }

    public static final class Inputs {
        public final boolean goalSelected;
        @Nullable public final String lunchTimeOfDay;
        @Nullable public final String dinnerTimeOfDay;
        @Nullable public final LocalDateTime lastGlassAt;
        @Nullable public final LocalDateTime lastReminderAt;
        public final int glassesToday;
        public final int targetGlasses;

        public Inputs(boolean goalSelected, @Nullable String lunchTimeOfDay,
                      @Nullable String dinnerTimeOfDay, @Nullable LocalDateTime lastGlassAt,
                      @Nullable LocalDateTime lastReminderAt, int glassesToday, int targetGlasses) {
            this.goalSelected = goalSelected;
            this.lunchTimeOfDay = lunchTimeOfDay;
            this.dinnerTimeOfDay = dinnerTimeOfDay;
            this.lastGlassAt = lastGlassAt;
            this.lastReminderAt = lastReminderAt;
            this.glassesToday = glassesToday;
            this.targetGlasses = targetGlasses;
        }
    }

    @Nullable
    public static String blockingReason(Inputs inputs, LocalDateTime now) {
        if (!inputs.goalSelected) {
            return "the water goal is not selected";
        }
        if (!isWithinWindow(inputs.lunchTimeOfDay, inputs.dinnerTimeOfDay, now)) {
            return "outside the window after lunch " + inputs.lunchTimeOfDay
                    + " and dinner " + inputs.dinnerTimeOfDay;
        }
        if (inputs.glassesToday >= inputs.targetGlasses) {
            return "today's target is met (" + inputs.glassesToday + " of "
                    + inputs.targetGlasses + " glasses)";
        }
        if (!hasPassedSince(inputs.lastGlassAt, DRY_INTERVAL, now)) {
            return "a glass was logged at " + inputs.lastGlassAt + ", within the dry interval";
        }
        if (!hasPassedSince(inputs.lastReminderAt, REMINDER_COOLDOWN, now)) {
            return "a reminder was sent at " + inputs.lastReminderAt + ", within the cooldown";
        }
        return null;
    }

    /** A missing meal time means no window, and so no reminder. */
    static boolean isWithinWindow(@Nullable String lunchTimeOfDay,
                                         @Nullable String dinnerTimeOfDay, LocalDateTime now) {
        LocalTime lunch = MealTimeText.toLocalTimeOrNull(lunchTimeOfDay);
        LocalTime dinner = MealTimeText.toLocalTimeOrNull(dinnerTimeOfDay);
        if (lunch == null || dinner == null) {
            return false;
        }
        LocalTime opens = lunch.plus(WINDOW_OFFSET_AFTER_MEAL);
        LocalTime closes = dinner.plus(WINDOW_OFFSET_AFTER_MEAL);
        LocalTime time = now.toLocalTime();

        if (opens.isBefore(closes)) {
            return !time.isBefore(opens) && !time.isAfter(closes);
        }
        // A late dinner can push the close past midnight, splitting the window in two.
        return !time.isBefore(opens) || !time.isAfter(closes);
    }

    /** True when nothing happened yet, or it happened at least {@code interval} ago. */
    static boolean hasPassedSince(@Nullable LocalDateTime lastTime, Duration interval, LocalDateTime now) {
        return lastTime == null || Duration.between(lastTime, now).compareTo(interval) >= 0;
    }
}
