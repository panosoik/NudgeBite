package com.panagiotisoikonomou.nudgebite.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.entity.WellScore;
import com.panagiotisoikonomou.nudgebite.data.entity.relation.MealWithIngredients;
import com.panagiotisoikonomou.nudgebite.progress.WeekPeriod;
import com.panagiotisoikonomou.nudgebite.quality.well.WellDataSufficiency;
import com.panagiotisoikonomou.nudgebite.quality.well.WellExpectedMeals;
import com.panagiotisoikonomou.nudgebite.quality.well.WellPointMatrix;
import com.panagiotisoikonomou.nudgebite.quality.well.WellScoreResult;
import com.panagiotisoikonomou.nudgebite.quality.well.WellWindow;
import com.panagiotisoikonomou.nudgebite.quality.well.WellWindowCalculator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/** A period with too little logging gets no row, so the chart draws a gap rather than a zero. */
public final class WellScoreRepository {

    private static final String TAG = "WellScoreRepository";

    private WellScoreRepository() {
    }

    public static LiveData<List<WellScore>> observeAll(Application application) {
        return AppDatabase.getDatabase(application).wellScoreDao().getAll();
    }

    public static void recomputeAll(AppDatabase database) {
        List<MealWithIngredients> allMeals = database.mealDao().getAllMealsWithIngredientsSync();
        if (allMeals.isEmpty()) {
            database.wellScoreDao().deleteAll();
            return;
        }
        LocalDate today = LocalDate.now();
        LocalDate firstDay = allMeals.get(0).meal.loggedAt.toLocalDate();
        LocalDateTime computedAt = LocalDateTime.now();
        int expectedMealsPerDay = expectedMealsPerDay(database);

        database.wellScoreDao().deleteAll();

        int written = 0;
        for (LocalDate day = firstDay; !day.isAfter(today); day = day.plusDays(1)) {
            written += store(database, WellWindow.DAILY,
                    WellWindowCalculator.dailyPeriod(allMeals, day), allMeals, computedAt,
                    expectedMealsPerDay);
        }
        // Anchored on the first week's Monday, otherwise a late first log skips the current week.
        for (LocalDate weekStart = WeekPeriod.containing(firstDay).startMonday;
             !weekStart.isAfter(today); weekStart = weekStart.plusWeeks(1)) {
            written += store(database, WellWindow.WEEKLY,
                    WellWindowCalculator.weeklyPeriod(allMeals, weekStart), allMeals, computedAt,
                    expectedMealsPerDay);
        }
        for (YearMonth month = YearMonth.from(firstDay); !month.isAfter(YearMonth.from(today));
             month = month.plusMonths(1)) {
            written += store(database, WellWindow.MONTHLY,
                    monthlyPeriodEndingIn(allMeals, month, today), allMeals, computedAt,
                    expectedMealsPerDay);
        }
        written += storeYearly(database, allMeals, today, computedAt, expectedMealsPerDay);

        Log.i(TAG, "Recomputed WELL-inspired scores: " + written + " periods stored from "
                + allMeals.size() + " meals");
    }

    private static WellWindowCalculator.Period monthlyPeriodEndingIn(List<MealWithIngredients> allMeals,
                                                                      YearMonth month, LocalDate today) {
        LocalDate monthEnd = month.atEndOfMonth();
        return WellWindowCalculator.monthlyPeriod(allMeals, monthEnd.isAfter(today) ? today : monthEnd);
    }

    private static int expectedMealsPerDay(AppDatabase database) {
        User user = database.userDao().getUserSync();
        if (user == null) {
            return WellExpectedMeals.DEFAULT_EXPECTED_MEALS_PER_DAY;
        }
        return WellExpectedMeals.fromDeclaredMeals(user.eatsBreakfast, user.eatsLunch, user.eatsDinner);
    }

    private static int store(AppDatabase database, WellWindow window,
                             WellWindowCalculator.Period period,
                             List<MealWithIngredients> allMeals, LocalDateTime computedAt,
                             int expectedMealsPerDay) {
        WellScoreResult result = WellWindowCalculator.score(allMeals, period, expectedMealsPerDay);
        if (!result.isScored()) {
            return 0;
        }
        WellScore row = new WellScore();
        row.windowType = window.name();
        row.periodStart = period.start;
        row.periodEnd = period.end;
        row.totalPoints = result.totalPoints;
        row.maxPoints = result.maxPoints;
        row.sufficiency = result.sufficiency.name();
        row.observedDays = result.observedDays;
        row.loggedMeals = result.loggedMeals;
        row.componentPoints = WellScoreComponents.formatPoints(result);
        row.componentFrequencies = WellScoreComponents.formatFrequencies(result);
        row.computedAt = computedAt;
        database.wellScoreDao().upsert(row);
        return 1;
    }

    private static int storeYearly(AppDatabase database, List<MealWithIngredients> allMeals,
                                   LocalDate today, LocalDateTime computedAt, int expectedMealsPerDay) {
        WellWindowCalculator.YearlySummary summary =
                WellWindowCalculator.yearlySummary(allMeals, today.getYear(), today, expectedMealsPerDay);
        if (!summary.isScored()) {
            return 0;
        }
        WellScore row = new WellScore();
        row.windowType = WellWindow.YEARLY.name();
        row.periodStart = LocalDate.of(summary.year, 1, 1);
        row.periodEnd = LocalDate.of(summary.year, 12, 31);
        row.totalPoints = (int) Math.round(summary.meanMonthlyPoints());
        row.maxPoints = WellPointMatrix.MAX_TOTAL_POINTS;
        row.sufficiency = WellDataSufficiency.PROVISIONAL.name();
        row.observedDays = summary.scoredMonths.size();
        row.loggedMeals = 0;
        row.componentPoints = "";
        row.componentFrequencies = "";
        row.computedAt = computedAt;
        database.wellScoreDao().upsert(row);
        return 1;
    }
}
