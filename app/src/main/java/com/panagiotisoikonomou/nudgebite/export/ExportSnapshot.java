package com.panagiotisoikonomou.nudgebite.export;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.entity.EatingOccasionEvent;
import com.panagiotisoikonomou.nudgebite.data.entity.Goal;
import com.panagiotisoikonomou.nudgebite.data.entity.InterventionLog;
import com.panagiotisoikonomou.nudgebite.data.entity.MomentaryAssessmentEvent;
import com.panagiotisoikonomou.nudgebite.data.entity.SignalEvent;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.entity.WaterLog;
import com.panagiotisoikonomou.nudgebite.data.entity.WellScore;
import com.panagiotisoikonomou.nudgebite.data.entity.relation.MealWithIngredients;
import com.panagiotisoikonomou.nudgebite.data.enums.DayContribution;
import com.panagiotisoikonomou.nudgebite.dashboard.DayContributionEvaluator;
import com.panagiotisoikonomou.nudgebite.progress.GoalMetricCalculator;
import com.panagiotisoikonomou.nudgebite.progress.GoalMetricType;
import com.panagiotisoikonomou.nudgebite.progress.WeekPeriod;
import com.panagiotisoikonomou.nudgebite.food.well.WellComponent;
import com.panagiotisoikonomou.nudgebite.quality.well.WellDailyAdequacy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Read-only. Day contributions and goal figures are recomputed from meals using the current profile. */
public final class ExportSnapshot {

    public final LocalDateTime exportedAt;
    public final int databaseVersion;

    @Nullable public final Profile profile;
    public final List<MealWithIngredients> meals;
    public final List<EatingOccasionEvent> eatingOccasions;
    public final List<InterventionLog> interventions;
    public final List<MomentaryAssessmentEvent> assessments;
    public final List<SignalEvent> signals;
    public final List<WellScore> wellScores;
    public final List<WaterLog> waterLog;
    public final List<Goal> goals;
    public final List<DayContributionRow> dayContributions;
    public final List<GoalProgressRow> goalProgress;

    ExportSnapshot(LocalDateTime exportedAt, int databaseVersion, @Nullable Profile profile,
                   List<MealWithIngredients> meals, List<EatingOccasionEvent> eatingOccasions,
                   List<InterventionLog> interventions, List<MomentaryAssessmentEvent> assessments,
                   List<SignalEvent> signals, List<WellScore> wellScores,
                   List<WaterLog> waterLog, List<Goal> goals,
                   List<DayContributionRow> dayContributions, List<GoalProgressRow> goalProgress) {
        this.exportedAt = exportedAt;
        this.databaseVersion = databaseVersion;
        this.profile = profile;
        this.meals = Collections.unmodifiableList(meals);
        this.eatingOccasions = Collections.unmodifiableList(eatingOccasions);
        this.interventions = Collections.unmodifiableList(interventions);
        this.assessments = Collections.unmodifiableList(assessments);
        this.signals = Collections.unmodifiableList(signals);
        this.wellScores = Collections.unmodifiableList(wellScores);
        this.waterLog = Collections.unmodifiableList(waterLog);
        this.goals = Collections.unmodifiableList(goals);
        this.dayContributions = Collections.unmodifiableList(dayContributions);
        this.goalProgress = Collections.unmodifiableList(goalProgress);
    }

    public static ExportSnapshot read(AppDatabase database) {
        List<MealWithIngredients> meals = database.mealDao().getAllMealsWithIngredientsSync();
        User user = database.userDao().getUserSync();
        List<Goal> goals = database.goalDao().getAllGoalsSync();

        return new ExportSnapshot(
                LocalDateTime.now(),
                AppDatabase.SCHEMA_VERSION,
                Profile.of(user),
                meals,
                database.eatingOccasionEventDao().getAllSync(),
                database.interventionLogDao().getAllSync(),
                database.momentaryAssessmentEventDao().getAllSync(),
                database.signalEventDao().getAllSync(),
                database.wellScoreDao().getAllSync(),
                database.waterLogDao().getAllSync(),
                goals,
                dayContributionRows(meals),
                goalProgressRows(meals, goals));
    }

    static List<DayContributionRow> dayContributionRows(List<MealWithIngredients> meals) {
        List<DayContributionRow> rows = new ArrayList<>();
        if (meals.isEmpty()) {
            return rows;
        }
        LocalDate[] loggedDays = firstAndLastLoggedDay(meals);
        for (LocalDate day = loggedDays[0]; !day.isAfter(loggedDays[1]); day = day.plusDays(1)) {
            rows.add(new DayContributionRow(day,
                    DayContributionEvaluator.contributionOn(meals, day),
                    sortedNames(WellDailyAdequacy.presentOn(meals, day))));
        }
        return rows;
    }

    private static List<String> sortedNames(Set<WellComponent> components) {
        Set<String> names = new TreeSet<>();
        for (WellComponent component : components) {
            names.add(component.name());
        }
        return new ArrayList<>(names);
    }

    /** Meals are not guaranteed to be in date order. */
    private static LocalDate[] firstAndLastLoggedDay(List<MealWithIngredients> meals) {
        LocalDate first = meals.get(0).meal.loggedAt.toLocalDate();
        LocalDate last = first;
        for (MealWithIngredients meal : meals) {
            LocalDate day = meal.meal.loggedAt.toLocalDate();
            if (day.isBefore(first)) {
                first = day;
            }
            if (day.isAfter(last)) {
                last = day;
            }
        }
        return new LocalDate[]{first, last};
    }

    static List<GoalProgressRow> goalProgressRows(List<MealWithIngredients> meals, List<Goal> goals) {
        List<GoalProgressRow> rows = new ArrayList<>();
        if (meals.isEmpty() || goals.isEmpty()) {
            return rows;
        }
        LocalDate[] loggedDays = firstAndLastLoggedDay(meals);
        for (WeekPeriod week = WeekPeriod.containing(loggedDays[0]);
             !week.startMonday.isAfter(loggedDays[1]); week = week.next()) {
            for (Goal goal : goals) {
                GoalMetricType metricType = GoalMetricType.parseOrNull(goal.metricType);
                if (metricType == null) {
                    continue;
                }
                double actual = GoalMetricCalculator.weeklyValue(metricType, meals, week.endSunday);
                rows.add(new GoalProgressRow(week.startMonday, week.endSunday, goal.id, goal.name,
                        metricType.name(), goal.targetValue, actual,
                        GoalMetricCalculator.meetsGoal(metricType, actual, goal.targetValue)));
            }
        }
        return rows;
    }

    public static final class DayContributionRow {
        public final LocalDate date;
        public final DayContribution contribution;
        public final List<String> componentsCleared;

        DayContributionRow(LocalDate date, DayContribution contribution, List<String> componentsCleared) {
            this.date = date;
            this.contribution = contribution;
            this.componentsCleared = Collections.unmodifiableList(componentsCleared);
        }
    }

    public static final class GoalProgressRow {
        public final LocalDate weekStart;
        public final LocalDate weekEnd;
        public final long goalId;
        public final String goalName;
        public final String metricType;
        public final double target;
        public final double actual;
        public final boolean met;

        GoalProgressRow(LocalDate weekStart, LocalDate weekEnd, long goalId, String goalName,
                        String metricType, double target, double actual, boolean met) {
            this.weekStart = weekStart;
            this.weekEnd = weekEnd;
            this.goalId = goalId;
            this.goalName = goalName;
            this.metricType = metricType;
            this.target = target;
            this.actual = actual;
            this.met = met;
        }
    }

    /** Its own type so a new User field can never widen the export; addresses, coordinates, height and weight are excluded. */
    public static final class Profile {
        public final String sex;
        public final int age;
        public final String activityLevel;
        public final boolean eatsBreakfast;
        public final boolean eatsLunch;
        public final boolean eatsDinner;
        public final boolean eatsSnacks;
        @Nullable public final String breakfastTimeOfDay;
        @Nullable public final String lunchTimeOfDay;
        @Nullable public final String dinnerTimeOfDay;
        public final List<String> preferredFoods;
        public final List<String> foodsToAvoid;
        @Nullable public final Integer vegServingsGoalPerDay;
        @Nullable public final Integer deliveryAcceptablePerWeek;

        private Profile(User user) {
            this.sex = user.sex == null ? null : user.sex.name();
            this.age = user.age;
            this.activityLevel = user.activityLevel == null ? null : user.activityLevel.name();
            this.eatsBreakfast = user.eatsBreakfast;
            this.eatsLunch = user.eatsLunch;
            this.eatsDinner = user.eatsDinner;
            this.eatsSnacks = user.eatsSnacks;
            this.breakfastTimeOfDay = user.breakfastTimeOfDay;
            this.lunchTimeOfDay = user.lunchTimeOfDay;
            this.dinnerTimeOfDay = user.dinnerTimeOfDay;
            this.preferredFoods = orEmpty(user.preferredFoods);
            this.foodsToAvoid = orEmpty(user.foodsToAvoid);
            this.vegServingsGoalPerDay = user.vegServingsGoalPerDay;
            this.deliveryAcceptablePerWeek = user.deliveryAcceptablePerWeek;
        }

        @Nullable
        static Profile of(@Nullable User user) {
            return user == null ? null : new Profile(user);
        }

        private static List<String> orEmpty(@Nullable List<String> values) {
            return values == null ? Collections.emptyList() : Collections.unmodifiableList(values);
        }
    }
}
