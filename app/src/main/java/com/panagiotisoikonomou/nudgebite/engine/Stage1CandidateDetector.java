package com.panagiotisoikonomou.nudgebite.engine;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.entity.EatingOccasionEvent;
import com.panagiotisoikonomou.nudgebite.data.enums.DecisionStage;
import com.panagiotisoikonomou.nudgebite.data.enums.TriggerCombo;
import com.panagiotisoikonomou.nudgebite.data.enums.TriggerType;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/** Checked in priority order: event-based, then opportunistic, then scheduled, with the weekly review last. */
public final class Stage1CandidateDetector {

    private static final int WEEKLY_REVIEW_START_HOUR = 18;

    private static final int WORK_DEPARTURE_START_HOUR = 15;

    private static final int POST_DELIVERY_PREP_START_HOUR = 12;
    private static final int POST_DELIVERY_PREP_END_HOUR = 14;

    private static final int ORGANIZE_MEALS_PREP_HOUR = 12;

    private Stage1CandidateDetector() {
    }

    @Nullable
    public static EatingOccasionEvent detectCandidate(SignalSnapshot snapshot) {
        if (matchesDeliveryHomeEvening(snapshot)) {
            return buildCandidate(TriggerCombo.DELIVERY_HOME_EVENING, TriggerType.EVENT_BASED, snapshot.now);
        }
        if (matchesWorkDeparture(snapshot)) {
            return buildCandidate(TriggerCombo.WORK_DEPARTURE, TriggerType.EVENT_BASED, snapshot.now);
        }
        if (matchesSupermarket(snapshot)) {
            return buildCandidate(TriggerCombo.SUPERMARKET, TriggerType.OPPORTUNITY, snapshot.now);
        }
        if (matchesEatingOutGap(snapshot)) {
            return buildCandidate(TriggerCombo.EATING_OUT_GAP, TriggerType.OPPORTUNITY, snapshot.now);
        }
        if (matchesDinnerNoPlan(snapshot)) {
            return buildCandidate(TriggerCombo.DINNER_NO_PLAN, TriggerType.SCHEDULED, snapshot.now);
        }
        if (matchesDinnerHome(snapshot)) {
            return buildCandidate(TriggerCombo.DINNER_HOME, TriggerType.SCHEDULED, snapshot.now);
        }
        if (matchesPostDeliveryPrep(snapshot)) {
            return buildCandidate(TriggerCombo.POST_DELIVERY_PREP, TriggerType.SCHEDULED, snapshot.now);
        }
        if (matchesOrganizeMealsPrep(snapshot)) {
            return buildCandidate(TriggerCombo.ORGANIZE_MEALS_PREP, TriggerType.SCHEDULED, snapshot.now);
        }
        if (matchesWeeklyReview(snapshot)) {
            return buildCandidate(TriggerCombo.WEEKLY_REVIEW, TriggerType.SCHEDULED, snapshot.now);
        }
        return null;
    }

    private static boolean matchesDeliveryHomeEvening(SignalSnapshot snapshot) {
        return snapshot.deliveryAppForeground
                && snapshot.currentZone == LocationZone.HOME
                && snapshot.currentMealWindow == MealWindow.DINNER;
    }

    private static boolean matchesWorkDeparture(SignalSnapshot snapshot) {
        return snapshot.recentlyLeftWorkplace
                && snapshot.now.getHour() >= WORK_DEPARTURE_START_HOUR;
    }

    private static boolean matchesSupermarket(SignalSnapshot snapshot) {
        return snapshot.currentZone == LocationZone.SUPERMARKET
                && snapshot.hasMissingFoodCategory;
    }

    private static boolean matchesEatingOutGap(SignalSnapshot snapshot) {
        return snapshot.currentZone == LocationZone.EATING_PLACE
                && snapshot.hasBetterChoicesOutGoal
                && !snapshot.eatingOutGapAlreadyDeliveredToday;
    }

    private static boolean matchesDinnerNoPlan(SignalSnapshot snapshot) {
        return snapshot.currentZone == LocationZone.HOME
                && snapshot.currentMealWindow == MealWindow.DINNER
                && (snapshot.hasFewerDeliveriesGoal || snapshot.hasQuickBalancedGoal)
                && !snapshot.dinnerLoggedToday
                && snapshot.dinnerPlanAnsweredNoRecently
                && !snapshot.dinnerNoPlanAlreadyDeliveredToday;
    }

    private static boolean matchesDinnerHome(SignalSnapshot snapshot) {
        return snapshot.currentZone == LocationZone.HOME
                && snapshot.currentMealWindow == MealWindow.DINNER;
    }

    private static boolean matchesPostDeliveryPrep(SignalSnapshot snapshot) {
        return snapshot.deliveryAppForegroundYesterday
                && snapshot.hasFewerDeliveriesGoal
                && snapshot.now.getHour() >= POST_DELIVERY_PREP_START_HOUR
                && snapshot.now.getHour() < POST_DELIVERY_PREP_END_HOUR;
    }

    private static boolean matchesOrganizeMealsPrep(SignalSnapshot snapshot) {
        return snapshot.hasOrganizeMealsGoal
                && snapshot.now.getDayOfWeek() == DayOfWeek.SATURDAY
                && snapshot.now.getHour() == ORGANIZE_MEALS_PREP_HOUR;
    }

    /** Once per week: the 15-minute worker would otherwise send a second review after the 4h cooldown. */
    private static boolean matchesWeeklyReview(SignalSnapshot snapshot) {
        return snapshot.now.getDayOfWeek() == DayOfWeek.SUNDAY
                && snapshot.now.getHour() >= WEEKLY_REVIEW_START_HOUR
                && !snapshot.weeklyReviewAlreadyDelivered;
    }

    private static EatingOccasionEvent buildCandidate(TriggerCombo combo, TriggerType type, LocalDateTime now) {
        EatingOccasionEvent event = new EatingOccasionEvent();
        event.creationDate = now;
        event.triggerCombo = combo;
        event.triggerType = type;
        event.decisionStage = DecisionStage.UNKNOWN;
        return event;
    }
}
