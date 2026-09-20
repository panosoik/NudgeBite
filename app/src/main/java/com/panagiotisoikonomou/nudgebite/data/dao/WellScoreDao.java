package com.panagiotisoikonomou.nudgebite.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.panagiotisoikonomou.nudgebite.data.entity.WellScore;

import java.util.List;

@Dao
public interface WellScoreDao {

    /** Replaces the row for the same window and period end, so recomputing never duplicates. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(WellScore score);

    @Query("SELECT * FROM well_score ORDER BY window_type ASC, period_start ASC")
    List<WellScore> getAllSync();

    @Query("SELECT * FROM well_score ORDER BY window_type ASC, period_start ASC")
    LiveData<List<WellScore>> getAll();

    @Query("DELETE FROM well_score")
    void deleteAll();
}
