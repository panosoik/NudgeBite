package com.panagiotisoikonomou.nudgebite.engine.ema;

import com.panagiotisoikonomou.nudgebite.data.enums.InterventionFormat;
import com.panagiotisoikonomou.nudgebite.engine.SignalSnapshot;

public enum EmaVariant {

    FOOD_AVAILABILITY(InterventionFormat.FOOD_AT_HOME_EMA),

    DINNER_PLAN(InterventionFormat.DINNER_PLAN_EMA);

    private final InterventionFormat format;

    EmaVariant(InterventionFormat format) {
        this.format = format;
    }

    public InterventionFormat format() {
        return format;
    }

    public static EmaVariant forWorkDeparture(SignalSnapshot snapshot) {
        boolean opted = snapshot.hasQuickBalancedGoal || snapshot.hasFewerDeliveriesGoal;
        return opted && !snapshot.dinnerLoggedToday ? DINNER_PLAN : FOOD_AVAILABILITY;
    }
}
