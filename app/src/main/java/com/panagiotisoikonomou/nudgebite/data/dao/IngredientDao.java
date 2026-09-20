package com.panagiotisoikonomou.nudgebite.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.panagiotisoikonomou.nudgebite.data.entity.Ingredient;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface IngredientDao {

    @Insert
    void insertAll(List<Ingredient> ingredients);

    @Query("DELETE FROM ingredient WHERE meal_id = :mealId")
    void deleteByMealId(long mealId);

    @Query("SELECT ingredient.* FROM ingredient "
            + "INNER JOIN meal ON ingredient.meal_id = meal.id "
            + "WHERE meal.logged_at >= :since")
    List<Ingredient> getIngredientsLoggedSince(LocalDateTime since);
}
