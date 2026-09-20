package com.panagiotisoikonomou.nudgebite.progress.well;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.entity.WellScore;
import com.panagiotisoikonomou.nudgebite.data.repository.WellScoreComponents;
import com.panagiotisoikonomou.nudgebite.food.well.WellComponent;
import com.panagiotisoikonomou.nudgebite.quality.well.WellPointMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/** Strong: one statement. Balanced: up to two positives and two next steps. Building: an opener and next steps. */
public final class ReviewNoteBuilder {

    public static final String BALANCED_WEEK_STATEMENT = "You've had a balanced week so far.";
    public static final String LOW_VARIETY_FALLBACK = "This week was lighter on variety — an easy fresh start ahead.";

    private static final int MAX_LINES_PER_SIDE = 2;
    private static final ReviewNote NOT_SCORED_NOTE =
            new ReviewNote(Collections.singletonList(LOW_VARIETY_FALLBACK), Collections.emptyList());

    public static final class ReviewNote {
        public final List<String> topLines;
        public final List<String> improvementLines;

        ReviewNote(List<String> topLines, List<String> improvementLines) {
            this.topLines = topLines;
            this.improvementLines = improvementLines;
        }
    }

    private ReviewNoteBuilder() {
    }

    public static ReviewNote build(@Nullable WellScore weekScore) {
        if (weekScore == null || weekScore.maxPoints <= 0) {
            return NOT_SCORED_NOTE;
        }
        Map<WellComponent, Integer> pointsByComponent = WellScoreComponents.parsePoints(weekScore.componentPoints);
        if (pointsByComponent.isEmpty()) {
            return NOT_SCORED_NOTE;
        }
        DietQualityZone zone = DietQualityZone.of((double) weekScore.totalPoints / weekScore.maxPoints);
        if (zone == DietQualityZone.STRONG) {
            return new ReviewNote(Collections.singletonList(BALANCED_WEEK_STATEMENT), Collections.emptyList());
        }

        List<WellComponent> weakest = weakestComponents(pointsByComponent);
        List<String> improvementLines = new ArrayList<>();
        for (WellComponent component : weakest) {
            improvementLines.add(WellComponentCopy.improvementNote(component));
        }
        if (zone == DietQualityZone.BALANCED) {
            List<String> positiveLines = new ArrayList<>();
            for (WellComponent component : strongestComponents(pointsByComponent, weakest)) {
                positiveLines.add(WellComponentCopy.positiveObservation(component));
            }
            return new ReviewNote(positiveLines, improvementLines);
        }
        return new ReviewNote(Collections.singletonList(LOW_VARIETY_FALLBACK), improvementLines);
    }

    /** Anything already at full marks has no next step to offer. */
    private static List<WellComponent> weakestComponents(Map<WellComponent, Integer> pointsByComponent) {
        return topCandidates(pointsByComponent,
                entry -> WellComponentCopy.canBeImprovement(entry.getKey())
                        && entry.getValue() < WellPointMatrix.MAX_POINTS_PER_COMPONENT,
                Map.Entry.comparingByValue());
    }

    /** Only components that actually scored, never one already named as a gap. */
    private static List<WellComponent> strongestComponents(Map<WellComponent, Integer> pointsByComponent,
                                                           List<WellComponent> alreadyNamed) {
        return topCandidates(pointsByComponent,
                entry -> WellComponentCopy.canBePositive(entry.getKey())
                        && !alreadyNamed.contains(entry.getKey())
                        && entry.getValue() > 0,
                Map.Entry.<WellComponent, Integer>comparingByValue().reversed());
    }

    private static List<WellComponent> topCandidates(Map<WellComponent, Integer> pointsByComponent,
                                                     Predicate<Map.Entry<WellComponent, Integer>> isCandidate,
                                                     Comparator<Map.Entry<WellComponent, Integer>> order) {
        List<Map.Entry<WellComponent, Integer>> candidates = new ArrayList<>();
        for (Map.Entry<WellComponent, Integer> entry : pointsByComponent.entrySet()) {
            if (isCandidate.test(entry)) {
                candidates.add(entry);
            }
        }
        candidates.sort(order);
        List<WellComponent> top = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_LINES_PER_SIDE, candidates.size()); i++) {
            top.add(candidates.get(i).getKey());
        }
        return top;
    }
}
