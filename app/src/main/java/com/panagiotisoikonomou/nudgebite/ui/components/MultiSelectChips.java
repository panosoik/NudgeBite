package com.panagiotisoikonomou.nudgebite.ui.components;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MultiSelectChips extends LinearLayout {

    public enum Style { WRAP, FULL_WIDTH }

    private final Set<String> selected = new LinkedHashSet<>();
    private Style style = Style.WRAP;
    @Nullable private Runnable onSelectionChangedListener;
    @Nullable private ChipGroup wrapChipGroup;
    @Nullable private TextView appendSectionHeader;

    public MultiSelectChips(Context context) {
        this(context, null);
    }

    public MultiSelectChips(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public void setOnSelectionChangedListener(@Nullable Runnable listener) {
        this.onSelectionChangedListener = listener;
    }

    public void setOptions(List<String> options, List<String> preSelected) {
        removeAllViews();
        selected.clear();
        selected.addAll(preSelected);
        wrapChipGroup = null;
        appendSectionHeader = null;
        if (style == Style.FULL_WIDTH) {
            for (String option : options) {
                addFullWidthRow(option);
            }
        } else {
            wrapChipGroup = addChipGroup();
            for (String option : options) {
                addWrapChip(option);
            }
        }
    }

    public void setSections(List<ChipSection> sections, List<String> preSelected) {
        removeAllViews();
        selected.clear();
        selected.addAll(preSelected);
        style = Style.WRAP;
        wrapChipGroup = null;
        appendSectionHeader = null;

        for (ChipSection section : sections) {
            TextView header = addSectionHeader(section.title());
            wrapChipGroup = addChipGroup();
            for (String option : section.options()) {
                addWrapChip(option);
            }
            if (section.options().isEmpty()) {
                header.setVisibility(GONE);
            }
            appendSectionHeader = header;
        }
    }

    private TextView addSectionHeader(String title) {
        TextView header = new TextView(getContext());
        header.setText(title.toUpperCase(Locale.getDefault()));
        header.setTextColor(ContextCompat.getColor(getContext(), R.color.nb_text_secondary));
        header.setTextSize(12);
        header.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.topMargin = getChildCount() == 0 ? 0 : DisplayUtils.dp(getContext(), 20);
        params.bottomMargin = DisplayUtils.dp(getContext(), 8);
        addView(header, params);
        return header;
    }

    private ChipGroup addChipGroup() {
        ChipGroup chipGroup = PillChipStyle.newChipGroup(getContext());
        addView(chipGroup);
        return chipGroup;
    }

    private void addWrapChip(String option) {
        Chip chip = new Chip(getContext());
        chip.setText(option);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(false);
        chip.setChecked(selected.contains(option));
        PillChipStyle.apply(chip, getContext());
        chip.setOnClickListener(v -> toggle(option, chip.isChecked()));
        if (wrapChipGroup != null) {
            wrapChipGroup.addView(chip);
        }
    }

    private void addFullWidthRow(String option) {
        TextView row = new TextView(getContext());
        row.setText(option);
        row.setTextColor(ContextCompat.getColor(getContext(), R.color.nb_text_primary));
        row.setTextSize(15);
        int horizontalPad = DisplayUtils.dp(getContext(), 16);
        int verticalPad = DisplayUtils.dp(getContext(), 14);
        row.setPadding(horizontalPad, verticalPad, horizontalPad, verticalPad);
        row.setClickable(true);
        row.setFocusable(true);
        applyRowState(row, selected.contains(option));
        row.setOnClickListener(v -> {
            boolean nowSelected = !selected.contains(option);
            toggle(option, nowSelected);
            applyRowState(row, nowSelected);
        });

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = DisplayUtils.dp(getContext(), 10);
        addView(row, params);
    }

    private void applyRowState(TextView row, boolean isSelected) {
        row.setBackgroundResource(isSelected ? R.drawable.bg_chip_row_selected : R.drawable.bg_chip_row_unselected);
        row.setTypeface(Typeface.create(Typeface.DEFAULT, isSelected ? Typeface.BOLD : Typeface.NORMAL));
    }

    private void toggle(String option, boolean checked) {
        if (checked) {
            selected.add(option);
        } else {
            selected.remove(option);
        }
        if (onSelectionChangedListener != null) {
            onSelectionChangedListener.run();
        }
    }

    public void addOption(String option) {
        if (selected.contains(option)) {
            return;
        }
        selected.add(option);
        if (style == Style.FULL_WIDTH) {
            addFullWidthRow(option);
        } else {
            addWrapChip(option);
            if (appendSectionHeader != null) {
                appendSectionHeader.setVisibility(VISIBLE);
            }
        }
        if (onSelectionChangedListener != null) {
            onSelectionChangedListener.run();
        }
    }

    public void bindAddOnEnter(EditText input) {
        input.setOnEditorActionListener((v, actionId, event) -> {
            boolean isDone = actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            if (!isDone) {
                return false;
            }
            commitInput(input);
            return true;
        });
    }

    public void bindAddButton(EditText input, View addButton) {
        addButton.setOnClickListener(v -> commitInput(input));
    }

    private void commitInput(EditText input) {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }
        addOption(text);
        input.setText("");
    }

    public List<String> getSelectedList() {
        return new ArrayList<>(selected);
    }
}
