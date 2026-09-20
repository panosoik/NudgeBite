package com.panagiotisoikonomou.nudgebite.engine.signals;

import com.panagiotisoikonomou.nudgebite.engine.LocationZone;

public interface LocationSignalSource {

    LocationZone getCurrentZone();

    boolean hasRecentlyLeftWorkplace();
}
