package com.panagiotisoikonomou.nudgebite.data.entity.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.panagiotisoikonomou.nudgebite.data.entity.Ingredient;
import com.panagiotisoikonomou.nudgebite.data.entity.Meal;

import java.util.List;

public class MealWithIngredients {

    @Embedded
    public Meal meal;

    @Relation(parentColumn = "id", entityColumn = "meal_id")
    public List<Ingredient> ingredients;
}
