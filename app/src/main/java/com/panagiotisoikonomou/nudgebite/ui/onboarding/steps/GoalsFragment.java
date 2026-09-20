package com.panagiotisoikonomou.nudgebite.ui.onboarding.steps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.progress.OnboardingGoal;
import com.panagiotisoikonomou.nudgebite.ui.components.MultiSelectChips;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.OnboardingActivity;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.OnboardingAnswers;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.OnboardingStep;

import java.util.List;

public class GoalsFragment extends Fragment implements OnboardingStep {

    private static final List<String> GOALS = OnboardingGoal.allLabels();

    private MultiSelectChips goalsSelect;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OnboardingAnswers answers = ((OnboardingActivity) requireActivity()).getAnswers();

        goalsSelect = view.findViewById(R.id.goals_select);
        goalsSelect.setStyle(MultiSelectChips.Style.FULL_WIDTH);
        goalsSelect.setOptions(GOALS, answers.selectedGoals);
        goalsSelect.setOnSelectionChangedListener(
                () -> ((OnboardingActivity) requireActivity()).refreshNextButtonState());
    }

    @Override
    public String primaryButtonLabel() {
        return "Next";
    }

    @Override
    public boolean isStepValid() {
        return goalsSelect != null && !goalsSelect.getSelectedList().isEmpty();
    }

    @Override
    public void onCommit(OnboardingAnswers answers) {
        answers.selectedGoals = goalsSelect.getSelectedList();
    }
}
