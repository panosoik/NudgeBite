package com.panagiotisoikonomou.nudgebite.engine.signals;

import android.content.Context;

import com.panagiotisoikonomou.nudgebite.engine.LocationZone;

public class RealLocationSignalSource implements LocationSignalSource {

    /** A work exit expires, otherwise work-departure (checked first) would block every other combo all day. */
    private static final long WORK_EXIT_RECENCY_WINDOW_MILLIS = 30 * 60 * 1000L;

    private final GeofenceZoneStore store;

    public RealLocationSignalSource(Context context) {
        this.store = new GeofenceZoneStore(context);
    }

    @Override
    public LocationZone getCurrentZone() {
        return store.getCurrentZone();
    }

    @Override
    public boolean hasRecentlyLeftWorkplace() {
        long lastExitAt = store.getLastWorkExitAtMillis();
        if (lastExitAt == 0L) {
            return false;
        }
        return System.currentTimeMillis() - lastExitAt <= WORK_EXIT_RECENCY_WINDOW_MILLIS;
    }
}
