package com.panagiotisoikonomou.nudgebite.data.enums;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.utils.EnumNames;

public enum FoodCategory {
    LEAFY_GREEN, CRUCIFEROUS, ROOT_VEGETABLE, OTHER_VEGETABLE,
    FRUIT,
    WHOLE_GRAIN,
    REFINED_GRAIN,
    LEGUME, NUT_SEED,
    FISH, SEAFOOD,
    RED_MEAT, PROCESSED_MEAT,
    SUGARY_DRINK,
    POULTRY, EGG, DAIRY, CHEESE, FAT_OIL, POTATO, SWEET_SNACK,
    OTHER, UNKNOWN;

    public static FoodCategory parseOrDefault(@Nullable String rawName, FoodCategory fallback) {
        return EnumNames.parseOrDefault(FoodCategory.class, rawName, fallback);
    }

    /** "Leafy Green" for LEAFY_GREEN. */
    public String displayName() {
        StringBuilder words = new StringBuilder();
        for (String word : name().toLowerCase(java.util.Locale.US).split("_")) {
            words.append(words.length() > 0 ? " " : "").append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return words.toString();
    }
}
