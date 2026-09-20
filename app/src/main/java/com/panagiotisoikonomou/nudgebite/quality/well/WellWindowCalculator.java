package com.panagiotisoikonomou.nudgebite.quality.well;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.entity.relation.MealWithIngredients;
import com.panagiotisoikonomou.nudgebite.progress.WeekPeriod;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** The daily window is progressive within the week; no window reaches before the first logged meal. */
public final class WellWindowCalculator {

    public static final int OBSERVATION_WINDOW_DAYS = 30;

    private WellWindowCalculator() {
    }

    public static Period dailyPeriod(List<MealWithIngredients> allMeals, LocalDate day) {
        LocalDate weekStart = WeekPeriod.containing(day).startMonday;
        return periodFrom(allMeals, weekStart, day);
    }

    public static Period weeklyPeriod(List<MealWithIngredients> allMeals, LocalDate day) {
        WeekPeriod week = WeekPeriod.containing(day);
        return periodFrom(allMeals, week.startMonday, week.endSunday);
    }

    public static Period monthlyPeriod(List<MealWithIngredients> allMeals, LocalDate asOf) {
        return periodFrom(allMeals, asOf.minusDays(OBSERVATION_WINDOW_DAYS - 1L), asOf);
    }

    public static WellScoreResult score(List<MealWithIngredients> allMeals, Period period,
                                        int expectedMealsPerDay) {
        return WellScorer.score(WellFrequencyEstimator.observe(
                allMeals, period.start, period.end, expectedMealsPerDay));
    }

    public static YearlySummary yearlySummary(List<MealWithIngredients> allMeals, int year, LocalDate asOf,
                                              int expectedMealsPerDay) {
        List<Integer> scoredMonthTotals = new ArrayList<>();
        List<YearMonth> scoredMonths = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            if (yearMonth.atDay(1).isAfter(asOf)) {
                break;
            }
            LocalDate monthEnd = yearMonth.atEndOfMonth().isAfter(asOf) ? asOf : yearMonth.atEndOfMonth();
            Period period = periodFrom(allMeals, yearMonth.atDay(1), monthEnd);
            WellScoreResult result = score(allMeals, period, expectedMealsPerDay);
            if (result.isScored()) {
                scoredMonthTotals.add(result.totalPoints);
                scoredMonths.add(yearMonth);
            }
        }
        return new YearlySummary(year, scoredMonths, scoredMonthTotals);
    }

    private static Period periodFrom(List<MealWithIngredients> allMeals, LocalDate requestedStart, LocalDate end) {
        LocalDate windowFloor = end.minusDays(OBSERVATION_WINDOW_DAYS - 1L);
        LocalDate start = requestedStart.isBefore(windowFloor) ? windowFloor : requestedStart;
        LocalDate firstLog = firstLoggedDay(allMeals);
        if (firstLog != null && start.isBefore(firstLog)) {
            start = firstLog;
        }
        if (start.isAfter(end)) {
            start = end;
        }
        return new Period(start, end);
    }

    @Nullable
    private static LocalDate firstLoggedDay(List<MealWithIngredients> allMeals) {
        LocalDate first = null;
        for (MealWithIngredients meal : allMeals) {
            LocalDate day = meal.meal.loggedAt.toLocalDate();
            if (first == null || day.isBefore(first)) {
                first = day;
            }
        }
        return first;
    }

    public static final class Period {
        public final LocalDate start;
        public final LocalDate end;

        Period(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }
    }

    public static final class YearlySummary {
        public final int year;
        public final List<YearMonth> scoredMonths;
        public final List<Integer> monthTotals;

        YearlySummary(int year, List<YearMonth> scoredMonths, List<Integer> monthTotals) {
            this.year = year;
            this.scoredMonths = scoredMonths;
            this.monthTotals = monthTotals;
        }

        public boolean isScored() {
            return !monthTotals.isEmpty();
        }

        public double meanMonthlyPoints() {
            if (monthTotals.isEmpty()) {
                return 0.0;
            }
            int sum = 0;
            for (int total : monthTotals) {
                sum += total;
            }
            return (double) sum / monthTotals.size();
        }
    }
}
