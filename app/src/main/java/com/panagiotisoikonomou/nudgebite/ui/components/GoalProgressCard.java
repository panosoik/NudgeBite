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
import com.panagiotisoikonomou.nudgebite.progress.GoalProgressStatus;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;

public class GoalProgressCard extends LinearLayout {

    private final View accentBar;
    private final TextView titleView;
    private final TextView messageView;
    private final TextView valueView;

    public GoalProgressCard(Context context) {
        this(context, null);
    }

    public GoalProgressCard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackgroundResource(R.drawable.bg_chip_row_unselected);

        int barWidth = DisplayUtils.dp(getContext(), 4);
        accentBar = new View(context);
        accentBar.setBackgroundResource(R.drawable.bg_goal_progress_accent);
        addView(accentBar, new LayoutParams(barWidth, LayoutParams.MATCH_PARENT));

        LinearLayout contentColumn = new LinearLayout(context);
        contentColumn.setOrientation(VERTICAL);
        int pad = DisplayUtils.dp(getContext(), 16);
        contentColumn.setPadding(pad, pad, pad, pad);

        titleView = new TextView(context);
        titleView.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titleView.setTextColor(ContextCompat.getColor(context, R.color.nb_text_primary));
        titleView.setTextSize(15);
        contentColumn.addView(titleView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        messageView = new TextView(context);
        messageView.setTextColor(ContextCompat.getColor(context, R.color.nb_text_secondary));
        messageView.setTextSize(13);
        LayoutParams messageParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        messageParams.topMargin = DisplayUtils.dp(getContext(), 4);
        contentColumn.addView(messageView, messageParams);

        addView(contentColumn, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        valueView = new TextView(context);
        valueView.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        valueView.setTextSize(22);
        LayoutParams valueParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        valueParams.rightMargin = pad;
        valueParams.leftMargin = DisplayUtils.dp(getContext(), 8);
        addView(valueView, valueParams);
    }

    public void bind(GoalProgressStatus status) {
        titleView.setText(status.title);
        messageView.setText(status.message);
        valueView.setText(status.valueText);

        int color = ContextCompat.getColor(getContext(),
                status.level == GoalProgressStatus.Level.GREEN ? R.color.nb_primary_green : R.color.nb_warning_amber);
        valueView.setTextColor(color);
        accentBar.getBackground().mutate().setTint(color);
    }
}
