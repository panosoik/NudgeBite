package com.panagiotisoikonomou.nudgebite.engine.signals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** To add an app, open it and read the "foreground packages seen" log line of the delivery signal. */
public final class DeliveryPackages {

    private static final List<String> KNOWN_PACKAGES = Collections.unmodifiableList(Arrays.asList(
            "gr.efood.android",
            "gr.cosmote.box",
            "com.wolt.android",
            "com.ubercab.eats"));

    private static final List<String> KNOWN_SUFFIXES = Collections.singletonList(".efood");

    private DeliveryPackages() {
    }

    public static boolean isDeliveryApp(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        String normalized = packageName.toLowerCase(Locale.US);
        for (String known : KNOWN_PACKAGES) {
            if (normalized.equals(known)) {
                return true;
            }
        }
        for (String suffix : KNOWN_SUFFIXES) {
            if (normalized.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
}
