package com.panagiotisoikonomou.nudgebite.engine.recommendation;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.enums.FoodCategory;
import com.panagiotisoikonomou.nudgebite.food.FoodItem;
import com.panagiotisoikonomou.nudgebite.food.FoodTaxonomy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Avoided foods are excluded, preferred foods only break ties; neither overrides the gap ranking. */
public final class MealRecommendationSelector {

    public static final class Selection {
        public final MealRecommendation recommendation;
        @Nullable public final FoodCategory gapCategory;
        /** Every dish the same gap could show, in order; the first is the recommendation itself. */
        public final List<String> alternativeIds;

        private Selection(MealRecommendation recommendation, @Nullable FoodCategory gapCategory,
                          List<String> alternativeIds) {
            this.recommendation = recommendation;
            this.gapCategory = gapCategory;
            this.alternativeIds = alternativeIds;
        }
    }

    private MealRecommendationSelector() {
    }

    /** Recently shown dishes move to the back rather than out, so "Show another" can still reach them. */
    public static Selection select(List<FoodCategory> rankedGaps, List<String> preferredFoods,
                                   List<String> foodsToAvoid, List<MealRecommendation> catalog,
                                   List<String> recentlyShownIds) {
        for (FoodCategory gap : rankedGaps) {
            List<MealRecommendation> candidates = candidatesForCategory(gap, preferredFoods, foodsToAvoid, catalog);
            List<MealRecommendation> unseenFirst = new ArrayList<>();
            for (MealRecommendation candidate : candidates) {
                if (!recentlyShownIds.contains(candidate.id)) {
                    unseenFirst.add(candidate);
                }
            }
            if (unseenFirst.isEmpty()) {
                continue;
            }
            for (MealRecommendation candidate : candidates) {
                if (recentlyShownIds.contains(candidate.id)) {
                    unseenFirst.add(candidate);
                }
            }
            return new Selection(unseenFirst.get(0), gap, idsOf(unseenFirst));
        }
        for (FoodCategory gap : rankedGaps) {
            List<MealRecommendation> candidates = candidatesForCategory(gap, preferredFoods, foodsToAvoid, catalog);
            if (!candidates.isEmpty()) {
                return new Selection(candidates.get(0), gap, idsOf(candidates));
            }
        }
        for (MealRecommendation dish : catalog) {
            if (!mentionsAnyFood(dish, foodsToAvoid)) {
                return new Selection(dish, null, Collections.emptyList());
            }
        }
        return new Selection(catalog.get(0), null, Collections.emptyList());
    }

    /** Dishes rich in the category, minus avoided ones, preferred ones first, each group in catalog order. */
    static List<MealRecommendation> candidatesForCategory(FoodCategory category, List<String> preferredFoods,
                                                          List<String> foodsToAvoid,
                                                          List<MealRecommendation> catalog) {
        List<MealRecommendation> preferred = new ArrayList<>();
        List<MealRecommendation> others = new ArrayList<>();
        for (MealRecommendation dish : catalog) {
            if (!dish.richIn.contains(category) || mentionsAnyFood(dish, foodsToAvoid)) {
                continue;
            }
            (mentionsAnyFood(dish, preferredFoods) ? preferred : others).add(dish);
        }
        preferred.addAll(others);
        return preferred;
    }

    private static List<String> idsOf(List<MealRecommendation> dishes) {
        List<String> ids = new ArrayList<>();
        for (MealRecommendation dish : dishes) {
            ids.add(dish.id);
        }
        return ids;
    }

    /** Taxonomy foods match by identity; user-typed foods fall back to substring matching on keywords. */
    private static boolean mentionsAnyFood(MealRecommendation dish, List<String> foods) {
        for (String food : foods) {
            if (food == null || food.trim().isEmpty()) {
                continue;
            }
            FoodItem namedItem = FoodTaxonomy.byLabel(food);
            boolean matches = namedItem != null
                    ? DishFoodItems.contains(dish, namedItem)
                    : keywordMatches(dish.ingredientKeywords, food);
            if (matches) {
                return true;
            }
        }
        return false;
    }

    private static boolean keywordMatches(List<String> ingredientKeywords, String food) {
        String foodLower = food.toLowerCase(Locale.US);
        for (String keyword : ingredientKeywords) {
            String keywordLower = keyword.toLowerCase(Locale.US);
            if (keywordLower.contains(foodLower) || foodLower.contains(keywordLower)) {
                return true;
            }
        }
        return false;
    }
}
