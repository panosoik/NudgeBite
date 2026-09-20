package com.panagiotisoikonomou.nudgebite.engine.recommendation;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.enums.FoodCategory;
import com.panagiotisoikonomou.nudgebite.data.enums.TriggerCombo;
import com.panagiotisoikonomou.nudgebite.food.FoodItem;
import com.panagiotisoikonomou.nudgebite.food.FoodTaxonomy;
import com.panagiotisoikonomou.nudgebite.quality.FoodCategoryComponentMapper;
import com.panagiotisoikonomou.nudgebite.quality.ScoreComponent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SuggestionPayloadBuilder {

    public static final String DEFAULT_EYEBROW = "Ordering? Here's a lighter pick";

    private static final String STYLE_ALTERNATIVE = "ALTERNATIVE";
    private static final String STYLE_SHOPPING = "SHOPPING";
    private static final String STYLE_HOME = "HOME";
    private static final String STYLE_ON_THE_WAY = "ON_THE_WAY";
    private static final String STYLE_ASKED_FOR_IDEA = "ASKED_FOR_IDEA";
    private static final String STYLE_LIGHT = "LIGHT";

    private static final String DELIVERY_EYEBROW = "BEFORE YOU ORDER";
    private static final String SUPERMARKET_EYEBROW = "WHILE YOU'RE HERE";
    private static final String DINNER_EYEBROW = "Deciding on dinner? Here's an idea";
    private static final String HOME_EYEBROW = "ALREADY SORTED";
    private static final String ON_THE_WAY_EYEBROW = "ON YOUR WAY HOME";
    private static final String ASKED_FOR_IDEA_EYEBROW = "ONE IDEA FOR TONIGHT";
    private static final String LIGHT_EYEBROW = "SOMETHING EASY TONIGHT";

    private static final String SUPERMARKET_BODY_TEMPLATE =
            "Your week could use more %1$s. Consider adding %2$s to your basket for the next couple of meals.";
    private static final String SUPERMARKET_FALLBACK_BODY =
            "Consider adding a few basics to your basket, such as vegetables, fruit or whole grains.";
    private static final String EATING_OUT_BODY_TEMPLATE = "Eating out? Look for something with %1$s, such as %2$s.";
    // Placeholders are positional, so %2$s may come before %1$s.
    private static final String POST_DELIVERY_PREP_BODY_TEMPLATE =
            "Planning to cook tonight? Picking up %2$s could make it easier to include more %1$s.";
    private static final String ORGANIZE_MEALS_BODY_TEMPLATE =
            "Planning the week ahead? See if you can work more %1$s into your meals.";
    private static final String ORGANIZE_MEALS_PREFERENCE_SUFFIX = " Worth adding %1$s to the basket.";

    private SuggestionPayloadBuilder() {
    }

    public static String alternativeEyebrowFor(TriggerCombo combo) {
        if (combo == TriggerCombo.DELIVERY_HOME_EVENING) {
            return DELIVERY_EYEBROW;
        }
        if (combo == TriggerCombo.DINNER_HOME) {
            return DINNER_EYEBROW;
        }
        return DEFAULT_EYEBROW;
    }

    public static String buildAlternativePayload(MealRecommendation dish, String eyebrow, List<String> alternativeIds) {
        return buildPayload(dish, eyebrow, dish.bodyText, STYLE_ALTERNATIVE, null, alternativeIds);
    }

    public static String buildShoppingPayload(MealRecommendation dish, List<FoodCategory> rankedGaps,
                                              List<String> alternativeIds) {
        FoodCategory topGap = rankedGaps.isEmpty() ? null : rankedGaps.get(0);
        return buildPayload(dish, SUPERMARKET_EYEBROW, shoppingBodyText(dish, rankedGaps),
                STYLE_SHOPPING, topGap, alternativeIds);
    }

    public static String buildHomePayload(MealRecommendation dish, List<String> alternativeIds) {
        return buildPayload(dish, HOME_EYEBROW, dish.homeBodyText, STYLE_HOME, null, alternativeIds);
    }

    public static String buildOnTheWayPayload(MealRecommendation dish, List<String> alternativeIds) {
        String body = "Not much in tonight? " + dish.title
                + " is an easy one to put together from a quick stop on the way home.";
        return buildPayload(dish, ON_THE_WAY_EYEBROW, body, STYLE_ON_THE_WAY, null, alternativeIds);
    }

    public static String buildAskedForIdeaPayload(MealRecommendation dish, List<String> alternativeIds) {
        String body = dish.title + " is one worth considering for tonight — a "
                + "straightforward way to round out what you've been eating this week.";
        return buildPayload(dish, ASKED_FOR_IDEA_EYEBROW, body, STYLE_ASKED_FOR_IDEA, null, alternativeIds);
    }

    public static String buildLightPayload(MealRecommendation dish, List<String> alternativeIds) {
        String body = "No plan for tonight? " + dish.title
                + " takes very little putting together, and still counts as a proper meal.";
        return buildPayload(dish, LIGHT_EYEBROW, body, STYLE_LIGHT, null, alternativeIds);
    }

    /** The same kind of suggestion for another dish, as "Show another" displays it. */
    public static String rebuildForDish(String payload, MealRecommendation dish) {
        JSONObject original = parsePayload(payload);
        List<String> noAlternatives = Collections.emptyList();
        switch (original.optString("style", STYLE_ALTERNATIVE)) {
            case STYLE_SHOPPING:
                String topGap = original.optString("topGapCategory", null);
                List<FoodCategory> rankedGaps = topGap != null
                        ? Collections.singletonList(FoodCategory.valueOf(topGap))
                        : Collections.emptyList();
                return buildShoppingPayload(dish, rankedGaps, noAlternatives);
            case STYLE_HOME:
                return buildHomePayload(dish, noAlternatives);
            case STYLE_ON_THE_WAY:
                return buildOnTheWayPayload(dish, noAlternatives);
            case STYLE_ASKED_FOR_IDEA:
                return buildAskedForIdeaPayload(dish, noAlternatives);
            case STYLE_LIGHT:
                return buildLightPayload(dish, noAlternatives);
            default:
                return buildAlternativePayload(dish, original.optString("eyebrow", DEFAULT_EYEBROW), noAlternatives);
        }
    }

    /** Names a food that is both in the gap's group and in the dish shown on screen. */
    static String shoppingBodyText(MealRecommendation dish, List<FoodCategory> rankedGaps) {
        if (rankedGaps.isEmpty()) {
            return SUPERMARKET_FALLBACK_BODY;
        }
        FoodCategory gap = rankedGaps.get(0);
        ScoreComponent gapGroup = FoodCategoryComponentMapper.componentOf(gap);
        String ingredient = dishIngredientFor(dish, gap, gapGroup);
        if (ingredient == null) {
            return SUPERMARKET_FALLBACK_BODY;
        }
        return String.format(Locale.US, SUPERMARKET_BODY_TEMPLATE, groupName(gapGroup), ingredient);
    }

    /** The dish's word for the exact gap category, else for any category in the same group. */
    @Nullable
    private static String dishIngredientFor(MealRecommendation dish, FoodCategory gap, ScoreComponent gapGroup) {
        String exact = dish.ingredientByCategory.get(gap);
        if (exact != null) {
            return exact;
        }
        for (Map.Entry<FoodCategory, String> entry : dish.ingredientByCategory.entrySet()) {
            if (FoodCategoryComponentMapper.componentOf(entry.getKey()) == gapGroup) {
                return entry.getValue();
            }
        }
        return null;
    }

    /** The example food comes from a fixed table per group, never from the dish, so it always matches the named gap. */
    public static String eatingOutBodyText(List<FoodCategory> rankedGaps) {
        return topGapSentence(rankedGaps, EATING_OUT_BODY_TEMPLATE);
    }

    public static String postDeliveryPrepBodyText(List<FoodCategory> rankedGaps) {
        return topGapSentence(rankedGaps, POST_DELIVERY_PREP_BODY_TEMPLATE);
    }

    /** Names the two widest gaps as food groups, plus one preferred food when it fills another gap. */
    public static String organizeMealsBodyText(List<FoodCategory> rankedGaps, List<String> preferredFoods) {
        List<ScoreComponent> groups = firstDistinctGroups(rankedGaps, 2);
        String named = groups.size() == 1
                ? groupName(groups.get(0))
                : groupName(groups.get(0)) + " and " + groupName(groups.get(1));
        String body = String.format(Locale.US, ORGANIZE_MEALS_BODY_TEMPLATE, named);
        String preferred = preferredFoodInWidestOtherGap(rankedGaps, preferredFoods, groups);
        return preferred == null ? body : body + String.format(Locale.US, ORGANIZE_MEALS_PREFERENCE_SUFFIX, preferred);
    }

    private static List<ScoreComponent> firstDistinctGroups(List<FoodCategory> rankedGaps, int count) {
        List<ScoreComponent> groups = new ArrayList<>();
        for (FoodCategory category : rankedGaps) {
            ScoreComponent group = FoodCategoryComponentMapper.componentOf(category);
            if (group != ScoreComponent.NONE && !groups.contains(group)) {
                groups.add(group);
                if (groups.size() == count) {
                    break;
                }
            }
        }
        return groups;
    }

    /** Ties go to whichever preference the user declared first. */
    @Nullable
    private static String preferredFoodInWidestOtherGap(List<FoodCategory> rankedGaps, List<String> preferredFoods,
                                                        List<ScoreComponent> alreadyNamed) {
        String best = null;
        int bestRank = Integer.MAX_VALUE;
        for (String food : preferredFoods) {
            FoodItem item = food == null ? null : FoodTaxonomy.byLabel(food.trim());
            if (item == null) {
                continue;
            }
            FoodCategory category = item.scoringCategory();
            ScoreComponent group = FoodCategoryComponentMapper.componentOf(category);
            int rank = rankedGaps.indexOf(category);
            if (group != ScoreComponent.NONE && !alreadyNamed.contains(group) && rank >= 0 && rank < bestRank) {
                bestRank = rank;
                best = item.label().toLowerCase(Locale.US);
            }
        }
        return best;
    }

    private static String topGapSentence(List<FoodCategory> rankedGaps, String template) {
        ScoreComponent group = FoodCategoryComponentMapper.componentOf(rankedGaps.get(0));
        return String.format(Locale.US, template, groupName(group), exampleFoodFor(group));
    }

    private static String exampleFoodFor(ScoreComponent group) {
        switch (group) {
            case VEGETABLE:
                return "spinach";
            case FRUIT:
                return "fresh fruit";
            case WHOLE_GRAIN:
                return "brown rice";
            case LEGUME_NUT:
                return "lentils";
            case FISH:
                return "salmon";
            default:
                throw new IllegalArgumentException(group + " is never a gap");
        }
    }

    private static String groupName(ScoreComponent group) {
        switch (group) {
            case VEGETABLE:
                return "vegetables";
            case FRUIT:
                return "fruit";
            case WHOLE_GRAIN:
                return "whole grains";
            case LEGUME_NUT:
                return "legumes & nuts";
            case FISH:
                return "fish";
            default:
                throw new IllegalArgumentException(group + " is never a gap");
        }
    }

    private static String buildPayload(MealRecommendation dish, String eyebrow, String bodyText, String style,
                                       @Nullable FoodCategory topGapCategory, List<String> alternativeIds) {
        try {
            JSONObject json = new JSONObject();
            json.put("recommendationId", dish.id);
            json.put("title", dish.title);
            json.put("eyebrow", eyebrow);
            json.put("bodyText", bodyText);
            json.put("tags", new JSONArray(dish.tags));
            json.put("style", style);
            if (topGapCategory != null) {
                json.put("topGapCategory", topGapCategory.name());
            }
            json.put("alternativeIds", new JSONArray(alternativeIds));
            return json.toString();
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    private static JSONObject parsePayload(String payload) {
        try {
            return new JSONObject(payload);
        } catch (JSONException e) {
            throw new IllegalStateException("Malformed suggestion payload", e);
        }
    }
}
