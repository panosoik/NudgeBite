package com.panagiotisoikonomou.nudgebite.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.panagiotisoikonomou.nudgebite.data.enums.MealSlot;
import com.panagiotisoikonomou.nudgebite.data.enums.MealSource;

import java.time.LocalDateTime;

@Entity(tableName = "meal")
public class Meal {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "logged_at")
    public LocalDateTime loggedAt;

    @NonNull
    @ColumnInfo(name = "meal_slot")
    public MealSlot mealSlot;

    @NonNull
    @ColumnInfo(name = "dish_name")
    public String dishName;

    @ColumnInfo(name = "photo_uri")
    public String photoUri;

    // Null means the question was never asked (unknown), never home-cooked.
    @Nullable
    @ColumnInfo(name = "meal_source")
    public MealSource mealSource;
}
