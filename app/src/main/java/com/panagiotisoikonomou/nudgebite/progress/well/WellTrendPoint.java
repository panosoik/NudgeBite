package com.panagiotisoikonomou.nudgebite.progress.well;

import java.util.List;

public final class WellTrendPoint {

    public final String label;
    public final boolean hasScore;

    public final double fraction;

    public final boolean provisional;

    public WellTrendPoint(String label, boolean hasScore, double fraction, boolean provisional) {
        this.label = label;
        this.hasScore = hasScore;
        this.fraction = fraction;
        this.provisional = provisional;
    }

    static WellTrendPoint noScore(String label) {
        return new WellTrendPoint(label, false, 0.0, false);
    }

    public static boolean anyScored(List<WellTrendPoint> points) {
        for (WellTrendPoint point : points) {
            if (point.hasScore) {
                return true;
            }
        }
        return false;
    }
}
