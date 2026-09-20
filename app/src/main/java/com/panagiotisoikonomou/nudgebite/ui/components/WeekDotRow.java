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

public class WeekDotRow extends LinearLayout {

    public static final class DayCell {
        public final String dayLetter;
        public final int dayOfMonth;
        public final boolean positive;

        public DayCell(String dayLetter, int dayOfMonth, boolean positive) {
            this.dayLetter = dayLetter;
            this.dayOfMonth = dayOfMonth;
            this.positive = positive;
        }
    }

    public WeekDotRow(Context context) {
        this(context, null);
    }

    public WeekDotRow(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
    }

    public void bind(List<DayCell> days) {
        removeAllViews();
        for (DayCell day : days) {
            addView(buildDayColumn(day), new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        }
    }

    private View buildDayColumn(DayCell day) {
        LinearLayout column = new LinearLayout(getContext());
        column.setOrientation(VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView letter = new TextView(getContext());
        letter.setText(day.dayLetter);
        letter.setGravity(Gravity.CENTER);
        letter.setTextColor(ContextCompat.getColor(getContext(), R.color.nb_text_secondary));
        letter.setTextSize(12);
        LayoutParams letterParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        letterParams.bottomMargin = DisplayUtils.dp(getContext(), 8);
        column.addView(letter, letterParams);

        TextView circle = new TextView(getContext());
        circle.setText(String.valueOf(day.dayOfMonth));
        circle.setGravity(Gravity.CENTER);
        circle.setTextSize(14);
        circle.setTypeface(Typeface.create(Typeface.DEFAULT, day.positive ? Typeface.BOLD : Typeface.NORMAL));
        circle.setTextColor(ContextCompat.getColor(getContext(), day.positive ? R.color.white : R.color.nb_text_primary));
        circle.setBackgroundResource(day.positive ? R.drawable.bg_icon_dot : R.drawable.bg_icon_dot_muted);
        int size = DisplayUtils.dp(getContext(), 40);
        column.addView(circle, new LinearLayout.LayoutParams(size, size));

        return column;
    }
}
