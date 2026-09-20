package com.panagiotisoikonomou.nudgebite.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.panagiotisoikonomou.nudgebite.data.enums.CookingWillingness;
import com.panagiotisoikonomou.nudgebite.data.enums.CravingType;
import com.panagiotisoikonomou.nudgebite.data.enums.DecisionStage;
import com.panagiotisoikonomou.nudgebite.data.enums.DeliveryIntent;
import com.panagiotisoikonomou.nudgebite.data.enums.DinnerPlan;
import com.panagiotisoikonomou.nudgebite.data.enums.FoodAvailability;
import com.panagiotisoikonomou.nudgebite.data.enums.InputMode;
import com.panagiotisoikonomou.nudgebite.data.enums.UserTone;

import java.time.LocalDateTime;

@Entity(
        tableName = "momentary_assessment_event",
        foreignKeys = @ForeignKey(
                entity = EatingOccasionEvent.class,
                parentColumns = "id",
                childColumns = "eating_occasion_event_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("eating_occasion_event_id")
)
public class MomentaryAssessmentEvent {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "creation_date")
    public LocalDateTime creationDate;

    @ColumnInfo(name = "eating_occasion_event_id")
    public long eatingOccasionEventId;

    @ColumnInfo(name = "hunger_level")
    public Integer hungerLevel;

    @ColumnInfo(name = "fatigue_level")
    public Integer fatigueLevel;

    @ColumnInfo(name = "time_pressure")
    public Integer timePressure;

    @NonNull
    @ColumnInfo(name = "food_available_at_home")
    public FoodAvailability foodAvailableAtHome;

    @NonNull
    @ColumnInfo(name = "meal_decision_stage")
    public DecisionStage mealDecisionStage;

    @NonNull
    @ColumnInfo(name = "delivery_intent")
    public DeliveryIntent deliveryIntent;

    /** Null means the question was never asked; only the low-energy work-departure branch asks it. */
    @ColumnInfo(name = "dinner_planned")
    public DinnerPlan dinnerPlanned;

    @NonNull
    @ColumnInfo(name = "cooking_willingness")
    public CookingWillingness cookingWillingness;

    @NonNull
    @ColumnInfo(name = "craving_type")
    public CravingType cravingType;

    @NonNull
    @ColumnInfo(name = "user_tone")
    public UserTone userTone;

    @NonNull
    @ColumnInfo(name = "input_mode")
    public InputMode inputMode;

    @ColumnInfo(name = "confidence")
    public Float confidence;

    @ColumnInfo(name = "needs_clarification")
    public boolean needsClarification;
}
