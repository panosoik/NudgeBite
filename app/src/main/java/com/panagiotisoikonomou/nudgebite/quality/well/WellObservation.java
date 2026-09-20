package com.panagiotisoikonomou.nudgebite.quality.well;

import com.panagiotisoikonomou.nudgebite.food.well.WellComponent;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Frequencies divide by days with logs, never by calendar days. */
public final class WellObservation {

    private final Map<WellComponent, Integer> occurrencesByComponent;
    private final int observedDays;
    private final int loggedMeals;
    private final int expectedMealsPerDay;

    public WellObservation(Map<WellComponent, Integer> occurrencesByComponent, int observedDays,
                           int loggedMeals, int expectedMealsPerDay) {
        this.occurrencesByComponent = Collections.unmodifiableMap(new EnumMap<>(occurrencesByComponent));
        this.observedDays = observedDays;
        this.loggedMeals = loggedMeals;
        this.expectedMealsPerDay = expectedMealsPerDay;
    }

    public int occurrencesOf(WellComponent component) {
        return occurrencesByComponent.getOrDefault(component, 0);
    }

    public int observedDays() {
        return observedDays;
    }

    public int loggedMeals() {
        return loggedMeals;
    }

    public double observedMealsPerDay() {
        return observedDays == 0 ? 0.0 : (double) loggedMeals / observedDays;
    }

    public double mealCoverage() {
        if (observedDays == 0) {
            return 0.0;
        }
        return Math.min(1.0, observedMealsPerDay() / expectedMealsPerDay);
    }

    public double ratePerDayOf(WellComponent component) {
        return observedDays == 0 ? 0.0 : (double) occurrencesOf(component) / observedDays;
    }
}
