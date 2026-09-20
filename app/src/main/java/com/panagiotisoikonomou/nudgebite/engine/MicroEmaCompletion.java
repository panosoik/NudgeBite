package com.panagiotisoikonomou.nudgebite.engine;

import android.content.Context;
import android.util.Log;

import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.entity.EatingOccasionEvent;
import com.panagiotisoikonomou.nudgebite.data.entity.InterventionLog;
import com.panagiotisoikonomou.nudgebite.data.entity.MomentaryAssessmentEvent;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.enums.CookingWillingness;
import com.panagiotisoikonomou.nudgebite.data.enums.CravingType;
import com.panagiotisoikonomou.nudgebite.data.enums.EatingOccasionOutcome;
import com.panagiotisoikonomou.nudgebite.data.enums.FoodCategory;
import com.panagiotisoikonomou.nudgebite.data.enums.InterventionFormat;
import com.panagiotisoikonomou.nudgebite.data.enums.TriggerCombo;
import com.panagiotisoikonomou.nudgebite.data.enums.UserResponse;
import com.panagiotisoikonomou.nudgebite.data.enums.UserTone;
import com.panagiotisoikonomou.nudgebite.engine.ema.EmaResolution;
import com.panagiotisoikonomou.nudgebite.engine.ema.MicroEmaAnswers;
import com.panagiotisoikonomou.nudgebite.engine.recommendation.MealRecommendation;
import com.panagiotisoikonomou.nudgebite.engine.recommendation.MealRecommendationSelector.Selection;
import com.panagiotisoikonomou.nudgebite.engine.recommendation.RecommendationContext;
import com.panagiotisoikonomou.nudgebite.engine.recommendation.SuggestionPayloadBuilder;
import com.panagiotisoikonomou.nudgebite.notifications.InterventionNotifier;

import java.time.LocalDateTime;
import java.util.List;

/** Records a micro-EMA answer and acts on it. Blocking: call off the main thread. */
public final class MicroEmaCompletion {

    private static final String TAG = SignalEngine.TAG;

    private static final String COPING_MESSAGE = "Long day? On tired nights the easiest balanced meal is usually "
            + "something you've already got in — no big effort needed.";

    private MicroEmaCompletion() {
    }

    /** True when a suggestion screen is now ready to open for this intervention. */
    public static boolean recordAnswers(Context context, long interventionLogId, MicroEmaAnswers answers) {
        AppDatabase db = AppDatabase.getDatabase(context);
        InterventionLog log = db.interventionLogDao().getByIdSync(interventionLogId);
        EatingOccasionEvent occasion = log == null || log.eatingOccasionEventId == null
                ? null : db.eatingOccasionEventDao().getByIdSync(log.eatingOccasionEventId);
        User user = db.userDao().getUserSync();
        if (occasion == null || user == null) {
            Log.w(TAG, "MicroEmaCompletion: nothing to resolve for intervention " + interventionLogId);
            return false;
        }

        db.momentaryAssessmentEventDao().insert(assessmentFrom(answers, occasion.id));
        db.eatingOccasionEventDao().updateDecisionStage(occasion.id, answers.mealDecisionStage);

        EmaResolution resolution = EmaResolution.resolve(occasion.triggerCombo, answers);
        Log.i(TAG, "MicroEmaCompletion: intervention " + interventionLogId + " resolved to " + resolution);
        switch (resolution) {
            case NOTHING_TO_SUGGEST:
                withdrawIntervention(db, interventionLogId, occasion);
                return false;
            case COPING_MESSAGE:
                sendFollowUpNotification(context, db, interventionLogId, resolution, COPING_MESSAGE);
                return false;
            case SHOPPING_REMINDER:
                List<FoodCategory> rankedGaps =
                        RecommendationContext.load(db.ingredientDao(), user, occasion.creationDate).rankedGaps;
                sendFollowUpNotification(context, db, interventionLogId, resolution,
                        SuggestionPayloadBuilder.postDeliveryPrepBodyText(rankedGaps));
                return false;
            default:
                Selection selection = RecommendationContext.load(db.ingredientDao(), user, occasion.creationDate)
                        .pickDish(context, MealRecommendation.CATALOG);
                db.interventionLogDao().updateAfterEma(interventionLogId, resolution.category,
                        InterventionFormat.FOOD_SUGGESTION_SCREEN,
                        suggestionPayload(resolution, occasion.triggerCombo, selection));
                return true;
        }
    }

    /** Stage 3 recorded an intervention before the answer existed; walk it back so it does not burn the daily cap and cooldown. */
    private static void withdrawIntervention(AppDatabase db, long interventionLogId, EatingOccasionEvent occasion) {
        db.eatingOccasionEventDao().updateOutcome(occasion.id, EatingOccasionOutcome.NO_INTERVENTION_NOT_NEEDED);
        // An answer tapped on the notification is engagement, so it closes as ACKNOWLEDGED and cannot trip the dismissal downgrade.
        boolean answeredOnTheNotification = occasion.triggerCombo == TriggerCombo.POST_DELIVERY_PREP
                || occasion.triggerCombo == TriggerCombo.WORK_DEPARTURE;
        db.interventionLogDao().updateResponse(interventionLogId,
                answeredOnTheNotification ? UserResponse.ACKNOWLEDGED : UserResponse.DISMISSED);
    }

    private static void sendFollowUpNotification(Context context, AppDatabase db, long interventionLogId,
                                                 EmaResolution resolution, String message) {
        db.interventionLogDao().updateAfterEma(interventionLogId, resolution.category,
                InterventionFormat.NOTIFICATION_ONLY, message);
        InterventionNotifier.notify(context, interventionLogId, InterventionFormat.NOTIFICATION_ONLY, message);
    }

    private static String suggestionPayload(EmaResolution resolution, TriggerCombo combo, Selection selection) {
        MealRecommendation dish = selection.recommendation;
        switch (resolution) {
            case COOK_WITH_WHAT_YOU_HAVE:
                return SuggestionPayloadBuilder.buildHomePayload(dish, selection.alternativeIds);
            case PICK_UP_ON_THE_WAY:
                return SuggestionPayloadBuilder.buildOnTheWayPayload(dish, selection.alternativeIds);
            case ASKED_FOR_IDEA:
                return SuggestionPayloadBuilder.buildAskedForIdeaPayload(dish, selection.alternativeIds);
            default:
                return SuggestionPayloadBuilder.buildAlternativePayload(dish,
                        SuggestionPayloadBuilder.alternativeEyebrowFor(combo), selection.alternativeIds);
        }
    }

    /** The questions the app does not ask yet are stored as "not collected", never guessed. */
    private static MomentaryAssessmentEvent assessmentFrom(MicroEmaAnswers answers, long occasionId) {
        MomentaryAssessmentEvent assessment = new MomentaryAssessmentEvent();
        assessment.creationDate = LocalDateTime.now();
        assessment.eatingOccasionEventId = occasionId;
        assessment.foodAvailableAtHome = answers.foodAvailableAtHome;
        assessment.dinnerPlanned = answers.dinnerPlan;
        assessment.mealDecisionStage = answers.mealDecisionStage;
        assessment.deliveryIntent = answers.deliveryIntent;
        assessment.inputMode = answers.inputMode;
        assessment.cookingWillingness = CookingWillingness.NONE;
        assessment.cravingType = CravingType.NONE;
        assessment.userTone = UserTone.UNKNOWN;
        assessment.needsClarification = false;
        return assessment;
    }
}
