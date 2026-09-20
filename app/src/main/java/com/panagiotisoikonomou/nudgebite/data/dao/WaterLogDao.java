package com.panagiotisoikonomou.nudgebite.data.dao;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.panagiotisoikonomou.nudgebite.data.entity.WaterLog;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface WaterLogDao {

    @Insert
    long insert(WaterLog waterLog);

    @Query("SELECT COUNT(*) FROM water_log WHERE logged_at >= :startInclusive AND logged_at < :endExclusive")
    int countBetweenSync(LocalDateTime startInclusive, LocalDateTime endExclusive);

    @Query("SELECT COUNT(*) FROM water_log WHERE logged_at >= :startInclusive AND logged_at < :endExclusive")
    LiveData<Integer> countBetween(LocalDateTime startInclusive, LocalDateTime endExclusive);

    @Query("SELECT MAX(logged_at) FROM water_log")
    @Nullable
    LocalDateTime lastLoggedAtSync();

    @Query("SELECT * FROM water_log WHERE logged_at >= :startInclusive AND logged_at < :endExclusive "
            + "ORDER BY logged_at DESC, id DESC LIMIT 1")
    @Nullable
    WaterLog mostRecentBetweenSync(LocalDateTime startInclusive, LocalDateTime endExclusive);

    @Query("DELETE FROM water_log WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM water_log ORDER BY logged_at ASC")
    List<WaterLog> getAllSync();
}
