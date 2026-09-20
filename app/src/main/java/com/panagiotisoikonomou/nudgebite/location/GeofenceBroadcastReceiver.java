package com.panagiotisoikonomou.nudgebite.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.engine.LocationZone;
import com.panagiotisoikonomou.nudgebite.engine.SignalEngine;
import com.panagiotisoikonomou.nudgebite.engine.signals.GeofenceZoneStore;
import com.panagiotisoikonomou.nudgebite.utils.EnumNames;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "NudgeBiteEngine";

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event == null) {
            Log.w(TAG, "GeofenceBroadcastReceiver: null GeofencingEvent, ignoring.");
            return;
        }
        if (event.hasError()) {
            Log.w(TAG, "GeofenceBroadcastReceiver error: "
                    + GeofenceStatusCodes.getStatusCodeString(event.getErrorCode()));
            return;
        }

        LocationZone zone = zoneFrom(event.getTriggeringGeofences());
        if (zone == LocationZone.UNKNOWN) {
            Log.w(TAG, "GeofenceBroadcastReceiver: triggering geofence didn't match a known zone.");
            return;
        }
        boolean entered = event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER;
        Log.i(TAG, "Geofence transition: " + zone + " " + (entered ? "ENTER" : "EXIT"));

        Context appContext = context.getApplicationContext();
        PendingResult pendingResult = goAsync();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                recordTransitionAndRunEngine(appContext, zone, entered);
            } finally {
                pendingResult.finish();
            }
        });
    }

    private void recordTransitionAndRunEngine(Context appContext, LocationZone zone, boolean entered) {
        GeofenceZoneStore store = new GeofenceZoneStore(appContext);
        if (entered) {
            store.setCurrentZone(zone);
        } else {
            if (zone == LocationZone.WORK) {
                store.recordWorkExitNow();
            }
            if (store.getCurrentZone() == zone) {
                store.setCurrentZone(LocationZone.UNKNOWN);
            }
        }

        User user = AppDatabase.getDatabase(appContext).userDao().getUserSync();
        if (user == null) {
            Log.i(TAG, "Geofence transition ignored: no user yet.");
            return;
        }

        SignalEngine.withRealSignals(appContext).runOnceSync(user);
    }

    private static LocationZone zoneFrom(List<Geofence> triggeringGeofences) {
        if (triggeringGeofences == null || triggeringGeofences.isEmpty()) {
            return LocationZone.UNKNOWN;
        }
        return EnumNames.parseOrDefault(LocationZone.class, triggeringGeofences.get(0).getRequestId(),
                LocationZone.UNKNOWN);
    }
}
