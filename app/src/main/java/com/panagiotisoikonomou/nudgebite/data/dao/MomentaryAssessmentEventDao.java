package com.panagiotisoikonomou.nudgebite.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.panagiotisoikonomou.nudgebite.data.entity.MomentaryAssessmentEvent;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface MomentaryAssessmentEventDao {

    @Insert
    long insert(MomentaryAssessmentEvent momentaryAssessmentEvent);

    @Query("SELECT * FROM momentary_assessment_event ORDER BY creation_date ASC")
    List<MomentaryAssessmentEvent> getAllSync();

    @Query("SELECT * FROM momentary_assessment_event WHERE creation_date >= :since "
            + "ORDER BY creation_date DESC")
    List<MomentaryAssessmentEvent> getSinceNewestFirstSync(LocalDateTime since);
}
