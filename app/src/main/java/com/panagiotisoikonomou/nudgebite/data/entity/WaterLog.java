package com.panagiotisoikonomou.nudgebite.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

/** One row per glass. Habit indicator only: feeds no score. */
@Entity(tableName = "water_log", indices = @androidx.room.Index("logged_at"))
public class WaterLog {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "logged_at")
    public LocalDateTime loggedAt;
}
