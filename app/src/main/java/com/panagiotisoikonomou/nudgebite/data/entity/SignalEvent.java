package com.panagiotisoikonomou.nudgebite.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.panagiotisoikonomou.nudgebite.data.enums.SignalType;

import java.time.LocalDateTime;

@Entity(
        tableName = "signal_event",
        foreignKeys = @ForeignKey(
                entity = EatingOccasionEvent.class,
                parentColumns = "id",
                childColumns = "eating_occasion_event_id",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = @Index("eating_occasion_event_id")
)
public class SignalEvent {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "type")
    public SignalType type;

    @NonNull
    @ColumnInfo(name = "creation_date")
    public LocalDateTime creationDate;

    @ColumnInfo(name = "eating_occasion_event_id")
    public Long eatingOccasionEventId;
}
