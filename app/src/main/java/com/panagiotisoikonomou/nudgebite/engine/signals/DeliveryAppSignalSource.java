package com.panagiotisoikonomou.nudgebite.engine.signals;

import java.util.concurrent.TimeUnit;

public interface DeliveryAppSignalSource {

    long DEFAULT_LOOKBACK_MILLIS = TimeUnit.MINUTES.toMillis(30);

    boolean wasDeliveryAppForegrounded(long withinLastMillis);
}
