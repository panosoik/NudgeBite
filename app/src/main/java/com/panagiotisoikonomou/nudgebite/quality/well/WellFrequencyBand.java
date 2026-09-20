package com.panagiotisoikonomou.nudgebite.quality.well;

/** Scored by band, not by formula, because the published points are not monotonic. */
public enum WellFrequencyBand {

    NEVER(0.0),
    ONCE_A_MONTH(1.0 / 30.0),
    TWO_TO_THREE_A_MONTH(2.0 / 30.0),
    ONE_TO_TWO_A_WEEK(1.0 / 7.0),
    THREE_TO_FOUR_A_WEEK(3.0 / 7.0),
    FIVE_TO_SIX_A_WEEK(5.0 / 7.0),
    ONCE_A_DAY(1.0),
    TWO_TO_THREE_A_DAY(2.0),
    FOUR_TO_FIVE_A_DAY(4.0),
    SIX_OR_MORE_A_DAY(6.0);

    private final double lowerBoundPerDay;

    WellFrequencyBand(double lowerBoundPerDay) {
        this.lowerBoundPerDay = lowerBoundPerDay;
    }

    public double lowerBoundPerDay() {
        return lowerBoundPerDay;
    }

    public static WellFrequencyBand fromPerDayRate(double ratePerDay) {
        if (ratePerDay <= 0.0) {
            return NEVER;
        }
        WellFrequencyBand[] bands = values();
        for (int i = bands.length - 1; i >= 1; i--) {
            if (ratePerDay >= bands[i].lowerBoundPerDay) {
                return bands[i];
            }
        }
        return ONCE_A_MONTH;
    }
}
