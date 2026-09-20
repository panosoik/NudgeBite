package com.panagiotisoikonomou.nudgebite.engine;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.enums.InterventionCategory;
import com.panagiotisoikonomou.nudgebite.data.enums.InterventionFormat;
import com.panagiotisoikonomou.nudgebite.data.enums.TriggerCombo;

public final class Stage4InterventionMapper {

    private Stage4InterventionMapper() {
    }

    @Nullable
    public static InterventionCategory categoryFor(TriggerCombo combo) {
        switch (combo) {
            case DELIVERY_HOME_EVENING:
            case SUPERMARKET:
            case DINNER_HOME:
            case WORK_DEPARTURE:
            case POST_DELIVERY_PREP:
            case ORGANIZE_MEALS_PREP:
            case EATING_OUT_GAP:
                return InterventionCategory.PROMPT_REMINDER;
            case DINNER_NO_PLAN:
                return InterventionCategory.PLANNING;
            case WEEKLY_REVIEW:
                return InterventionCategory.FEEDBACK;
            default:
                return null;
        }
    }

    @Nullable
    public static InterventionFormat formatFor(TriggerCombo combo) {
        switch (combo) {
            case DELIVERY_HOME_EVENING:
            case SUPERMARKET:
            case DINNER_NO_PLAN:
                return InterventionFormat.FOOD_SUGGESTION_SCREEN;
            case DINNER_HOME:
                return InterventionFormat.MICRO_EMA;
            case WORK_DEPARTURE:
                return InterventionFormat.FOOD_AT_HOME_EMA;
            case POST_DELIVERY_PREP:
                return InterventionFormat.NOTIFICATION_EMA;
            case ORGANIZE_MEALS_PREP:
            case EATING_OUT_GAP:
                return InterventionFormat.NOTIFICATION_ONLY;
            case WEEKLY_REVIEW:
                return InterventionFormat.WEEKLY_REVIEW;
            default:
                return null;
        }
    }
}
