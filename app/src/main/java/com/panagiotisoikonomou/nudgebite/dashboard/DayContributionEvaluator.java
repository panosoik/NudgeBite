package com.panagiotisoikonomou.nudgebite.dashboard;

import com.panagiotisoikonomou.nudgebite.data.entity.relation.MealWithIngredients;
import com.panagiotisoikonomou.nudgebite.data.enums.DayContribution;
import com.panagiotisoikonomou.nudgebite.quality.GreenDotCalibration;
import com.panagiotisoikonomou.nudgebite.quality.well.WellDailyAdequacy;

import java.time.LocalDate;
import java.util.List;

public final class DayContributionEvaluator {

    private DayContributionEvaluator() {
    }

    public static DayContribution contributionOn(List<MealWithIngredients> allMeals, LocalDate day) {
        return WellDailyAdequacy.countOn(allMeals, day) >= GreenDotCalibration.MIN_DISTINCT_WELL_ADEQUACY_GROUPS
                ? DayContribution.POSITIVE
                : DayContribution.NEUTRAL;
    }
}
