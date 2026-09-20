package com.panagiotisoikonomou.nudgebite.dashboard;

import com.panagiotisoikonomou.nudgebite.data.enums.DayContribution;

public final class HomeStatusText {

    private HomeStatusText() {
    }

    public static String forDay(DayContribution today, DayContribution yesterday, boolean noLogsYet) {
        boolean todayPositive = today == DayContribution.POSITIVE;
        if (todayPositive && yesterday == DayContribution.POSITIVE) {
            return "You're building a good rhythm";
        }
        if (todayPositive) {
            return "A balanced start today";
        }
        if (noLogsYet) {
            return "Nothing logged yet today";
        }
        return "Your day so far";
    }

    public static String forPastDay(DayContribution contribution, boolean nothingLogged) {
        if (contribution == DayContribution.POSITIVE) {
            return "A balanced day";
        }
        if (nothingLogged) {
            return "Nothing logged";
        }
        return "What you logged";
    }
}
