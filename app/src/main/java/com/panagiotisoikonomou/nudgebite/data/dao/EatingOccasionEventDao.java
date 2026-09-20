package com.panagiotisoikonomou.nudgebite.data.dao;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.panagiotisoikonomou.nudgebite.data.entity.EatingOccasionEvent;
import com.panagiotisoikonomou.nudgebite.data.enums.DecisionStage;
import com.panagiotisoikonomou.nudgebite.data.enums.EatingOccasionOutcome;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface EatingOccasionEventDao {

    @Insert
    long insert(EatingOccasionEvent eatingOccasionEvent);

    @Query("DELETE FROM eating_occasion_event")
    void deleteAll();

    @Query("SELECT * FROM eating_occasion_event WHERE id = :id")
    @Nullable
    EatingOccasionEvent getByIdSync(long id);

    @Query("UPDATE eating_occasion_event SET decision_stage = :stage WHERE id = :id")
    void updateDecisionStage(long id, DecisionStage stage);

    @Query("UPDATE eating_occasion_event SET outcome = :outcome WHERE id = :id")
    void updateOutcome(long id, EatingOccasionOutcome outcome);

    @Query("SELECT * FROM eating_occasion_event WHERE creation_date >= :startInclusive "
            + "AND creation_date < :endExclusive ORDER BY creation_date")
    List<EatingOccasionEvent> getEventsBetweenSync(LocalDateTime startInclusive, LocalDateTime endExclusive);

    @Query("SELECT * FROM eating_occasion_event ORDER BY creation_date ASC")
    List<EatingOccasionEvent> getAllSync();
}
