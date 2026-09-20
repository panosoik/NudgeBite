package com.panagiotisoikonomou.nudgebite.utils;

import androidx.annotation.Nullable;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.util.Locale;

/** Meal times are stored as 24-hour "HH:mm" text and shown as "7:30 pm". */
public final class MealTimeText {

    private MealTimeText() {
    }

    public static String toStored(int hour, int minute) {
        return String.format(Locale.US, "%02d:%02d", hour, minute);
    }

    /** "7:30 pm", or "Not set" when there is no readable time. */
    public static String display(@Nullable String stored) {
        int[] hourAndMinute = parse(stored);
        if (hourAndMinute == null) {
            return "Not set";
        }
        int hour12 = hourAndMinute[0] % 12 == 0 ? 12 : hourAndMinute[0] % 12;
        return String.format(Locale.US, "%d:%02d %s", hour12, hourAndMinute[1], hourAndMinute[0] >= 12 ? "pm" : "am");
    }

    /** Where a time picker opens: the stored time, or midday when there is none. */
    public static int[] parseOrNoon(@Nullable String stored) {
        int[] hourAndMinute = parse(stored);
        return hourAndMinute != null ? hourAndMinute : new int[]{12, 0};
    }

    @Nullable
    public static LocalTime toLocalTimeOrNull(@Nullable String stored) {
        int[] hourAndMinute = parse(stored);
        if (hourAndMinute == null) {
            return null;
        }
        try {
            return LocalTime.of(hourAndMinute[0], hourAndMinute[1]);
        } catch (DateTimeException e) {
            return null;
        }
    }

    @Nullable
    private static int[] parse(@Nullable String stored) {
        String[] parts = stored == null ? new String[0] : stored.split(":");
        if (parts.length != 2) {
            return null;
        }
        try {
            return new int[]{Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
