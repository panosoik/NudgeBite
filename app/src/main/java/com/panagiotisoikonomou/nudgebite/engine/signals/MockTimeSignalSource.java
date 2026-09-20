package com.panagiotisoikonomou.nudgebite.engine.signals;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.engine.MealWindow;
import com.panagiotisoikonomou.nudgebite.engine.MealWindowResolver;

import java.time.LocalDateTime;

public class MockTimeSignalSource implements TimeSignalSource {

    @Nullable private LocalDateTime override;

    public void setOverride(@Nullable LocalDateTime override) {
        this.override = override;
    }

    @Override
    public LocalDateTime now() {
        return override != null ? override : LocalDateTime.now();
    }

    @Override
    public MealWindow resolveCurrentMealWindow(User user) {
        return MealWindowResolver.resolveMealWindow(user, now());
    }
}
