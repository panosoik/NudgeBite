package com.panagiotisoikonomou.nudgebite.engine.ema;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.enums.DecisionStage;
import com.panagiotisoikonomou.nudgebite.data.enums.DeliveryIntent;
import com.panagiotisoikonomou.nudgebite.data.enums.DinnerPlan;
import com.panagiotisoikonomou.nudgebite.data.enums.FoodAvailability;
import com.panagiotisoikonomou.nudgebite.data.enums.InterventionCategory;
import com.panagiotisoikonomou.nudgebite.data.enums.TriggerCombo;

/** What a completed micro-EMA leads to: nothing, a follow-up notification, or a suggestion screen. */
public enum EmaResolution {

    NOTHING_TO_SUGGEST(null),
    COPING_MESSAGE(InterventionCategory.COPING),
    SHOPPING_REMINDER(InterventionCategory.PLANNING),
    DELIVERY_ALTERNATIVE(InterventionCategory.PLANNING),
    COOK_WITH_WHAT_YOU_HAVE(InterventionCategory.PLANNING),
    PICK_UP_ON_THE_WAY(InterventionCategory.PLANNING),
    ASKED_FOR_IDEA(InterventionCategory.PLANNING);

    @Nullable
    public final InterventionCategory category;

    EmaResolution(@Nullable InterventionCategory category) {
        this.category = category;
    }

    public static EmaResolution resolve(TriggerCombo combo, MicroEmaAnswers answers) {
        if (combo == TriggerCombo.WORK_DEPARTURE) {
            return answers.dinnerPlan != null
                    ? forDinnerPlan(answers.dinnerPlan)
                    : forFoodAtHome(answers.foodAvailableAtHome);
        }
        if (combo == TriggerCombo.POST_DELIVERY_PREP) {
            return answers.foodAvailableAtHome == FoodAvailability.YES ? NOTHING_TO_SUGGEST : SHOPPING_REMINDER;
        }
        return forDinnerAtHome(answers);
    }

    /** Only asking for an idea gets one; "no plan yet" is left for the evening follow-up. */
    private static EmaResolution forDinnerPlan(DinnerPlan dinnerPlan) {
        return dinnerPlan == DinnerPlan.WANTS_RECOMMENDATION ? ASKED_FOR_IDEA : NOTHING_TO_SUGGEST;
    }

    private static EmaResolution forFoodAtHome(FoodAvailability foodAvailableAtHome) {
        if (foodAvailableAtHome == FoodAvailability.YES) {
            return NOTHING_TO_SUGGEST;
        }
        return foodAvailableAtHome == FoodAvailability.NO ? PICK_UP_ON_THE_WAY : COOK_WITH_WHAT_YOU_HAVE;
    }

    /** An order already placed outranks everything; otherwise only someone still considering an order hears from us. */
    private static EmaResolution forDinnerAtHome(MicroEmaAnswers answers) {
        if (answers.mealDecisionStage == DecisionStage.POST_DECISION) {
            return COPING_MESSAGE;
        }
        return answers.deliveryIntent == DeliveryIntent.NONE ? NOTHING_TO_SUGGEST : DELIVERY_ALTERNATIVE;
    }
}
