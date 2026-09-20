package com.panagiotisoikonomou.nudgebite.engine.recommendation;

import com.panagiotisoikonomou.nudgebite.food.FoodItem;
import com.panagiotisoikonomou.nudgebite.food.FoodTaxonomy;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DishFoodItems {

    private static final Map<String, Set<FoodItem>> ITEMS_BY_DISH = new HashMap<>();

    static {
        put("grilled_chicken_brown_rice_salad", FoodItem.CHICKEN, FoodItem.CHICKEN_BREAST, FoodItem.BROWN_RICE, FoodItem.MIXED_VEGETABLES);
        put("grilled_fish_greens", FoodItem.SALMON, FoodItem.SEA_BASS, FoodItem.SPINACH, FoodItem.LETTUCE);
        put("lentil_soup_vegetables", FoodItem.LENTILS, FoodItem.MIXED_VEGETABLES);
        put("grilled_chicken_broccoli", FoodItem.CHICKEN, FoodItem.CHICKEN_BREAST, FoodItem.BROCCOLI);
        put("sweet_potato_chickpea_salad", FoodItem.SWEET_POTATO, FoodItem.CHICKPEAS, FoodItem.MIXED_VEGETABLES);
        put("shrimp_grilled_vegetables", FoodItem.SHRIMP, FoodItem.MIXED_VEGETABLES);
        put("greek_yogurt_honey_walnuts_fruit", FoodItem.GREEK_YOGURT, FoodItem.PLAIN_YOGURT, FoodItem.HONEY, FoodItem.WALNUTS,
                FoodItem.STRAWBERRY, FoodItem.BLUEBERRY);
        put("whole_wheat_pasta_vegetables", FoodItem.WHOLE_WHEAT_PASTA, FoodItem.TOMATO, FoodItem.MIXED_VEGETABLES);
        put("spinach_feta_omelette_salad", FoodItem.EGG, FoodItem.EGG_WHITES, FoodItem.SPINACH, FoodItem.FETA, FoodItem.MIXED_VEGETABLES);
        put("chickpea_salad_whole_grain_pita", FoodItem.CHICKPEAS, FoodItem.WHOLE_GRAIN_PITA, FoodItem.MIXED_VEGETABLES);
        put("chicken_souvlaki_pita_salad", FoodItem.CHICKEN, FoodItem.CHICKEN_BREAST, FoodItem.PITA_BREAD, FoodItem.MIXED_VEGETABLES);
        put("chicken_souvlaki_potatoes_salad", FoodItem.CHICKEN, FoodItem.CHICKEN_BREAST, FoodItem.POTATO, FoodItem.MIXED_VEGETABLES);
        put("beef_burger_salad_potatoes", FoodItem.BEEF, FoodItem.BEEF_MINCE, FoodItem.WHITE_BREAD, FoodItem.POTATO, FoodItem.MIXED_VEGETABLES);
        put("beef_meatballs_tomato_rice", FoodItem.BEEF, FoodItem.BEEF_MINCE, FoodItem.TOMATO, FoodItem.WHITE_RICE);
        put("beef_stew_potatoes_carrots", FoodItem.BEEF, FoodItem.POTATO, FoodItem.CARROT);
        put("pork_souvlaki_pita_vegetables", FoodItem.PORK, FoodItem.PITA_BREAD, FoodItem.MIXED_VEGETABLES);
        put("pork_chop_potatoes_salad", FoodItem.PORK, FoodItem.POTATO, FoodItem.MIXED_VEGETABLES);
        put("spaghetti_minced_beef_tomato", FoodItem.BEEF, FoodItem.BEEF_MINCE, FoodItem.PASTA, FoodItem.TOMATO);
        put("omelette_tomato_peppers_feta", FoodItem.EGG, FoodItem.EGG_WHITES, FoodItem.TOMATO, FoodItem.BELL_PEPPER, FoodItem.FETA);
        put("scrambled_eggs_whole_grain_toast_tomato", FoodItem.EGG, FoodItem.EGG_WHITES, FoodItem.WHOLE_GRAIN_BREAD, FoodItem.TOMATO);
        put("greek_salad_feta_olives", FoodItem.TOMATO, FoodItem.CUCUMBER, FoodItem.ONION, FoodItem.FETA,
                FoodItem.OLIVE_OIL);
        put("dakos_tomato_feta", FoodItem.BARLEY, FoodItem.TOMATO, FoodItem.FETA, FoodItem.OLIVE_OIL);
        put("spinach_pie_feta", FoodItem.SPINACH, FoodItem.FETA);
        put("stuffed_tomatoes_peppers_rice", FoodItem.TOMATO, FoodItem.BELL_PEPPER, FoodItem.WHITE_RICE);
        put("briam_roasted_vegetables", FoodItem.ZUCCHINI, FoodItem.AUBERGINE, FoodItem.BELL_PEPPER,
                FoodItem.POTATO, FoodItem.OLIVE_OIL, FoodItem.MIXED_VEGETABLES);
        put("green_beans_tomato_potatoes", FoodItem.GREEN_BEANS, FoodItem.TOMATO, FoodItem.POTATO, FoodItem.OLIVE_OIL);
        put("peas_carrots_potatoes", FoodItem.GREEN_PEAS, FoodItem.CARROT, FoodItem.POTATO);
        put("chickpea_soup_lemon", FoodItem.CHICKPEAS, FoodItem.LEMON);
        put("bean_soup_vegetables", FoodItem.WHITE_BEANS, FoodItem.CARROT, FoodItem.CELERY, FoodItem.MIXED_VEGETABLES);
        put("giant_beans_tomato_sauce", FoodItem.WHITE_BEANS, FoodItem.TOMATO);
        put("lentil_salad_tomato_cucumber_feta", FoodItem.LENTILS, FoodItem.TOMATO, FoodItem.CUCUMBER, FoodItem.FETA);
        put("tuna_salad_vegetables", FoodItem.TUNA, FoodItem.TOMATO, FoodItem.CUCUMBER, FoodItem.MIXED_VEGETABLES);
        put("grilled_sardines_salad", FoodItem.SARDINES, FoodItem.MIXED_VEGETABLES);
        // Sea bream has no taxonomy item, so nothing is substituted for it.
        put("baked_sea_bream_potatoes", FoodItem.POTATO);
        put("shrimp_pasta_tomato_sauce", FoodItem.SHRIMP, FoodItem.PASTA, FoodItem.TOMATO);
        put("seafood_risotto", FoodItem.SHRIMP, FoodItem.MUSSELS, FoodItem.SQUID, FoodItem.WHITE_RICE);
        put("yogurt_banana_walnuts_honey", FoodItem.GREEK_YOGURT, FoodItem.PLAIN_YOGURT, FoodItem.BANANA, FoodItem.WALNUTS, FoodItem.HONEY);
        put("oatmeal_yogurt_apple_walnuts", FoodItem.OATS, FoodItem.GREEK_YOGURT, FoodItem.PLAIN_YOGURT, FoodItem.APPLE, FoodItem.WALNUTS);
        put("whole_grain_toast_tahini_banana", FoodItem.WHOLE_GRAIN_BREAD, FoodItem.TAHINI, FoodItem.BANANA);
        put("chicken_sandwich_cheese_tomato_lettuce", FoodItem.CHICKEN, FoodItem.CHICKEN_BREAST, FoodItem.WHITE_BREAD, FoodItem.CHEDDAR,
                FoodItem.TOMATO, FoodItem.LETTUCE);
    }

    private DishFoodItems() {
    }

    public static Set<FoodItem> itemsIn(MealRecommendation dish) {
        Set<FoodItem> declared = ITEMS_BY_DISH.get(dish.id);
        return declared != null ? declared : derivedFrom(dish.ingredientKeywords);
    }

    public static boolean contains(MealRecommendation dish, FoodItem item) {
        return itemsIn(dish).contains(item);
    }

    static boolean isDeclared(String dishId) {
        return ITEMS_BY_DISH.containsKey(dishId);
    }

    private static Set<FoodItem> derivedFrom(List<String> ingredientKeywords) {
        Set<FoodItem> derived = EnumSet.noneOf(FoodItem.class);
        for (String keyword : ingredientKeywords) {
            FoodItem item = FoodTaxonomy.byLabel(keyword);
            if (item != null) {
                derived.add(item);
            }
        }
        return Collections.unmodifiableSet(derived);
    }

    private static void put(String dishId, FoodItem... items) {
        ITEMS_BY_DISH.put(dishId, Collections.unmodifiableSet(EnumSet.of(items[0], items)));
    }
}
