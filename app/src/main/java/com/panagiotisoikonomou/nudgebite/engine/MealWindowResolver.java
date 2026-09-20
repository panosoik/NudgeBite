package com.panagiotisoikonomou.nudgebite.engine;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.entity.User;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class MealWindowResolver {

    private static final int BUFFER_MINUTES = 60;
    private static final int MINUTES_PER_DAY = 24 * 60;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private MealWindowResolver() {
    }

    public static MealWindow resolveMealWindow(User user, LocalDateTime at) {
        if (user.eatsDinner && isWithinWindow(user.dinnerTimeOfDay, at)) {
            return MealWindow.DINNER;
        }
        if (user.eatsLunch && isWithinWindow(user.lunchTimeOfDay, at)) {
            return MealWindow.LUNCH;
        }
        if (user.eatsBreakfast && isWithinWindow(user.breakfastTimeOfDay, at)) {
            return MealWindow.BREAKFAST;
        }
        return MealWindow.NONE;
    }

    private static boolean isWithinWindow(@Nullable String timeOfDay, LocalDateTime at) {
        if (timeOfDay == null) {
            return false;
        }
        try {
            LocalTime target = LocalTime.parse(timeOfDay, TIME_FORMAT);
            int targetMinutes = target.getHour() * 60 + target.getMinute();
            LocalTime now = at.toLocalTime();
            int nowMinutes = now.getHour() * 60 + now.getMinute();
            // Wraps past midnight: for a 23:30 meal, 00:15 is 45 minutes away, not 23 hours.
            int rawDiff = Math.abs(targetMinutes - nowMinutes);
            int circularDiff = Math.min(rawDiff, MINUTES_PER_DAY - rawDiff);
            return circularDiff <= BUFFER_MINUTES;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
