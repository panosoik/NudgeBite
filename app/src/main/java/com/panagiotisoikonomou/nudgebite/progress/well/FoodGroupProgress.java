package com.panagiotisoikonomou.nudgebite.progress.well;

import java.util.Collections;
import java.util.List;

public final class FoodGroupProgress {

    public enum Status {
        BELOW,
        WITHIN,
        ABOVE
    }

    public final TrackedFoodGroup group;
    public final FoodGroupTarget target;

    public final boolean hasData;

    /** Rounded before the status is decided, so a bar never reads "1 a week" next to "under the target of 1 a week". */
    public final double observedPerUnit;

    public final Status status;

    public final double axisMax;

    public final List<WellTrendPoint> history;

    public FoodGroupProgress(TrackedFoodGroup group, FoodGroupTarget target, boolean hasData,
                             double observedPerUnit, Status status, double axisMax,
                             List<WellTrendPoint> history) {
        this.group = group;
        this.target = target;
        this.hasData = hasData;
        this.observedPerUnit = observedPerUnit;
        this.status = status;
        this.axisMax = axisMax;
        this.history = Collections.unmodifiableList(history);
    }

    public double targetFraction() {
        return fractionOf(target.direction == TargetDirection.RANGE ? target.rangeLow : target.target);
    }

    public double bandHighFraction() {
        return fractionOf(target.direction == TargetDirection.RANGE ? target.rangeHigh : target.target);
    }

    public String valueText() {
        if (!hasData) {
            return "";
        }
        return FrequencyAxis.formatFrequency(observedPerUnit) + " " + target.unit.shortLabel();
    }

    private double fractionOf(double value) {
        if (axisMax <= 0.0) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value / axisMax));
    }

    static double roundToShownPrecision(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    static double axisMaxFor(FoodGroupTarget target, double observedPerUnit) {
        double highestMarker;
        double headroom = 1.25;
        switch (target.direction) {
            case RANGE:
                highestMarker = target.rangeHigh;
                break;
            case MAX:
                highestMarker = Math.max(target.target, 1.0);
                break;
            case MIN:
            default:
                highestMarker = target.target;
                headroom = target.openEnded ? 1.6 : headroom;
                break;
        }
        double needed = Math.max(highestMarker, observedPerUnit);
        return Math.max(highestMarker * headroom, needed * 1.1);
    }

    static Status statusFor(FoodGroupTarget target, double observedPerUnit) {
        switch (target.direction) {
            case RANGE:
                if (observedPerUnit < target.rangeLow) {
                    return Status.BELOW;
                }
                return observedPerUnit > target.rangeHigh ? Status.ABOVE : Status.WITHIN;
            case MAX:
                return observedPerUnit > target.target ? Status.ABOVE : Status.WITHIN;
            case MIN:
            default:
                return observedPerUnit < target.target ? Status.BELOW : Status.WITHIN;
        }
    }

    public String describeStatus() {
        if (!hasData) {
            return "Not enough logged yet";
        }
        switch (target.direction) {
            case RANGE:
                if (status == Status.WITHIN) {
                    return "Within the WELL-inspired range";
                }
                return status == Status.BELOW ? "Under the range" : "Above the range";
            case MAX:
                return status == Status.WITHIN ? "At or under the target" : "Above the target";
            case MIN:
            default:
                if (status == Status.WITHIN) {
                    return "At or above the target";
                }
                return target.openEnded ? "Room to add more" : "Under the target";
        }
    }
}
