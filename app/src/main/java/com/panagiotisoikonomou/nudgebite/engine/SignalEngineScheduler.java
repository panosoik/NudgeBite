package com.panagiotisoikonomou.nudgebite.engine;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.panagiotisoikonomou.nudgebite.water.WaterReminderWorker;

import java.util.concurrent.TimeUnit;

/** Safe to call on every launch: an existing schedule is kept, never duplicated. */
public final class SignalEngineScheduler {

    private static final long INTERVAL_MINUTES = 15;

    private SignalEngineScheduler() {
    }

    public static void schedulePeriodicChecks(Context context) {
        schedule(context, "signal_engine_periodic_check", SignalEngineWorker.class);
        schedule(context, "water_reminder_periodic_check", WaterReminderWorker.class);
    }

    private static void schedule(Context context, String uniqueName, Class<? extends ListenableWorker> worker) {
        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(worker, INTERVAL_MINUTES, TimeUnit.MINUTES).build();
        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(uniqueName, ExistingPeriodicWorkPolicy.KEEP, request);
    }
}
