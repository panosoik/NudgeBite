package com.panagiotisoikonomou.nudgebite.ui.components;

import android.content.Context;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;

public class NumberEntry extends LinearLayout {

    private final TextView labelView;
    private final EditText inputView;
    private final TextView unitView;

    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;

    public NumberEntry(Context context) {
        this(context, null);
    }

    public NumberEntry(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

        labelView = new TextView(context);
        labelView.setTextColor(ContextCompat.getColor(context, R.color.nb_text_primary));
        labelView.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        labelView.setTextSize(14);
        labelView.setPadding(0, 0, 0, DisplayUtils.dp(getContext(), 6));
        addView(labelView);

        LinearLayout inputRow = new LinearLayout(context);
        inputRow.setOrientation(HORIZONTAL);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);
        inputRow.setBackgroundResource(R.drawable.bg_input_field);
        inputRow.setPadding(DisplayUtils.dp(getContext(), 16), DisplayUtils.dp(getContext(), 14), DisplayUtils.dp(getContext(), 16), DisplayUtils.dp(getContext(), 14));

        inputView = new EditText(context);
        inputView.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputView.setBackground(null);
        inputView.setTextColor(ContextCompat.getColor(context, R.color.nb_text_primary));
        inputView.setTextSize(15);
        inputView.setPadding(0, 0, 0, 0);
        inputView.setMinHeight(0);
        inputView.setMinimumHeight(0);
        LayoutParams inputParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        inputRow.addView(inputView, inputParams);

        unitView = new TextView(context);
        unitView.setTextColor(ContextCompat.getColor(context, R.color.nb_text_secondary));
        unitView.setTextSize(15);
        unitView.setVisibility(GONE);
        inputRow.addView(unitView);

        addView(inputRow, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public void setLabel(String label) {
        labelView.setText(label);
    }

    public void setHint(String hint) {
        inputView.setHint(hint);
    }

    public void setUnit(@Nullable String unit) {
        if (unit == null || unit.isEmpty()) {
            unitView.setVisibility(GONE);
        } else {
            unitView.setText(unit);
            unitView.setVisibility(VISIBLE);
        }
    }

    public void setRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public void setValue(@Nullable Integer value) {
        inputView.setText(value == null ? "" : String.valueOf(value));
    }

    @Nullable
    public Integer getValue() {
        String text = inputView.getText().toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean isValid() {
        Integer value = getValue();
        return value == null || (value >= min && value <= max);
    }
}
