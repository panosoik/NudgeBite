package com.panagiotisoikonomou.nudgebite.progress.well;

/** The frequency at which the published WELL matrix stops awarding points. Not advice. */
public final class FoodGroupTarget {

    public final TrackedFoodGroup group;
    public final TargetDirection direction;
    public final TargetUnit unit;

    public final double target;

    public final double rangeLow;

    public final double rangeHigh;

    public final boolean openEnded;

    FoodGroupTarget(TrackedFoodGroup group, TargetDirection direction, TargetUnit unit,
                    double target, double rangeLow, double rangeHigh, boolean openEnded) {
        this.group = group;
        this.direction = direction;
        this.unit = unit;
        this.target = target;
        this.rangeLow = rangeLow;
        this.rangeHigh = rangeHigh;
        this.openEnded = openEnded;
    }

    public String describe() {
        switch (direction) {
            case RANGE:
                return "WELL-inspired target: " + FrequencyAxis.formatFrequency(rangeLow) + " to " + FrequencyAxis.formatFrequency(rangeHigh)
                        + " " + unit.shortLabel();
            case MAX:
                return target <= 0.0
                        ? "WELL-inspired target: rarely"
                        : "WELL-inspired target: up to " + FrequencyAxis.formatFrequency(target) + " " + unit.shortLabel();
            case MIN:
            default:
                return openEnded
                        ? "WELL-inspired target: more is better, levelling off around "
                                + FrequencyAxis.formatFrequency(target) + " " + unit.shortLabel()
                        : "WELL-inspired target: " + FrequencyAxis.formatFrequency(target) + " " + unit.shortLabel();
        }
    }

    public String describeShort() {
        switch (direction) {
            case RANGE:
                return "target " + FrequencyAxis.formatFrequency(rangeLow) + "-" + FrequencyAxis.formatFrequency(rangeHigh) + unit.suffix();
            case MAX:
                return target <= 0.0
                        ? "target rarely"
                        : "target up to " + FrequencyAxis.formatFrequency(target) + unit.suffix();
            case MIN:
            default:
                return "target " + FrequencyAxis.formatFrequency(target) + unit.suffix();
        }
    }
}
