package com.panagiotisoikonomou.nudgebite.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.dao.MealDao;
import com.panagiotisoikonomou.nudgebite.data.entity.relation.MealWithIngredients;

import java.util.List;

public class MealRepository {
    private final MealDao mealDao;

    public MealRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.mealDao = db.mealDao();
    }

    public LiveData<List<MealWithIngredients>> getAllMealsWithIngredients() {
        return mealDao.getAllMealsWithIngredients();
    }
}
