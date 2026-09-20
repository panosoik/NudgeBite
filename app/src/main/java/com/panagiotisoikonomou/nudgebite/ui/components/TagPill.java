package com.panagiotisoikonomou.nudgebite.ui.components;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;

public class TagPill extends AppCompatTextView {

    public TagPill(Context context) {
        this(context, null);
    }

    public TagPill(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setTextColor(ContextCompat.getColor(getContext(), R.color.nb_primary_green));
        setTextSize(12);
        setBackgroundResource(R.drawable.bg_pill_tag_static);
        int hPad = DisplayUtils.dp(getContext(), 12);
        int vPad = DisplayUtils.dp(getContext(), 6);
        setPadding(hPad, vPad, hPad, vPad);
    }
}
