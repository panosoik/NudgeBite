package com.panagiotisoikonomou.nudgebite;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.enums.MealSlot;
import com.panagiotisoikonomou.nudgebite.data.enums.UserResponse;
import com.panagiotisoikonomou.nudgebite.data.repository.UserRepository;
import com.panagiotisoikonomou.nudgebite.data.repository.WellScoreRepository;
import com.panagiotisoikonomou.nudgebite.engine.SignalEngineScheduler;
import com.panagiotisoikonomou.nudgebite.location.GeofenceRegistrar;
import com.panagiotisoikonomou.nudgebite.notifications.InterventionNotifier;
import com.panagiotisoikonomou.nudgebite.progress.GoalBootstrap;
import com.panagiotisoikonomou.nudgebite.ui.components.BottomNavBar;
import com.panagiotisoikonomou.nudgebite.ui.dashboard.TodayFragment;
import com.panagiotisoikonomou.nudgebite.ui.logging.MealEditActivity;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.OnboardingActivity;
import com.panagiotisoikonomou.nudgebite.ui.progress.ProgressFragment;
import com.panagiotisoikonomou.nudgebite.ui.settings.SettingsFragment;
import com.panagiotisoikonomou.nudgebite.utils.SystemBarInsets;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String EXTRA_INTERVENTION_LOG_ID = "intervention_log_id";
    private static final String SUB_SCREEN_BACK_STACK = "sub_screen";

    private Uri pendingCameraPhotoUri;
    @Nullable
    private MealSlot pendingPresetSlot;
    private BottomNavBar.Tab currentTab = BottomNavBar.Tab.HOME;
    private BottomNavBar bottomNavBar;

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher;

    public static Intent newIntentForWeeklyReview(Context context, long interventionLogId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_INTERVENTION_LOG_ID, interventionLogId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && pendingCameraPhotoUri != null) {
                openMealEdit(pendingCameraPhotoUri);
            }
        });
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                openMealEdit(uri);
            }
        });

        AppDatabase.databaseWriteExecutor.execute(() ->
                WellScoreRepository.recomputeAll(AppDatabase.getDatabase(getApplication())));

        new UserRepository(getApplication()).getUser().observe(this, user -> {
            if (user == null) {
                startActivity(new Intent(this, OnboardingActivity.class));
                finish();
            } else {
                SignalEngineScheduler.schedulePeriodicChecks(getApplication());
                GeofenceRegistrar.registerAll(getApplication(), user);
                if (bottomNavBar == null) {
                    showDashboard();
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        markInterventionAcceptedIfPresent(intent);
        if (bottomNavBar != null) {
            selectTab(requestedTabFrom(intent));
        }
    }

    private void showDashboard() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        SystemBarInsets.padForSystemBarsAndKeyboard(findViewById(R.id.main));

        GoalBootstrap.reconcileTrackedGoals(getApplication());

        bottomNavBar = findViewById(R.id.bottom_nav_bar);

        bottomNavBar.setOnTabSelectedListener(tab -> {
            if (tab == BottomNavBar.Tab.LOG) {
                bottomNavBar.setSelectedTab(currentTab);
                startLogMealFlow(null);
                return;
            }
            selectTab(tab);
        });

        markInterventionAcceptedIfPresent(getIntent());
        selectTab(requestedTabFrom(getIntent()));
    }

    private void markInterventionAcceptedIfPresent(Intent intent) {
        if (!intent.hasExtra(EXTRA_INTERVENTION_LOG_ID)) {
            return;
        }
        long interventionLogId = intent.getLongExtra(EXTRA_INTERVENTION_LOG_ID, -1);
        InterventionNotifier.cancel(this, interventionLogId);
        AppDatabase db = AppDatabase.getDatabase(getApplication());
        AppDatabase.databaseWriteExecutor.execute(() ->
                db.interventionLogDao().updateResponse(interventionLogId, UserResponse.ACCEPTED));
    }

    /** Only the weekly-review notification asks for a tab: Progress. */
    private static BottomNavBar.Tab requestedTabFrom(Intent intent) {
        return intent.hasExtra(EXTRA_INTERVENTION_LOG_ID) ? BottomNavBar.Tab.PROGRESS : BottomNavBar.Tab.HOME;
    }

    private void selectTab(BottomNavBar.Tab tab) {
        getSupportFragmentManager().popBackStackImmediate(
                SUB_SCREEN_BACK_STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        currentTab = tab;
        bottomNavBar.setSelectedTab(tab);
        getSupportFragmentManager().beginTransaction().replace(R.id.dashboard_content, fragmentFor(tab)).commitNow();
    }

    public void showSubScreen(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboard_content, fragment)
                .addToBackStack(SUB_SCREEN_BACK_STACK)
                .commit();
    }

    private static Fragment fragmentFor(BottomNavBar.Tab tab) {
        switch (tab) {
            case PROGRESS:
                return new ProgressFragment();
            case SETTINGS:
                return new SettingsFragment();
            case HOME:
            default:
                return new TodayFragment();
        }
    }

    public void startLogMealFlow(@Nullable MealSlot presetSlot) {
        pendingPresetSlot = presetSlot;
        new AlertDialog.Builder(this)
                .setTitle("Log a meal")
                .setItems(new CharSequence[]{"Take a photo", "Choose from gallery", "Enter manually"}, (dialog, which) -> {
                    if (which == 0) {
                        launchCamera();
                    } else if (which == 1) {
                        pickImageLauncher.launch(new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build());
                    } else {
                        openMealEdit(null);
                    }
                })
                .show();
    }

    private void launchCamera() {
        File cameraDir = new File(getCacheDir(), "camera");
        if (!cameraDir.exists() && !cameraDir.mkdirs()) {
            return;
        }
        File photoFile = new File(cameraDir, "meal_" + System.currentTimeMillis() + ".jpg");
        pendingCameraPhotoUri = FileProvider.getUriForFile(
                this, getPackageName() + ".fileprovider", photoFile);
        takePictureLauncher.launch(pendingCameraPhotoUri);
    }

    private void openMealEdit(@Nullable Uri imageUri) {
        Intent intent = new Intent(this, MealEditActivity.class);
        if (imageUri != null) {
            intent.putExtra(MealEditActivity.EXTRA_IMAGE_URI, imageUri);
        } else {
            intent.putExtra(MealEditActivity.EXTRA_MANUAL_ENTRY, true);
        }
        if (pendingPresetSlot != null) {
            intent.putExtra(MealEditActivity.EXTRA_PRESET_SLOT, pendingPresetSlot.name());
            pendingPresetSlot = null;
        }
        startActivity(intent);
    }
}
