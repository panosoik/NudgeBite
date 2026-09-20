package com.panagiotisoikonomou.nudgebite.ui.components;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;

import java.util.List;

/** A month's dot means it was scored, not that it was a good month. */
public class MonthDotRow extends LinearLayout {

    public static final class MonthCell {
        public final String label;
        public final boolean scored;

        public MonthCell(String label, boolean scored) {
            this.label = label;
            this.scored = scored;
        }
    }

    public MonthDotRow(Context context) {
        this(context, null);
    }

    public MonthDotRow(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
    }

    public void bind(List<MonthCell> months) {
        removeAllViews();
        for (MonthCell month : months) {
            addView(buildColumn(month), new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        }
    }

    private View buildColumn(MonthCell month) {
        LinearLayout column = new LinearLayout(getContext());
        column.setOrientation(VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView label = new TextView(getContext());
        label.setText(month.label);
        label.setGravity(Gravity.CENTER);
        label.setTextColor(ContextCompat.getColor(getContext(),
                month.scored ? R.color.nb_text_primary : R.color.nb_text_secondary));
        label.setTypeface(Typeface.create(Typeface.DEFAULT, month.scored ? Typeface.BOLD : Typeface.NORMAL));
        label.setTextSize(11);
        LayoutParams labelParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        labelParams.bottomMargin = DisplayUtils.dp(getContext(), 6);
        column.addView(label, labelParams);

        View dot = new View(getContext());
        dot.setBackgroundResource(month.scored ? R.drawable.dot_active : R.drawable.dot_inactive);
        int size = DisplayUtils.dp(getContext(), 8);
        column.addView(dot, new LayoutParams(size, size));

        column.setContentDescription(month.label
                + (month.scored ? ", logged enough to score" : ", not enough logged"));
        return column;
    }
}
