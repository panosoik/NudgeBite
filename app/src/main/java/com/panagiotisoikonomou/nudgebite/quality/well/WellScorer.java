package com.panagiotisoikonomou.nudgebite.quality.well;

import com.panagiotisoikonomou.nudgebite.food.well.WellComponent;

import java.util.ArrayList;
import java.util.List;

public final class WellScorer {

    public static final int MIN_OBSERVED_DAYS_TO_SCORE = 3;
    public static final double MIN_MEAL_COVERAGE_TO_SCORE = 0.50;
    public static final int OBSERVED_DAYS_FOR_FULL_CONFIDENCE = 7;
    public static final double MEAL_COVERAGE_FOR_FULL_CONFIDENCE = 0.75;

    private WellScorer() {
    }

    public static WellScoreResult score(WellObservation observation) {
        WellDataSufficiency sufficiency = sufficiencyOf(observation);
        if (sufficiency == WellDataSufficiency.INSUFFICIENT) {
            return WellScoreResult.INSUFFICIENT_DATA;
        }

        List<WellComponentScore> componentScores = new ArrayList<>();
        int total = 0;
        for (WellComponent component : WellComponent.values()) {
            WellComponentScore componentScore = scoreComponent(component, observation, sufficiency);
            componentScores.add(componentScore);
            total += componentScore.points;
        }

        return new WellScoreResult(total, WellPointMatrix.MAX_TOTAL_POINTS, componentScores,
                sufficiency, observation.observedDays(), observation.loggedMeals());
    }

    private static WellComponentScore scoreComponent(WellComponent component,
                                                     WellObservation observation,
                                                     WellDataSufficiency sufficiency) {
        int occurrences = observation.occurrencesOf(component);
        double ratePerDay = observation.ratePerDayOf(component);
        WellFrequencyBand band = WellFrequencyBand.fromPerDayRate(ratePerDay);

        // A provisional period cannot prove something was never eaten, so negative components are held back one band.
        if (sufficiency == WellDataSufficiency.PROVISIONAL
                && band == WellFrequencyBand.NEVER
                && WellPointMatrix.isNegative(component)) {
            band = WellFrequencyBand.ONCE_A_MONTH;
        }
        return new WellComponentScore(component, occurrences, ratePerDay, WellPointMatrix.pointsFor(component, band));
    }

    static WellDataSufficiency sufficiencyOf(WellObservation observation) {
        if (observation.observedDays() < MIN_OBSERVED_DAYS_TO_SCORE
                || observation.mealCoverage() < MIN_MEAL_COVERAGE_TO_SCORE) {
            return WellDataSufficiency.INSUFFICIENT;
        }
        if (observation.observedDays() < OBSERVED_DAYS_FOR_FULL_CONFIDENCE
                || observation.mealCoverage() < MEAL_COVERAGE_FOR_FULL_CONFIDENCE) {
            return WellDataSufficiency.PROVISIONAL;
        }
        return WellDataSufficiency.SUFFICIENT;
    }
}
