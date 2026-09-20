package com.panagiotisoikonomou.nudgebite.engine;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.entity.User;

public class SignalEngineWorker extends Worker {

    private static final String TAG = SignalEngine.TAG;

    public SignalEngineWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context appContext = getApplicationContext();
        User user = AppDatabase.getDatabase(appContext).userDao().getUserSync();
        if (user == null) {
            Log.i(TAG, "SignalEngineWorker: no user yet (onboarding not finished) — skipping run.");
            return Result.success();
        }

        SignalEngine.withRealSignals(appContext).runOnceSync(user);
        return Result.success();
    }
}
