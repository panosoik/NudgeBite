package com.panagiotisoikonomou.nudgebite.export;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.entity.EatingOccasionEvent;
import com.panagiotisoikonomou.nudgebite.data.entity.Goal;
import com.panagiotisoikonomou.nudgebite.data.entity.Ingredient;
import com.panagiotisoikonomou.nudgebite.data.entity.InterventionLog;
import com.panagiotisoikonomou.nudgebite.data.entity.MomentaryAssessmentEvent;
import com.panagiotisoikonomou.nudgebite.data.entity.SignalEvent;
import com.panagiotisoikonomou.nudgebite.data.entity.WaterLog;
import com.panagiotisoikonomou.nudgebite.data.entity.WellScore;
import com.panagiotisoikonomou.nudgebite.data.entity.relation.MealWithIngredients;
import com.panagiotisoikonomou.nudgebite.data.repository.WellScoreComponents;
import com.panagiotisoikonomou.nudgebite.food.well.WellComponent;

import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CsvTables {

    private CsvTables() {
    }

    private interface Table {
        void write(ExportSnapshot snapshot, CsvWriter csv) throws IOException;
    }

    private static final Map<String, Table> TABLES = new LinkedHashMap<>();

    static {
        TABLES.put("eating_occasions.csv", CsvTables::eatingOccasions);
        TABLES.put("interventions.csv", CsvTables::interventions);
        TABLES.put("signal_events.csv", CsvTables::signalEvents);
        TABLES.put("ema.csv", CsvTables::ema);
        TABLES.put("ema_answers.csv", CsvTables::emaAnswers);
        TABLES.put("meals.csv", CsvTables::meals);
        TABLES.put("meal_ingredients.csv", CsvTables::mealIngredients);
        TABLES.put("well_scores.csv", CsvTables::wellScores);
        TABLES.put("well_component_points.csv", CsvTables::wellComponentPoints);
        TABLES.put("well_component_frequencies.csv", CsvTables::wellComponentFrequencies);
        TABLES.put("water_log.csv", CsvTables::waterLog);
        TABLES.put("day_contributions.csv", CsvTables::dayContributions);
        TABLES.put("goals.csv", CsvTables::goals);
        TABLES.put("goal_progress.csv", CsvTables::goalProgress);
        TABLES.put("profile.csv", CsvTables::profile);
    }

    public static final String[] FILE_NAMES = TABLES.keySet().toArray(new String[0]);

    public static void write(String fileName, ExportSnapshot snapshot, Writer out) throws IOException {
        Table table = TABLES.get(fileName);
        if (table == null) {
            throw new IllegalArgumentException("no such export table: " + fileName);
        }
        try (CsvWriter csv = new CsvWriter(out)) {
            table.write(snapshot, csv);
        }
    }

    private static void eatingOccasions(ExportSnapshot snapshot, CsvWriter csv) throws IOException {
        csv.writeHeader("id", "created_at", "trigger_combo", "trigger_type", "decision_stage", "outcome");
        for (EatingOccasionEvent event : snapshot.eatingOccasions) {
            csv.writeRow(event.id, event.creationDate, event.triggerCombo, event.triggerType,
                    event.decisionStage, event.outcome);
        }
    }

    private static void interventions(ExportSnapshot snapshot, CsvWriter csv) throws IOException {
        csv.writeHeader("id", "created_at", "responded_at", "response_seconds",
                "eating_occasion_event_id", "category", "format", "user_response",
                "trigger_combo", "trigger_type", "occasion_outcome", "payload");
        for (InterventionLog log : snapshot.interventions) {
            EatingOccasionEvent occasion = occasionFor(snapshot, log.eatingOccasionEventId);
            csv.writeRow(log.id, log.creationDate, log.respondedAt,
                    secondsBetween(log), log.eatingOccasionEventId, log.category, log.format,
                    log.userResponse,
                    occasion == null ? null : occasion.triggerCombo,
                    occasion == null ? null : occasion.triggerType,
                    occasion == null ? null : occasion.outcome,
                    log.payload);
        }
    }

    private static void signalEvents(ExportSnapshot snapshot, CsvWriter csv) throws IOException {
        csv.writeHeader("id", "created_at", "type", "eating_occasion_event_id");
        for (SignalEvent event : snapshot.signals) {
            csv.writeRow(event.id, event.creationDate, event.type, event.eatingOccasionEventId);
        }
    }

    private static void ema(ExportSnapshot snapshot, CsvWriter csv) throws IOException {
        csv.writeHeader("id", "created_at", "eating_occasion_event_id", "trigger_combo",
                "hunger_level", "fatigue_level", "time_pressure", "food_available_at_home",
                "meal_decision_stage", "delivery_intent", "dinner_planned",
                "cooking_willingness", "craving_type",
                "user_tone", "input_mode", "confidence", "needs_clarification");
        for (MomentaryAssessmentEvent event : snapshot.assessments) {
            EatingOccasionEvent occasion = occasionFor(snapshot, event.eatingOccasionEventId);
            csv.writeRow(event.id, event.creationDate, event.eatingOccasionEventId,
                    occasion == null ? null : occasion.triggerCombo,
                    event.hungerLevel, event.fatigueLevel, event.timePressure,
                    event.foodAvailableAtHome, event.mealDecisionStage, event.deliveryIntent,
                    event.dinnerPlanned,
                    event.cookingWillingness, event.cravingType, event.userTone,
                    event.inputMode, event.confidence, event.needsClarification);
        }
    }

    private static void emaAnswers(ExportSnapshot snapshot, CsvWriter csv) throws IOException {
        csv.writeHeader("ema_event_id", "created_at", "eating_occasion_event_id", "question", "answer");
        for (MomentaryAssessmentEvent event : snapshot.assessments) {
            answer(csv, event, "hunger_level", event.hungerLevel);
            answer(csv, event, "fatigue_level", event.fatigueLevel);
            answer(csv, event, "time_pressure", event.timePressure);
            answer(csv, event, "food_available_at_home", event.foodAvailableAtHome);
            answer(csv, event, "meal_decision_stage", event.mealDecisionStage);
            answer(csv, event, "delivery_intent", event.deliveryIntent);
            answer(csv, event, "dinner_planned", event.dinnerPlanned);
            answer(csv, event, "cooking_willingness", event.cookingWillingness);
            answer(csv, event, "craving_type", event.cravingType);
            answer(csv, event, "user_tone", event.userTone);
        }
    }

    /** Unanswered questions are left out, so every row here is a real answer. */
    private static void answer(CsvWriter csv, MomentaryAssessmentEvent event, String question,
                               @Nullable Object value) throws IOException {
        if (value == null) {
            return;
        }
        csv.writeRow(event.id, event.creationDate, event.eatingOccasionEventId, question, value);
    }

    private static void meals(ExportSnapshot snapshot, CsvWriter csv) throws IOException {
        csv.writeHeader("id", "logged_at", "meal_slot", "meal_source", "dish_name",
                "has_photo", "ingredient_count");
        for (MealWithIngredients meal : snapshot.meals) {
            csv.writeRow(meal.meal.id, meal.meal.loggedAt, meal.meal.mealSlot, meal.meal.mealSource,
                    meal.meal.dishName, meal.meal.photoUri != null,
                    meal.ingredients == null ? 0 : meal.ingredients.size());
        }
    }

    private static void mealIngredients(ExportSnapshot snapshot, CsvWriter csv) throws IOException {
        csv.writeHeader("id", "meal_id", "logged_at", "name", "food_item", "food_category",
                "uom", "serving_value", "calories", "protein_g", "carbs_g", "fat_g");
        for (MealWithIngredients meal : snapshot.meals) {
            if (meal.ingredients == null) {
                continue;
            }
            for (Ingredient ingredient : meal.ingredients) {
                csv.writeRow(ingredient.id, meal.meal.id, meal.meal.loggedAt, ingredient.name,
                        ingredient.foodItem, ingredient.foodCategory, ingredient.uom,
                        ingredient.servingValue, ingredient.calories, ingredient.macroProtein,
                        ingredient.macroCarbs, ingredient.macroFat);
            }
        }
    }

    private static void waterLog(ExportSnapshot snapshot, CsvWriter csv) throws IOException {
        csv.writeHeader("id", "logged_at");
        for (WaterLog glass : snapshot.waterLog) {
            csv.writeRow(glass.id, glass.loggedAt);
        }
    }

    private static void wellScores(ExportSnapshot snapshot, CsvWriter csv) throws IOException {
        csv.writeHeader("id", "window_type", "period_start", "period_end", "total_points",
                "max_points", "sufficiency", "observed_days", "logged_meals", "computed_at");
        for (WellScore score : snapshot.wellScores) {
            csv.writeRow(score.id, score.windowType, score.periodStart, score.periodEnd,
                    score.totalPoints, score.maxPoints, score.sufficiency, score.observedDays,
                    score.loggedMeals, score.computedAt);
        }
    }

    private static void wellComponentPoints(ExportSnapshot snapshot, CsvWriter csv) throws IOException {
        csv.writeHeader("well_score_id", "window_type", "period_end", "component", "points");
        for (WellScore score : snapshot.wellScores) {
            Map<WellComponent, Integer> points = WellScoreComponents.parsePoints(score.componentPoints);
            for (Map.Entry<WellComponent, Integer> entry : points.entrySet()) {
                csv.writeRow(score.id, score.windowType, score.periodEnd,
                        entry.getKey(), entry.getValue());
            }
        }
    }

    private static void wellComponentFrequencies(ExportSnapshot snapshot, CsvWriter csv) throws IOException {
        csv.writeHeader("well_score_id", "window_type", "period_end", "component",
                "occasions", "rate_per_day");
        for (WellScore score : snapshot.wellScores) {
            Map<WellComponent, WellScoreComponents.Frequency> frequencies =
                    WellScoreComponents.parseFrequencies(score.componentFrequencies);
            for (Map.Entry<WellComponent, WellScoreComponents.Frequency> entry : frequencies.entrySet()) {
                csv.writeRow(score.id, score.windowType, score.periodEnd, entry.getKey(),
                        entry.getValue().occasions, entry.getValue().ratePerDay);
            }
        }
    }

    private static void dayContributions(ExportSnapshot snapshot, CsvWriter csv) throws IOException {
        csv.writeHeader("date", "contribution", "components_cleared", "component_count");
        for (ExportSnapshot.DayContributionRow row : snapshot.dayContributions) {
            csv.writeRow(row.date, row.contribution, row.componentsCleared,
                    row.componentsCleared.size());
        }
    }

    private static void goals(ExportSnapshot snapshot, CsvWriter csv) throws IOException {
        csv.writeHeader("id", "name", "metric_type", "target_value");
        for (Goal goal : snapshot.goals) {
            csv.writeRow(goal.id, goal.name, goal.metricType, goal.targetValue);
        }
    }

    private static void goalProgress(ExportSnapshot snapshot, CsvWriter csv) throws IOException {
        csv.writeHeader("week_start", "week_end", "goal_id", "goal_name", "metric_type",
                "target_value", "actual_value", "met");
        for (ExportSnapshot.GoalProgressRow row : snapshot.goalProgress) {
            csv.writeRow(row.weekStart, row.weekEnd, row.goalId, row.goalName, row.metricType,
                    row.target, row.actual, row.met);
        }
    }

    private static void profile(ExportSnapshot snapshot, CsvWriter csv) throws IOException {
        csv.writeHeader("sex", "age", "activity_level", "eats_breakfast", "eats_lunch",
                "eats_dinner", "eats_snacks", "breakfast_time_of_day", "lunch_time_of_day",
                "dinner_time_of_day", "preferred_foods", "foods_to_avoid",
                "veg_servings_goal_per_day", "delivery_acceptable_per_week");
        ExportSnapshot.Profile profile = snapshot.profile;
        if (profile == null) {
            return;
        }
        csv.writeRow(profile.sex, profile.age, profile.activityLevel, profile.eatsBreakfast,
                profile.eatsLunch, profile.eatsDinner, profile.eatsSnacks,
                profile.breakfastTimeOfDay, profile.lunchTimeOfDay, profile.dinnerTimeOfDay,
                profile.preferredFoods, profile.foodsToAvoid, profile.vegServingsGoalPerDay,
                profile.deliveryAcceptablePerWeek);
    }

    @Nullable
    private static EatingOccasionEvent occasionFor(ExportSnapshot snapshot, @Nullable Long id) {
        if (id == null) {
            return null;
        }
        for (EatingOccasionEvent event : snapshot.eatingOccasions) {
            if (event.id == id) {
                return event;
            }
        }
        return null;
    }

    @Nullable
    private static Long secondsBetween(InterventionLog log) {
        if (log.respondedAt == null || log.creationDate == null) {
            return null;
        }
        return Duration.between(log.creationDate, log.respondedAt).getSeconds();
    }
}
