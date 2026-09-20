package com.panagiotisoikonomou.nudgebite.engine.signals;

import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.engine.MealWindow;
import com.panagiotisoikonomou.nudgebite.engine.MealWindowResolver;

import java.time.LocalDateTime;

public class SystemTimeSignalSource implements TimeSignalSource {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    @Override
    public MealWindow resolveCurrentMealWindow(User user) {
        return MealWindowResolver.resolveMealWindow(user, now());
    }
}
