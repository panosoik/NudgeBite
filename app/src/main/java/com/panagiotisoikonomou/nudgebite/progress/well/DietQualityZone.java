package com.panagiotisoikonomou.nudgebite.progress.well;

/** Indicative WELL-inspired bands, not clinical cutoffs. The lowest reads "building", never failure. */
public enum DietQualityZone {

    BUILDING("Building", 0.00, 0.40, "A fresh start this week"),
    BALANCED("Balanced", 0.40, 0.70, "Finding your rhythm"),
    STRONG("Strong", 0.70, 1.00, "A strong week so far");

    private final String label;
    private final double lowerFraction;
    private final double upperFraction;
    private final String weeklyMessage;

    DietQualityZone(String label, double lowerFraction, double upperFraction, String weeklyMessage) {
        this.label = label;
        this.lowerFraction = lowerFraction;
        this.upperFraction = upperFraction;
        this.weeklyMessage = weeklyMessage;
    }

    public String label() {
        return label;
    }

    public double lowerFraction() {
        return lowerFraction;
    }

    public double upperFraction() {
        return upperFraction;
    }

    public String weeklyMessage() {
        return weeklyMessage;
    }

    public double labelFraction() {
        return (lowerFraction + upperFraction) / 2.0;
    }

    public static DietQualityZone of(double fraction) {
        if (fraction >= STRONG.lowerFraction) {
            return STRONG;
        }
        return fraction >= BALANCED.lowerFraction ? BALANCED : BUILDING;
    }
}
