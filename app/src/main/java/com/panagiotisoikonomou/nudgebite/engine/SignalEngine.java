package com.panagiotisoikonomou.nudgebite.engine;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.dao.EatingOccasionEventDao;
import com.panagiotisoikonomou.nudgebite.data.dao.InterventionLogDao;
import com.panagiotisoikonomou.nudgebite.data.entity.EatingOccasionEvent;
import com.panagiotisoikonomou.nudgebite.data.entity.Goal;
import com.panagiotisoikonomou.nudgebite.data.entity.InterventionLog;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.enums.DinnerPlan;
import com.panagiotisoikonomou.nudgebite.data.enums.EatingOccasionOutcome;
import com.panagiotisoikonomou.nudgebite.data.enums.InterventionCategory;
import com.panagiotisoikonomou.nudgebite.data.enums.InterventionFormat;
import com.panagiotisoikonomou.nudgebite.data.enums.MealSlot;
import com.panagiotisoikonomou.nudgebite.data.enums.TriggerCombo;
import com.panagiotisoikonomou.nudgebite.data.enums.UserResponse;
import com.panagiotisoikonomou.nudgebite.engine.ema.EmaVariant;
import com.panagiotisoikonomou.nudgebite.engine.ema.RecentEmaAnswers;
import com.panagiotisoikonomou.nudgebite.engine.recommendation.FoodCategoryGapAnalyzer;
import com.panagiotisoikonomou.nudgebite.engine.recommendation.MealRecommendation;
import com.panagiotisoikonomou.nudgebite.engine.recommendation.MealRecommendationSelector.Selection;
import com.panagiotisoikonomou.nudgebite.engine.recommendation.RecommendationContext;
import com.panagiotisoikonomou.nudgebite.engine.recommendation.SuggestionPayloadBuilder;
import com.panagiotisoikonomou.nudgebite.engine.signals.DeliveryAppSignalSource;
import com.panagiotisoikonomou.nudgebite.engine.signals.LocationSignalSource;
import com.panagiotisoikonomou.nudgebite.engine.signals.RealLocationSignalSource;
import com.panagiotisoikonomou.nudgebite.engine.signals.SystemTimeSignalSource;
import com.panagiotisoikonomou.nudgebite.engine.signals.TimeSignalSource;
import com.panagiotisoikonomou.nudgebite.engine.signals.UsageStatsDeliveryAppSignalSource;
import com.panagiotisoikonomou.nudgebite.notifications.InterventionNotifier;
import com.panagiotisoikonomou.nudgebite.progress.OnboardingGoal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** One pass of the JITAI pipeline: read the signals, detect a candidate (stage 1), check receptivity (stage 3), deliver (stage 4). */
public class SignalEngine {

    public static final String TAG = "NudgeBiteEngine";

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final long YESTERDAYS_DELIVERY_LOOKBACK_MILLIS = TimeUnit.HOURS.toMillis(20);
    private static final int RECENT_RESPONSE_COUNT = 5;

    private final Context appContext;
    private final DeliveryAppSignalSource deliverySource;
    private final LocationSignalSource locationSource;
    private final TimeSignalSource timeSource;
    private final AppDatabase db;
    private final EatingOccasionEventDao eatingOccasionEventDao;
    private final InterventionLogDao interventionLogDao;

    public SignalEngine(Context context, DeliveryAppSignalSource deliverySource,
                        LocationSignalSource locationSource, TimeSignalSource timeSource) {
        this.appContext = context.getApplicationContext();
        this.deliverySource = deliverySource;
        this.locationSource = locationSource;
        this.timeSource = timeSource;
        this.db = AppDatabase.getDatabase(appContext);
        this.eatingOccasionEventDao = db.eatingOccasionEventDao();
        this.interventionLogDao = db.interventionLogDao();
    }

    public static SignalEngine withRealSignals(Context context) {
        return new SignalEngine(context, new UsageStatsDeliveryAppSignalSource(context),
                new RealLocationSignalSource(context), new SystemTimeSignalSource());
    }

    /** Runs on the database executor; the dev screen follows along through runLog. */
    public void runOnce(User user, @Nullable EngineRunLog runLog) {
        AppDatabase.databaseWriteExecutor.execute(() -> runPipeline(user, runLog));
    }

    /** For callers that are already off the main thread. */
    public void runOnceSync(User user) {
        runPipeline(user, null);
    }

    private void runPipeline(User user, @Nullable EngineRunLog runLog) {
        Narrator narrate = new Narrator(runLog);
        narrate.line("=== runOnce ===");

        SignalSnapshot snapshot = readSignals(user);
        narrate.line("Signals: delivery=" + snapshot.deliveryAppForeground
                + ", zone=" + snapshot.currentZone
                + ", leftWorkplace=" + snapshot.recentlyLeftWorkplace
                + ", time=" + snapshot.now.format(TIME_FORMAT)
                + ", mealWindow=" + snapshot.currentMealWindow);

        EatingOccasionEvent candidate = Stage1CandidateDetector.detectCandidate(snapshot);
        if (candidate == null) {
            narrate.line("Stage 1: no combo matched — no eating occasion, nothing persisted.");
            narrate.line("=== end ===");
            return;
        }
        narrate.line("Stage 1: candidate " + candidate.triggerCombo + " (" + candidate.triggerType + ")");

        InterventionHistory history = todaysHistory();
        narrate.line("History: " + history.todaysEvents.size() + " event(s) today, "
                + countIntervened(history.todaysEvents) + " intervened, "
                + history.recentResponsesOldestFirst.size() + " recent response(s)");

        EatingOccasionOutcome outcome = Stage3ReceptivityEvaluator.evaluateReceptivity(candidate, user, history);
        narrate.line("Stage 3: " + outcome + " — " + explain(outcome));

        // Resolved before writing: a nudge that cannot be mapped or delivered must never be recorded as INTERVENED.
        InterventionCategory category = Stage4InterventionMapper.categoryFor(candidate.triggerCombo);
        InterventionFormat format = Stage4InterventionMapper.formatFor(candidate.triggerCombo);
        if (outcome == EatingOccasionOutcome.INTERVENED) {
            if (category == null || format == null) {
                narrate.line("Stage 4: no category/format mapping for " + candidate.triggerCombo
                        + " — nothing to deliver, so this is not an intervention.");
                outcome = EatingOccasionOutcome.NO_INTERVENTION_NOT_NEEDED;
            } else if (!InterventionNotifier.canPost(appContext)) {
                narrate.line("Stage 4: notifications are off — nothing can reach the user, "
                        + "so this is not an intervention.");
                outcome = EatingOccasionOutcome.NO_INTERVENTION_NOT_NEEDED;
            }
        }

        candidate.outcome = outcome;
        long occasionId = eatingOccasionEventDao.insert(candidate);
        narrate.line("Persisted eating_occasion_event id=" + occasionId + " outcome=" + outcome);

        if (outcome == EatingOccasionOutcome.INTERVENED) {
            deliverIntervention(occasionId, candidate, user, category, format, snapshot, narrate);
        }
        narrate.line("=== end ===");
    }

    private SignalSnapshot readSignals(User user) {
        LocalDateTime now = timeSource.now();
        List<EatingOccasionEvent> occasionsToday = occasionsOn(now.toLocalDate());
        Set<String> storedGoalNames = storedGoalNames();
        return SignalSnapshot.builder(now)
                .deliveryAppForeground(deliverySource.wasDeliveryAppForegrounded(
                        DeliveryAppSignalSource.DEFAULT_LOOKBACK_MILLIS))
                .currentZone(locationSource.getCurrentZone())
                .recentlyLeftWorkplace(locationSource.hasRecentlyLeftWorkplace())
                .currentMealWindow(timeSource.resolveCurrentMealWindow(user))
                .hasMissingFoodCategory(FoodCategoryGapAnalyzer.isAnyFoodGroupMissing(db.ingredientDao()
                        .getIngredientsLoggedSince(now.minusDays(FoodCategoryGapAnalyzer.LOOKBACK_DAYS))))
                .weeklyReviewAlreadyDelivered(wasDeliveredIn(occasionsToday, TriggerCombo.WEEKLY_REVIEW))
                .deliveryAppForegroundYesterday(
                        deliverySource.wasDeliveryAppForegrounded(YESTERDAYS_DELIVERY_LOOKBACK_MILLIS))
                .hasFewerDeliveriesGoal(storedGoalNames.contains(OnboardingGoal.FEWER_DELIVERIES.label))
                .hasOrganizeMealsGoal(storedGoalNames.contains(OnboardingGoal.ORGANIZE_MEALS.label))
                .hasBetterChoicesOutGoal(storedGoalNames.contains(OnboardingGoal.BETTER_CHOICES_OUT.label))
                .eatingOutGapAlreadyDeliveredToday(wasDeliveredIn(occasionsToday, TriggerCombo.EATING_OUT_GAP))
                .hasQuickBalancedGoal(storedGoalNames.contains(OnboardingGoal.QUICK_BALANCED.label))
                .dinnerLoggedToday(db.mealDao().countBySlotBetweenSync(MealSlot.DINNER,
                        now.toLocalDate().atStartOfDay(), now.toLocalDate().plusDays(1).atStartOfDay()) > 0)
                .dinnerPlanAnsweredNoRecently(RecentEmaAnswers.latestDinnerPlan(db.momentaryAssessmentEventDao()
                        .getSinceNewestFirstSync(now.minus(RecentEmaAnswers.LOOKBACK)), now) == DinnerPlan.NO)
                .dinnerNoPlanAlreadyDeliveredToday(wasDeliveredIn(occasionsToday, TriggerCombo.DINNER_NO_PLAN))
                .build();
    }

    /** Same day boundary as the daily cap. The DAO returns responses newest first; the downgrade reads oldest first. */
    private InterventionHistory todaysHistory() {
        LocalDate today = LocalDate.now();
        List<UserResponse> responses =
                new ArrayList<>(interventionLogDao.getRecentResponsesSync(today.atStartOfDay(), RECENT_RESPONSE_COUNT));
        Collections.reverse(responses);
        return new InterventionHistory(occasionsOn(today), responses);
    }

    private List<EatingOccasionEvent> occasionsOn(LocalDate day) {
        return eatingOccasionEventDao.getEventsBetweenSync(day.atStartOfDay(), day.plusDays(1).atStartOfDay());
    }

    private Set<String> storedGoalNames() {
        Set<String> names = new HashSet<>();
        for (Goal goal : db.goalDao().getAllGoalsSync()) {
            names.add(goal.name);
        }
        return names;
    }

    private static boolean wasDeliveredIn(List<EatingOccasionEvent> occasions, TriggerCombo combo) {
        for (EatingOccasionEvent occasion : occasions) {
            if (occasion.triggerCombo == combo && occasion.outcome == EatingOccasionOutcome.INTERVENED) {
                return true;
            }
        }
        return false;
    }

    private static int countIntervened(List<EatingOccasionEvent> occasions) {
        int count = 0;
        for (EatingOccasionEvent occasion : occasions) {
            if (occasion.outcome == EatingOccasionOutcome.INTERVENED) {
                count++;
            }
        }
        return count;
    }

    private static String explain(EatingOccasionOutcome outcome) {
        switch (outcome) {
            case INTERVENED:
                return "receptive, within caps, worth saying something";
            case NO_INTERVENTION_CAP:
                return "blocked by the 4h cooldown, the daily cap of 4, or one-per-meal-window";
            case NO_INTERVENTION_NOT_NEEDED:
                return "another trigger already covered this meal window, or this combo has nothing to deliver";
            case NO_INTERVENTION_NOT_RECEPTIVE:
                return "recent nudges were ignored or dismissed";
            default:
                return "missing context (Stage 2 — not implemented yet)";
        }
    }

    private void deliverIntervention(long occasionId, EatingOccasionEvent candidate, User user,
                                     InterventionCategory category, InterventionFormat format,
                                     SignalSnapshot snapshot, Narrator narrate) {
        if (candidate.triggerCombo == TriggerCombo.WORK_DEPARTURE) {
            EmaVariant variant = EmaVariant.forWorkDeparture(snapshot);
            format = variant.format();
            narrate.line("EMA question: " + variant + " (" + format + ")");
        }

        String payload;
        String notificationBody;
        if (format == InterventionFormat.NOTIFICATION_ONLY) {
            payload = notificationOnlyMessage(candidate, user, narrate);
            notificationBody = payload;
        } else {
            payload = format == InterventionFormat.FOOD_SUGGESTION_SCREEN
                    ? suggestionPayload(candidate, user, narrate) : null;
            notificationBody = notificationTeaser(candidate.triggerCombo, format);
        }

        InterventionLog log = new InterventionLog();
        log.creationDate = candidate.creationDate;
        log.eatingOccasionEventId = occasionId;
        log.category = category;
        log.format = format;
        log.userResponse = UserResponse.PENDING;
        log.payload = payload;
        long interventionLogId = interventionLogDao.insert(log);
        narrate.line("Stage 4: " + category + "/" + format
                + ", persisted intervention_log id=" + interventionLogId + " user_response=PENDING");

        InterventionNotifier.notify(appContext, interventionLogId, format, notificationBody);
    }

    private String notificationOnlyMessage(EatingOccasionEvent candidate, User user, Narrator narrate) {
        RecommendationContext recommendationContext =
                RecommendationContext.load(db.ingredientDao(), user, candidate.creationDate);
        narrate.line("Widest gap: " + recommendationContext.rankedGaps.get(0)
                + ", preferences: " + recommendationContext.preferredFoods);
        return candidate.triggerCombo == TriggerCombo.ORGANIZE_MEALS_PREP
                ? SuggestionPayloadBuilder.organizeMealsBodyText(
                        recommendationContext.rankedGaps, recommendationContext.preferredFoods)
                : SuggestionPayloadBuilder.eatingOutBodyText(recommendationContext.rankedGaps);
    }

    /** Dinner-no-plan hands the selector only the light dishes, so every alternative is light too. */
    private String suggestionPayload(EatingOccasionEvent candidate, User user, Narrator narrate) {
        boolean lightDishesOnly = candidate.triggerCombo == TriggerCombo.DINNER_NO_PLAN;
        RecommendationContext recommendationContext =
                RecommendationContext.load(db.ingredientDao(), user, candidate.creationDate);
        Selection selection = recommendationContext.pickDish(appContext,
                lightDishesOnly ? MealRecommendation.LIGHT_CATALOG : MealRecommendation.CATALOG);
        MealRecommendation dish = selection.recommendation;
        narrate.line("Recommendation=" + dish.id + " (gap: " + selection.gapCategory
                + ", widest gap: " + recommendationContext.rankedGaps.get(0)
                + (lightDishesOnly ? ", light dishes only)" : ")"));
        if (lightDishesOnly) {
            return SuggestionPayloadBuilder.buildLightPayload(dish, selection.alternativeIds);
        }
        if (candidate.triggerCombo == TriggerCombo.SUPERMARKET) {
            return SuggestionPayloadBuilder.buildShoppingPayload(
                    dish, recommendationContext.rankedGaps, selection.alternativeIds);
        }
        return SuggestionPayloadBuilder.buildAlternativePayload(dish,
                SuggestionPayloadBuilder.alternativeEyebrowFor(candidate.triggerCombo), selection.alternativeIds);
    }

    private static String notificationTeaser(TriggerCombo combo, InterventionFormat format) {
        if (format == InterventionFormat.FOOD_AT_HOME_EMA) {
            return "Heading home? Anything to eat at home tonight?";
        }
        if (format == InterventionFormat.DINNER_PLAN_EMA) {
            return "Heading home? Have you thought about what you'll eat tonight?";
        }
        switch (combo) {
            case DELIVERY_HOME_EVENING:
                return "Thinking of ordering tonight? Here's one easy way to keep dinner balanced.";
            case SUPERMARKET:
                return "Doing some shopping? Here's one thing that could be useful to have this week.";
            case DINNER_HOME:
                return "Thinking about dinner? A couple of quick questions can help narrow it down.";
            case DINNER_NO_PLAN:
                return "Still no plan for tonight? Here's something light and easy.";
            case WEEKLY_REVIEW:
                return "Here's a quick look at how your week went.";
            case POST_DELIVERY_PREP:
                return "Ordered in yesterday? Anything to eat at home tonight?";
            default:
                return "Here's a quick idea.";
        }
    }

    /** Writes each line to logcat and, when the dev screen is watching, to it as well. */
    private static final class Narrator {

        @Nullable private final EngineRunLog runLog;

        Narrator(@Nullable EngineRunLog runLog) {
            this.runLog = runLog;
        }

        void line(String message) {
            Log.i(TAG, message);
            if (runLog != null) {
                runLog.line(message);
            }
        }
    }
}
