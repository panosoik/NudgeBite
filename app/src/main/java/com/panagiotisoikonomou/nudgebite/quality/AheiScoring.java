package com.panagiotisoikonomou.nudgebite.quality;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.enums.Sex;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** AHEI-informed, not a validated AHEI score: never present it as clinically validated. */
public final class AheiScoring {

    public static final List<ScoreComponent> SCORED_COMPONENTS = Collections.unmodifiableList(Arrays.asList(
            ScoreComponent.VEGETABLE, ScoreComponent.FRUIT, ScoreComponent.WHOLE_GRAIN,
            ScoreComponent.LEGUME_NUT, ScoreComponent.FISH));

    private AheiScoring() {
    }

    /** Linear from 0 points at no intake to 10 points at the ten-point intake, clamped. */
    public static double points(ScoreComponent component, double meanDailyIntake, @Nullable Sex sex) {
        double points = meanDailyIntake * 10.0 / tenPointIntake(component, sex);
        return Math.max(0.0, Math.min(10.0, points));
    }

    static double tenPointIntake(ScoreComponent component, @Nullable Sex sex) {
        switch (component) {
            case VEGETABLE:
                return Ahei2010Thresholds.VEGETABLE_TEN_POINT_SERVINGS_PER_DAY;
            case FRUIT:
                return Ahei2010Thresholds.FRUIT_TEN_POINT_SERVINGS_PER_DAY;
            case WHOLE_GRAIN:
                // An unstated sex scores against the male (lower) target, as it always has.
                return sex == Sex.FEMALE
                        ? Ahei2010Thresholds.WHOLE_GRAIN_TEN_POINT_GRAMS_FEMALE
                        : Ahei2010Thresholds.WHOLE_GRAIN_TEN_POINT_GRAMS_MALE;
            case LEGUME_NUT:
                return Ahei2010Thresholds.LEGUME_NUT_TEN_POINT_SERVINGS_PER_DAY;
            case FISH:
                return MvpAdaptationConstants.FISH_TEN_POINT_SERVINGS_PER_DAY_PROXY;
            default:
                throw new IllegalArgumentException(component + " is not scored");
        }
    }
}
