package com.panagiotisoikonomou.nudgebite.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.panagiotisoikonomou.nudgebite.data.enums.UnitOfMeasure;

@Entity(
        tableName = "ingredient",
        foreignKeys = @ForeignKey(
                entity = Meal.class,
                parentColumns = "id",
                childColumns = "meal_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("meal_id")
)
public class Ingredient {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "meal_id")
    public long mealId;

    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    // Plain text so an unexpected vision-model category never fails to load.
    @NonNull
    @ColumnInfo(name = "food_category")
    public String foodCategory;

    /** Taxonomy FoodItem name, or null when only the category is known. WELL scoring prefers it over food_category. */
    @Nullable
    @ColumnInfo(name = "food_item")
    public String foodItem;

    @NonNull
    @ColumnInfo(name = "uom")
    public UnitOfMeasure uom;

    @ColumnInfo(name = "serving_value")
    public double servingValue;

    @ColumnInfo(name = "calories")
    public double calories;

    @ColumnInfo(name = "macro_protein")
    public double macroProtein;

    @ColumnInfo(name = "macro_carbs")
    public double macroCarbs;

    @ColumnInfo(name = "macro_fat")
    public double macroFat;
}
