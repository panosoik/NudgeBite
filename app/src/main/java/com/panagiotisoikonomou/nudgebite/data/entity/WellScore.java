package com.panagiotisoikonomou.nudgebite.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** WELL-inspired diet quality score for one period, keyed on window + period END (daily windows share a start date). */
@Entity(
        tableName = "well_score",
        indices = @Index(value = {"window_type", "period_end"}, unique = true)
)
public class WellScore {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "window_type")
    public String windowType;

    @NonNull
    @ColumnInfo(name = "period_start")
    public LocalDate periodStart;

    @NonNull
    @ColumnInfo(name = "period_end")
    public LocalDate periodEnd;

    @ColumnInfo(name = "total_points")
    public int totalPoints;

    /** Not the published 120: the high-sodium component is not scored. */
    @ColumnInfo(name = "max_points")
    public int maxPoints;

    @NonNull
    @ColumnInfo(name = "sufficiency")
    public String sufficiency;

    @ColumnInfo(name = "observed_days")
    public int observedDays;

    @ColumnInfo(name = "logged_meals")
    public int loggedMeals;

    /** "COMPONENT:points" pairs, comma separated. */
    @NonNull
    @ColumnInfo(name = "component_points")
    public String componentPoints;

    /** "COMPONENT:occasions:ratePerDay" triples, comma separated. */
    @NonNull
    @ColumnInfo(name = "component_frequencies")
    public String componentFrequencies;

    @NonNull
    @ColumnInfo(name = "computed_at")
    public LocalDateTime computedAt;
}
