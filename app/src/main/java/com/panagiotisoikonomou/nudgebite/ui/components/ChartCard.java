package com.panagiotisoikonomou.nudgebite.ui.components;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;

public class ChartCard extends LinearLayout {

    private final TextView titleView;
    private final TextView rangeView;
    private final TrendLineView chartView;
    private final TextView emptyView;

    public ChartCard(Context context) {
        this(context, null);
    }

    public ChartCard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setBackgroundResource(R.drawable.bg_chip_row_unselected);
        int pad = DisplayUtils.dp(context, 16);
        setPadding(pad, pad, pad, pad);

        LinearLayout header = new LinearLayout(context);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);

        titleView = new TextView(context);
        titleView.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titleView.setTextColor(ContextCompat.getColor(context, R.color.nb_text_primary));
        titleView.setTextSize(16);
        titleView.setVisibility(GONE);
        header.addView(titleView, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        rangeView = new TextView(context);
        rangeView.setTextColor(ContextCompat.getColor(context, R.color.nb_text_secondary));
        rangeView.setTextSize(12);
        header.addView(rangeView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        addView(header, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        chartView = new TrendLineView(context);
        LayoutParams chartParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                DisplayUtils.dp(context, 132));
        chartParams.topMargin = DisplayUtils.dp(context, 12);
        addView(chartView, chartParams);

        emptyView = new TextView(context);
        emptyView.setTextColor(ContextCompat.getColor(context, R.color.nb_text_secondary));
        emptyView.setTextSize(13);
        LayoutParams emptyParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        emptyParams.topMargin = DisplayUtils.dp(context, 16);
        addView(emptyView, emptyParams);
        emptyView.setVisibility(GONE);
    }

    public TrendLineView chart() {
        return chartView;
    }

    public void setTitle(@Nullable String title) {
        titleView.setText(title == null ? "" : title);
        titleView.setVisibility(title == null || title.isEmpty() ? GONE : VISIBLE);
    }

    public void setRangeLabel(@Nullable String label) {
        rangeView.setText(label == null ? "" : label);
        rangeView.setVisibility(label == null || label.isEmpty() ? GONE : VISIBLE);
    }

    public void setChartDescription(String description) {
        chartView.setContentDescription(description);
    }

    public void showEmptyMessage(@Nullable String message) {
        boolean empty = message != null;
        emptyView.setText(empty ? message : "");
        emptyView.setVisibility(empty ? VISIBLE : GONE);
        chartView.setVisibility(empty ? GONE : VISIBLE);
    }
}
