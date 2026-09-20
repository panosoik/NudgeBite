package com.panagiotisoikonomou.nudgebite.engine.signals;

import com.panagiotisoikonomou.nudgebite.engine.LocationZone;

public class MockLocationSignalSource implements LocationSignalSource {

    private LocationZone currentZone = LocationZone.HOME;
    private boolean recentlyLeftWorkplace = false;

    public void setCurrentZone(LocationZone currentZone) {
        this.currentZone = currentZone;
    }

    public void setRecentlyLeftWorkplace(boolean recentlyLeftWorkplace) {
        this.recentlyLeftWorkplace = recentlyLeftWorkplace;
    }

    @Override
    public LocationZone getCurrentZone() {
        return currentZone;
    }

    @Override
    public boolean hasRecentlyLeftWorkplace() {
        return recentlyLeftWorkplace;
    }
}
