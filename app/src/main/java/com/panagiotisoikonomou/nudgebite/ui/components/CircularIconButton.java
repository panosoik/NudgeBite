package com.panagiotisoikonomou.nudgebite.ui.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;

public class CircularIconButton extends AppCompatImageButton {

    public CircularIconButton(Context context) {
        this(context, null);
    }

    public CircularIconButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Drawable content = ContextCompat.getDrawable(getContext(), R.drawable.bg_circular_icon_button);
        setBackground(new RippleDrawable(
                ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.nb_row_selected_bg)),
                content, null));
        int pad = DisplayUtils.dp(getContext(), 14);
        setPadding(pad, pad, pad, pad);
        setScaleType(ScaleType.CENTER_INSIDE);
    }
}
