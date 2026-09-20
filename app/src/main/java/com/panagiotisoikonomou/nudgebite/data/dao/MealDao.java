package com.panagiotisoikonomou.nudgebite.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.panagiotisoikonomou.nudgebite.data.entity.Meal;
import com.panagiotisoikonomou.nudgebite.data.entity.relation.MealWithIngredients;
import com.panagiotisoikonomou.nudgebite.data.enums.MealSlot;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface MealDao {

    @Insert
    long insert(Meal meal);

    @Update
    void update(Meal meal);

    @Transaction
    @Query("SELECT * FROM meal ORDER BY logged_at DESC")
    LiveData<List<MealWithIngredients>> getAllMealsWithIngredients();

    @Transaction
    @Query("SELECT * FROM meal WHERE id = :mealId")
    MealWithIngredients getMealWithIngredientsSync(long mealId);

    @Transaction
    @Query("SELECT * FROM meal ORDER BY logged_at ASC")
    List<MealWithIngredients> getAllMealsWithIngredientsSync();

    @Query("SELECT COUNT(*) FROM meal WHERE meal_slot = :slot "
            + "AND logged_at >= :startInclusive AND logged_at < :endExclusive")
    int countBySlotBetweenSync(MealSlot slot, LocalDateTime startInclusive, LocalDateTime endExclusive);

    @Query("DELETE FROM meal WHERE id = :mealId")
    void deleteById(long mealId);

    @Query("DELETE FROM meal")
    void deleteAll();
}
