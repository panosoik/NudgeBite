package com.panagiotisoikonomou.nudgebite.data.dao;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.panagiotisoikonomou.nudgebite.data.entity.InterventionLog;
import com.panagiotisoikonomou.nudgebite.data.enums.InterventionCategory;
import com.panagiotisoikonomou.nudgebite.data.enums.InterventionFormat;
import com.panagiotisoikonomou.nudgebite.data.enums.UserResponse;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface InterventionLogDao {

    @Insert
    long insert(InterventionLog interventionLog);

    @Query("DELETE FROM intervention_log")
    void deleteAll();

    @Query("SELECT * FROM intervention_log WHERE id = :id")
    @Nullable
    InterventionLog getByIdSync(long id);

    /** Day-scoped so two dismissals mute nudges for the rest of that day only; hydration rows never count. */
    @Query("SELECT user_response FROM intervention_log WHERE category != 'HYDRATION' "
            + "AND creation_date >= :since ORDER BY creation_date DESC LIMIT :limit")
    List<UserResponse> getRecentResponsesSync(LocalDateTime since, int limit);

    @Query("SELECT MAX(creation_date) FROM intervention_log WHERE category = 'HYDRATION'")
    @Nullable
    LocalDateTime lastHydrationReminderAtSync();

    @Query("UPDATE intervention_log SET user_response = :response, responded_at = :respondedAt WHERE id = :id")
    void updateResponse(long id, UserResponse response, LocalDateTime respondedAt);

    default void updateResponse(long id, UserResponse response) {
        updateResponse(id, response, LocalDateTime.now());
    }

    @Query("SELECT * FROM intervention_log ORDER BY creation_date ASC")
    List<InterventionLog> getAllSync();

    @Query("UPDATE intervention_log SET category = :category, format = :format, payload = :payload WHERE id = :id")
    void updateAfterEma(long id, InterventionCategory category, InterventionFormat format, String payload);
}
