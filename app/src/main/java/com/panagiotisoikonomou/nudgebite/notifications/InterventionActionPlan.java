package com.panagiotisoikonomou.nudgebite.notifications;

import com.panagiotisoikonomou.nudgebite.data.enums.InterventionFormat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** The buttons each intervention format shows. Hydration is not here: its notifier owns its own buttons. */
public final class InterventionActionPlan {

    private InterventionActionPlan() {
    }

    public static List<String> answerLabels(InterventionFormat format) {
        if (format == InterventionFormat.FOOD_AT_HOME_EMA) {
            return Arrays.asList("Yes", "Some", "Not really");
        }
        if (format == InterventionFormat.DINNER_PLAN_EMA) {
            return Arrays.asList("No", "Suggest one", "Yes");
        }
        return Collections.emptyList();
    }

    public static boolean isAnsweredOnTheNotification(InterventionFormat format) {
        return !answerLabels(format).isEmpty();
    }

    public static boolean opensScreen(InterventionFormat format) {
        return format != InterventionFormat.NOTIFICATION_ONLY
                && format != InterventionFormat.NOTIFICATION_EMA
                && !isAnsweredOnTheNotification(format);
    }

    public static String primaryLabel(InterventionFormat format) {
        if (format == InterventionFormat.NOTIFICATION_EMA) {
            return "Yes";
        }
        return opensScreen(format) ? "Show me" : "Okay";
    }

    public static String secondaryLabel(InterventionFormat format) {
        if (format == InterventionFormat.FOOD_SUGGESTION_SCREEN) {
            return "Not now";
        }
        if (format == InterventionFormat.NOTIFICATION_ONLY) {
            return "Ignore";
        }
        if (format == InterventionFormat.NOTIFICATION_EMA) {
            return "Not really";
        }
        return "Skip";
    }
}
