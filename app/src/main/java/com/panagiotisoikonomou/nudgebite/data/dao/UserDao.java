package com.panagiotisoikonomou.nudgebite.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.panagiotisoikonomou.nudgebite.data.entity.User;

@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM user LIMIT 1")
    LiveData<User> getUser();

    @Query("SELECT * FROM user LIMIT 1")
    User getUserSync();
}
