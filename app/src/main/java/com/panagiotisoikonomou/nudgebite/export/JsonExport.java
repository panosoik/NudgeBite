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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public final class JsonExport {

    public static final int FORMAT_VERSION = 1;

    private JsonExport() {
    }

    public static void write(ExportSnapshot snapshot, Writer out) throws IOException {
        try {
            out.write("{\n");
            writeField(out, "export_meta", meta(snapshot));
            out.write(",\n");
            writeField(out, "profile", profile(snapshot.profile));
            out.write(",\n");

            writeArray(out, "eating_occasions", snapshot.eatingOccasions, JsonExport::occasion);
            out.write(",\n");
            writeArray(out, "interventions", snapshot.interventions, JsonExport::intervention);
            out.write(",\n");
            writeArray(out, "signal_events", snapshot.signals, JsonExport::signal);
            out.write(",\n");
            writeArray(out, "ema", snapshot.assessments, JsonExport::assessment);
            out.write(",\n");
            writeArray(out, "meals", snapshot.meals, JsonExport::meal);
            out.write(",\n");
            writeArray(out, "well_scores", snapshot.wellScores, JsonExport::wellScore);
            out.write(",\n");
            writeArray(out, "water_log", snapshot.waterLog, JsonExport::waterGlass);
            out.write(",\n");
            writeArray(out, "day_contributions", snapshot.dayContributions, JsonExport::dayContribution);
            out.write(",\n");
            writeArray(out, "goals", snapshot.goals, JsonExport::goal);
            out.write(",\n");
            writeArray(out, "goal_progress", snapshot.goalProgress, JsonExport::goalProgress);
            out.write("\n}\n");
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    private interface Mapper<T> {
        JSONObject map(T value) throws JSONException;
    }

    private static <T> void writeArray(Writer out, String name, List<T> values, Mapper<T> mapper)
            throws IOException, JSONException {
        out.write(JSONObject.quote(name));
        out.write(": [");
        for (int i = 0; i < values.size(); i++) {
            out.write(i == 0 ? "\n    " : ",\n    ");
            out.write(mapper.map(values.get(i)).toString());
        }
        out.write(values.isEmpty() ? "]" : "\n  ]");
    }

    private static void writeField(Writer out, String name, JSONObject value)
            throws IOException, JSONException {
        out.write(JSONObject.quote(name));
        out.write(": ");
        out.write(value == null ? "null" : value.toString(2));
    }

    private static JSONObject meta(ExportSnapshot snapshot) throws JSONException {
        JSONObject counts = new JSONObject();
        counts.put("eating_occasions", snapshot.eatingOccasions.size());
        counts.put("interventions", snapshot.interventions.size());
        counts.put("signal_events", snapshot.signals.size());
        counts.put("ema", snapshot.assessments.size());
        counts.put("meals", snapshot.meals.size());
        counts.put("well_scores", snapshot.wellScores.size());
        counts.put("water_log", snapshot.waterLog.size());
        counts.put("day_contributions", snapshot.dayContributions.size());
        counts.put("goals", snapshot.goals.size());
        counts.put("goal_progress", snapshot.goalProgress.size());

        JSONObject meta = new JSONObject();
        meta.put("format_version", FORMAT_VERSION);
        meta.put("exported_at", String.valueOf(snapshot.exportedAt));
        meta.put("database_schema_version", snapshot.databaseVersion);
        meta.put("row_counts", counts);
        return meta;
    }

    @Nullable
    private static JSONObject profile(@Nullable ExportSnapshot.Profile profile) throws JSONException {
        if (profile == null) {
            return null;
        }
        JSONObject json = new JSONObject();
        json.put("sex", profile.sex);
        json.put("age", profile.age);
        json.put("activity_level", profile.activityLevel);
        json.put("eats_breakfast", profile.eatsBreakfast);
        json.put("eats_lunch", profile.eatsLunch);
        json.put("eats_dinner", profile.eatsDinner);
        json.put("eats_snacks", profile.eatsSnacks);
        json.put("breakfast_time_of_day", profile.breakfastTimeOfDay);
        json.put("lunch_time_of_day", profile.lunchTimeOfDay);
        json.put("dinner_time_of_day", profile.dinnerTimeOfDay);
        json.put("preferred_foods", new JSONArray(profile.preferredFoods));
        json.put("foods_to_avoid", new JSONArray(profile.foodsToAvoid));
        json.put("veg_servings_goal_per_day", profile.vegServingsGoalPerDay);
        json.put("delivery_acceptable_per_week", profile.deliveryAcceptablePerWeek);
        return json;
    }

    private static JSONObject occasion(EatingOccasionEvent event) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", event.id);
        json.put("created_at", String.valueOf(event.creationDate));
        json.put("trigger_combo", nameOrNull(event.triggerCombo));
        json.put("trigger_type", nameOrNull(event.triggerType));
        json.put("decision_stage", nameOrNull(event.decisionStage));
        json.put("outcome", nameOrNull(event.outcome));
        return json;
    }

    private static JSONObject intervention(InterventionLog log) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", log.id);
        json.put("created_at", String.valueOf(log.creationDate));
        json.put("responded_at", log.respondedAt == null ? null : String.valueOf(log.respondedAt));
        json.put("eating_occasion_event_id", log.eatingOccasionEventId);
        json.put("category", nameOrNull(log.category));
        json.put("format", nameOrNull(log.format));
        json.put("user_response", nameOrNull(log.userResponse));
        json.put("payload", log.payload);
        return json;
    }

    private static JSONObject signal(SignalEvent event) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", event.id);
        json.put("created_at", String.valueOf(event.creationDate));
        json.put("type", nameOrNull(event.type));
        json.put("eating_occasion_event_id", event.eatingOccasionEventId);
        return json;
    }

    private static JSONObject assessment(MomentaryAssessmentEvent event) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", event.id);
        json.put("created_at", String.valueOf(event.creationDate));
        json.put("eating_occasion_event_id", event.eatingOccasionEventId);
        json.put("hunger_level", event.hungerLevel);
        json.put("fatigue_level", event.fatigueLevel);
        json.put("time_pressure", event.timePressure);
        json.put("food_available_at_home", nameOrNull(event.foodAvailableAtHome));
        json.put("meal_decision_stage", nameOrNull(event.mealDecisionStage));
        json.put("delivery_intent", nameOrNull(event.deliveryIntent));
        json.put("dinner_planned", nameOrNull(event.dinnerPlanned));
        json.put("cooking_willingness", nameOrNull(event.cookingWillingness));
        json.put("craving_type", nameOrNull(event.cravingType));
        json.put("user_tone", nameOrNull(event.userTone));
        json.put("input_mode", nameOrNull(event.inputMode));
        json.put("confidence", event.confidence);
        json.put("needs_clarification", event.needsClarification);
        return json;
    }

    private static JSONObject meal(MealWithIngredients meal) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", meal.meal.id);
        json.put("logged_at", String.valueOf(meal.meal.loggedAt));
        json.put("meal_slot", nameOrNull(meal.meal.mealSlot));
        json.put("meal_source", nameOrNull(meal.meal.mealSource));
        json.put("dish_name", meal.meal.dishName);
        json.put("has_photo", meal.meal.photoUri != null);

        JSONArray ingredients = new JSONArray();
        if (meal.ingredients != null) {
            for (Ingredient ingredient : meal.ingredients) {
                JSONObject item = new JSONObject();
                item.put("id", ingredient.id);
                item.put("name", ingredient.name);
                item.put("food_item", ingredient.foodItem);
                item.put("food_category", ingredient.foodCategory);
                item.put("uom", nameOrNull(ingredient.uom));
                item.put("serving_value", ingredient.servingValue);
                item.put("calories", ingredient.calories);
                item.put("protein_g", ingredient.macroProtein);
                item.put("carbs_g", ingredient.macroCarbs);
                item.put("fat_g", ingredient.macroFat);
                ingredients.put(item);
            }
        }
        json.put("ingredients", ingredients);
        return json;
    }

    private static JSONObject waterGlass(WaterLog glass) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", glass.id);
        json.put("logged_at", String.valueOf(glass.loggedAt));
        return json;
    }

    private static JSONObject wellScore(WellScore score) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", score.id);
        json.put("window_type", score.windowType);
        json.put("period_start", String.valueOf(score.periodStart));
        json.put("period_end", String.valueOf(score.periodEnd));
        json.put("total_points", score.totalPoints);
        json.put("max_points", score.maxPoints);
        json.put("sufficiency", score.sufficiency);
        json.put("observed_days", score.observedDays);
        json.put("logged_meals", score.loggedMeals);
        json.put("computed_at", String.valueOf(score.computedAt));

        Map<WellComponent, Integer> points = WellScoreComponents.parsePoints(score.componentPoints);
        Map<WellComponent, WellScoreComponents.Frequency> frequencies =
                WellScoreComponents.parseFrequencies(score.componentFrequencies);

        JSONArray components = new JSONArray();
        for (WellComponent component : WellComponent.values()) {
            Integer componentPoints = points.get(component);
            WellScoreComponents.Frequency frequency = frequencies.get(component);
            if (componentPoints == null && frequency == null) {
                continue;
            }
            JSONObject item = new JSONObject();
            item.put("component", component.name());
            item.put("points", componentPoints);
            item.put("occasions", frequency == null ? null : frequency.occasions);
            item.put("rate_per_day", frequency == null ? null : frequency.ratePerDay);
            components.put(item);
        }
        json.put("components", components);
        return json;
    }

    private static JSONObject dayContribution(ExportSnapshot.DayContributionRow row) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("date", String.valueOf(row.date));
        json.put("contribution", nameOrNull(row.contribution));
        json.put("components_cleared", new JSONArray(row.componentsCleared));
        json.put("component_count", row.componentsCleared.size());
        return json;
    }

    private static JSONObject goal(Goal goal) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", goal.id);
        json.put("name", goal.name);
        json.put("metric_type", goal.metricType);
        json.put("target_value", goal.targetValue);
        return json;
    }

    private static JSONObject goalProgress(ExportSnapshot.GoalProgressRow row) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("week_start", String.valueOf(row.weekStart));
        json.put("week_end", String.valueOf(row.weekEnd));
        json.put("goal_id", row.goalId);
        json.put("goal_name", row.goalName);
        json.put("metric_type", row.metricType);
        json.put("target_value", row.target);
        json.put("actual_value", row.actual);
        json.put("met", row.met);
        return json;
    }

    @Nullable
    private static String nameOrNull(@Nullable Enum<?> value) {
        return value == null ? null : value.name();
    }
}
