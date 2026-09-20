package com.panagiotisoikonomou.nudgebite.utils;

import android.content.Context;

public final class DisplayUtils {

    private DisplayUtils() {
    }

    public static int dp(Context context, int value) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
