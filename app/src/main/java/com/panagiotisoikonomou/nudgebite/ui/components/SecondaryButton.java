package com.panagiotisoikonomou.nudgebite.ui.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;

public class SecondaryButton extends AppCompatButton {

    public SecondaryButton(Context context) {
        this(context, null);
    }

    public SecondaryButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setAllCaps(false);
        setTextColor(ContextCompat.getColor(getContext(), R.color.nb_primary_green));
        setTextSize(16);
        int vPad = DisplayUtils.dp(getContext(), 16);
        setPadding(DisplayUtils.dp(getContext(), 24), vPad, DisplayUtils.dp(getContext(), 24), vPad);
        setStateListAnimator(null);
        applyStyle(R.drawable.bg_button_secondary, R.color.nb_row_selected_bg, R.color.nb_primary_green);
    }

    public void setDestructive(boolean destructive) {
        if (destructive) {
            applyStyle(R.drawable.bg_button_secondary_destructive, R.color.nb_row_selected_bg_red,
                    R.color.nb_destructive_red);
        } else {
            applyStyle(R.drawable.bg_button_secondary, R.color.nb_row_selected_bg, R.color.nb_primary_green);
        }
    }

    private void applyStyle(int backgroundRes, int rippleColorRes, int textColorRes) {
        setTextColor(ContextCompat.getColor(getContext(), textColorRes));
        Drawable content = ContextCompat.getDrawable(getContext(), backgroundRes);
        setBackground(new RippleDrawable(
                ColorStateList.valueOf(ContextCompat.getColor(getContext(), rippleColorRes)),
                content, null));
    }
}
