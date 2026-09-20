package com.panagiotisoikonomou.nudgebite.dashboard;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public final class DayNavigation {

    public static final int MAX_DAYS_BACK = 30;

    private DayNavigation() {
    }

    private static LocalDate earliestDay(LocalDate today) {
        return today.minusDays(MAX_DAYS_BACK);
    }

    public static boolean canGoBack(LocalDate selected, LocalDate today) {
        return selected.isAfter(earliestDay(today));
    }

    public static boolean canGoForward(LocalDate selected, LocalDate today) {
        return selected.isBefore(today);
    }

    public static LocalDate clamp(LocalDate selected, LocalDate today) {
        if (selected.isAfter(today)) {
            return today;
        }
        LocalDate earliest = earliestDay(today);
        return selected.isBefore(earliest) ? earliest : selected;
    }

    public static String titleFor(LocalDate selected, LocalDate today) {
        if (selected.equals(today)) {
            return "Today";
        }
        if (selected.equals(today.minusDays(1))) {
            return "Yesterday";
        }
        return selected.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
    }

    public static String mealsHeaderFor(LocalDate selected, LocalDate today) {
        if (selected.equals(today)) {
            return "TODAY'S MEALS";
        }
        if (selected.equals(today.minusDays(1))) {
            return "YESTERDAY'S MEALS";
        }
        return "MEALS LOGGED";
    }
}
