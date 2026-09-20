package com.panagiotisoikonomou.nudgebite.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.panagiotisoikonomou.nudgebite.data.enums.InterventionCategory;
import com.panagiotisoikonomou.nudgebite.data.enums.InterventionFormat;
import com.panagiotisoikonomou.nudgebite.data.enums.UserResponse;

import java.time.LocalDateTime;

@Entity(
        tableName = "intervention_log",
        foreignKeys = @ForeignKey(
                entity = EatingOccasionEvent.class,
                parentColumns = "id",
                childColumns = "eating_occasion_event_id",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = @Index("eating_occasion_event_id")
)
public class InterventionLog {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "creation_date")
    public LocalDateTime creationDate;

    @ColumnInfo(name = "eating_occasion_event_id")
    public Long eatingOccasionEventId;

    @NonNull
    @ColumnInfo(name = "category")
    public InterventionCategory category;

    @NonNull
    @ColumnInfo(name = "format")
    public InterventionFormat format;

    @NonNull
    @ColumnInfo(name = "user_response")
    public UserResponse userResponse;

    @ColumnInfo(name = "responded_at")
    public LocalDateTime respondedAt;

    @ColumnInfo(name = "payload")
    public String payload;
}
