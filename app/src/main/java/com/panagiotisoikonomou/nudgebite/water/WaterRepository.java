package com.panagiotisoikonomou.nudgebite.water;

import android.app.Application;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.entity.WaterLog;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class WaterRepository {

    private WaterRepository() {
    }

    public static LiveData<Integer> observeGlassesToday(Application application, LocalDate today) {
        return AppDatabase.getDatabase(application).waterLogDao()
                .countBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    public static int glassesOnSync(Context context, LocalDate day) {
        return AppDatabase.getDatabase(context.getApplicationContext()).waterLogDao()
                .countBetweenSync(day.atStartOfDay(), day.plusDays(1).atStartOfDay());
    }

    @Nullable
    public static LocalDateTime lastGlassAtSync(Context context) {
        return AppDatabase.getDatabase(context.getApplicationContext()).waterLogDao().lastLoggedAtSync();
    }

    public static void addGlassSync(Context context, LocalDateTime at) {
        WaterLog glass = new WaterLog();
        glass.loggedAt = at;
        AppDatabase.getDatabase(context.getApplicationContext()).waterLogDao().insert(glass);
    }

    public static void removeLastGlassSync(Context context, LocalDate day) {
        AppDatabase database = AppDatabase.getDatabase(context.getApplicationContext());
        WaterLog mostRecent = database.waterLogDao().mostRecentBetweenSync(
                day.atStartOfDay(), day.plusDays(1).atStartOfDay());
        if (mostRecent != null) {
            database.waterLogDao().deleteById(mostRecent.id);
        }
    }
}
