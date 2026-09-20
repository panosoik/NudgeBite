package com.panagiotisoikonomou.nudgebite.engine.ema;

import com.panagiotisoikonomou.nudgebite.data.enums.DecisionStage;
import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.enums.DeliveryIntent;
import com.panagiotisoikonomou.nudgebite.data.enums.DinnerPlan;
import com.panagiotisoikonomou.nudgebite.data.enums.FoodAvailability;
import com.panagiotisoikonomou.nudgebite.data.enums.InputMode;

public final class MicroEmaAnswers {

    public final DeliveryIntent deliveryIntent;
    public final FoodAvailability foodAvailableAtHome;
    public final DecisionStage mealDecisionStage;
    public final InputMode inputMode;
    @Nullable public final DinnerPlan dinnerPlan;

    private MicroEmaAnswers(DeliveryIntent deliveryIntent, FoodAvailability foodAvailableAtHome,
                             DecisionStage mealDecisionStage, InputMode inputMode,
                             @Nullable DinnerPlan dinnerPlan) {
        this.deliveryIntent = deliveryIntent;
        this.foodAvailableAtHome = foodAvailableAtHome;
        this.mealDecisionStage = mealDecisionStage;
        this.inputMode = inputMode;
        this.dinnerPlan = dinnerPlan;
    }

    public static MicroEmaAnswers fromOptions(DeliveryIntent deliveryIntent, FoodAvailability foodAvailableAtHome) {
        DecisionStage stage = deliveryIntent == DeliveryIntent.ALREADY_ORDERED
                ? DecisionStage.POST_DECISION
                : DecisionStage.PRE_DECISION;
        return new MicroEmaAnswers(deliveryIntent, foodAvailableAtHome, stage, InputMode.OPTIONS, null);
    }

    public static MicroEmaAnswers fromFoodAvailabilityOnly(FoodAvailability foodAvailableAtHome) {
        return new MicroEmaAnswers(DeliveryIntent.UNKNOWN, foodAvailableAtHome,
                DecisionStage.PRE_DECISION, InputMode.OPTIONS, null);
    }

    public static MicroEmaAnswers fromDinnerPlan(DinnerPlan dinnerPlan) {
        DecisionStage stage = dinnerPlan == DinnerPlan.YES
                ? DecisionStage.POST_DECISION
                : DecisionStage.PRE_DECISION;
        return new MicroEmaAnswers(DeliveryIntent.UNKNOWN, FoodAvailability.UNKNOWN,
                stage, InputMode.OPTIONS, dinnerPlan);
    }
}
