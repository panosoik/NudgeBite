package com.panagiotisoikonomou.nudgebite.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.entity.User;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "NudgeBiteEngine";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        Context appContext = context.getApplicationContext();
        PendingResult pendingResult = goAsync();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                User user = AppDatabase.getDatabase(appContext).userDao().getUserSync();
                if (user == null) {
                    Log.i(TAG, "Boot completed: no user yet, skipping geofence re-registration.");
                    return;
                }
                GeofenceRegistrar.registerAll(appContext, user);
            } finally {
                pendingResult.finish();
            }
        });
    }
}
