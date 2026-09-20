package com.panagiotisoikonomou.nudgebite.engine.recommendation;

import android.content.Context;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.dao.IngredientDao;
import com.panagiotisoikonomou.nudgebite.data.entity.Ingredient;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.enums.FoodCategory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/** What every recommendation is chosen from: last week's diet gaps and the user's liked and avoided foods. */
public final class RecommendationContext {

    public final List<FoodCategory> rankedGaps;
    public final List<String> preferredFoods;
    private final List<String> foodsToAvoid;

    private RecommendationContext(List<FoodCategory> rankedGaps, List<String> preferredFoods,
                                  List<String> foodsToAvoid) {
        this.rankedGaps = rankedGaps;
        this.preferredFoods = preferredFoods;
        this.foodsToAvoid = foodsToAvoid;
    }

    /** Blocking: call off the main thread. */
    public static RecommendationContext load(IngredientDao ingredientDao, User user, LocalDateTime at) {
        List<Ingredient> loggedLastWeek =
                ingredientDao.getIngredientsLoggedSince(at.minusDays(FoodCategoryGapAnalyzer.LOOKBACK_DAYS));
        return new RecommendationContext(FoodCategoryGapAnalyzer.rankUnderrepresented(loggedLastWeek, user.sex),
                orEmpty(user.preferredFoods), orEmpty(user.foodsToAvoid));
    }

    /** Picks a dish, passing over the ones shown recently, and remembers it as shown. */
    public MealRecommendationSelector.Selection pickDish(Context context, List<MealRecommendation> catalog) {
        RecentRecommendationStore recentlyShown = new RecentRecommendationStore(context);
        MealRecommendationSelector.Selection selection = MealRecommendationSelector.select(
                rankedGaps, preferredFoods, foodsToAvoid, catalog, recentlyShown.recentIds());
        recentlyShown.record(selection.recommendation.id);
        return selection;
    }

    private static List<String> orEmpty(@Nullable List<String> foods) {
        return foods != null ? foods : Collections.emptyList();
    }
}
