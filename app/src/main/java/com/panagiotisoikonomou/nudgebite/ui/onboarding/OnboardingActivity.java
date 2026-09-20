package com.panagiotisoikonomou.nudgebite.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.panagiotisoikonomou.nudgebite.MainActivity;
import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.entity.Goal;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.engine.SignalEngineScheduler;
import com.panagiotisoikonomou.nudgebite.location.GeofenceRegistrar;
import com.panagiotisoikonomou.nudgebite.ui.components.PrimaryButton;
import com.panagiotisoikonomou.nudgebite.ui.components.SecondaryButton;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.steps.GoalsFragment;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.steps.LocationFragment;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.steps.MealTimesFragment;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.steps.PermissionsFragment;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.steps.WelcomeFragment;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;
import com.panagiotisoikonomou.nudgebite.utils.SystemBarInsets;

import java.util.List;
import java.util.function.Supplier;

public class OnboardingActivity extends AppCompatActivity {

    private static final List<Supplier<Fragment>> STEPS = List.of(
            WelcomeFragment::new,
            GoalsFragment::new,
            MealTimesFragment::new,
            LocationFragment::new,
            PermissionsFragment::new);

    private final OnboardingAnswers answers = new OnboardingAnswers();
    private int currentIndex = 0;

    private PrimaryButton nextButton;
    private SecondaryButton backButton;
    private LinearLayout dotsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);
        SystemBarInsets.padForSystemBars(findViewById(R.id.onboarding_root));

        nextButton = findViewById(R.id.next_button);
        backButton = findViewById(R.id.back_button);
        dotsContainer = findViewById(R.id.dots_container);

        backButton.setOnClickListener(v -> goToStep(currentIndex - 1));
        nextButton.setOnClickListener(v -> onNextPressed());

        if (savedInstanceState == null) {
            showStep(0);
        } else {
            currentIndex = savedInstanceState.getInt("currentIndex", 0);
            updateFooter();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentIndex", currentIndex);
    }

    public OnboardingAnswers getAnswers() {
        return answers;
    }

    public void refreshNextButtonState() {
        OnboardingStep step = currentStep();
        if (step != null) {
            nextButton.setEnabled(step.isStepValid());
        }
    }

    private void onNextPressed() {
        OnboardingStep step = currentStep();
        if (step == null || !step.isStepValid()) {
            return;
        }
        step.onCommit(answers);
        if (currentIndex == STEPS.size() - 1) {
            finishOnboarding();
        } else {
            goToStep(currentIndex + 1);
        }
    }

    private void goToStep(int index) {
        if (index < 0 || index >= STEPS.size()) {
            return;
        }
        currentIndex = index;
        showStep(index);
    }

    private void showStep(int index) {
        Fragment fragment = STEPS.get(index).get();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.onboarding_content, fragment)
                .commitNow();
        updateFooter();
    }

    private void updateFooter() {
        OnboardingStep step = currentStep();
        if (step == null) {
            return;
        }
        nextButton.setText(step.primaryButtonLabel());
        nextButton.setEnabled(step.isStepValid());
        backButton.setVisibility(currentIndex > 0 ? View.VISIBLE : View.GONE);
        renderDots();
    }

    private void renderDots() {
        dotsContainer.removeAllViews();
        int dotSize = DisplayUtils.dp(this, 8);
        int spacing = DisplayUtils.dp(this, 4);
        for (int i = 0; i < STEPS.size(); i++) {
            ImageView dot = new ImageView(this);
            dot.setImageResource(i == currentIndex ? R.drawable.dot_active : R.drawable.dot_inactive);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
            params.leftMargin = spacing;
            params.rightMargin = spacing;
            dotsContainer.addView(dot, params);
        }
    }

    @Nullable
    private OnboardingStep currentStep() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.onboarding_content);
        return fragment instanceof OnboardingStep ? (OnboardingStep) fragment : null;
    }

    private void finishOnboarding() {
        User user = answers.user;
        List<Goal> goals = answers.toGoalEntities();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplication());
            db.userDao().insert(user);
            db.goalDao().insertAll(goals);
            SignalEngineScheduler.schedulePeriodicChecks(getApplication());
            GeofenceRegistrar.registerAll(getApplication(), user);
            runOnUiThread(this::goToMainActivity);
        });
    }

    private void goToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
