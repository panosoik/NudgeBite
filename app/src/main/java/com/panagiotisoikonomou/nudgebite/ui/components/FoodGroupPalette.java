package com.panagiotisoikonomou.nudgebite.ui.components;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.progress.well.TrackedFoodGroup;

public final class FoodGroupPalette {

    private FoodGroupPalette() {
    }

    @ColorRes
    public static int colorResFor(TrackedFoodGroup group) {
        switch (group) {
            case VEGETABLES: return R.color.nb_group_vegetables;
            case FRUIT: return R.color.nb_group_fruit;
            case WHOLE_GRAINS: return R.color.nb_group_whole_grains;
            case LEGUMES: return R.color.nb_group_legumes;
            case NUTS_AND_SEEDS: return R.color.nb_group_nuts_seeds;
            case FISH: return R.color.nb_group_fish;
            case RED_AND_PROCESSED_MEAT: return R.color.nb_group_red_meat;
            case FAST_FOOD:
            default: return R.color.nb_group_fast_food;
        }
    }

    @ColorInt
    public static int colorFor(Context context, TrackedFoodGroup group) {
        return ContextCompat.getColor(context, colorResFor(group));
    }

    @ColorInt
    public static int bandColorFor(Context context, TrackedFoodGroup group) {
        int color = colorFor(context, group);
        return Color.argb(28, Color.red(color), Color.green(color), Color.blue(color));
    }
}
