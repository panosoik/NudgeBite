package com.panagiotisoikonomou.nudgebite.ui.onboarding.steps;

import android.app.TimePickerDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.utils.MealTimeText;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.OnboardingActivity;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.OnboardingAnswers;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.OnboardingStep;

import java.util.ArrayList;
import java.util.List;

public class MealTimesFragment extends Fragment implements OnboardingStep {

    private static final String DEFAULT_BREAKFAST = "07:30";
    private static final String DEFAULT_LUNCH = "12:30";
    private static final String DEFAULT_DINNER = "19:00";

    private final List<MealRow> rows = new ArrayList<>();

    private final class MealRow {
        private final View row;
        private final TextView label;
        @Nullable private final TextView timeView;
        private boolean eaten;
        @Nullable private String time24h;

        MealRow(View parent, int rowId, int labelId, int timeId, boolean eaten, @Nullable String time24h) {
            this.row = parent.findViewById(rowId);
            this.label = parent.findViewById(labelId);
            this.timeView = timeId == 0 ? null : parent.findViewById(timeId);
            this.eaten = eaten;
            this.time24h = time24h;

            row.setOnClickListener(v -> setEaten(!this.eaten));
            if (timeView != null) {
                timeView.setOnClickListener(v -> openTimePicker());
            }
            render();
        }

        private void setEaten(boolean eaten) {
            this.eaten = eaten;
            render();
            ((OnboardingActivity) requireActivity()).refreshNextButtonState();
        }

        private void openTimePicker() {
            int[] parsed = MealTimeText.parseOrNoon(time24h);
            new TimePickerDialog(requireContext(), (picker, hourOfDay, minute) -> {
                time24h = MealTimeText.toStored(hourOfDay, minute);
                render();
            }, parsed[0], parsed[1], false).show();
        }

        private void render() {
            row.setBackgroundResource(eaten
                    ? R.drawable.bg_chip_row_selected : R.drawable.bg_chip_row_unselected);
            label.setTypeface(Typeface.create(Typeface.DEFAULT, eaten ? Typeface.BOLD : Typeface.NORMAL));
            if (timeView != null) {
                timeView.setVisibility(eaten ? View.VISIBLE : View.GONE);
                timeView.setText(MealTimeText.display(time24h));
            }
        }

        @Nullable
        String committedTime() {
            return eaten ? time24h : null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal_times, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OnboardingAnswers answers = ((OnboardingActivity) requireActivity()).getAnswers();

        rows.clear();
        rows.add(new MealRow(view, R.id.breakfast_row, R.id.breakfast_label, R.id.breakfast_time,
                answers.user.eatsBreakfast, orDefault(answers.user.breakfastTimeOfDay, DEFAULT_BREAKFAST)));
        rows.add(new MealRow(view, R.id.lunch_row, R.id.lunch_label, R.id.lunch_time,
                answers.user.eatsLunch, orDefault(answers.user.lunchTimeOfDay, DEFAULT_LUNCH)));
        rows.add(new MealRow(view, R.id.dinner_row, R.id.dinner_label, R.id.dinner_time,
                answers.user.eatsDinner, orDefault(answers.user.dinnerTimeOfDay, DEFAULT_DINNER)));
        rows.add(new MealRow(view, R.id.snacks_row, R.id.snacks_label, 0,
                answers.user.eatsSnacks, null));
    }

    private static String orDefault(@Nullable String stored, String fallback) {
        return stored != null ? stored : fallback;
    }

    @Override
    public String primaryButtonLabel() {
        return "Next";
    }

    @Override
    public boolean isStepValid() {
        for (MealRow row : rows) {
            if (row.eaten) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCommit(OnboardingAnswers answers) {
        answers.user.eatsBreakfast = rows.get(0).eaten;
        answers.user.eatsLunch = rows.get(1).eaten;
        answers.user.eatsDinner = rows.get(2).eaten;
        answers.user.eatsSnacks = rows.get(3).eaten;
        answers.user.breakfastTimeOfDay = rows.get(0).committedTime();
        answers.user.lunchTimeOfDay = rows.get(1).committedTime();
        answers.user.dinnerTimeOfDay = rows.get(2).committedTime();
    }
}
