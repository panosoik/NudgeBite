package com.panagiotisoikonomou.nudgebite.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.panagiotisoikonomou.nudgebite.data.enums.DecisionStage;
import com.panagiotisoikonomou.nudgebite.data.enums.EatingOccasionOutcome;
import com.panagiotisoikonomou.nudgebite.data.enums.TriggerCombo;
import com.panagiotisoikonomou.nudgebite.data.enums.TriggerType;

import java.time.LocalDateTime;

@Entity(tableName = "eating_occasion_event")
public class EatingOccasionEvent {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "creation_date")
    public LocalDateTime creationDate;

    @NonNull
    @ColumnInfo(name = "trigger_combo")
    public TriggerCombo triggerCombo;

    @NonNull
    @ColumnInfo(name = "trigger_type")
    public TriggerType triggerType;

    @NonNull
    @ColumnInfo(name = "decision_stage")
    public DecisionStage decisionStage;

    @ColumnInfo(name = "outcome")
    public EatingOccasionOutcome outcome;
}
