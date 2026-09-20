package com.panagiotisoikonomou.nudgebite.quality.well;

import java.util.Collections;
import java.util.List;

public final class WellScoreResult {

    public static final WellScoreResult INSUFFICIENT_DATA = new WellScoreResult(
            0, WellPointMatrix.MAX_TOTAL_POINTS, Collections.emptyList(),
            WellDataSufficiency.INSUFFICIENT, 0, 0);

    public final int totalPoints;
    public final int maxPoints;
    public final List<WellComponentScore> componentScores;
    public final WellDataSufficiency sufficiency;
    public final int observedDays;
    public final int loggedMeals;

    WellScoreResult(int totalPoints, int maxPoints, List<WellComponentScore> componentScores,
                    WellDataSufficiency sufficiency, int observedDays, int loggedMeals) {
        this.totalPoints = totalPoints;
        this.maxPoints = maxPoints;
        this.componentScores = Collections.unmodifiableList(componentScores);
        this.sufficiency = sufficiency;
        this.observedDays = observedDays;
        this.loggedMeals = loggedMeals;
    }

    public boolean isScored() {
        return sufficiency != WellDataSufficiency.INSUFFICIENT;
    }
}
