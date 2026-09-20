package com.panagiotisoikonomou.nudgebite.data.enums;

public enum InterventionCategory {
    PROMPT_REMINDER,
    PLANNING,
    COPING,
    FEEDBACK,

    /** Hydration reminders are logged for the study but excluded from every dispatch rule, cap and score. */
    HYDRATION
}
