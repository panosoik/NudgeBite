package com.panagiotisoikonomou.nudgebite.engine;

import com.panagiotisoikonomou.nudgebite.data.entity.EatingOccasionEvent;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.enums.EatingOccasionOutcome;
import com.panagiotisoikonomou.nudgebite.data.enums.TriggerCombo;
import com.panagiotisoikonomou.nudgebite.data.enums.UserResponse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** First match wins: 4h cooldown, daily cap, one per meal window, delivery priority, repeated dismissals. */
public final class Stage3ReceptivityEvaluator {

    /** Counts occasions that reached INTERVENED; hydration never creates an occasion. */
    private static final int DAILY_CAP = 4;
    private static final int COOLDOWN_HOURS = 4;
    private static final int DOWNGRADE_LOOKBACK = 2;

    private Stage3ReceptivityEvaluator() {
    }

    public static EatingOccasionOutcome evaluateReceptivity(EatingOccasionEvent candidate, User user,
                                                             InterventionHistory history) {
        MealWindow candidateWindow = MealWindowResolver.resolveMealWindow(user, candidate.creationDate);
        List<EatingOccasionEvent> intervenedToday = intervenedEvents(history.todaysEvents);

        for (EatingOccasionEvent event : intervenedToday) {
            long hoursSince = Duration.between(event.creationDate, candidate.creationDate).toHours();
            if (hoursSince < COOLDOWN_HOURS) {
                return EatingOccasionOutcome.NO_INTERVENTION_CAP;
            }
        }

        if (intervenedToday.size() >= DAILY_CAP) {
            return EatingOccasionOutcome.NO_INTERVENTION_CAP;
        }

        for (EatingOccasionEvent event : intervenedToday) {
            if (MealWindowResolver.resolveMealWindow(user, event.creationDate) == candidateWindow) {
                return EatingOccasionOutcome.NO_INTERVENTION_CAP;
            }
        }

        if (candidate.triggerCombo == TriggerCombo.DINNER_HOME
                && deliveryAlreadyClaimedWindow(history.todaysEvents, user, candidateWindow)) {
            return EatingOccasionOutcome.NO_INTERVENTION_NOT_NEEDED;
        }

        if (isRecentlyUnreceptive(history.recentResponsesOldestFirst)) {
            return EatingOccasionOutcome.NO_INTERVENTION_NOT_RECEPTIVE;
        }

        return EatingOccasionOutcome.INTERVENED;
    }

    private static List<EatingOccasionEvent> intervenedEvents(List<EatingOccasionEvent> todaysEvents) {
        List<EatingOccasionEvent> intervened = new ArrayList<>();
        for (EatingOccasionEvent event : todaysEvents) {
            if (event.outcome == EatingOccasionOutcome.INTERVENED) {
                intervened.add(event);
            }
        }
        return intervened;
    }

    private static boolean deliveryAlreadyClaimedWindow(List<EatingOccasionEvent> todaysEvents, User user,
                                                         MealWindow candidateWindow) {
        for (EatingOccasionEvent event : todaysEvents) {
            if (event.triggerCombo == TriggerCombo.DELIVERY_HOME_EVENING
                    && MealWindowResolver.resolveMealWindow(user, event.creationDate) == candidateWindow) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRecentlyUnreceptive(List<UserResponse> responsesOldestFirst) {
        if (responsesOldestFirst.size() < DOWNGRADE_LOOKBACK) {
            return false;
        }
        List<UserResponse> lastFew = responsesOldestFirst.subList(
                responsesOldestFirst.size() - DOWNGRADE_LOOKBACK, responsesOldestFirst.size());
        for (UserResponse response : lastFew) {
            if (response != UserResponse.DISMISSED && response != UserResponse.IGNORED) {
                return false;
            }
        }
        return true;
    }
}
