package com.panagiotisoikonomou.nudgebite.ui.intervention;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.entity.InterventionLog;
import com.panagiotisoikonomou.nudgebite.data.enums.UserResponse;
import com.panagiotisoikonomou.nudgebite.engine.recommendation.MealRecommendation;
import com.panagiotisoikonomou.nudgebite.engine.recommendation.RecentRecommendationStore;
import com.panagiotisoikonomou.nudgebite.engine.recommendation.SuggestionPayloadBuilder;
import com.panagiotisoikonomou.nudgebite.notifications.InterventionNotifier;
import com.panagiotisoikonomou.nudgebite.ui.components.TagPill;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;
import com.panagiotisoikonomou.nudgebite.utils.SystemBarInsets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Shows the dish suggestion stored on the intervention, opened from its notification. */
public class SuggestionActivity extends AppCompatActivity {

    private static final String EXTRA_INTERVENTION_LOG_ID = "intervention_log_id";

    private long interventionLogId;
    private String storedPayload;
    private final List<String> alternativeIds = new ArrayList<>();
    private int shownAlternativeIndex = 0;

    public static Intent newIntent(Context context, long interventionLogId) {
        Intent intent = new Intent(context, SuggestionActivity.class);
        intent.putExtra(EXTRA_INTERVENTION_LOG_ID, interventionLogId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suggestion);
        SystemBarInsets.padForSystemBars(findViewById(R.id.suggestion_root));

        interventionLogId = getIntent().getLongExtra(EXTRA_INTERVENTION_LOG_ID, -1);
        if (interventionLogId == -1) {
            finish();
            return;
        }
        InterventionNotifier.cancel(this, interventionLogId);

        findViewById(R.id.suggestion_accept_button).setOnClickListener(v -> finish());
        findViewById(R.id.suggestion_close_button).setOnClickListener(v -> recordDismissedAndClose());
        loadSuggestion();
    }

    /** Opening the screen is what counts as accepting the suggestion. */
    private void loadSuggestion() {
        AppDatabase db = AppDatabase.getDatabase(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            InterventionLog log = db.interventionLogDao().getByIdSync(interventionLogId);
            db.interventionLogDao().updateResponse(interventionLogId, UserResponse.ACCEPTED);
            runOnUiThread(() -> {
                if (log == null) {
                    finish();
                } else {
                    showSuggestion(log.payload);
                }
            });
        });
    }

    private void recordDismissedAndClose() {
        AppDatabase db = AppDatabase.getDatabase(this);
        AppDatabase.databaseWriteExecutor.execute(() ->
                db.interventionLogDao().updateResponse(interventionLogId, UserResponse.DISMISSED));
        finish();
    }

    private void showSuggestion(String payload) {
        storedPayload = payload;
        try {
            // opt*, not get*: rows saved before these fields existed still load.
            JSONArray ids = new JSONObject(payload).optJSONArray("alternativeIds");
            for (int i = 0; ids != null && i < ids.length(); i++) {
                alternativeIds.add(ids.getString(i));
            }
        } catch (JSONException e) {
            throw new IllegalStateException("Malformed intervention payload", e);
        }
        View showAnotherButton = findViewById(R.id.suggestion_alternative_button);
        showAnotherButton.setVisibility(alternativeIds.size() > 1 ? View.VISIBLE : View.GONE);
        showAnotherButton.setOnClickListener(v -> showNextAlternative());
        showDish(payload);
    }

    private void showNextAlternative() {
        shownAlternativeIndex = (shownAlternativeIndex + 1) % alternativeIds.size();
        MealRecommendation next = MealRecommendation.byId(alternativeIds.get(shownAlternativeIndex));
        if (next == null) {
            return;
        }
        new RecentRecommendationStore(this).record(next.id);
        showDish(SuggestionPayloadBuilder.rebuildForDish(storedPayload, next));
    }

    private void showDish(String payload) {
        try {
            JSONObject dish = new JSONObject(payload);
            ((TextView) findViewById(R.id.suggestion_eyebrow))
                    .setText(dish.optString("eyebrow", SuggestionPayloadBuilder.DEFAULT_EYEBROW));
            ((TextView) findViewById(R.id.suggestion_title)).setText(dish.getString("title"));
            ((TextView) findViewById(R.id.suggestion_body)).setText(dish.getString("bodyText"));
            showTags(dish.getJSONArray("tags"));
            Integer photo = RecommendationImages.forId(dish.optString("recommendationId", null));
            if (photo != null) {
                ((ImageView) findViewById(R.id.suggestion_hero_image)).setImageResource(photo);
            }
        } catch (JSONException e) {
            throw new IllegalStateException("Malformed intervention payload", e);
        }
    }

    private void showTags(JSONArray tags) throws JSONException {
        LinearLayout tagsRow = findViewById(R.id.suggestion_tags_row);
        tagsRow.removeAllViews();
        for (int i = 0; i < tags.length(); i++) {
            TagPill pill = new TagPill(this);
            pill.setText(tags.getString(i));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) {
                params.leftMargin = DisplayUtils.dp(this, 8);
            }
            tagsRow.addView(pill, params);
        }
    }
}
