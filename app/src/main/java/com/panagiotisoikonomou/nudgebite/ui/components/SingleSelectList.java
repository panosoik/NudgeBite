package com.panagiotisoikonomou.nudgebite.ui.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleSelectList extends LinearLayout {

    private final Map<String, Chip> chipsByOption = new HashMap<>();
    @Nullable private String selected;
    @Nullable private OnSelectionChangedListener listener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(@Nullable String selectedOption);
    }

    public SingleSelectList(Context context) {
        this(context, null);
    }

    public SingleSelectList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public void setOnSelectionChangedListener(@Nullable OnSelectionChangedListener listener) {
        this.listener = listener;
    }

    public void setOptions(List<String> options) {
        setOptions(options, null);
    }

    public void setOptions(List<String> options, @Nullable String preSelected) {
        List<ColoredOption> plain = new ArrayList<>(options.size());
        for (String option : options) {
            plain.add(new ColoredOption(option, null));
        }
        setColoredOptions(plain, preSelected);
    }

    public void setColoredOptions(List<ColoredOption> options, @Nullable String preSelected) {
        removeAllViews();
        chipsByOption.clear();
        selected = preSelected;

        ChipGroup chipGroup = PillChipStyle.newChipGroup(getContext());
        for (ColoredOption option : options) {
            Chip chip = new Chip(getContext());
            chip.setText(option.label);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setChecked(option.label.equals(selected));
            chip.setMaxWidth(Integer.MAX_VALUE);
            chip.setEllipsize(null);
            if (option.color == null) {
                PillChipStyle.apply(chip, getContext());
            } else {
                PillChipStyle.apply(chip, getContext(), option.color);
                chip.setChipIcon(dot());
                chip.setChipIconTint(dotTint(option.color));
                chip.setChipIconVisible(true);
                chip.setChipIconSize(DisplayUtils.dp(getContext(), 8));
                chip.setIconStartPadding(DisplayUtils.dp(getContext(), 4));
                chip.setTextStartPadding(DisplayUtils.dp(getContext(), 6));
            }
            chip.setOnClickListener(v -> select(option.label));
            chipsByOption.put(option.label, chip);
            chipGroup.addView(chip);
        }
        addView(chipGroup);
    }

    private GradientDrawable dot() {
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        int size = DisplayUtils.dp(getContext(), 8);
        dot.setSize(size, size);
        dot.setColor(0xFFFFFFFF);
        return dot;
    }

    private ColorStateList dotTint(int accentColor) {
        int[][] states = new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}};
        return new ColorStateList(states,
                new int[]{ContextCompat.getColor(getContext(), R.color.white), accentColor});
    }

    public static final class ColoredOption {
        public final String label;
        @Nullable public final Integer color;

        public ColoredOption(String label, @Nullable Integer color) {
            this.label = label;
            this.color = color;
        }
    }

    private void select(String option) {
        boolean changed = !option.equals(selected);
        Chip previous = selected == null ? null : chipsByOption.get(selected);
        if (previous != null) {
            previous.setChecked(false);
        }
        selected = option;
        // A checkable chip unchecks itself on a second tap, so re-check it.
        Chip current = chipsByOption.get(option);
        if (current != null) {
            current.setChecked(true);
        }
        if (changed && listener != null) {
            listener.onSelectionChanged(option);
        }
    }

    @Nullable
    public String getSelected() {
        return selected;
    }

    /** Selects an option as a tap would, telling the listener. */
    public void setSelected(String option) {
        select(option);
    }
}
