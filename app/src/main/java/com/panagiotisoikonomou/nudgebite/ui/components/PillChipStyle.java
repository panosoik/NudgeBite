package com.panagiotisoikonomou.nudgebite.ui.components;

import android.content.Context;
import android.content.res.ColorStateList;

import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;

final class PillChipStyle {

    private PillChipStyle() {
    }

    static ChipGroup newChipGroup(Context context) {
        ChipGroup chipGroup = new ChipGroup(context);
        chipGroup.setChipSpacingHorizontal(DisplayUtils.dp(context, 8));
        chipGroup.setChipSpacingVertical(DisplayUtils.dp(context, 8));
        return chipGroup;
    }

    static void apply(Chip chip, Context context) {
        apply(chip, context, ContextCompat.getColor(context, R.color.nb_primary_green));
    }

    static void apply(Chip chip, Context context, int accentColor) {
        int[][] states = new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}};
        int selectedBg = accentColor;
        int unselectedBg = ContextCompat.getColor(context, R.color.white);
        int selectedText = ContextCompat.getColor(context, R.color.white);
        int unselectedText = ContextCompat.getColor(context, R.color.nb_text_primary);
        int selectedStroke = accentColor;
        int unselectedStroke = ContextCompat.getColor(context, R.color.nb_neutral_border);

        chip.setChipBackgroundColor(new ColorStateList(states, new int[]{selectedBg, unselectedBg}));
        chip.setTextColor(new ColorStateList(states, new int[]{selectedText, unselectedText}));
        chip.setChipStrokeWidth(DisplayUtils.dp(context, 1));
        chip.setChipStrokeColor(new ColorStateList(states, new int[]{selectedStroke, unselectedStroke}));
        chip.setChipCornerRadius(DisplayUtils.dp(context, 20));
        chip.setRippleColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.nb_row_selected_bg)));
    }
}
