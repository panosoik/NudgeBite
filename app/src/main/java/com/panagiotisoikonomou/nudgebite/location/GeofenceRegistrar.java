package com.panagiotisoikonomou.nudgebite.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.engine.LocationZone;

import java.util.ArrayList;
import java.util.List;

public final class GeofenceRegistrar {

    private static final String TAG = "NudgeBiteEngine";

    /** 100m is about the smallest radius that still fires reliably indoors. */
    private static final float GEOFENCE_RADIUS_METERS = 100f;

    private GeofenceRegistrar() {
    }

    public static void registerAll(Context context, User user) {
        if (!hasFineLocationPermission(context)) {
            Log.w(TAG, "Geofence registration skipped: ACCESS_FINE_LOCATION not granted.");
            return;
        }
        if (!hasBackgroundLocationPermission(context)) {
            Log.w(TAG, "ACCESS_BACKGROUND_LOCATION not granted — geofence transitions may not "
                    + "arrive reliably while the app is backgrounded. Registering anyway.");
        }

        List<Geofence> geofences = buildGeofences(user);
        if (geofences.isEmpty()) {
            Log.i(TAG, "Geofence registration skipped: no zone has both coordinates set.");
            return;
        }

        GeofencingClient client = LocationServices.getGeofencingClient(context);
        PendingIntent pendingIntent = geofencePendingIntent(context);
        client.removeGeofences(pendingIntent)
                .addOnCompleteListener(ignored -> addGeofences(client, geofences, pendingIntent));
    }

    /** The caller checked the permission just before this runs. */
    @SuppressLint("MissingPermission")
    private static void addGeofences(GeofencingClient client, List<Geofence> geofences,
                                      PendingIntent pendingIntent) {
        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofences)
                .build();
        client.addGeofences(request, pendingIntent)
                .addOnSuccessListener(v -> Log.i(TAG, "Geofences registered: " + geofences.size() + " zone(s)."))
                .addOnFailureListener(e -> Log.w(TAG, "Geofence registration failed.", e));
    }

    private static List<Geofence> buildGeofences(User user) {
        List<Geofence> geofences = new ArrayList<>();
        addIfGeofenceable(geofences, LocationZone.HOME, user.homeLatitude, user.homeLongitude);
        addIfGeofenceable(geofences, LocationZone.WORK, user.workLatitude, user.workLongitude);
        addIfGeofenceable(geofences, LocationZone.SUPERMARKET, user.supermarketLatitude, user.supermarketLongitude);
        addIfGeofenceable(geofences, LocationZone.EATING_PLACE, user.eatingPlaceLatitude, user.eatingPlaceLongitude);
        return geofences;
    }

    private static void addIfGeofenceable(List<Geofence> geofences, LocationZone zone,
                                          Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            Log.i(TAG, "Geofence skipped for " + zone + ": no coordinates set.");
            return;
        }
        geofences.add(new Geofence.Builder()
                .setRequestId(zone.name())
                .setCircularRegion(latitude, longitude, GEOFENCE_RADIUS_METERS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
    }

    private static PendingIntent geofencePendingIntent(Context context) {
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }

    private static boolean hasFineLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private static boolean hasBackgroundLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
}
