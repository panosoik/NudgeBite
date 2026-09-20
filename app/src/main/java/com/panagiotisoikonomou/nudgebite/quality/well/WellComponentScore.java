package com.panagiotisoikonomou.nudgebite.quality.well;

import com.panagiotisoikonomou.nudgebite.food.well.WellComponent;

public final class WellComponentScore {

    public final WellComponent component;
    public final int occurrences;
    public final double ratePerDay;
    public final int points;

    WellComponentScore(WellComponent component, int occurrences, double ratePerDay, int points) {
        this.component = component;
        this.occurrences = occurrences;
        this.ratePerDay = ratePerDay;
        this.points = points;
    }
}
