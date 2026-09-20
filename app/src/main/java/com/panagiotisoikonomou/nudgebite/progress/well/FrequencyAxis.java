package com.panagiotisoikonomou.nudgebite.progress.well;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FrequencyAxis {

    private static final double[] MANTISSAS = {1, 2, 5};

    private static final double[] SUB_UNIT_STEPS = {0.25, 0.5};

    private static final int MAX_TICKS = 5;

    private FrequencyAxis() {
    }

    public static List<Double> ticksUpTo(double axisMax) {
        List<Double> ticks = new ArrayList<>();
        ticks.add(0.0);
        if (axisMax <= 0.0) {
            return ticks;
        }
        double step = stepFor(axisMax);
        for (double value = step; value <= axisMax + 1e-9; value += step) {
            ticks.add(round(value));
        }
        return ticks;
    }

    private static double stepFor(double axisMax) {
        int maxIntervals = MAX_TICKS - 1;
        if (axisMax < 2) {
            for (double step : SUB_UNIT_STEPS) {
                if (axisMax / step <= maxIntervals) {
                    return step;
                }
            }
        }
        for (int power = 0; power <= 12; power++) {
            double scale = Math.pow(10, power);
            for (double mantissa : MANTISSAS) {
                double step = mantissa * scale;
                if (axisMax / step <= maxIntervals) {
                    return step;
                }
            }
        }
        return axisMax / maxIntervals;
    }

    public static String formatFrequency(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.001) {
            return String.valueOf((int) Math.rint(value));
        }
        return String.format(Locale.US, "%.1f", value);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
