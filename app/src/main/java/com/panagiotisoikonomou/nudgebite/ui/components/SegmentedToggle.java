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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SegmentedToggle extends LinearLayout {

    public interface OnSegmentSelectedListener {
        void onSegmentSelected(String label);
    }

    private final Map<String, TextView> segmentsByLabel = new LinkedHashMap<>();
    @Nullable private String selected;
    @Nullable private OnSegmentSelectedListener listener;

    private final int inset;

    public SegmentedToggle(Context context) {
        this(context, null);
    }

    public SegmentedToggle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setBackgroundResource(R.drawable.bg_segmented_track);
        inset = DisplayUtils.dp(context, 4);
        setPadding(inset, inset, inset, inset);
    }

    public void setOnSegmentSelectedListener(@Nullable OnSegmentSelectedListener listener) {
        this.listener = listener;
    }

    public void setSegments(List<String> labels, @Nullable String preSelected) {
        removeAllViews();
        segmentsByLabel.clear();
        selected = preSelected;

        for (String label : labels) {
            TextView segment = buildSegment(label);
            segmentsByLabel.put(label, segment);
            addView(segment, new LayoutParams(0, DisplayUtils.dp(getContext(), 40), 1f));
        }
        applySelection();
    }

    private TextView buildSegment(String label) {
        TextView segment = new TextView(getContext());
        segment.setText(label);
        segment.setGravity(Gravity.CENTER);
        segment.setTextSize(15);
        segment.setClickable(true);
        segment.setFocusable(true);
        segment.setOnClickListener(v -> select(label));
        return segment;
    }

    private void select(String label) {
        if (label.equals(selected)) {
            return;
        }
        selected = label;
        applySelection();
        if (listener != null) {
            listener.onSegmentSelected(label);
        }
    }

    private void applySelection() {
        for (Map.Entry<String, TextView> entry : segmentsByLabel.entrySet()) {
            boolean isSelected = entry.getKey().equals(selected);
            TextView segment = entry.getValue();
            segment.setBackgroundResource(isSelected ? R.drawable.bg_segmented_selected : 0);
            segment.setTextColor(ContextCompat.getColor(getContext(),
                    isSelected ? R.color.white : R.color.nb_text_primary));
            segment.setTypeface(Typeface.create(Typeface.DEFAULT,
                    isSelected ? Typeface.BOLD : Typeface.NORMAL));
            segment.setSelected(isSelected);
            segment.setContentDescription(entry.getKey() + (isSelected ? ", selected" : ""));
        }
    }
}
