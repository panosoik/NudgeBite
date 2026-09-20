package com.panagiotisoikonomou.nudgebite.ui.intervention;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.enums.DeliveryIntent;
import com.panagiotisoikonomou.nudgebite.data.enums.FoodAvailability;
import com.panagiotisoikonomou.nudgebite.engine.MicroEmaCompletion;
import com.panagiotisoikonomou.nudgebite.engine.ema.MicroEmaAnswers;
import com.panagiotisoikonomou.nudgebite.notifications.InterventionNotifier;
import com.panagiotisoikonomou.nudgebite.ui.components.PrimaryButton;
import com.panagiotisoikonomou.nudgebite.ui.components.SingleSelectList;
import com.panagiotisoikonomou.nudgebite.utils.SystemBarInsets;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MicroEmaActivity extends AppCompatActivity {

    private static final String EXTRA_INTERVENTION_LOG_ID = "intervention_log_id";

    private static final Map<String, DeliveryIntent> DELIVERY_INTENT_OPTIONS = new LinkedHashMap<>();
    private static final Map<String, FoodAvailability> FOOD_AVAILABILITY_OPTIONS = new LinkedHashMap<>();

    static {
        DELIVERY_INTENT_OPTIONS.put("Already ordered", DeliveryIntent.ALREADY_ORDERED);
        DELIVERY_INTENT_OPTIONS.put("Thinking about it", DeliveryIntent.CONSIDERING);
        DELIVERY_INTENT_OPTIONS.put("Not ordering", DeliveryIntent.NONE);

        FOOD_AVAILABILITY_OPTIONS.put("Yes", FoodAvailability.YES);
        FOOD_AVAILABILITY_OPTIONS.put("Some", FoodAvailability.PARTIAL);
        FOOD_AVAILABILITY_OPTIONS.put("Not really", FoodAvailability.NO);
    }

    private long interventionLogId;
    private DeliveryIntent selectedDeliveryIntent;
    private FoodAvailability selectedFoodAvailability;
    private PrimaryButton continueButton;

    public static Intent newIntent(Context context, long interventionLogId) {
        Intent intent = new Intent(context, MicroEmaActivity.class);
        intent.putExtra(EXTRA_INTERVENTION_LOG_ID, interventionLogId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_micro_ema);
        SystemBarInsets.padForSystemBars(findViewById(R.id.micro_ema_root));

        interventionLogId = getIntent().getLongExtra(EXTRA_INTERVENTION_LOG_ID, -1);
        if (interventionLogId == -1) {
            finish();
            return;
        }

        continueButton = findViewById(R.id.micro_ema_continue_button);
        continueButton.setOnClickListener(v -> onContinue());

        bindQuestion(R.id.delivery_intent_select, DELIVERY_INTENT_OPTIONS, answer -> selectedDeliveryIntent = answer);
        bindQuestion(R.id.food_available_select, FOOD_AVAILABILITY_OPTIONS, answer -> selectedFoodAvailability = answer);
    }

    /** A second notification reuses this instance, so rebuild it for the new intervention. */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        recreate();
    }

    private <T> void bindQuestion(int selectListId, Map<String, T> optionsByLabel, Consumer<T> onAnswered) {
        SingleSelectList select = findViewById(selectListId);
        select.setOptions(new ArrayList<>(optionsByLabel.keySet()));
        select.setOnSelectionChangedListener(optionLabel -> {
            onAnswered.accept(optionsByLabel.get(optionLabel));
            refreshContinueEnabled();
        });
    }

    private void refreshContinueEnabled() {
        continueButton.setEnabled(selectedDeliveryIntent != null && selectedFoodAvailability != null);
    }

    private void onContinue() {
        continueButton.setEnabled(false);

        // Cancelled on Continue, not on open, so backing out leaves the notification to return to.
        InterventionNotifier.cancel(this, interventionLogId);

        MicroEmaAnswers answers = MicroEmaAnswers.fromOptions(selectedDeliveryIntent, selectedFoodAvailability);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean suggestionReady = MicroEmaCompletion.recordAnswers(getApplicationContext(), interventionLogId, answers);
            runOnUiThread(() -> {
                if (suggestionReady) {
                    startActivity(SuggestionActivity.newIntent(this, interventionLogId));
                }
                finish();
            });
        });
    }
}
