package com.panagiotisoikonomou.nudgebite.logging;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.dashboard.TodaySlots;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.enums.MealSlot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LoggableSlots {

    private static final List<MealSlot> ORDER =
            Arrays.asList(MealSlot.BREAKFAST, MealSlot.LUNCH, MealSlot.DINNER, MealSlot.SNACK);

    private LoggableSlots() {
    }

    public static List<MealSlot> allowedSlots(@Nullable User user, @Nullable MealSlot editingSlot) {
        List<MealSlot> declared = TodaySlots.visibleSlots(user);
        List<MealSlot> allowed = new ArrayList<>();
        for (MealSlot slot : ORDER) {
            if (declared.contains(slot) || slot == editingSlot) {
                allowed.add(slot);
            }
        }
        return allowed;
    }

    public static MealSlot nearestAllowed(MealSlot preferred, List<MealSlot> allowed) {
        if (allowed.contains(preferred)) {
            return preferred;
        }
        MealSlot best = allowed.get(0);
        int bestDistance = Integer.MAX_VALUE;
        for (MealSlot slot : allowed) {
            int distance = Math.abs(ORDER.indexOf(slot) - ORDER.indexOf(preferred));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = slot;
            }
        }
        return best;
    }
}
