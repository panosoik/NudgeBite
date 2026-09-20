package com.panagiotisoikonomou.nudgebite.ui.progress;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.dashboard.DayContributionEvaluator;
import com.panagiotisoikonomou.nudgebite.data.entity.Goal;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.entity.WellScore;
import com.panagiotisoikonomou.nudgebite.data.entity.relation.MealWithIngredients;
import com.panagiotisoikonomou.nudgebite.data.enums.DayContribution;
import com.panagiotisoikonomou.nudgebite.data.repository.GoalRepository;
import com.panagiotisoikonomou.nudgebite.data.repository.MealRepository;
import com.panagiotisoikonomou.nudgebite.data.repository.UserRepository;
import com.panagiotisoikonomou.nudgebite.data.repository.WellScoreRepository;
import com.panagiotisoikonomou.nudgebite.progress.GoalProgressStatus;
import com.panagiotisoikonomou.nudgebite.progress.TrackableGoalRows;
import com.panagiotisoikonomou.nudgebite.progress.WeekPeriod;
import com.panagiotisoikonomou.nudgebite.progress.well.DietQualityZone;
import com.panagiotisoikonomou.nudgebite.progress.well.FoodGroupProgress;
import com.panagiotisoikonomou.nudgebite.progress.well.FrequencyAxis;
import com.panagiotisoikonomou.nudgebite.progress.well.TargetDirection;
import com.panagiotisoikonomou.nudgebite.progress.well.TrackedFoodGroup;
import com.panagiotisoikonomou.nudgebite.progress.well.TrendSpan;
import com.panagiotisoikonomou.nudgebite.progress.well.ReviewNoteBuilder;
import com.panagiotisoikonomou.nudgebite.progress.well.WellProgressSeries;
import com.panagiotisoikonomou.nudgebite.progress.well.WellTrendPoint;
import com.panagiotisoikonomou.nudgebite.ui.components.ChartCard;
import com.panagiotisoikonomou.nudgebite.ui.components.FoodGroupPalette;
import com.panagiotisoikonomou.nudgebite.ui.components.GoalProgressCard;
import com.panagiotisoikonomou.nudgebite.ui.components.MonthDotRow;
import com.panagiotisoikonomou.nudgebite.ui.components.SegmentedToggle;
import com.panagiotisoikonomou.nudgebite.ui.components.SingleSelectList;
import com.panagiotisoikonomou.nudgebite.ui.components.TrendLineView;
import com.panagiotisoikonomou.nudgebite.ui.components.WeekDotRow;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ProgressFragment extends Fragment {

    private static final DateTimeFormatter DAY_LETTER_FORMAT = DateTimeFormatter.ofPattern("EEEEE", Locale.US);

    private List<MealWithIngredients> allMeals = new ArrayList<>();
    private List<Goal> allGoals = new ArrayList<>();
    private List<WeekPeriod> weeksInCurrentMonth = new ArrayList<>();
    @Nullable private User currentUser;

    private YearMonth currentMonth;
    private WeekPeriod selectedWeek;

    private TextView periodLabel;
    private SegmentedToggle spanSelect;
    private SingleSelectList weekPillSelect;
    private View weekSummaryRow;
    private MonthDotRow monthDotRow;
    private View qualityDot;
    private TextView qualitySummaryText;
    private TextView qualityCompletenessText;
    private WeekDotRow weekDotRow;
    private View reviewNoteCard;
    private TextView reviewNoteText;
    private TextView reviewNoteImprovementText;
    private View goalsSection;
    private TextView goalsHeaderText;
    private LinearLayout goalsContainer;
    private SingleSelectList foodGroupSelect;
    private ChartCard overallChartCard;
    private ChartCard groupChartCard;

    private List<WellScore> wellScores = new ArrayList<>();
    private TrendSpan selectedSpan = TrendSpan.WEEK;
    private TrackedFoodGroup selectedGroup = TrackedFoodGroup.values()[0];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView prevMonthButton = view.findViewById(R.id.prev_month_button);
        TextView nextMonthButton = view.findViewById(R.id.next_month_button);
        periodLabel = view.findViewById(R.id.month_label);
        spanSelect = view.findViewById(R.id.span_select);
        weekPillSelect = view.findViewById(R.id.week_pill_select);
        weekSummaryRow = view.findViewById(R.id.week_summary_row);
        monthDotRow = view.findViewById(R.id.month_dot_row);
        qualityDot = view.findViewById(R.id.quality_dot);
        qualitySummaryText = view.findViewById(R.id.quality_summary_text);
        qualityCompletenessText = view.findViewById(R.id.quality_completeness_text);
        weekDotRow = view.findViewById(R.id.week_dot_row);
        reviewNoteCard = view.findViewById(R.id.review_note_card);
        reviewNoteText = view.findViewById(R.id.review_note_text);
        reviewNoteImprovementText = view.findViewById(R.id.review_note_improvement_text);
        goalsSection = view.findViewById(R.id.goals_section);
        goalsHeaderText = view.findViewById(R.id.goals_header_text);
        goalsContainer = view.findViewById(R.id.goals_container);
        foodGroupSelect = view.findViewById(R.id.food_group_select);
        overallChartCard = view.findViewById(R.id.overall_chart_card);
        groupChartCard = view.findViewById(R.id.group_chart_card);

        overallChartCard.chart().setBands(zoneBands());
        overallChartCard.chart().setAxisLabels(zoneAxisLabels());
        overallChartCard.chart().setMinAxisGutterText(widestZoneLabel());
        groupChartCard.chart().setMinAxisGutterText(widestZoneLabel());

        List<String> spanLabels = new ArrayList<>();
        for (TrendSpan span : TrendSpan.values()) {
            spanLabels.add(span.label());
        }
        spanSelect.setSegments(spanLabels, selectedSpan.label());
        spanSelect.setOnSegmentSelectedListener(label -> {
            selectedSpan = TrendSpan.fromLabel(label);
            refreshPeriodNav();
        });

        List<SingleSelectList.ColoredOption> groupOptions = new ArrayList<>();
        for (TrackedFoodGroup group : TrackedFoodGroup.values()) {
            groupOptions.add(new SingleSelectList.ColoredOption(group.label(),
                    FoodGroupPalette.colorFor(requireContext(), group)));
        }
        foodGroupSelect.setColoredOptions(groupOptions, selectedGroup.label());
        foodGroupSelect.setOnSelectionChangedListener(label -> {
            for (TrackedFoodGroup group : TrackedFoodGroup.values()) {
                if (group.label().equals(label)) {
                    selectedGroup = group;
                    break;
                }
            }
            refreshDietQualitySection();
        });

        currentMonth = YearMonth.now();

        prevMonthButton.setOnClickListener(v -> stepPeriod(-1));
        nextMonthButton.setOnClickListener(v -> stepPeriod(1));

        refreshPeriodNav();

        new MealRepository(requireActivity().getApplication()).getAllMealsWithIngredients().observe(getViewLifecycleOwner(), meals -> {
            allMeals = meals;
            refreshWeekView();
        });
        new GoalRepository(requireActivity().getApplication()).getAllGoals().observe(getViewLifecycleOwner(), goals -> {
            allGoals = goals;
            refreshWeekView();
        });
        new UserRepository(requireActivity().getApplication()).getUser().observe(getViewLifecycleOwner(), user -> {
            currentUser = user;
            refreshWeekView();
        });
        WellScoreRepository.observeAll(requireActivity().getApplication())
                .observe(getViewLifecycleOwner(), scores -> {
                    wellScores = scores == null ? new ArrayList<>() : scores;
                    refreshWeekView();
                });
    }

    private void stepPeriod(int direction) {
        currentMonth = selectedSpan == TrendSpan.YEAR
                ? currentMonth.plusYears(direction)
                : currentMonth.plusMonths(direction);
        refreshPeriodNav();
    }

    private void refreshPeriodNav() {
        periodLabel.setText(selectedSpan.periodLabel(currentMonth, YearMonth.now()));

        weeksInCurrentMonth = WeekPeriod.weeksInMonth(currentMonth);

        List<String> weekLabels = new ArrayList<>();
        for (int i = 0; i < weeksInCurrentMonth.size(); i++) {
            weekLabels.add("Week " + (i + 1));
        }

        int defaultIndex = weeksInCurrentMonth.indexOf(WeekPeriod.currentWeek());
        if (defaultIndex < 0) {
            defaultIndex = 0;
        }
        selectedWeek = weeksInCurrentMonth.get(defaultIndex);

        weekPillSelect.setOptions(weekLabels, weekLabels.get(defaultIndex));
        weekPillSelect.setOnSelectionChangedListener(label -> {
            int index = weekLabels.indexOf(label);
            if (index >= 0) {
                selectedWeek = weeksInCurrentMonth.get(index);
                refreshWeekView();
            }
        });

        applySpanVisibility();
        refreshWeekView();
    }

    private static int visibility(boolean shown) {
        return shown ? View.VISIBLE : View.GONE;
    }

    private void applySpanVisibility() {
        boolean isWeek = selectedSpan == TrendSpan.WEEK;
        weekPillSelect.setVisibility(visibility(isWeek));
        weekSummaryRow.setVisibility(visibility(isWeek));
        weekDotRow.setVisibility(visibility(isWeek));
        reviewNoteCard.setVisibility(visibility(isWeek));
        monthDotRow.setVisibility(visibility(selectedSpan == TrendSpan.YEAR));
        if (!isWeek) {
            goalsSection.setVisibility(View.GONE);
            qualityCompletenessText.setVisibility(View.GONE);
        }
    }

    private void refreshWeekView() {
        if (selectedWeek == null || currentUser == null) {
            return;
        }
        if (selectedSpan != TrendSpan.WEEK) {
            refreshDietQualitySection();
            return;
        }

        LocalDate windowEnd = selectedWeek.endSunday;
        List<WeekDotRow.DayCell> dayCells = new ArrayList<>();
        for (LocalDate day : selectedWeek.days()) {
            DayContribution contribution = DayContributionEvaluator.contributionOn(allMeals, day);
            dayCells.add(new WeekDotRow.DayCell(
                    day.format(DAY_LETTER_FORMAT), day.getDayOfMonth(), contribution == DayContribution.POSITIVE));
        }
        weekDotRow.bind(dayCells);

        int weekNumber = weeksInCurrentMonth.indexOf(selectedWeek) + 1;
        WellScore weekScore = WellProgressSeries.weeklyRow(wellScores, selectedWeek);
        bindQualitySummary(weekNumber, weekScore);
        bindReviewNote(weekScore);

        bindGoalsSection(weekNumber, windowEnd);
        refreshDietQualitySection();
    }

    private void bindQualitySummary(int weekNumber, @Nullable WellScore weekScore) {
        if (weekScore == null || weekScore.maxPoints <= 0) {
            qualityDot.setVisibility(View.GONE);
            qualityCompletenessText.setVisibility(View.GONE);
            qualitySummaryText.setTextColor(requireContext().getColor(R.color.nb_text_secondary));
            qualitySummaryText.setText("Week " + weekNumber + ": log a few meals to see your pattern");
            return;
        }

        double fraction = (double) weekScore.totalPoints / weekScore.maxPoints;
        qualityDot.setVisibility(View.VISIBLE);
        qualitySummaryText.setTextColor(requireContext().getColor(R.color.nb_primary_green));
        qualitySummaryText.setText("Week " + weekNumber + ": " + DietQualityZone.of(fraction).weeklyMessage());

        if (weekScore.observedDays < WeekPeriod.DAYS) {
            qualityCompletenessText.setVisibility(View.VISIBLE);
            qualityCompletenessText.setText("Based on " + weekScore.observedDays
                    + " of the last " + WeekPeriod.DAYS + " days logged");
        } else {
            qualityCompletenessText.setVisibility(View.GONE);
        }
    }

    private void bindReviewNote(@Nullable WellScore weekScore) {
        if (weekScore == null) {
            reviewNoteCard.setVisibility(View.GONE);
            return;
        }
        reviewNoteCard.setVisibility(View.VISIBLE);
        ReviewNoteBuilder.ReviewNote note = ReviewNoteBuilder.build(weekScore);
        reviewNoteText.setText(String.join(" ", note.topLines));
        if (note.improvementLines.isEmpty()) {
            reviewNoteImprovementText.setVisibility(View.GONE);
        } else {
            reviewNoteImprovementText.setVisibility(View.VISIBLE);
            reviewNoteImprovementText.setText(String.join(" ", note.improvementLines));
        }
    }

    private void bindGoalsSection(int weekNumber, LocalDate windowEnd) {
        goalsContainer.removeAllViews();
        List<GoalProgressStatus> rows = TrackableGoalRows.forWeekEnding(allGoals, allMeals, windowEnd, currentUser);
        if (rows.isEmpty()) {
            goalsSection.setVisibility(View.GONE);
            return;
        }

        goalsSection.setVisibility(View.VISIBLE);
        goalsHeaderText.setText("YOUR GOALS FOR WEEK " + weekNumber);

        for (int i = 0; i < rows.size(); i++) {
            GoalProgressCard card = new GoalProgressCard(requireContext());
            card.bind(rows.get(i));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) {
                params.topMargin = DisplayUtils.dp(requireContext(), 12);
            }
            goalsContainer.addView(card, params);
        }
    }

    private void bindMonthDots() {
        int year = currentMonth.getYear();
        List<MonthDotRow.MonthCell> cells = new ArrayList<>(12);
        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            cells.add(new MonthDotRow.MonthCell(yearMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.US),
                    WellProgressSeries.monthlyRow(wellScores, yearMonth) != null));
        }
        monthDotRow.bind(cells);
    }

    private void refreshDietQualitySection() {
        if (selectedWeek == null || overallChartCard == null) {
            return;
        }
        WellProgressSeries.Data data = WellProgressSeries.build(
                wellScores, selectedSpan, selectedWeek, currentMonth, LocalDate.now());

        if (selectedSpan == TrendSpan.YEAR) {
            bindMonthDots();
        }
        String range = WellProgressSeries.describeRange(selectedSpan, selectedWeek, currentMonth);
        overallChartCard.setRangeLabel(range);
        overallChartCard.chart().bind(data.trend);

        overallChartCard.showEmptyMessage(WellTrendPoint.anyScored(data.trend)
                ? null
                : "Still building your diet profile. Keep logging meals and the trend will fill in.");
        overallChartCard.setChartDescription(describeTrend(data, range));

        bindGroupChart(data);
    }

    private static String describeTrend(WellProgressSeries.Data data, String range) {
        WellTrendPoint latest = null;
        int scored = 0;
        for (WellTrendPoint point : data.trend) {
            if (point.hasScore) {
                latest = point;
                scored++;
            }
        }
        if (latest == null) {
            return "Diet quality for " + range + ". Not enough logged yet to show a trend.";
        }
        return "Diet quality for " + range + ". " + scored + " of " + data.trend.size()
                + " periods scored. The most recent is in the "
                + DietQualityZone.of(latest.fraction).label() + " zone.";
    }

    private void bindGroupChart(WellProgressSeries.Data data) {
        FoodGroupProgress progress = data.progressOf(selectedGroup);

        int color = FoodGroupPalette.colorFor(requireContext(), selectedGroup);
        TrendLineView chart = groupChartCard.chart();

        groupChartCard.setTitle(selectedGroup.label());
        groupChartCard.setRangeLabel(progress.valueText());
        chart.setLineColor(color);
        chart.setBands(Collections.singletonList(new TrendLineView.Band(
                targetBandLow(progress), targetBandHigh(progress),
                FoodGroupPalette.bandColorFor(requireContext(), selectedGroup))));
        chart.setGuides(targetGuides(progress));
        chart.setAxisLabels(frequencyAxisLabels(progress));
        chart.setOverlayText(selectedGroup.label() + " \u00b7 " + progress.target.describeShort());
        chart.bind(progress.history);

        groupChartCard.showEmptyMessage(WellTrendPoint.anyScored(progress.history) ? null : "Not enough logged yet for this group.");
        groupChartCard.setChartDescription(selectedGroup.label() + ". "
                + (progress.hasData ? progress.valueText() + ". " : "")
                + progress.describeStatus() + ". " + progress.target.describe() + ".");
    }

    private static double targetBandLow(FoodGroupProgress progress) {
        return progress.target.direction == TargetDirection.MAX ? 0.0 : progress.targetFraction();
    }

    private static double targetBandHigh(FoodGroupProgress progress) {
        switch (progress.target.direction) {
            case RANGE:
                return progress.bandHighFraction();
            case MAX:
                return progress.targetFraction();
            case MIN:
            default:
                return 1.0;
        }
    }

    private static List<Double> targetGuides(FoodGroupProgress progress) {
        List<Double> guides = new ArrayList<>();
        guides.add(progress.targetFraction());
        if (progress.target.direction == TargetDirection.RANGE) {
            guides.add(progress.bandHighFraction());
        }
        return guides;
    }

    private static List<TrendLineView.AxisLabel> frequencyAxisLabels(FoodGroupProgress progress) {
        List<TrendLineView.AxisLabel> labels = new ArrayList<>();
        if (progress.axisMax <= 0.0) {
            return labels;
        }
        for (double tick : FrequencyAxis.ticksUpTo(progress.axisMax)) {
            labels.add(new TrendLineView.AxisLabel(tick / progress.axisMax,
                    FrequencyAxis.formatFrequency(tick)));
        }
        return labels;
    }

    private List<TrendLineView.Band> zoneBands() {
        List<TrendLineView.Band> bands = new ArrayList<>();
        for (DietQualityZone zone : DietQualityZone.values()) {
            bands.add(new TrendLineView.Band(zone.lowerFraction(), zone.upperFraction(),
                    ContextCompat.getColor(requireContext(), colorFor(zone))));
        }
        return bands;
    }

    private static List<TrendLineView.AxisLabel> zoneAxisLabels() {
        List<TrendLineView.AxisLabel> labels = new ArrayList<>();
        for (DietQualityZone zone : DietQualityZone.values()) {
            labels.add(new TrendLineView.AxisLabel(zone.labelFraction(), zone.label()));
        }
        return labels;
    }

    private static String widestZoneLabel() {
        String widest = "";
        for (DietQualityZone zone : DietQualityZone.values()) {
            if (zone.label().length() > widest.length()) {
                widest = zone.label();
            }
        }
        return widest;
    }

    private static int colorFor(DietQualityZone zone) {
        switch (zone) {
            case STRONG: return R.color.nb_zone_strong;
            case BALANCED: return R.color.nb_zone_balanced;
            case BUILDING:
            default: return R.color.nb_zone_building;
        }
    }
}
