package com.panagiotisoikonomou.nudgebite.ui.intervention;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.R;

final class RecommendationImages {

    private RecommendationImages() {
    }

    @DrawableRes
    @Nullable
    static Integer forId(@Nullable String recommendationId) {
        if (recommendationId == null) {
            return null;
        }
        switch (recommendationId) {
            case "grilled_chicken_brown_rice_salad": return R.drawable.grilled_chicken_brown_rice_salad;
            case "grilled_fish_greens": return R.drawable.grilled_fish_greens;
            case "lentil_soup_vegetables": return R.drawable.lentil_soup_vegetables;
            case "grilled_chicken_broccoli": return R.drawable.grilled_chicken_broccoli;
            case "sweet_potato_chickpea_salad": return R.drawable.sweet_potato_chickpea_salad;
            case "shrimp_grilled_vegetables": return R.drawable.shrimp_grilled_vegetables;
            case "greek_yogurt_honey_walnuts_fruit": return R.drawable.greek_yogurt_honey_walnuts_fruit;
            case "whole_wheat_pasta_vegetables": return R.drawable.whole_wheat_pasta_vegetables;
            case "spinach_feta_omelette_salad": return R.drawable.spinach_feta_omelette_salad;
            case "chickpea_salad_whole_grain_pita": return R.drawable.chickpea_salad_whole_grain_pita;
            case "baked_sea_bream_potatoes": return R.drawable.baked_sea_bream_potatoes;
            case "bean_soup_vegetables": return R.drawable.bean_soup_vegetables;
            case "beef_burger_salad_potatoes": return R.drawable.beef_burger_salad_potatoes;
            case "beef_meatballs_tomato_rice": return R.drawable.beef_meatballs_tomato_rice;
            case "beef_stew_potatoes_carrots": return R.drawable.beef_stew_potatoes_carrots;
            case "briam_roasted_vegetables": return R.drawable.briam_roasted_vegetables;
            case "chicken_sandwich_cheese_tomato_lettuce": return R.drawable.chicken_sandwich_cheese_tomato_lettuce;
            case "chicken_souvlaki_pita_salad": return R.drawable.chicken_souvlaki_pita_salad;
            case "chicken_souvlaki_potatoes_salad": return R.drawable.chicken_souvlaki_potatoes_salad;
            case "chickpea_soup_lemon": return R.drawable.chickpea_soup_lemon;
            case "dakos_tomato_feta": return R.drawable.dakos_tomato_feta;
            case "giant_beans_tomato_sauce": return R.drawable.giant_beans_tomato_sauce;
            case "greek_salad_feta_olives": return R.drawable.greek_salad_feta_olives;
            case "green_beans_tomato_potatoes": return R.drawable.green_beans_tomato_potatoes;
            case "grilled_sardines_salad": return R.drawable.grilled_sardines_salad;
            case "lentil_salad_tomato_cucumber_feta": return R.drawable.lentil_salad_tomato_cucumber_feta;
            case "oatmeal_yogurt_apple_walnuts": return R.drawable.oatmeal_yogurt_apple_walnuts;
            case "omelette_tomato_peppers_feta": return R.drawable.omelette_tomato_peppers_feta;
            case "peas_carrots_potatoes": return R.drawable.peas_carrots_potatoes;
            case "pork_chop_potatoes_salad": return R.drawable.pork_chop_potatoes_salad;
            case "pork_souvlaki_pita_vegetables": return R.drawable.pork_souvlaki_pita_vegetables;
            case "scrambled_eggs_whole_grain_toast_tomato": return R.drawable.scrambled_eggs_whole_grain_toast_tomato;
            case "seafood_risotto": return R.drawable.seafood_risotto;
            case "shrimp_pasta_tomato_sauce": return R.drawable.shrimp_pasta_tomato_sauce;
            case "spaghetti_minced_beef_tomato": return R.drawable.spaghetti_minced_beef_tomato;
            case "spinach_pie_feta": return R.drawable.spinach_pie_feta;
            case "stuffed_tomatoes_peppers_rice": return R.drawable.stuffed_tomatoes_peppers_rice;
            case "tuna_salad_vegetables": return R.drawable.tuna_salad_vegetables;
            case "whole_grain_toast_tahini_banana": return R.drawable.whole_grain_toast_tahini_banana;
            case "yogurt_banana_walnuts_honey": return R.drawable.yogurt_banana_walnuts_honey;
            default: return null;
        }
    }
}
