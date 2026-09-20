package com.panagiotisoikonomou.nudgebite.ui.settings;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.panagiotisoikonomou.nudgebite.MainActivity;
import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.entity.Goal;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.enums.ActivityLevel;
import com.panagiotisoikonomou.nudgebite.data.enums.Sex;
import com.panagiotisoikonomou.nudgebite.data.repository.GoalRepository;
import com.panagiotisoikonomou.nudgebite.data.repository.UserRepository;
import com.panagiotisoikonomou.nudgebite.export.UsageDataExporter;
import com.panagiotisoikonomou.nudgebite.location.GeofenceRegistrar;
import com.panagiotisoikonomou.nudgebite.progress.TrackableGoalThresholds;
import com.panagiotisoikonomou.nudgebite.ui.components.NumberEntry;
import com.panagiotisoikonomou.nudgebite.ui.components.SingleSelectList;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.AddressPickerBottomSheet;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.SavedPlace;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;
import com.panagiotisoikonomou.nudgebite.utils.MealTimeText;
import com.panagiotisoikonomou.nudgebite.water.WaterGoal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/** Everything that tailors the app without being needed to run it, each row opening its own editor. */
public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private static final String NOT_SET = "Not set";


    private UserRepository userRepository;
    @Nullable private User currentUser;
    private List<Goal> allGoals = new ArrayList<>();
    private boolean exportInProgress;

    private LinearLayout profileSection;
    private LinearLayout placesSection;
    private LinearLayout foodSection;
    private LinearLayout goalThresholdsSection;
    private LinearLayout mealTimesSection;

    /** One tappable settings row: a label, its current value, and what tapping it opens. */
    private static final class Row {
        final String label;
        final String value;
        final View.OnClickListener onClick;

        Row(String label, String value, View.OnClickListener onClick) {
            this.label = label;
            this.value = value;
            this.onClick = onClick;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userRepository = new UserRepository(requireActivity().getApplication());
        profileSection = view.findViewById(R.id.profile_container);
        placesSection = view.findViewById(R.id.places_container);
        foodSection = view.findViewById(R.id.food_container);
        goalThresholdsSection = view.findViewById(R.id.goals_thresholds_container);
        mealTimesSection = view.findViewById(R.id.meal_times_container);
        // Not gated on the profile: an export must work even when the profile never loaded.
        showRows(view.findViewById(R.id.data_container),
                Collections.singletonList(new Row("Export data", "CSV + JSON", v -> exportAndShare())));

        getChildFragmentManager().setFragmentResultListener(AddressPickerBottomSheet.RESULT_KEY,
                getViewLifecycleOwner(), (requestKey, result) -> savePickedPlace(result));
        userRepository.getUser().observe(getViewLifecycleOwner(), user -> {
            currentUser = user;
            showSections();
        });
        new GoalRepository(requireActivity().getApplication()).getAllGoals().observe(getViewLifecycleOwner(), goals -> {
            allGoals = goals == null ? new ArrayList<>() : goals;
            showSections();
        });
    }

    private void showSections() {
        User user = currentUser;
        if (user == null) {
            return;
        }
        showRows(profileSection, Arrays.asList(
                new Row("Sex", user.sex != null ? user.sex.label : NOT_SET, v -> editChoice("Sex",
                        Sex.values(), user.sex, sex -> sex.label, sex -> user.sex = sex)),
                // Age is a primitive column, so a cleared field is stored as zero.
                new Row("Age", user.age > 0 ? String.valueOf(user.age) : NOT_SET, v -> editNumber("Age", "e.g. 30",
                        "years", 10, 100, user.age == 0 ? null : user.age, age -> user.age = age == null ? 0 : age)),
                new Row("Weight", withUnit(user.weight, "kg"), v -> editNumber("Weight", "e.g. 65", "kg", 20, 300,
                        user.weight, weight -> user.weight = weight)),
                new Row("Height", withUnit(user.height, "cm"), v -> editNumber("Height", "e.g. 170", "cm", 50, 250,
                        user.height, height -> user.height = height)),
                new Row("Activity level", user.activityLevel != null ? user.activityLevel.label : NOT_SET,
                        v -> editChoice("Activity level", ActivityLevel.values(), user.activityLevel,
                                level -> level.label, level -> user.activityLevel = level))));

        showRows(placesSection, Arrays.asList(
                placeRow("Home", SavedPlace.HOME, user),
                placeRow("Work / school", SavedPlace.WORK, user),
                placeRow("Usual supermarket", SavedPlace.SUPERMARKET, user),
                placeRow("Usual eating place", SavedPlace.EATING_PLACE, user)));

        showRows(foodSection, Arrays.asList(
                new Row("Food preferences", countOf(user.preferredFoods),
                        v -> ((MainActivity) requireActivity()).showSubScreen(new FoodPreferencesFragment())),
                new Row("Foods to avoid", countOf(user.foodsToAvoid),
                        v -> ((MainActivity) requireActivity()).showSubScreen(new FoodsToAvoidFragment()))));

        int vegTarget = user.vegServingsGoalPerDay != null
                ? user.vegServingsGoalPerDay : (int) TrackableGoalThresholds.VEG_TARGET_SERVINGS_PER_DAY;
        int deliveryTarget = user.deliveryAcceptablePerWeek != null
                ? user.deliveryAcceptablePerWeek : TrackableGoalThresholds.DELIVERY_ACCEPTABLE_PER_WEEK;
        List<Row> goalRows = new ArrayList<>(Arrays.asList(
                new Row("Veg servings / day", "≥ " + vegTarget, v -> editNumber("Veg servings / day goal",
                        "e.g. 2", "servings", 0, 10, vegTarget, value -> user.vegServingsGoalPerDay = value)),
                new Row("Deliveries / fast food", "≤ " + deliveryTarget, v -> editNumber("Deliveries / fast food goal",
                        "e.g. 1", "per week", 0, 14, deliveryTarget, value -> user.deliveryAcceptablePerWeek = value))));
        if (WaterGoal.isSelected(allGoals)) {
            int waterTarget = WaterGoal.targetFor(user);
            goalRows.add(new Row("Water glasses / day", "≥ " + waterTarget, v -> editNumber("Water glasses / day goal",
                    "e.g. 8", "glasses", WaterGoal.MIN_GLASSES_PER_DAY, WaterGoal.MAX_GLASSES_PER_DAY, waterTarget,
                    value -> user.waterGlassesGoalPerDay = value)));
        }
        showRows(goalThresholdsSection, goalRows);

        List<Row> mealTimeRows = new ArrayList<>();
        if (user.eatsBreakfast) {
            mealTimeRows.add(mealTimeRow("Breakfast", user.breakfastTimeOfDay, time -> user.breakfastTimeOfDay = time));
        }
        if (user.eatsLunch) {
            mealTimeRows.add(mealTimeRow("Lunch", user.lunchTimeOfDay, time -> user.lunchTimeOfDay = time));
        }
        if (user.eatsDinner) {
            mealTimeRows.add(mealTimeRow("Dinner", user.dinnerTimeOfDay, time -> user.dinnerTimeOfDay = time));
        }
        showRows(mealTimesSection, mealTimeRows);
    }

    private Row placeRow(String label, SavedPlace place, User user) {
        String value = place.isSetOn(user) ? place.addressOf(user) : NOT_SET;
        return new Row(label, value, v -> AddressPickerBottomSheet.show(this, place, place.addressOf(user)));
    }

    private Row mealTimeRow(String label, @Nullable String storedTime, Consumer<String> setTime) {
        return new Row(label, MealTimeText.display(storedTime), v -> {
            int[] hourAndMinute = MealTimeText.parseOrNoon(storedTime);
            new TimePickerDialog(requireContext(), (picker, hour, minute) -> {
                setTime.accept(MealTimeText.toStored(hour, minute));
                saveUser();
            }, hourAndMinute[0], hourAndMinute[1], false).show();
        });
    }

    private static String withUnit(@Nullable Integer value, String unit) {
        return value != null ? value + " " + unit : NOT_SET;
    }

    private static String countOf(@Nullable List<String> values) {
        return values == null || values.isEmpty() ? "None" : String.valueOf(values.size());
    }

    /** Re-register geofences so the old address stops triggering nudges. */
    private void savePickedPlace(Bundle result) {
        User user = currentUser;
        if (user == null) {
            return;
        }
        AddressPickerBottomSheet.saveResultTo(user, result);
        saveUser();
        GeofenceRegistrar.registerAll(requireContext().getApplicationContext(), user);
    }

    private void editNumber(String title, String hint, String unit, int min, int max,
                            @Nullable Integer currentValue, Consumer<Integer> setValue) {
        NumberEntry entry = new NumberEntry(requireContext());
        entry.setLabel(title);
        entry.setHint(hint);
        entry.setUnit(unit);
        entry.setRange(min, max);
        entry.setValue(currentValue);
        showEditDialog(title, entry, () -> {
            if (entry.isValid()) {
                setValue.accept(entry.getValue());
                saveUser();
            }
        });
    }

    private <T> void editChoice(String title, T[] choices, @Nullable T current, Function<T, String> labelOf,
                                Consumer<T> setChoice) {
        List<String> labels = new ArrayList<>();
        for (T choice : choices) {
            labels.add(labelOf.apply(choice));
        }
        SingleSelectList select = new SingleSelectList(requireContext());
        select.setOptions(labels, current != null ? labelOf.apply(current) : null);
        showEditDialog(title, select, () -> {
            int selectedIndex = labels.indexOf(select.getSelected());
            if (selectedIndex >= 0) {
                setChoice.accept(choices[selectedIndex]);
                saveUser();
            }
        });
    }

    private void showEditDialog(String title, View editor, Runnable onSave) {
        FrameLayout padded = new FrameLayout(requireContext());
        int padding = DisplayUtils.dp(requireContext(), 24);
        padded.setPadding(padding, padding, padding, 0);
        padded.addView(editor);
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(padded)
                .setPositiveButton("Save", (dialog, which) -> onSave.run())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveUser() {
        userRepository.updateUser(currentUser);
        showSections();
    }

    /** The button is latched while the archive is built, so a second tap cannot start a second export. */
    private void exportAndShare() {
        if (exportInProgress) {
            return;
        }
        exportInProgress = true;
        Toast.makeText(requireContext(), "Preparing your data\u2026", Toast.LENGTH_SHORT).show();
        Context appContext = requireContext().getApplicationContext();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            File archive = null;
            String failure = null;
            try {
                archive = UsageDataExporter.export(appContext);
            } catch (Exception e) {
                failure = e.getMessage();
                Log.e(TAG, "Usage data export failed", e);
            }
            File finishedArchive = archive;
            String failureMessage = failure;
            if (!isAdded()) {
                exportInProgress = false;
                return;
            }
            requireActivity().runOnUiThread(() -> {
                exportInProgress = false;
                if (!isAdded()) {
                    return;
                }
                if (finishedArchive == null) {
                    Toast.makeText(requireContext(), "Could not prepare the export"
                            + (failureMessage == null ? "" : ": " + failureMessage), Toast.LENGTH_LONG).show();
                } else {
                    shareArchive(finishedArchive);
                }
            });
        });
    }

    private void shareArchive(File archive) {
        Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider",
                archive);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/zip");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(Intent.EXTRA_SUBJECT, archive.getName());
        // The receiving app cannot read our cache directory without this grant.
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Send your NudgeBite data"));
    }

    /** Rows with a thin divider between each pair. */
    private void showRows(LinearLayout section, List<Row> rows) {
        section.removeAllViews();
        Context context = section.getContext();
        for (int i = 0; i < rows.size(); i++) {
            section.addView(rowView(context, rows.get(i)), new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            if (i < rows.size() - 1) {
                View divider = new View(context);
                divider.setBackgroundColor(ContextCompat.getColor(context, R.color.nb_neutral_border));
                section.addView(divider, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, DisplayUtils.dp(context, 1)));
            }
        }
    }

    /** The label keeps its natural width and the value takes what is left, so a long address never hides the label. */
    private static View rowView(Context context, Row row) {
        LinearLayout rowLayout = new LinearLayout(context);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(Gravity.CENTER_VERTICAL);
        rowLayout.setClickable(true);
        rowLayout.setFocusable(true);
        int verticalPadding = DisplayUtils.dp(context, 16);
        rowLayout.setPadding(0, verticalPadding, 0, verticalPadding);
        rowLayout.setOnClickListener(row.onClick);

        TextView label = rowText(context, row.label, R.color.nb_text_primary, 15);
        rowLayout.addView(label, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView value = rowText(context, row.value, R.color.nb_text_secondary, 15);
        value.setGravity(Gravity.END);
        value.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams valueParams =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        valueParams.leftMargin = DisplayUtils.dp(context, 12);
        rowLayout.addView(value, valueParams);

        LinearLayout.LayoutParams chevronParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        chevronParams.leftMargin = DisplayUtils.dp(context, 8);
        rowLayout.addView(rowText(context, "›", R.color.nb_text_secondary, 17), chevronParams);
        return rowLayout;
    }

    private static TextView rowText(Context context, String text, int colorRes, int textSizeSp) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(context, colorRes));
        view.setTextSize(textSizeSp);
        view.setMaxLines(1);
        return view;
    }
}
