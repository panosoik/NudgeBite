package com.panagiotisoikonomou.nudgebite.progress;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public final class WeekPeriod {

    public static final int DAYS = 7;

    public final LocalDate startMonday;
    public final LocalDate endSunday;

    private WeekPeriod(LocalDate startMonday) {
        this.startMonday = startMonday;
        this.endSunday = startMonday.plusDays(DAYS - 1L);
    }

    public static WeekPeriod containing(LocalDate date) {
        LocalDate monday = date.minusDays(date.getDayOfWeek().getValue() - 1L);
        return new WeekPeriod(monday);
    }

    public static WeekPeriod currentWeek() {
        return containing(LocalDate.now());
    }

    public WeekPeriod next() {
        return new WeekPeriod(startMonday.plusWeeks(1));
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(startMonday) && !date.isAfter(endSunday);
    }

    public List<LocalDate> days() {
        List<LocalDate> days = new ArrayList<>(DAYS);
        for (int i = 0; i < DAYS; i++) {
            days.add(startMonday.plusDays(i));
        }
        return days;
    }

    public static List<WeekPeriod> weeksInMonth(YearMonth month) {
        List<WeekPeriod> weeks = new ArrayList<>();
        LocalDate lastOfMonth = month.atEndOfMonth();
        WeekPeriod week = containing(month.atDay(1));
        while (!week.startMonday.isAfter(lastOfMonth)) {
            weeks.add(week);
            week = week.next();
        }
        return weeks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WeekPeriod)) {
            return false;
        }
        return startMonday.equals(((WeekPeriod) o).startMonday);
    }

    @Override
    public int hashCode() {
        return startMonday.hashCode();
    }
}
