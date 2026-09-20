package com.panagiotisoikonomou.nudgebite.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.dao.UserDao;
import com.panagiotisoikonomou.nudgebite.data.entity.User;

public class UserRepository {
    private final UserDao userDao;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.userDao = db.userDao();
    }

    public void updateUser(User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> userDao.update(user));
    }

    public LiveData<User> getUser() {
        return userDao.getUser();
    }
}
