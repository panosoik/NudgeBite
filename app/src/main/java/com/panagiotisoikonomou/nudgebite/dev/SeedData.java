package com.panagiotisoikonomou.nudgebite.dev;

import android.content.Context;

import com.panagiotisoikonomou.nudgebite.BuildConfig;
import com.panagiotisoikonomou.nudgebite.data.enums.FoodCategory;
import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.entity.Ingredient;
import com.panagiotisoikonomou.nudgebite.data.entity.Meal;
import com.panagiotisoikonomou.nudgebite.data.enums.MealSlot;
import com.panagiotisoikonomou.nudgebite.data.enums.MealSource;
import com.panagiotisoikonomou.nudgebite.data.enums.UnitOfMeasure;
import com.panagiotisoikonomou.nudgebite.data.repository.WellScoreRepository;
import com.panagiotisoikonomou.nudgebite.food.FoodItem;
import com.panagiotisoikonomou.nudgebite.progress.WeekPeriod;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.function.Consumer;

public final class SeedData {

    private SeedData() {
    }

    public static void reseed(Context context) {
        replaceMealsInBackground(context, SeedData::seedSampleMeals);
    }

    public static void seedDayStatusScenario(Context context, DayStatusScenario scenario) {
        replaceMealsInBackground(context, db -> {
            LocalDateTime now = LocalDateTime.now();
            switch (scenario) {
                case NOTHING_LOGGED:
                    break;
                case LOGGED_NOT_GREEN:
                    insertMeal(db, now, MealSlot.LUNCH, "Chicken & rice", MealSource.HOME_COOKED, notGreenIngredients());
                    break;
                case GREEN_TODAY_ONLY:
                    insertMeal(db, now, MealSlot.LUNCH, "Balanced plate", MealSource.HOME_COOKED, greenDayIngredients());
                    insertMeal(db, now.minusDays(1), MealSlot.LUNCH, "Chicken & rice", MealSource.HOME_COOKED, notGreenIngredients());
                    break;
                case GREEN_TODAY_AND_YESTERDAY:
                    insertMeal(db, now, MealSlot.LUNCH, "Balanced plate", MealSource.HOME_COOKED, greenDayIngredients());
                    insertMeal(db, now.minusDays(1), MealSlot.LUNCH, "Balanced plate", MealSource.HOME_COOKED, greenDayIngredients());
                    break;
            }
        });
    }

    private static List<Ingredient> greenDayIngredients() {
        return List.of(
                ingredient("Mixed vegetables", FoodCategory.OTHER_VEGETABLE, UnitOfMeasure.GRAMS, 150, 80, 4, 15, 0),
                ingredient("Mixed fruit", FoodCategory.FRUIT, UnitOfMeasure.GRAMS, 150, 85, 1, 20, 0),
                ingredient("Brown rice", FoodCategory.WHOLE_GRAIN, UnitOfMeasure.GRAMS, 100, 130, 3, 27, 1)
        );
    }

    private static List<Ingredient> notGreenIngredients() {
        return List.of(
                ingredient("Chicken breast", FoodCategory.POULTRY, UnitOfMeasure.GRAMS, 150, 250, 45, 0, 6),
                ingredient("White rice", FoodCategory.REFINED_GRAIN, UnitOfMeasure.GRAMS, 150, 195, 4, 43, 0)
        );
    }

    public static void seedMultiWeekProgress(Context context) {
        replaceMealsInBackground(context, db -> {
            LocalDateTime now = LocalDateTime.now();

            insertMeal(db, now, MealSlot.BREAKFAST, "Big veg bowl", MealSource.HOME_COOKED, List.of(
                    ingredient("Mixed vegetables", FoodCategory.OTHER_VEGETABLE, UnitOfMeasure.GRAMS, 900, 400, 15, 80, 2)
            ));
            insertMeal(db, now, MealSlot.LUNCH, "Big veg salad", MealSource.HOME_COOKED, List.of(
                    ingredient("Mixed vegetables", FoodCategory.OTHER_VEGETABLE, UnitOfMeasure.GRAMS, 900, 400, 15, 80, 2)
            ));

            LocalDateTime lastWeekMonday = now.minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            insertMeal(db, lastWeekMonday, MealSlot.LUNCH, "Delivery burger", MealSource.DELIVERY_OR_TAKEOUT, List.of(
                    ingredient("Beef burger", FoodCategory.RED_MEAT, UnitOfMeasure.GRAMS, 150, 400, 25, 30, 20)
            ));
            insertMeal(db, lastWeekMonday.plusDays(3), MealSlot.DINNER, "Delivery pizza", MealSource.DELIVERY_OR_TAKEOUT, List.of(
                    ingredient("Cheese pizza", FoodCategory.CHEESE, UnitOfMeasure.GRAMS, 200, 500, 20, 55, 22)
            ));
            insertMeal(db, lastWeekMonday.plusDays(5), MealSlot.DINNER, "Light dinner", MealSource.HOME_COOKED, List.of(
                    ingredient("Mixed vegetables", FoodCategory.OTHER_VEGETABLE, UnitOfMeasure.GRAMS, 60, 30, 1, 6, 0)
            ));
        });
    }

    public static void seedWellProgress(Context context) {
        replaceMealsInBackground(context, db -> {
            LocalDateTime today = LocalDateTime.now().withHour(12).withMinute(0);

            for (int daysAgo = WELL_SEED_DAYS - 1; daysAgo >= 0; daysAgo--) {
                LocalDateTime day = today.minusDays(daysAgo);
                int inWeek = daysAgo % 7;

                insertMeal(db, day.withHour(8), MealSlot.BREAKFAST, "Oats, banana & walnuts",
                        MealSource.HOME_COOKED, List.of(
                                taxonomyIngredient("Oats", FoodItem.OATS, 60),
                                taxonomyIngredient("Banana", FoodItem.BANANA, 150),
                                taxonomyIngredient("Walnuts", FoodItem.WALNUTS, 30)));

                insertMeal(db, day.withHour(13), MealSlot.LUNCH, lunchNameFor(inWeek),
                        lunchSourceFor(inWeek), lunchIngredientsFor(inWeek));

                insertMeal(db, day.withHour(20), MealSlot.DINNER, "Salad plate",
                        MealSource.HOME_COOKED, List.of(
                                taxonomyIngredient("Tomato", FoodItem.TOMATO, 150),
                                taxonomyIngredient("Cucumber", FoodItem.CUCUMBER, 120),
                                taxonomyIngredient("Lettuce", FoodItem.LETTUCE, 80)));
            }

            WellScoreRepository.recomputeAll(db);
        });
    }

    private static final int WELL_SEED_DAYS = 45;

    private static String lunchNameFor(int inWeek) {
        switch (inWeek) {
            case 1:
                return "Grilled salmon & salad";
            case 2:
                return "Beef with tomato";
            case 4:
                return "Fast food burger";
            case 5:
                return "Chicken with brown rice";
            case 6:
                return "Rice with tomato";
            default:
                return "Lentils with tomato";
        }
    }

    private static MealSource lunchSourceFor(int inWeek) {
        return inWeek == 4 ? MealSource.FAST_FOOD : MealSource.HOME_COOKED;
    }

    private static List<Ingredient> lunchIngredientsFor(int inWeek) {
        switch (inWeek) {
            case 1:
                return List.of(taxonomyIngredient("Salmon", FoodItem.SALMON, 160),
                        taxonomyIngredient("Tomato", FoodItem.TOMATO, 150));
            case 2:
                return List.of(taxonomyIngredient("Beef", FoodItem.BEEF, 150),
                        taxonomyIngredient("Tomato", FoodItem.TOMATO, 150));
            case 4:
                return List.of(taxonomyIngredient("Beef", FoodItem.BEEF, 150),
                        taxonomyIngredient("White rice", FoodItem.WHITE_RICE, 150));
            case 5:
                return List.of(taxonomyIngredient("Chicken breast", FoodItem.CHICKEN_BREAST, 140),
                        taxonomyIngredient("Brown rice", FoodItem.BROWN_RICE, 150),
                        taxonomyIngredient("Tomato", FoodItem.TOMATO, 150));
            case 6:
                return List.of(taxonomyIngredient("White rice", FoodItem.WHITE_RICE, 150),
                        taxonomyIngredient("Tomato", FoodItem.TOMATO, 150));
            default:
                return List.of(taxonomyIngredient("Lentils", FoodItem.LENTILS, 140),
                        taxonomyIngredient("Tomato", FoodItem.TOMATO, 150));
        }
    }

    private static Ingredient taxonomyIngredient(String name, FoodItem foodItem, double grams) {
        Ingredient ingredient = ingredient(name, foodItem.scoringCategory(), UnitOfMeasure.GRAMS,
                grams, 0, 0, 0, 0);
        ingredient.foodItem = foodItem.name();
        return ingredient;
    }

    private static void seedSampleMeals(AppDatabase db) {
        LocalDateTime now = LocalDateTime.now();
        WeekPeriod currentWeek = WeekPeriod.currentWeek();

        insertMeal(db, now, MealSlot.BREAKFAST, "Greek yogurt & berries", MealSource.HOME_COOKED, List.of(
                ingredient("Greek yogurt", FoodCategory.DAIRY, UnitOfMeasure.GRAMS, 200, 130, 15, 8, 4),
                ingredient("Mixed berries", FoodCategory.FRUIT, UnitOfMeasure.GRAMS, 80, 45, 1, 10, 0)
        ));
        insertMeal(db, now, MealSlot.LUNCH, "Grilled chicken, veg & rice", MealSource.DELIVERY_OR_TAKEOUT, List.of(
                ingredient("Grilled chicken breast", FoodCategory.POULTRY, UnitOfMeasure.GRAMS, 140, 230, 42, 0, 5),
                ingredient("Mixed vegetables", FoodCategory.OTHER_VEGETABLE, UnitOfMeasure.GRAMS, 120, 60, 3, 12, 0),
                ingredient("White rice", FoodCategory.REFINED_GRAIN, UnitOfMeasure.GRAMS, 150, 195, 4, 43, 0)
        ));

        insertMeal(db, now.minusDays(1), MealSlot.LUNCH, "Turkey sandwich", MealSource.HOME_COOKED, List.of(
                ingredient("Turkey breast", FoodCategory.POULTRY, UnitOfMeasure.GRAMS, 100, 145, 30, 0, 2),
                ingredient("White bread", FoodCategory.REFINED_GRAIN, UnitOfMeasure.GRAMS, 90, 240, 8, 45, 3)
        ));

        LocalDateTime twoDaysAgo = now.minusDays(2);
        if (currentWeek.contains(twoDaysAgo.toLocalDate())) {
            insertMeal(db, twoDaysAgo, MealSlot.DINNER, "Cheese pizza slice", MealSource.DELIVERY_OR_TAKEOUT, List.of(
                    ingredient("Cheese pizza", FoodCategory.CHEESE, UnitOfMeasure.GRAMS, 200, 500, 20, 55, 22)
            ));
        }
    }

    /** Debug builds only: wipes every logged meal, then seeds new ones off the main thread. */
    private static void replaceMealsInBackground(Context context, Consumer<AppDatabase> seedMeals) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            db.mealDao().deleteAll();
            seedMeals.accept(db);
        });
    }

    private static void insertMeal(AppDatabase db, LocalDateTime loggedAt, MealSlot slot, String dishName,
                                    MealSource mealSource, List<Ingredient> ingredients) {
        Meal meal = new Meal();
        meal.loggedAt = loggedAt;
        meal.mealSlot = slot;
        meal.mealSource = mealSource;
        meal.dishName = dishName;
        long mealId = db.mealDao().insert(meal);
        for (Ingredient ingredient : ingredients) {
            ingredient.mealId = mealId;
        }
        db.ingredientDao().insertAll(ingredients);
    }

    private static Ingredient ingredient(String name, FoodCategory category, UnitOfMeasure uom,
                                          double portionValue, double calories, double protein,
                                          double carbs, double fat) {
        Ingredient ingredient = new Ingredient();
        ingredient.name = name;
        ingredient.foodCategory = category.name();
        ingredient.uom = uom;
        ingredient.servingValue = portionValue;
        ingredient.calories = calories;
        ingredient.macroProtein = protein;
        ingredient.macroCarbs = carbs;
        ingredient.macroFat = fat;
        return ingredient;
    }
}
