package com.panagiotisoikonomou.nudgebite.utils;

import androidx.annotation.Nullable;

/** Reads an enum back from its stored name without throwing on data from an older build. */
public final class EnumNames {

    private EnumNames() {
    }

    public static <E extends Enum<E>> E parseOrDefault(Class<E> type, @Nullable String name, @Nullable E fallback) {
        if (name == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
