package com.panagiotisoikonomou.nudgebite.data.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.panagiotisoikonomou.nudgebite.data.entity.SignalEvent;

import java.util.List;

@Dao
public interface SignalEventDao {

    @Query("SELECT * FROM signal_event ORDER BY creation_date ASC")
    List<SignalEvent> getAllSync();
}
