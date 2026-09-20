package com.panagiotisoikonomou.nudgebite.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.progress.well.WellTrendPoint;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

/** Unscored samples leave a gap in the line; provisional samples are drawn hollow. */
public class TrendLineView extends View {

    public static final class Band {
        final double lowFraction;
        final double highFraction;
        @ColorInt final int color;

        public Band(double lowFraction, double highFraction, @ColorInt int color) {
            this.lowFraction = lowFraction;
            this.highFraction = highFraction;
            this.color = color;
        }
    }

    public static final class AxisLabel {
        final double fraction;
        final String text;

        public AxisLabel(double fraction, String text) {
            this.fraction = fraction;
            this.text = text;
        }
    }

    private final List<WellTrendPoint> points = new ArrayList<>();
    private final List<Band> bands = new ArrayList<>();
    private final List<AxisLabel> axisLabels = new ArrayList<>();
    private final List<Double> guideFractions = new ArrayList<>();

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hollowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint baselinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint guidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final RectF rect = new RectF();

    private final int dotRadius;
    private final int labelGap;
    private final int overlayGap;
    private final int verticalPadding;
    private final int axisGap;

    @Nullable private String overlayText;
    @Nullable private String minAxisGutterText;

    public TrendLineView(Context context) {
        this(context, null);
    }

    public TrendLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        int green = ContextCompat.getColor(context, R.color.nb_primary_green);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(DisplayUtils.dp(context, 2));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        dotPaint.setStyle(Paint.Style.FILL);
        hollowPaint.setStyle(Paint.Style.STROKE);
        hollowPaint.setStrokeWidth(DisplayUtils.dp(context, 2));
        bandPaint.setStyle(Paint.Style.FILL);

        baselinePaint.setColor(ContextCompat.getColor(context, R.color.nb_neutral_border));
        baselinePaint.setStyle(Paint.Style.STROKE);
        baselinePaint.setStrokeWidth(Math.max(1, DisplayUtils.dp(context, 1) / 2));

        guidePaint.setColor(ContextCompat.getColor(context, R.color.nb_chart_guide));
        guidePaint.setStyle(Paint.Style.STROKE);
        guidePaint.setStrokeWidth(Math.max(1, DisplayUtils.dp(context, 1)));
        guidePaint.setPathEffect(new DashPathEffect(
                new float[]{DisplayUtils.dp(context, 4), DisplayUtils.dp(context, 4)}, 0));

        labelPaint.setColor(ContextCompat.getColor(context, R.color.nb_text_secondary));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(DisplayUtils.dp(context, 11));

        axisLabelPaint.setColor(ContextCompat.getColor(context, R.color.nb_text_secondary));
        axisLabelPaint.setTextAlign(Paint.Align.LEFT);
        axisLabelPaint.setTextSize(DisplayUtils.dp(context, 10));

        overlayPaint.setTextAlign(Paint.Align.RIGHT);
        overlayPaint.setTextSize(DisplayUtils.dp(context, 11));

        setLineColor(green);
        dotRadius = DisplayUtils.dp(context, 3);
        labelGap = DisplayUtils.dp(context, 16);
        overlayGap = DisplayUtils.dp(context, 16);
        verticalPadding = DisplayUtils.dp(context, 10);
        axisGap = DisplayUtils.dp(context, 6);
    }

    public void bind(List<WellTrendPoint> trendPoints) {
        points.clear();
        points.addAll(trendPoints);
        invalidate();
    }

    public void setLineColor(@ColorInt int color) {
        linePaint.setColor(color);
        dotPaint.setColor(color);
        hollowPaint.setColor(color);
        overlayPaint.setColor(color);
        invalidate();
    }

    public void setBands(List<Band> newBands) {
        bands.clear();
        bands.addAll(newBands);
        invalidate();
    }

    public void setAxisLabels(List<AxisLabel> labels) {
        axisLabels.clear();
        axisLabels.addAll(labels);
        invalidate();
    }

    public void setGuides(List<Double> fractions) {
        guideFractions.clear();
        guideFractions.addAll(fractions);
        invalidate();
    }

    public void setOverlayText(@Nullable String text) {
        this.overlayText = text;
        invalidate();
    }

    public void setMinAxisGutterText(@Nullable String text) {
        this.minAxisGutterText = text;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (points.isEmpty()) {
            return;
        }

        float labelSpace = labelGap;
        float overlaySpace = overlayText == null ? 0f : overlayGap;
        float plotBottom = getHeight() - getPaddingBottom() - labelSpace - overlaySpace;
        float plotTop = getPaddingTop() + verticalPadding;
        float plotHeight = plotBottom - plotTop - verticalPadding;
        if (plotHeight <= 0) {
            return;
        }

        float axisGutter = 0f;
        for (AxisLabel label : axisLabels) {
            axisGutter = Math.max(axisGutter, axisLabelPaint.measureText(label.text));
        }
        if (minAxisGutterText != null) {
            axisGutter = Math.max(axisGutter, axisLabelPaint.measureText(minAxisGutterText));
        }
        if (axisGutter > 0f) {
            axisGutter += axisGap;
        }

        float plotLeft = getPaddingLeft() + axisGutter;
        float plotRight = getWidth() - getPaddingRight();
        if (plotRight <= plotLeft) {
            return;
        }

        for (Band band : bands) {
            bandPaint.setColor(band.color);
            float top = yFor(Math.max(band.lowFraction, band.highFraction), plotBottom, plotHeight);
            float bottom = yFor(Math.min(band.lowFraction, band.highFraction), plotBottom, plotHeight);
            rect.set(plotLeft, top, plotRight, bottom);
            canvas.drawRect(rect, bandPaint);
        }

        for (double fraction : guideFractions) {
            float y = yFor(fraction, plotBottom, plotHeight);
            canvas.drawLine(plotLeft, y, plotRight, y, guidePaint);
        }

        canvas.drawLine(plotLeft, plotBottom, plotRight, plotBottom, baselinePaint);

        for (AxisLabel label : axisLabels) {
            float y = yFor(label.fraction, plotBottom, plotHeight);
            float baseline = Math.max(getPaddingTop() + axisLabelPaint.getTextSize(),
                    Math.min(plotBottom, y + axisLabelPaint.getTextSize() / 3f));
            canvas.drawText(label.text, getPaddingLeft(), baseline, axisLabelPaint);
        }

        float inset = dotRadius;
        for (WellTrendPoint point : points) {
            inset = Math.max(inset, labelPaint.measureText(point.label) / 2f);
        }
        float usableWidth = Math.max(0f, plotRight - plotLeft - inset * 2f);
        float step = points.size() > 1 ? usableWidth / (points.size() - 1) : 0f;
        float startX = points.size() > 1 ? plotLeft + inset : plotLeft + usableWidth / 2f + inset;

        linePath.reset();
        boolean penDown = false;
        for (int i = 0; i < points.size(); i++) {
            WellTrendPoint point = points.get(i);
            float x = startX + step * i;
            if (!point.hasScore) {
                penDown = false;
                continue;
            }
            float y = yFor(point.fraction, plotBottom, plotHeight);
            if (penDown) {
                linePath.lineTo(x, y);
            } else {
                linePath.moveTo(x, y);
                penDown = true;
            }
        }
        canvas.drawPath(linePath, linePaint);

        for (int i = 0; i < points.size(); i++) {
            WellTrendPoint point = points.get(i);
            float x = startX + step * i;
            if (point.hasScore) {
                canvas.drawCircle(x, yFor(point.fraction, plotBottom, plotHeight), dotRadius,
                        point.provisional ? hollowPaint : dotPaint);
            }
            canvas.drawText(point.label, x, getHeight() - getPaddingBottom() - labelPaint.descent(), labelPaint);
        }

        if (overlayText != null) {
            canvas.drawText(overlayText, plotRight,
                    plotBottom + overlayGap - overlayPaint.descent(), overlayPaint);
        }
    }

    private static float yFor(double fraction, float plotBottom, float plotHeight) {
        double clamped = Math.max(0.0, Math.min(1.0, fraction));
        return plotBottom - (float) clamped * plotHeight;
    }
}
