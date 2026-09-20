package com.panagiotisoikonomou.nudgebite.ui.onboarding;

public interface OnboardingStep {

    String primaryButtonLabel();

    default boolean isStepValid() {
        return true;
    }

    default void onCommit(OnboardingAnswers answers) {
    }
}
