package com.panagiotisoikonomou.nudgebite.dashboard;

import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.enums.MealSlot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TodaySlots {

    private static final List<MealSlot> DEFAULT_SLOTS =
            Arrays.asList(MealSlot.BREAKFAST, MealSlot.LUNCH, MealSlot.DINNER);

    private TodaySlots() {
    }

    public static List<MealSlot> visibleSlots(User user) {
        if (user == null || declaredNoMainMeal(user)) {
            List<MealSlot> fallback = new ArrayList<>(DEFAULT_SLOTS);
            if (user != null && user.eatsSnacks) {
                fallback.add(MealSlot.SNACK);
            }
            return fallback;
        }
        List<MealSlot> slots = new ArrayList<>();
        if (user.eatsBreakfast) {
            slots.add(MealSlot.BREAKFAST);
        }
        if (user.eatsLunch) {
            slots.add(MealSlot.LUNCH);
        }
        if (user.eatsDinner) {
            slots.add(MealSlot.DINNER);
        }
        if (user.eatsSnacks) {
            slots.add(MealSlot.SNACK);
        }
        return slots;
    }

    /** Snacks-only also falls back to the main meals: WellExpectedMeals cannot score a snacks-only user. */
    private static boolean declaredNoMainMeal(User user) {
        return !user.eatsBreakfast && !user.eatsLunch && !user.eatsDinner;
    }
}
