package com.panagiotisoikonomou.nudgebite.water;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.entity.InterventionLog;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.enums.InterventionCategory;
import com.panagiotisoikonomou.nudgebite.data.enums.InterventionFormat;
import com.panagiotisoikonomou.nudgebite.data.enums.UserResponse;
import com.panagiotisoikonomou.nudgebite.notifications.WaterReminderNotifier;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WaterReminderWorker extends Worker {

    private static final String TAG = "WaterReminder";

    public WaterReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context appContext = getApplicationContext();
        AppDatabase database = AppDatabase.getDatabase(appContext);

        User user = database.userDao().getUserSync();
        if (user == null) {
            return Result.success();
        }

        LocalDateTime now = LocalDateTime.now();
        WaterReminderRule.Inputs inputs = new WaterReminderRule.Inputs(
                WaterGoal.isSelected(database.goalDao().getAllGoalsSync()),
                user.lunchTimeOfDay,
                user.dinnerTimeOfDay,
                WaterRepository.lastGlassAtSync(appContext),
                database.interventionLogDao().lastHydrationReminderAtSync(),
                WaterRepository.glassesOnSync(appContext, LocalDate.now()),
                WaterGoal.targetFor(user));

        String blockingReason = WaterReminderRule.blockingReason(inputs, now);
        // Checked before writing: a reminder nobody saw must not be logged as ignored.
        if (blockingReason == null && !WaterReminderNotifier.canPostReminder(appContext)) {
            blockingReason = "the reminder could not be shown (notifications are off)";
        }
        if (blockingReason != null) {
            Log.i(TAG, "No hydration reminder: " + blockingReason + ".");
            return Result.success();
        }

        InterventionLog log = new InterventionLog();
        log.creationDate = now;
        log.eatingOccasionEventId = null;
        log.category = InterventionCategory.HYDRATION;
        log.format = InterventionFormat.HYDRATION_REMINDER;
        log.userResponse = UserResponse.PENDING;
        log.payload = WaterReminderNotifier.reminderBody();
        long interventionLogId = database.interventionLogDao().insert(log);

        WaterReminderNotifier.notifyReminder(appContext, interventionLogId);
        Log.i(TAG, "Hydration reminder due: " + inputs.glassesToday + " of " + inputs.targetGlasses
                + " glasses today, last glass " + inputs.lastGlassAt);
        return Result.success();
    }
}
