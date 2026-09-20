package com.panagiotisoikonomou.nudgebite.engine.signals;

import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.engine.MealWindow;

import java.time.LocalDateTime;

public interface TimeSignalSource {

    LocalDateTime now();

    MealWindow resolveCurrentMealWindow(User user);
}
