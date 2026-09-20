package com.panagiotisoikonomou.nudgebite.ui.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;

import java.util.EnumMap;
import java.util.Map;

public class BottomNavBar extends LinearLayout {

    public enum Tab {HOME, LOG, PROGRESS, SETTINGS}

    public interface OnTabSelectedListener {
        void onTabSelected(Tab tab);
    }

    private final Map<Tab, ImageView> icons = new EnumMap<>(Tab.class);
    private final Map<Tab, TextView> labels = new EnumMap<>(Tab.class);
    private Tab selectedTab = Tab.HOME;
    @Nullable
    private OnTabSelectedListener listener;

    public BottomNavBar(Context context) {
        this(context, null);
    }

    public BottomNavBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        int vPad = DisplayUtils.dp(getContext(), 10);
        setPadding(0, vPad, 0, vPad);

        addTab(Tab.HOME, R.drawable.ic_nav_home, "Home");
        addTab(Tab.LOG, R.drawable.ic_nav_log, "Log");
        addTab(Tab.PROGRESS, R.drawable.ic_nav_progress, "Progress");
        addTab(Tab.SETTINGS, R.drawable.ic_nav_settings, "Settings");

        refreshTabColors();
    }

    public void setOnTabSelectedListener(@Nullable OnTabSelectedListener listener) {
        this.listener = listener;
    }

    public void setSelectedTab(Tab tab) {
        selectedTab = tab;
        refreshTabColors();
    }

    private void addTab(Tab tab, int iconResource, String label) {
        LinearLayout column = new LinearLayout(getContext());
        column.setOrientation(VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        column.setClickable(true);
        column.setFocusable(true);

        ImageView icon = new ImageView(getContext());
        icon.setImageResource(iconResource);
        column.addView(icon, new LayoutParams(DisplayUtils.dp(getContext(), 24), DisplayUtils.dp(getContext(), 24)));

        TextView labelView = new TextView(getContext());
        labelView.setText(label);
        labelView.setTextSize(11);
        labelView.setGravity(Gravity.CENTER_HORIZONTAL);
        LayoutParams labelParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        labelParams.topMargin = DisplayUtils.dp(getContext(), 4);
        column.addView(labelView, labelParams);

        column.setOnClickListener(v -> {
            setSelectedTab(tab);
            if (listener != null) {
                listener.onTabSelected(tab);
            }
        });

        icons.put(tab, icon);
        labels.put(tab, labelView);
        addView(column, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
    }

    private void refreshTabColors() {
        int selectedColor = ContextCompat.getColor(getContext(), R.color.nb_primary_green);
        int unselectedColor = ContextCompat.getColor(getContext(), R.color.nb_text_secondary);
        for (Tab tab : Tab.values()) {
            int color = tab == selectedTab ? selectedColor : unselectedColor;
            icons.get(tab).setColorFilter(color, PorterDuff.Mode.SRC_IN);
            labels.get(tab).setTextColor(color);
        }
    }
}
