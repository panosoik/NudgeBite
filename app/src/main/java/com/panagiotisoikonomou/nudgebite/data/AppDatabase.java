package com.panagiotisoikonomou.nudgebite.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.panagiotisoikonomou.nudgebite.data.dao.EatingOccasionEventDao;
import com.panagiotisoikonomou.nudgebite.data.dao.GoalDao;
import com.panagiotisoikonomou.nudgebite.data.dao.IngredientDao;
import com.panagiotisoikonomou.nudgebite.data.dao.InterventionLogDao;
import com.panagiotisoikonomou.nudgebite.data.dao.MealDao;
import com.panagiotisoikonomou.nudgebite.data.dao.MomentaryAssessmentEventDao;
import com.panagiotisoikonomou.nudgebite.data.dao.SignalEventDao;
import com.panagiotisoikonomou.nudgebite.data.dao.UserDao;
import com.panagiotisoikonomou.nudgebite.data.dao.WaterLogDao;
import com.panagiotisoikonomou.nudgebite.data.dao.WellScoreDao;
import com.panagiotisoikonomou.nudgebite.data.entity.EatingOccasionEvent;
import com.panagiotisoikonomou.nudgebite.data.entity.Goal;
import com.panagiotisoikonomou.nudgebite.data.entity.GoalProgress;
import com.panagiotisoikonomou.nudgebite.data.entity.Ingredient;
import com.panagiotisoikonomou.nudgebite.data.entity.InterventionLog;
import com.panagiotisoikonomou.nudgebite.data.entity.Meal;
import com.panagiotisoikonomou.nudgebite.data.entity.MomentaryAssessmentEvent;
import com.panagiotisoikonomou.nudgebite.data.entity.SignalEvent;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.entity.WaterLog;
import com.panagiotisoikonomou.nudgebite.data.entity.WellScore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        Meal.class,
        Ingredient.class,
        User.class,
        Goal.class,
        GoalProgress.class,
        SignalEvent.class,
        MomentaryAssessmentEvent.class,
        EatingOccasionEvent.class,
        InterventionLog.class,
        WellScore.class,
        WaterLog.class
}, version = 12, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    public static final int SCHEMA_VERSION = 12;

    public abstract MealDao mealDao();

    public abstract IngredientDao ingredientDao();

    public abstract UserDao userDao();

    public abstract GoalDao goalDao();

    public abstract SignalEventDao signalEventDao();

    public abstract MomentaryAssessmentEventDao momentaryAssessmentEventDao();

    public abstract EatingOccasionEventDao eatingOccasionEventDao();

    public abstract InterventionLogDao interventionLogDao();

    public abstract WellScoreDao wellScoreDao();

    public abstract WaterLogDao waterLogDao();

    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room
                            .databaseBuilder(context.getApplicationContext(), AppDatabase.class, "nudgebite.db")
                            // No migrations ship with this build: any schema change recreates the database and wipes it.
                            .fallbackToDestructiveMigration(true)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
