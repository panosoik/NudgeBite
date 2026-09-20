package com.panagiotisoikonomou.nudgebite.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.enums.DinnerPlan;
import com.panagiotisoikonomou.nudgebite.data.enums.FoodAvailability;
import com.panagiotisoikonomou.nudgebite.data.enums.InterventionFormat;
import com.panagiotisoikonomou.nudgebite.data.enums.UserResponse;
import com.panagiotisoikonomou.nudgebite.engine.MicroEmaCompletion;
import com.panagiotisoikonomou.nudgebite.engine.ema.MicroEmaAnswers;
import com.panagiotisoikonomou.nudgebite.ui.intervention.SuggestionActivity;
import com.panagiotisoikonomou.nudgebite.water.WaterRepository;

import java.time.LocalDateTime;

/** Handles every notification button that does not open a screen, and swipe-away. */
public class InterventionActionReceiver extends BroadcastReceiver {

    private static final String EXTRA_INTERVENTION_LOG_ID = "intervention_log_id";

    /** Actions must be distinct: the pending intents share a request code and differ only by action. */
    public enum Action {
        SKIP("INTERVENTION_SKIP"),
        ACKNOWLEDGE("INTERVENTION_ACKNOWLEDGE"),
        SWIPED_AWAY("INTERVENTION_DISMISSED"),
        FOOD_AT_HOME_YES("FOOD_AT_HOME_YES"),
        FOOD_AT_HOME_SOME("FOOD_AT_HOME_SOME"),
        FOOD_AT_HOME_NO("FOOD_AT_HOME_NO"),
        DINNER_PLAN_NO("DINNER_PLAN_NO"),
        DINNER_PLAN_SUGGEST("DINNER_PLAN_SUGGEST"),
        DINNER_PLAN_YES("DINNER_PLAN_YES"),
        WATER_OK("WATER_OK"),
        WATER_SKIP("WATER_SKIP");

        private final String intentAction;

        Action(String name) {
            this.intentAction = "com.panagiotisoikonomou.nudgebite.action." + name;
        }

        @Nullable
        private static Action fromIntentAction(@Nullable String intentAction) {
            for (Action action : values()) {
                if (action.intentAction.equals(intentAction)) {
                    return action;
                }
            }
            return null;
        }
    }

    public static Intent intentFor(Context context, Action action, long interventionLogId) {
        Intent intent = new Intent(context, InterventionActionReceiver.class);
        intent.setAction(action.intentAction);
        intent.putExtra(EXTRA_INTERVENTION_LOG_ID, interventionLogId);
        return intent;
    }

    /** The answer at this position of a question asked on the notification, in InterventionActionPlan's label order. */
    static Action answerAction(InterventionFormat format, int answerIndex) {
        if (format == InterventionFormat.FOOD_AT_HOME_EMA) {
            return new Action[]{Action.FOOD_AT_HOME_YES, Action.FOOD_AT_HOME_SOME, Action.FOOD_AT_HOME_NO}[answerIndex];
        }
        if (format == InterventionFormat.DINNER_PLAN_EMA) {
            return new Action[]{Action.DINNER_PLAN_NO, Action.DINNER_PLAN_SUGGEST, Action.DINNER_PLAN_YES}[answerIndex];
        }
        throw new IllegalArgumentException("No notification answers for " + format);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        long interventionLogId = intent.getLongExtra(EXTRA_INTERVENTION_LOG_ID, -1);
        Action action = Action.fromIntentAction(intent.getAction());
        if (interventionLogId == -1 || action == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        switch (action) {
            case WATER_OK:
            case WATER_SKIP:
                answerWaterReminder(appContext, interventionLogId, action == Action.WATER_OK);
                return;
            case SKIP:
            case SWIPED_AWAY:
                // Swipe-away is an explicit decline, recorded as DISMISSED.
                InterventionNotifier.cancel(appContext, interventionLogId);
                recordResponse(appContext, interventionLogId, UserResponse.DISMISSED);
                return;
            case ACKNOWLEDGE:
                InterventionNotifier.cancel(appContext, interventionLogId);
                recordResponse(appContext, interventionLogId, UserResponse.ACKNOWLEDGED);
                return;
            default:
                InterventionNotifier.cancel(appContext, interventionLogId);
                applyAnswer(appContext, interventionLogId, answersFor(action));
        }
    }

    private static void answerWaterReminder(Context appContext, long interventionLogId, boolean drankAGlass) {
        WaterReminderNotifier.cancel(appContext);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (drankAGlass) {
                WaterRepository.addGlassSync(appContext, LocalDateTime.now());
            }
            AppDatabase.getDatabase(appContext).interventionLogDao().updateResponse(interventionLogId,
                    drankAGlass ? UserResponse.ACCEPTED : UserResponse.DISMISSED);
        });
    }

    private static void recordResponse(Context appContext, long interventionLogId, UserResponse response) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                AppDatabase.getDatabase(appContext).interventionLogDao().updateResponse(interventionLogId, response));
    }

    /** Starting an activity from a broadcast is allowed here because notification actions carry a short allowlist. */
    private static void applyAnswer(Context appContext, long interventionLogId, MicroEmaAnswers answers) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (MicroEmaCompletion.recordAnswers(appContext, interventionLogId, answers)) {
                appContext.startActivity(SuggestionActivity.newIntent(appContext, interventionLogId));
            }
        });
    }

    private static MicroEmaAnswers answersFor(Action answer) {
        switch (answer) {
            case FOOD_AT_HOME_YES:
                return MicroEmaAnswers.fromFoodAvailabilityOnly(FoodAvailability.YES);
            case FOOD_AT_HOME_SOME:
                return MicroEmaAnswers.fromFoodAvailabilityOnly(FoodAvailability.PARTIAL);
            case FOOD_AT_HOME_NO:
                return MicroEmaAnswers.fromFoodAvailabilityOnly(FoodAvailability.NO);
            case DINNER_PLAN_NO:
                return MicroEmaAnswers.fromDinnerPlan(DinnerPlan.NO);
            case DINNER_PLAN_SUGGEST:
                return MicroEmaAnswers.fromDinnerPlan(DinnerPlan.WANTS_RECOMMENDATION);
            case DINNER_PLAN_YES:
                return MicroEmaAnswers.fromDinnerPlan(DinnerPlan.YES);
            default:
                throw new IllegalArgumentException(answer + " is not an answer");
        }
    }
}
