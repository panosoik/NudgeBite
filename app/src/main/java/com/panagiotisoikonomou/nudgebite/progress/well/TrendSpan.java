package com.panagiotisoikonomou.nudgebite.progress.well;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public enum TrendSpan {

    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year");

    private final String label;

    TrendSpan(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public String periodLabel(YearMonth period, YearMonth today) {
        if (this == YEAR) {
            return String.valueOf(period.getYear());
        }
        String month = period.getMonth().getDisplayName(TextStyle.FULL, Locale.US);
        return period.getYear() == today.getYear() ? month : month + " " + period.getYear();
    }

    public static TrendSpan fromLabel(String label) {
        for (TrendSpan span : values()) {
            if (span.label.equals(label)) {
                return span;
            }
        }
        return WEEK;
    }
}
