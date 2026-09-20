package com.panagiotisoikonomou.nudgebite.ui.dashboard;

import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.panagiotisoikonomou.nudgebite.MainActivity;
import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.dashboard.DayContributionEvaluator;
import com.panagiotisoikonomou.nudgebite.dashboard.DayNavigation;
import com.panagiotisoikonomou.nudgebite.dashboard.HomeStatusText;
import com.panagiotisoikonomou.nudgebite.dashboard.TodaySlots;
import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.entity.Goal;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.entity.relation.MealWithIngredients;
import com.panagiotisoikonomou.nudgebite.data.enums.DayContribution;
import com.panagiotisoikonomou.nudgebite.data.enums.MealSlot;
import com.panagiotisoikonomou.nudgebite.data.repository.GoalRepository;
import com.panagiotisoikonomou.nudgebite.data.repository.MealRepository;
import com.panagiotisoikonomou.nudgebite.data.repository.UserRepository;
import com.panagiotisoikonomou.nudgebite.ui.components.MealSlotCard;
import com.panagiotisoikonomou.nudgebite.ui.components.WaterCounterView;
import com.panagiotisoikonomou.nudgebite.water.WaterGoal;
import com.panagiotisoikonomou.nudgebite.water.WaterRepository;
import com.panagiotisoikonomou.nudgebite.ui.logging.MealEditActivity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** A past day is read-only: new meals and glasses are stamped with the current time, so they would land in today. */
public class TodayFragment extends Fragment {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.US);

    @Nullable private User currentUser;
    private List<MealWithIngredients> allMeals = new ArrayList<>();
    private TextView dayTitle;
    private TextView dateText;
    private TextView mealsHeader;
    private TextView statusText;
    private View statusDot;
    private View previousDayButton;
    private View nextDayButton;
    private final Map<MealSlot, MealSlotCard> cardsBySlot = new EnumMap<>(MealSlot.class);
    private View waterSection;
    private WaterCounterView waterCounter;
    @Nullable private LocalDate observedWaterDay;
    @Nullable private LiveData<Integer> waterCountSource;
    private List<Goal> allGoals = new ArrayList<>();

    private LocalDate selectedDay = LocalDate.now();

    private void bindWaterCounter() {
        Context appContext = requireActivity().getApplication();
        waterCounter.setOnGlassChangeListener(new WaterCounterView.OnGlassChangeListener() {
            @Override
            public void onAddGlass() {
                AppDatabase.databaseWriteExecutor.execute(() ->
                        WaterRepository.addGlassSync(appContext, LocalDateTime.now()));
            }

            @Override
            public void onRemoveGlass() {
                AppDatabase.databaseWriteExecutor.execute(() ->
                        WaterRepository.removeLastGlassSync(appContext, LocalDate.now()));
            }
        });
    }

    /** The query's date range is fixed when it is built, so a new day needs a new subscription. */
    private void observeWaterCountFor(LocalDate day) {
        if (day.equals(observedWaterDay)) {
            return;
        }
        if (waterCountSource != null) {
            waterCountSource.removeObservers(getViewLifecycleOwner());
        }
        observedWaterDay = day;
        waterCountSource = WaterRepository.observeGlassesToday(requireActivity().getApplication(), day);
        waterCountSource.observe(getViewLifecycleOwner(), glasses ->
                waterCounter.setGlasses(glasses == null ? 0 : glasses));
    }

    /** Browsing back is transient: resuming always returns to today with logging live. */
    @Override
    public void onResume() {
        super.onResume();
        showDay(LocalDate.now());
    }

    private void refreshWaterVisibility() {
        waterSection.setVisibility(WaterGoal.isSelected(allGoals) ? View.VISIBLE : View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_today, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dayTitle = view.findViewById(R.id.day_title);
        dateText = view.findViewById(R.id.date_text);
        mealsHeader = view.findViewById(R.id.meals_header);
        statusText = view.findViewById(R.id.status_text);
        statusDot = view.findViewById(R.id.status_dot);
        previousDayButton = view.findViewById(R.id.previous_day_button);
        nextDayButton = view.findViewById(R.id.next_day_button);
        cardsBySlot.put(MealSlot.BREAKFAST, view.findViewById(R.id.breakfast_card));
        cardsBySlot.put(MealSlot.LUNCH, view.findViewById(R.id.lunch_card));
        cardsBySlot.put(MealSlot.DINNER, view.findViewById(R.id.dinner_card));
        cardsBySlot.put(MealSlot.SNACK, view.findViewById(R.id.snack_card));
        waterSection = view.findViewById(R.id.water_section);
        waterCounter = view.findViewById(R.id.water_counter);
        bindWaterCounter();
        observedWaterDay = null;
        waterCountSource = null;

        previousDayButton.setOnClickListener(v -> stepDay(-1));
        nextDayButton.setOnClickListener(v -> stepDay(1));

        new MealRepository(requireActivity().getApplication()).getAllMealsWithIngredients().observe(getViewLifecycleOwner(), meals -> {
            allMeals = meals;
            refreshDay();
        });
        new GoalRepository(requireActivity().getApplication()).getAllGoals().observe(getViewLifecycleOwner(), goals -> {
            allGoals = goals == null ? new ArrayList<>() : goals;
            refreshWaterVisibility();
        });
        new UserRepository(requireActivity().getApplication()).getUser().observe(getViewLifecycleOwner(), user -> {
            currentUser = user;
            refreshDay();
        });

        showDay(LocalDate.now());
    }

    private void stepDay(int days) {
        LocalDate today = LocalDate.now();
        boolean allowed = days < 0
                ? DayNavigation.canGoBack(selectedDay, today)
                : DayNavigation.canGoForward(selectedDay, today);
        if (allowed) {
            showDay(selectedDay.plusDays(days));
        }
    }

    private void showDay(LocalDate day) {
        LocalDate today = LocalDate.now();
        selectedDay = DayNavigation.clamp(day, today);
        boolean showingToday = selectedDay.equals(today);

        dayTitle.setText(DayNavigation.titleFor(selectedDay, today));
        dateText.setText(selectedDay.format(DATE_FORMAT));
        mealsHeader.setText(DayNavigation.mealsHeaderFor(selectedDay, today));
        setArrowEnabled(previousDayButton, DayNavigation.canGoBack(selectedDay, today));
        setArrowEnabled(nextDayButton, DayNavigation.canGoForward(selectedDay, today));

        waterCounter.setInteractive(showingToday);
        observeWaterCountFor(selectedDay);

        refreshDay();
    }

    private void setArrowEnabled(View arrow, boolean enabled) {
        arrow.setEnabled(enabled);
        arrow.setAlpha(enabled ? 1f : 0.3f);
    }

    private void refreshDay() {
        if (currentUser == null || dayTitle == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        boolean showingToday = selectedDay.equals(today);

        List<MealWithIngredients> dayMeals = new ArrayList<>();
        Map<MealSlot, List<MealWithIngredients>> bySlot = new EnumMap<>(MealSlot.class);
        for (MealSlot slot : MealSlot.values()) {
            bySlot.put(slot, new ArrayList<>());
        }
        for (MealWithIngredients meal : allMeals) {
            if (selectedDay.equals(meal.meal.loggedAt.toLocalDate())) {
                dayMeals.add(meal);
                bySlot.get(meal.meal.mealSlot).add(meal);
            }
        }

        DayContribution contribution =
                DayContributionEvaluator.contributionOn(allMeals, selectedDay);
        if (showingToday) {
            DayContribution yesterdayContribution = DayContributionEvaluator.contributionOn(allMeals, selectedDay.minusDays(1));
            updateStatus(contribution,
                    HomeStatusText.forDay(contribution, yesterdayContribution, dayMeals.isEmpty()));
        } else {
            updateStatus(contribution, HomeStatusText.forPastDay(contribution, dayMeals.isEmpty()));
        }

        bindSlots(bySlot, showingToday);
    }

    private void bindSlots(Map<MealSlot, List<MealWithIngredients>> bySlot, boolean allowAdding) {
        List<MealSlot> visibleSlots = TodaySlots.visibleSlots(currentUser);
        for (Map.Entry<MealSlot, MealSlotCard> entry : cardsBySlot.entrySet()) {
            MealSlot slot = entry.getKey();
            MealSlotCard card = entry.getValue();
            boolean visible = visibleSlots.contains(slot);
            card.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (visible) {
                card.bind(slot == MealSlot.SNACK ? "Snacks" : slot.label, bySlot.get(slot), this::openExistingMeal,
                        () -> startLogFlow(slot), allowAdding);
            }
        }
    }

    private void updateStatus(DayContribution contribution, String line) {
        boolean positive = contribution == DayContribution.POSITIVE;
        statusDot.setBackgroundResource(positive ? R.drawable.dot_active : R.drawable.dot_inactive);
        int color = ContextCompat.getColor(requireContext(),
                positive ? R.color.nb_primary_green : R.color.nb_text_secondary);
        statusText.setTextColor(color);
        statusText.setText(line);
    }

    private void openExistingMeal(long mealId) {
        Intent intent = new Intent(requireContext(), MealEditActivity.class);
        intent.putExtra(MealEditActivity.EXTRA_MEAL_ID, mealId);
        startActivity(intent);
    }

    private void startLogFlow(MealSlot presetSlot) {
        if (!selectedDay.equals(LocalDate.now())) {
            return;
        }
        ((MainActivity) requireActivity()).startLogMealFlow(presetSlot);
    }
}
