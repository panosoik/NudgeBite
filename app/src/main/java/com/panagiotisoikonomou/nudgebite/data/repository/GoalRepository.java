package com.panagiotisoikonomou.nudgebite.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.dao.GoalDao;
import com.panagiotisoikonomou.nudgebite.data.entity.Goal;

import java.util.List;

public class GoalRepository {
    private final GoalDao goalDao;

    public GoalRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.goalDao = db.goalDao();
    }

    public LiveData<List<Goal>> getAllGoals() {
        return goalDao.getAllGoals();
    }
}
