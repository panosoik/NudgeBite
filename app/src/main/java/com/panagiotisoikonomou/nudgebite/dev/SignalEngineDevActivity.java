package com.panagiotisoikonomou.nudgebite.dev;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.panagiotisoikonomou.nudgebite.BuildConfig;
import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.repository.UserRepository;
import com.panagiotisoikonomou.nudgebite.engine.LocationZone;
import com.panagiotisoikonomou.nudgebite.engine.SignalEngine;
import com.panagiotisoikonomou.nudgebite.engine.SignalEngineWorker;
import com.panagiotisoikonomou.nudgebite.engine.signals.MockLocationSignalSource;
import com.panagiotisoikonomou.nudgebite.engine.signals.MockTimeSignalSource;
import com.panagiotisoikonomou.nudgebite.engine.signals.UsageStatsDeliveryAppSignalSource;
import com.panagiotisoikonomou.nudgebite.progress.OnboardingGoal;
import com.panagiotisoikonomou.nudgebite.ui.components.PrimaryButton;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.panagiotisoikonomou.nudgebite.water.WaterReminderWorker;
import com.panagiotisoikonomou.nudgebite.ui.components.SingleSelectList;
import com.panagiotisoikonomou.nudgebite.utils.MealTimeText;
import com.panagiotisoikonomou.nudgebite.utils.SystemBarInsets;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Dev-only trigger for the signal engine with stubbed signals. Finishes immediately in release builds. */
public class SignalEngineDevActivity extends AppCompatActivity {

    private static final Map<LocationZone, String> ZONE_LABELS = new LinkedHashMap<>();

    static {
        ZONE_LABELS.put(LocationZone.HOME, "Home");
        ZONE_LABELS.put(LocationZone.WORK, "Work");
        ZONE_LABELS.put(LocationZone.SUPERMARKET, "Supermarket");
        ZONE_LABELS.put(LocationZone.EATING_PLACE, "Eating place");
        ZONE_LABELS.put(LocationZone.UNKNOWN, "Unknown");
    }

    private static final String TIME_NOW = "Now (real)";
    private static final String TIME_BREAKFAST = "Breakfast";
    private static final String TIME_LUNCH = "Lunch";
    private static final String TIME_DINNER = "Dinner";
    private static final String TIME_WEEKLY_REVIEW = "Weekly review (Sun 19:00)";
    private static final String TIME_ORGANIZE_MEALS_PREP = "Organize-meals prep (Sat 12:00)";
    private static final String TIME_POST_DELIVERY_PREP = "Post-delivery prep (13:00)";
    private static final List<String> TIME_LABELS =
            List.of(TIME_NOW, TIME_BREAKFAST, TIME_LUNCH, TIME_DINNER, TIME_WEEKLY_REVIEW,
                    TIME_ORGANIZE_MEALS_PREP, TIME_POST_DELIVERY_PREP);

    private final MockLocationSignalSource locationSource = new MockLocationSignalSource();
    private final MockTimeSignalSource timeSource = new MockTimeSignalSource();

    private boolean forceDeliveryForeground = false;

    private DayStatusScenario selectedDayStatusScenario = DayStatusScenario.NOTHING_LOGGED;

    private UsageStatsDeliveryAppSignalSource realDeliverySource;
    private SignalEngine signalEngine;

    @Nullable private User currentUser;

    private TextView usageAccessStatus;
    private TextView runOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            finish();
            return;
        }
        setContentView(R.layout.activity_signal_engine_dev);
        SystemBarInsets.padForSystemBars(findViewById(R.id.signal_engine_dev_root));

        realDeliverySource = new UsageStatsDeliveryAppSignalSource(this);
        signalEngine = new SignalEngine(getApplication(),
                withinLastMillis -> forceDeliveryForeground
                        || realDeliverySource.wasDeliveryAppForegrounded(withinLastMillis),
                locationSource,
                timeSource);

        usageAccessStatus = findViewById(R.id.usage_access_status);
        runOutput = findViewById(R.id.run_output);
        SingleSelectList zoneSelect = findViewById(R.id.location_zone_select);
        SwitchCompat leftWorkplaceSwitch = findViewById(R.id.left_workplace_switch);
        SwitchCompat deliveryAppSwitch = findViewById(R.id.delivery_app_switch);
        SingleSelectList timeSelect = findViewById(R.id.time_select);
        PrimaryButton runButton = findViewById(R.id.run_engine_button);

        zoneSelect.setOptions(new ArrayList<>(ZONE_LABELS.values()), ZONE_LABELS.get(LocationZone.HOME));
        zoneSelect.setOnSelectionChangedListener(label -> locationSource.setCurrentZone(zoneFor(label)));

        leftWorkplaceSwitch.setOnCheckedChangeListener(
                (button, checked) -> locationSource.setRecentlyLeftWorkplace(checked));

        deliveryAppSwitch.setOnCheckedChangeListener(
                (button, checked) -> forceDeliveryForeground = checked);

        bindGoalSwitch(R.id.goal_fewer_deliveries_switch, OnboardingGoal.FEWER_DELIVERIES);
        bindGoalSwitch(R.id.goal_organize_meals_switch, OnboardingGoal.ORGANIZE_MEALS);
        bindGoalSwitch(R.id.goal_better_choices_out_switch, OnboardingGoal.BETTER_CHOICES_OUT);
        bindGoalSwitch(R.id.goal_quick_balanced_switch, OnboardingGoal.QUICK_BALANCED);
        bindGoalSwitch(R.id.goal_more_fruit_veg_switch, OnboardingGoal.MORE_FRUIT_VEG);

        timeSelect.setOptions(TIME_LABELS, TIME_NOW);
        timeSelect.setOnSelectionChangedListener(label -> timeSource.setOverride(timeForLabel(label)));

        findViewById(R.id.grant_usage_access_button).setOnClickListener(
                v -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
        runButton.setOnClickListener(v -> runEngine());

        findViewById(R.id.run_via_workmanager_button).setOnClickListener(v -> runEngineViaWorkManager());
        findViewById(R.id.reset_history_button).setOnClickListener(v -> resetEngineHistory());

        bindActionButton(R.id.seed_meals_button, "Sample meals reseeded.", () -> SeedData.reseed(getApplication()));

        bindActionButton(R.id.seed_multi_week_button, "Multi-week progress data seeded.", () -> SeedData.seedMultiWeekProgress(getApplication()));

        bindActionButton(R.id.seed_well_progress_button, "Six weeks of diet quality data seeded.", () -> SeedData.seedWellProgress(getApplication()));

        bindActionButton(R.id.run_water_reminder_button,
                "Hydration check queued — it posts only if a reminder is due.",
                () -> WorkManager.getInstance(getApplicationContext()).enqueue(
                        new OneTimeWorkRequest.Builder(WaterReminderWorker.class).build()));

        List<String> scenarioLabels = new ArrayList<>();
        for (DayStatusScenario scenario : DayStatusScenario.values()) {
            scenarioLabels.add(scenario.label);
        }
        SingleSelectList dayStatusScenarioSelect = findViewById(R.id.day_status_scenario_select);
        dayStatusScenarioSelect.setOptions(scenarioLabels, selectedDayStatusScenario.label);
        dayStatusScenarioSelect.setOnSelectionChangedListener(label -> selectedDayStatusScenario =
                DayStatusScenario.values()[scenarioLabels.indexOf(label)]);

        bindActionButton(R.id.seed_day_status_button, "Day-status scenario seeded.", () -> SeedData.seedDayStatusScenario(getApplication(), selectedDayStatusScenario));

        findViewById(R.id.trigger_delivery_home_evening_button).setOnClickListener(v -> {
            zoneSelect.setSelected(ZONE_LABELS.get(LocationZone.HOME));
            deliveryAppSwitch.setChecked(true);
            timeSelect.setSelected(TIME_DINNER);
            runEngine();
        });

        findViewById(R.id.trigger_supermarket_button).setOnClickListener(v -> {
            leftWorkplaceSwitch.setChecked(false);
            zoneSelect.setSelected(ZONE_LABELS.get(LocationZone.SUPERMARKET));
            runEngine();
        });

        findViewById(R.id.trigger_dinner_no_plan_button).setOnClickListener(v -> {
            leftWorkplaceSwitch.setChecked(false);
            deliveryAppSwitch.setChecked(false);
            zoneSelect.setSelected(ZONE_LABELS.get(LocationZone.HOME));
            timeSelect.setSelected(TIME_DINNER);
            runEngine();
        });

        new UserRepository(getApplication()).getUser().observe(this, user -> currentUser = user);
    }

    private void bindActionButton(int buttonId, String doneMessage, Runnable action) {
        findViewById(buttonId).setOnClickListener(v -> {
            action.run();
            Toast.makeText(this, doneMessage, Toast.LENGTH_SHORT).show();
        });
    }

    private void bindGoalSwitch(int switchId, OnboardingGoal goal) {
        SwitchCompat goalSwitch = findViewById(switchId);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean stored = GoalStub.isSet(getApplication(), goal);
            runOnUiThread(() -> {
                goalSwitch.setChecked(stored);
                goalSwitch.setOnCheckedChangeListener(
                        (button, checked) -> GoalStub.setGoal(getApplication(), goal, checked));
            });
        });
    }

    @Nullable
    private LocalDateTime timeForLabel(String label) {
        LocalDate today = LocalDate.now();
        switch (label) {
            case TIME_BREAKFAST:
                return today.atTime(mealTimeOrDefault(currentUser != null ? currentUser.breakfastTimeOfDay : null, 8));
            case TIME_LUNCH:
                return today.atTime(mealTimeOrDefault(currentUser != null ? currentUser.lunchTimeOfDay : null, 13));
            case TIME_DINNER:
                return today.atTime(mealTimeOrDefault(currentUser != null ? currentUser.dinnerTimeOfDay : null, 19));
            case TIME_WEEKLY_REVIEW:
                return today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(19, 0);
            case TIME_ORGANIZE_MEALS_PREP:
                return today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).atTime(12, 0);
            case TIME_POST_DELIVERY_PREP:
                return today.atTime(13, 0);
            default:
                return null;
        }
    }

    private static LocalTime mealTimeOrDefault(@Nullable String stored, int fallbackHour) {
        LocalTime storedTime = MealTimeText.toLocalTimeOrNull(stored);
        return storedTime != null ? storedTime : LocalTime.of(fallbackHour, 0);
    }

    private void resetEngineHistory() {
        AppDatabase db = AppDatabase.getDatabase(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.interventionLogDao().deleteAll();
            db.eatingOccasionEventDao().deleteAll();
            runOnUiThread(() -> Toast.makeText(this, "Engine history cleared.", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUsageAccessStatus();
    }

    private void refreshUsageAccessStatus() {
        boolean granted = realDeliverySource.hasUsageAccess();
        usageAccessStatus.setText(granted
                ? "Usage access: GRANTED — the delivery signal is live (or the stub above, if on)."
                : "Usage access: NOT GRANTED — the delivery signal reads false unless the stub above is on.");
    }

    private void runEngine() {
        if (currentUser == null) {
            Toast.makeText(this, "No user yet — finish onboarding first.", Toast.LENGTH_SHORT).show();
            return;
        }
        runOutput.setText("");
        signalEngine.runOnce(currentUser, message -> runOnUiThread(() -> appendLine(message)));
    }

    private void runEngineViaWorkManager() {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SignalEngineWorker.class).build();
        WorkManager workManager = WorkManager.getInstance(this);
        workManager.enqueue(request);
        appendLine("--- enqueued one-time WorkManager run (real signals, not the stub above) ---");
        workManager.getWorkInfoByIdLiveData(request.getId()).observe(this, info -> {
            if (info != null && info.getState().isFinished()) {
                appendLine("WorkManager run finished: " + info.getState());
            }
        });
    }

    private void appendLine(String message) {
        CharSequence existing = runOutput.getText();
        runOutput.setText(existing.length() == 0 ? message : existing + "\n" + message);
    }

    private static LocationZone zoneFor(String label) {
        for (Map.Entry<LocationZone, String> entry : ZONE_LABELS.entrySet()) {
            if (entry.getValue().equals(label)) {
                return entry.getKey();
            }
        }
        return LocationZone.UNKNOWN;
    }
}
