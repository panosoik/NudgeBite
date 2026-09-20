package com.panagiotisoikonomou.nudgebite.ui.components;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;

public class WaterCounterView extends LinearLayout {

    public interface OnGlassChangeListener {
        void onAddGlass();

        void onRemoveGlass();
    }

    private final TextView countView;
    private final TextView minusButton;
    private final TextView plusButton;
    @Nullable private OnGlassChangeListener listener;

    private boolean interactive = true;

    private int glasses;

    public WaterCounterView(Context context) {
        this(context, null);
    }

    public WaterCounterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackgroundResource(R.drawable.bg_water_counter);
        int pad = DisplayUtils.dp(context, 10);
        setPadding(pad, pad, pad, pad);

        ImageView glass = new ImageView(context);
        glass.setImageResource(R.drawable.ic_water_glass);
        glass.setBackgroundResource(R.drawable.bg_water_icon_tile);
        glass.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int tile = DisplayUtils.dp(context, 40);
        int glassPad = DisplayUtils.dp(context, 8);
        glass.setPadding(glassPad, glassPad, glassPad, glassPad);
        glass.setContentDescription("Glasses of water");
        addView(glass, new LayoutParams(tile, tile));

        minusButton = stepButton(context, "−", "Remove a glass");
        LayoutParams minusParams = stepParams(context);
        minusParams.leftMargin = DisplayUtils.dp(context, 12);
        addView(minusButton, minusParams);

        countView = new TextView(context);
        countView.setGravity(Gravity.CENTER);
        countView.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        countView.setTextColor(ContextCompat.getColor(context, R.color.nb_text_primary));
        countView.setTextSize(22);
        addView(countView, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        plusButton = stepButton(context, "+", "Add a glass");
        addView(plusButton, stepParams(context));

        setGlasses(0);
    }

    private TextView stepButton(Context context, String label, String description) {
        TextView button = new TextView(context);
        button.setText(label);
        button.setGravity(Gravity.CENTER);
        button.setTextSize(20);
        button.setTextColor(ContextCompat.getColor(context, R.color.nb_text_primary));
        button.setBackgroundResource(R.drawable.bg_water_step_button);
        button.setClickable(true);
        button.setFocusable(true);
        button.setContentDescription(description);
        return button;
    }

    private LayoutParams stepParams(Context context) {
        int size = DisplayUtils.dp(context, 48);
        return new LayoutParams(size, size);
    }

    public void setOnGlassChangeListener(@Nullable OnGlassChangeListener listener) {
        this.listener = listener;
        minusButton.setOnClickListener(v -> {
            if (this.listener != null && glasses > 0) {
                this.listener.onRemoveGlass();
            }
        });
        plusButton.setOnClickListener(v -> {
            if (this.listener != null) {
                this.listener.onAddGlass();
            }
        });
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
        minusButton.setVisibility(interactive ? VISIBLE : GONE);
        plusButton.setVisibility(interactive ? VISIBLE : GONE);
        setGlasses(glasses);
    }

    public void setGlasses(int glasses) {
        this.glasses = Math.max(0, glasses);
        countView.setText(String.valueOf(this.glasses));
        minusButton.setEnabled(this.glasses > 0);
        minusButton.setAlpha(this.glasses > 0 ? 1f : 0.4f);
        setContentDescription(this.glasses
                + (this.glasses == 1 ? " glass" : " glasses") + " of water logged"
                + (interactive ? " today" : " that day"));
    }
}
