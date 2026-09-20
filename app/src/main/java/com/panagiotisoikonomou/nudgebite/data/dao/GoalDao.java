package com.panagiotisoikonomou.nudgebite.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.panagiotisoikonomou.nudgebite.data.entity.Goal;

import java.util.List;

@Dao
public interface GoalDao {

    @Insert
    long insert(Goal goal);

    @Insert
    void insertAll(List<Goal> goals);

    @Query("SELECT * FROM goal")
    LiveData<List<Goal>> getAllGoals();

    @Query("SELECT * FROM goal")
    List<Goal> getAllGoalsSync();

    @Query("DELETE FROM goal WHERE id = :goalId")
    void deleteById(long goalId);
}
