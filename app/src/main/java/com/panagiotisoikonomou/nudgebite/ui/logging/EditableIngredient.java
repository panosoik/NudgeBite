package com.panagiotisoikonomou.nudgebite.ui.logging;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.entity.Ingredient;
import com.panagiotisoikonomou.nudgebite.data.enums.FoodCategory;
import com.panagiotisoikonomou.nudgebite.data.enums.UnitOfMeasure;
import com.panagiotisoikonomou.nudgebite.food.FoodItem;
import com.panagiotisoikonomou.nudgebite.vision.VisionItem;

/** One ingredient row on the meal edit screen. Its nutrients rescale with the portion. */
class EditableIngredient {

    String name = "";
    @Nullable FoodItem foodItem;
    FoodCategory foodCategory = FoodCategory.OTHER;
    UnitOfMeasure uom = UnitOfMeasure.GRAMS;
    double portionValue;
    double calories;
    double protein;
    double carbs;
    double fat;

    private double caloriesPerUnit;
    private double proteinPerUnit;
    private double carbsPerUnit;
    private double fatPerUnit;

    static EditableIngredient fromVision(VisionItem item) {
        EditableIngredient ingredient = new EditableIngredient();
        ingredient.name = item.name;
        ingredient.setFood(item.foodItem, item.foodCategory);
        ingredient.uom = "ML".equals(item.uom) ? UnitOfMeasure.ML : UnitOfMeasure.GRAMS;
        ingredient.portionValue = item.portionValue;
        ingredient.setNutrients(item.calories, item.protein, item.carbs, item.fat);
        return ingredient;
    }

    static EditableIngredient fromSaved(Ingredient saved) {
        EditableIngredient ingredient = new EditableIngredient();
        ingredient.name = saved.name;
        ingredient.setFood(saved.foodItem, saved.foodCategory);
        ingredient.uom = saved.uom;
        ingredient.portionValue = saved.servingValue;
        ingredient.setNutrients(saved.calories, saved.macroProtein, saved.macroCarbs, saved.macroFat);
        return ingredient;
    }

    Ingredient toEntity() {
        Ingredient entity = new Ingredient();
        entity.name = name.trim();
        entity.foodCategory = foodCategory.name();
        entity.foodItem = foodItem != null ? foodItem.name() : null;
        entity.uom = uom;
        entity.servingValue = portionValue;
        entity.calories = calories;
        entity.macroProtein = protein;
        entity.macroCarbs = carbs;
        entity.macroFat = fat;
        return entity;
    }

    /** The item wins and the category is re-derived from it, so reopening and saving a meal never loses its detail. */
    void setFood(@Nullable String foodItemName, @Nullable String foodCategoryName) {
        FoodItem item = FoodItem.parseOrNull(foodItemName);
        if (item != null) {
            setFoodItem(item);
        } else {
            setCategoryOnly(FoodCategory.parseOrDefault(foodCategoryName, FoodCategory.UNKNOWN));
        }
    }

    void setFoodItem(FoodItem item) {
        foodItem = item;
        foodCategory = item.scoringCategory();
    }

    void setCategoryOnly(FoodCategory category) {
        foodItem = null;
        foodCategory = category;
    }

    String foodLabel() {
        return foodItem != null ? foodItem.label() : foodCategory.displayName();
    }

    void adjustPortion(double delta) {
        portionValue = Math.max(0, portionValue + delta);
        calories = caloriesPerUnit * portionValue;
        protein = proteinPerUnit * portionValue;
        carbs = carbsPerUnit * portionValue;
        fat = fatPerUnit * portionValue;
    }

    private void setNutrients(double calories, double protein, double carbs, double fat) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        double units = portionValue > 0 ? portionValue : 1;
        caloriesPerUnit = calories / units;
        proteinPerUnit = protein / units;
        carbsPerUnit = carbs / units;
        fatPerUnit = fat / units;
    }
}
