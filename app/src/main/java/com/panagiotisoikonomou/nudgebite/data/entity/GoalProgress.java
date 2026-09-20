package com.panagiotisoikonomou.nudgebite.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(
        tableName = "goal_progress",
        foreignKeys = @ForeignKey(
                entity = Goal.class,
                parentColumns = "id",
                childColumns = "goal_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("goal_id")
)
public class GoalProgress {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "goal_id")
    public long goalId;

    @NonNull
    @ColumnInfo(name = "logged_date")
    public LocalDate loggedDate;
}
