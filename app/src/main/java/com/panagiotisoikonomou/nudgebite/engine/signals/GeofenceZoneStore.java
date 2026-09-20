package com.panagiotisoikonomou.nudgebite.engine.signals;

import android.content.Context;
import android.content.SharedPreferences;

import com.panagiotisoikonomou.nudgebite.engine.LocationZone;
import com.panagiotisoikonomou.nudgebite.utils.EnumNames;

public class GeofenceZoneStore {

    private static final String PREFS_NAME = "geofence_zone_store";
    private static final String KEY_CURRENT_ZONE = "current_zone";
    private static final String KEY_LAST_WORK_EXIT_AT = "last_work_exit_at_millis";

    private final SharedPreferences prefs;

    public GeofenceZoneStore(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public LocationZone getCurrentZone() {
        return EnumNames.parseOrDefault(LocationZone.class, prefs.getString(KEY_CURRENT_ZONE, null),
                LocationZone.UNKNOWN);
    }

    public void setCurrentZone(LocationZone zone) {
        prefs.edit().putString(KEY_CURRENT_ZONE, zone.name()).apply();
    }

    public long getLastWorkExitAtMillis() {
        return prefs.getLong(KEY_LAST_WORK_EXIT_AT, 0L);
    }

    public void recordWorkExitNow() {
        prefs.edit().putLong(KEY_LAST_WORK_EXIT_AT, System.currentTimeMillis()).apply();
    }
}
