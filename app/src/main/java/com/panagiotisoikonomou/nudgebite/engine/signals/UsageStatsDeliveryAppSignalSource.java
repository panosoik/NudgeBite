package com.panagiotisoikonomou.nudgebite.engine.signals;

import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Process;
import android.util.Log;

import java.util.LinkedHashSet;
import java.util.Set;

public class UsageStatsDeliveryAppSignalSource implements DeliveryAppSignalSource {

    private static final String TAG = "NudgeBiteEngine";

    private final Context context;

    public UsageStatsDeliveryAppSignalSource(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean hasUsageAccess() {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) {
            return false;
        }
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @Override
    public boolean wasDeliveryAppForegrounded(long withinLastMillis) {
        if (!hasUsageAccess()) {
            Log.w(TAG, "Delivery signal unavailable: usage access (PACKAGE_USAGE_STATS) is not "
                    + "granted. Reporting no delivery signal — grant it in Settings > Special app "
                    + "access > Usage access, or via the dev screen's grant button.");
            return false;
        }

        UsageStatsManager usageStatsManager =
                (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            Log.w(TAG, "Delivery signal unavailable: no UsageStatsManager on this device. "
                    + "Reporting no delivery signal.");
            return false;
        }

        long now = System.currentTimeMillis();
        long windowStart = now - withinLastMillis;

        Set<String> foregroundPackages = new LinkedHashSet<>();
        String matchedPackage = null;
        long matchedAt = 0L;

        UsageEvents events = usageStatsManager.queryEvents(windowStart, now);
        UsageEvents.Event event = new UsageEvents.Event();
        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            if (event.getEventType() != UsageEvents.Event.MOVE_TO_FOREGROUND) {
                continue;
            }
            String packageName = event.getPackageName();
            foregroundPackages.add(packageName);
            if (DeliveryPackages.isDeliveryApp(packageName) && event.getTimeStamp() >= matchedAt) {
                matchedPackage = packageName;
                matchedAt = event.getTimeStamp();
            }
        }

        Log.i(TAG, "  delivery scan: window=" + (withinLastMillis / 60000) + "min, foreground "
                + "packages seen=" + foregroundPackages);
        if (matchedPackage == null) {
            Log.i(TAG, "  delivery scan: no known delivery app foregrounded in the window.");
            return false;
        }
        Log.i(TAG, "  delivery scan: MATCH " + matchedPackage + " foregrounded "
                + ((now - matchedAt) / 1000) + "s ago.");
        return true;
    }
}
