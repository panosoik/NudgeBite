package com.panagiotisoikonomou.nudgebite.progress.well;

import com.panagiotisoikonomou.nudgebite.quality.well.WellFrequencyBand;
import com.panagiotisoikonomou.nudgebite.quality.well.WellPointMatrix;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Derived from the WELL matrix row shape: full points at never = MAX, still full at the top = MIN, a peak in between = RANGE. */
public final class FoodGroupTargets {

    private static final Set<TrackedFoodGroup> OPEN_ENDED =
            Collections.unmodifiableSet(EnumSet.of(TrackedFoodGroup.VEGETABLES, TrackedFoodGroup.LEGUMES));

    private static final Map<TrackedFoodGroup, FoodGroupTarget> TARGETS =
            new EnumMap<>(TrackedFoodGroup.class);

    static {
        for (TrackedFoodGroup group : TrackedFoodGroup.values()) {
            TARGETS.put(group, derive(group));
        }
    }

    private FoodGroupTargets() {
    }

    public static FoodGroupTarget of(TrackedFoodGroup group) {
        return TARGETS.get(group);
    }

    private static FoodGroupTarget derive(TrackedFoodGroup group) {
        WellFrequencyBand[] bands = WellFrequencyBand.values();
        int best = Integer.MIN_VALUE;
        for (WellFrequencyBand band : bands) {
            best = Math.max(best, WellPointMatrix.pointsFor(group.component(), band));
        }
        int firstPeak = -1;
        int lastPeak = -1;
        for (int i = 0; i < bands.length; i++) {
            if (WellPointMatrix.pointsFor(group.component(), bands[i]) == best) {
                if (firstPeak < 0) {
                    firstPeak = i;
                }
                lastPeak = i;
            }
        }

        TargetUnit unit = group.unit();
        if (firstPeak == 0) {
            double ceiling = inUnit(bands[lastPeak], unit);
            return new FoodGroupTarget(group, TargetDirection.MAX, unit, ceiling, 0.0, ceiling, false);
        }
        double low = inUnit(bands[firstPeak], unit);
        if (lastPeak == bands.length - 1) {
            return new FoodGroupTarget(group, TargetDirection.MIN, unit, low, low, low,
                    OPEN_ENDED.contains(group));
        }
        // Bands are whole occasions, so one below the next band's floor is the top of the range.
        double high = inUnit(bands[lastPeak + 1], unit) - 1.0;
        return new FoodGroupTarget(group, TargetDirection.RANGE, unit, low, low, high, false);
    }

    private static double inUnit(WellFrequencyBand band, TargetUnit unit) {
        return Math.round(unit.fromRatePerDay(band.lowerBoundPerDay()) * 100.0) / 100.0;
    }
}
