package com.panagiotisoikonomou.nudgebite.engine.recommendation;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** SharedPreferences, not memory: picks happen in cold processes (receivers, workers). */
public class RecentRecommendationStore {

    public static final int MAX_REMEMBERED = 5;

    private static final String PREFS_NAME = "recent_recommendation_store";
    private static final String KEY_RECENT_IDS = "recent_ids";
    private static final String SEPARATOR = ",";

    private final SharedPreferences prefs;

    public RecentRecommendationStore(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<String> recentIds() {
        return decode(prefs.getString(KEY_RECENT_IDS, null));
    }

    public void record(String recommendationId) {
        prefs.edit().putString(KEY_RECENT_IDS, encode(withRecorded(recentIds(), recommendationId))).apply();
    }

    @VisibleForTesting
    static List<String> withRecorded(List<String> current, String recommendationId) {
        List<String> updated = new ArrayList<>(current.size() + 1);
        updated.add(recommendationId);
        for (String id : current) {
            if (!id.equals(recommendationId) && updated.size() < MAX_REMEMBERED) {
                updated.add(id);
            }
        }
        return updated;
    }

    @VisibleForTesting
    static String encode(List<String> ids) {
        return String.join(SEPARATOR, ids);
    }

    @VisibleForTesting
    static List<String> decode(@Nullable String raw) {
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<>();
        Collections.addAll(ids, raw.split(SEPARATOR));
        return ids;
    }
}
