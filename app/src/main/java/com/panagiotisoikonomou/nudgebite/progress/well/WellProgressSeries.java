package com.panagiotisoikonomou.nudgebite.progress.well;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.entity.WellScore;
import com.panagiotisoikonomou.nudgebite.data.repository.WellScoreComponents;
import com.panagiotisoikonomou.nudgebite.food.well.WellComponent;
import com.panagiotisoikonomou.nudgebite.progress.WeekPeriod;
import com.panagiotisoikonomou.nudgebite.quality.well.WellDataSufficiency;
import com.panagiotisoikonomou.nudgebite.quality.well.WellWindow;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class WellProgressSeries {

    private static final String[] DAY_LETTERS = {"M", "T", "W", "T", "F", "S", "S"};
    private static final String[] MONTH_LETTERS = {"J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"};

    private WellProgressSeries() {
    }

    public static final class Data {
        public final TrendSpan span;
        public final List<WellTrendPoint> trend;
        public final List<FoodGroupProgress> groups;

        Data(TrendSpan span, List<WellTrendPoint> trend, List<FoodGroupProgress> groups) {
            this.span = span;
            this.trend = Collections.unmodifiableList(trend);
            this.groups = Collections.unmodifiableList(groups);
        }

        public FoodGroupProgress progressOf(TrackedFoodGroup group) {
            for (FoodGroupProgress progress : groups) {
                if (progress.group == group) {
                    return progress;
                }
            }
            throw new IllegalArgumentException("no progress for " + group);
        }
    }

    private static final class Sample {
        final String label;
        @Nullable final WellScore row;
        final Map<WellComponent, WellScoreComponents.Frequency> frequencies;

        Sample(String label, @Nullable WellScore row) {
            this.label = label;
            this.row = row;
            this.frequencies = row == null
                    ? Collections.emptyMap()
                    : WellScoreComponents.parseFrequencies(row.componentFrequencies);
        }
    }

    public static Data build(List<WellScore> rows, TrendSpan span, WeekPeriod selectedWeek,
                             YearMonth selectedMonth, LocalDate today) {
        List<Sample> samples = samplesFor(rows, span, selectedWeek, selectedMonth, today);

        List<WellTrendPoint> trend = new ArrayList<>(samples.size());
        for (Sample sample : samples) {
            trend.add(trendPoint(sample));
        }

        List<Sample> summaryRows = summaryRowsFor(rows, span, selectedWeek, selectedMonth);
        List<FoodGroupProgress> groups = new ArrayList<>(TrackedFoodGroup.values().length);
        for (TrackedFoodGroup group : TrackedFoodGroup.values()) {
            groups.add(progressFor(group, summaryRows, samples));
        }
        return new Data(span, trend, groups);
    }

    private static List<Sample> samplesFor(List<WellScore> rows, TrendSpan span, WeekPeriod week,
                                           YearMonth month, LocalDate today) {
        List<Sample> samples = new ArrayList<>();
        switch (span) {
            case MONTH:
                List<WeekPeriod> weeks = WeekPeriod.weeksInMonth(month);
                for (int i = 0; i < weeks.size(); i++) {
                    samples.add(new Sample("W" + (i + 1),
                            findByEnd(rows, WellWindow.WEEKLY, weeks.get(i).endSunday)));
                }
                return samples;
            case YEAR:
                int year = month.getYear();
                int lastMonth = year == today.getYear() ? today.getMonthValue() : 12;
                for (int monthValue = 1; monthValue <= lastMonth; monthValue++) {
                    samples.add(new Sample(MONTH_LETTERS[monthValue - 1],
                            monthlyRow(rows, YearMonth.of(year, monthValue))));
                }
                return samples;
            case WEEK:
            default:
                List<LocalDate> days = week.days();
                for (int i = 0; i < days.size(); i++) {
                    samples.add(new Sample(DAY_LETTERS[i],
                            findByEnd(rows, WellWindow.DAILY, days.get(i))));
                }
                return samples;
        }
    }

    private static List<Sample> summaryRowsFor(List<WellScore> rows, TrendSpan span,
                                               WeekPeriod week, YearMonth month) {
        switch (span) {
            case MONTH:
                return singletonOrEmpty(monthlyRow(rows, month));
            case YEAR:
                List<Sample> inYear = new ArrayList<>();
                for (WellScore row : rows) {
                    if (WellWindow.MONTHLY.name().equals(row.windowType)
                            && row.periodEnd.getYear() == month.getYear()) {
                        inYear.add(new Sample(String.valueOf(row.periodEnd.getMonthValue()), row));
                    }
                }
                return inYear;
            case WEEK:
            default:
                return singletonOrEmpty(findByEnd(rows, WellWindow.WEEKLY, week.endSunday));
        }
    }

    private static List<Sample> singletonOrEmpty(@Nullable WellScore row) {
        return row == null ? Collections.emptyList() : Collections.singletonList(new Sample("", row));
    }

    private static WellTrendPoint trendPoint(Sample sample) {
        WellScore row = sample.row;
        if (row == null || row.maxPoints <= 0) {
            return WellTrendPoint.noScore(sample.label);
        }
        return new WellTrendPoint(sample.label, true, (double) row.totalPoints / row.maxPoints,
                WellDataSufficiency.PROVISIONAL.name().equals(row.sufficiency));
    }

    private static FoodGroupProgress progressFor(TrackedFoodGroup group, List<Sample> summaryRows,
                                                 List<Sample> samples) {
        FoodGroupTarget target = FoodGroupTargets.of(group);

        double rateTotal = 0.0;
        int scoredRows = 0;
        for (Sample summary : summaryRows) {
            WellScoreComponents.Frequency frequency = summary.frequencies.get(group.component());
            if (frequency == null) {
                continue;
            }
            rateTotal += frequency.ratePerDay;
            scoredRows++;
        }
        boolean hasData = scoredRows > 0;
        double observed = hasData
                ? FoodGroupProgress.roundToShownPrecision(target.unit.fromRatePerDay(rateTotal / scoredRows))
                : 0.0;

        List<Double> sampleValues = new ArrayList<>(samples.size());
        double highestSample = 0.0;
        for (Sample sample : samples) {
            WellScoreComponents.Frequency frequency = sample.frequencies.get(group.component());
            Double value = frequency == null ? null : target.unit.fromRatePerDay(frequency.ratePerDay);
            sampleValues.add(value);
            if (value != null) {
                highestSample = Math.max(highestSample, value);
            }
        }

        double axisMax = FoodGroupProgress.axisMaxFor(target, Math.max(observed, highestSample));
        List<WellTrendPoint> history = new ArrayList<>(samples.size());
        for (int i = 0; i < samples.size(); i++) {
            Double value = sampleValues.get(i);
            String label = samples.get(i).label;
            history.add(value == null
                    ? WellTrendPoint.noScore(label)
                    : new WellTrendPoint(label, true, Math.min(1.0, value / axisMax), false));
        }
        return new FoodGroupProgress(group, target, hasData, observed,
                FoodGroupProgress.statusFor(target, observed), axisMax, history);
    }

    @Nullable
    public static WellScore weeklyRow(List<WellScore> rows, WeekPeriod week) {
        return findByEnd(rows, WellWindow.WEEKLY, week.endSunday);
    }

    @Nullable
    public static WellScore monthlyRow(List<WellScore> rows, YearMonth month) {
        for (WellScore row : rows) {
            if (WellWindow.MONTHLY.name().equals(row.windowType)
                    && YearMonth.from(row.periodEnd).equals(month)) {
                return row;
            }
        }
        return null;
    }

    @Nullable
    private static WellScore findByEnd(List<WellScore> rows, WellWindow window, LocalDate end) {
        for (WellScore row : rows) {
            if (window.name().equals(row.windowType) && row.periodEnd.equals(end)) {
                return row;
            }
        }
        return null;
    }

    public static String describeRange(TrendSpan span, WeekPeriod week, YearMonth month) {
        switch (span) {
            case MONTH:
                return month.getMonth().getDisplayName(TextStyle.FULL, Locale.US);
            case YEAR:
                return "Jan - Dec " + month.getYear();
            case WEEK:
            default:
                DateTimeFormatter dayAndMonth = DateTimeFormatter.ofPattern("MMM d", Locale.US);
                DateTimeFormatter dayOnly = DateTimeFormatter.ofPattern("d", Locale.US);
                boolean sameMonth = week.startMonday.getMonth() == week.endSunday.getMonth();
                return dayAndMonth.format(week.startMonday) + "-"
                        + (sameMonth ? dayOnly : dayAndMonth).format(week.endSunday);
        }
    }
}
