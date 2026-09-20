package com.panagiotisoikonomou.nudgebite.ui.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;

public class PrimaryButton extends AppCompatButton {

    public PrimaryButton(Context context) {
        this(context, null);
    }

    public PrimaryButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setAllCaps(false);
        setTextColor(Color.WHITE);
        setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        setTextSize(16);
        int vPad = DisplayUtils.dp(getContext(), 16);
        setPadding(DisplayUtils.dp(getContext(), 24), vPad, DisplayUtils.dp(getContext(), 24), vPad);
        setStateListAnimator(null);
        updateBackground();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateBackground();
    }

    private void updateBackground() {
        int bgRes = isEnabled() ? R.drawable.bg_button_primary : R.drawable.bg_button_primary_disabled;
        Drawable content = ContextCompat.getDrawable(getContext(), bgRes);
        if (isEnabled()) {
            setBackground(new RippleDrawable(ColorStateList.valueOf(Color.argb(60, 255, 255, 255)), content, null));
        } else {
            setBackground(content);
        }
    }
}
