package com.panagiotisoikonomou.nudgebite.ui.logging;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.dashboard.ContributionTagBuilder;
import com.panagiotisoikonomou.nudgebite.data.AppDatabase;
import com.panagiotisoikonomou.nudgebite.data.entity.Ingredient;
import com.panagiotisoikonomou.nudgebite.data.entity.Meal;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.entity.relation.MealWithIngredients;
import com.panagiotisoikonomou.nudgebite.data.enums.FoodCategory;
import com.panagiotisoikonomou.nudgebite.data.enums.MealSlot;
import com.panagiotisoikonomou.nudgebite.data.enums.MealSource;
import com.panagiotisoikonomou.nudgebite.data.enums.UnitOfMeasure;
import com.panagiotisoikonomou.nudgebite.data.repository.UserRepository;
import com.panagiotisoikonomou.nudgebite.data.repository.WellScoreRepository;
import com.panagiotisoikonomou.nudgebite.food.FoodGroup;
import com.panagiotisoikonomou.nudgebite.food.FoodItem;
import com.panagiotisoikonomou.nudgebite.food.FoodTaxonomy;
import com.panagiotisoikonomou.nudgebite.logging.LoggableSlots;
import com.panagiotisoikonomou.nudgebite.logging.MealPhotoFile;
import com.panagiotisoikonomou.nudgebite.ui.components.AfterTextChanged;
import com.panagiotisoikonomou.nudgebite.ui.components.PrimaryButton;
import com.panagiotisoikonomou.nudgebite.ui.components.SecondaryButton;
import com.panagiotisoikonomou.nudgebite.ui.components.SingleSelectList;
import com.panagiotisoikonomou.nudgebite.utils.ImageUtils;
import com.panagiotisoikonomou.nudgebite.vision.VisionClient;
import com.panagiotisoikonomou.nudgebite.vision.VisionItem;
import com.panagiotisoikonomou.nudgebite.vision.VisionResult;
import com.panagiotisoikonomou.nudgebite.utils.EnumNames;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntConsumer;

/** Logs a meal from a photo (analysed by the vision model) or by hand, or edits one already saved. */
public class MealEditActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URI = "extra_image_uri";
    public static final String EXTRA_MEAL_ID = "extra_meal_id";
    public static final String EXTRA_PRESET_SLOT = "extra_preset_slot";
    public static final String EXTRA_MANUAL_ENTRY = "extra_manual_entry";

    private static final int MAX_IMAGE_DIMENSION = 1024;
    private static final int JPEG_QUALITY = 90;
    private static final String NOT_LISTED_OPTION = "Not listed…";

    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private final VisionClient visionClient = new VisionClient();
    private final List<EditableIngredient> ingredients = new ArrayList<>();

    private Uri imageUri;
    private byte[] photoBytes;
    private MealSlot selectedSlot;
    @Nullable private MealSource selectedSource;

    /** The intended slot, re-resolved whenever the profile arrives, so a late "I snack" restores it. */
    private MealSlot desiredSlot;
    @Nullable private User currentUser;

    /** Set when editing a saved meal; its own slot always stays on offer. */
    @Nullable private Long editingMealId;
    @Nullable private MealSlot editingSlot;
    private LocalDateTime existingLoggedAt;
    private String existingPhotoPath;

    private View loadingState;
    private View errorState;
    private View contentState;
    private TextView errorDetail;
    private ImageView mealPhoto;
    private EditText dishNameInput;
    private TextView macroSummary;
    private SingleSelectList mealSlotSelect;
    private SingleSelectList mealSourceSelect;
    private LinearLayout ingredientsContainer;
    private PrimaryButton saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_edit);

        //noinspection deprecation
        imageUri = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
        long mealIdExtra = getIntent().getLongExtra(EXTRA_MEAL_ID, -1);
        editingMealId = mealIdExtra != -1 ? mealIdExtra : null;

        loadingState = findViewById(R.id.loading_state);
        errorState = findViewById(R.id.error_state);
        contentState = findViewById(R.id.content_state);
        errorDetail = findViewById(R.id.error_detail);
        mealPhoto = findViewById(R.id.meal_photo);
        dishNameInput = findViewById(R.id.dish_name_input);
        macroSummary = findViewById(R.id.macro_summary);
        mealSlotSelect = findViewById(R.id.meal_slot_select);
        mealSourceSelect = findViewById(R.id.meal_source_select);
        ingredientsContainer = findViewById(R.id.ingredients_container);
        saveButton = findViewById(R.id.save_button);
        SecondaryButton deleteButton = findViewById(R.id.delete_meal_button);
        deleteButton.setDestructive(true);
        deleteButton.setVisibility(editingMealId != null ? View.VISIBLE : View.GONE);
        deleteButton.setOnClickListener(v -> confirmDelete());

        setUpMealSlotSelector();
        setUpMealSourceSelector();
        new UserRepository(getApplication()).getUser().observe(this, user -> {
            currentUser = user;
            refreshSlotOptions();
        });

        findViewById(R.id.cancel_button).setOnClickListener(v -> finish());
        findViewById(R.id.retry_button).setOnClickListener(v -> {
            if (editingMealId != null) {
                loadSavedMeal(editingMealId);
            } else {
                analysePhoto();
            }
        });
        findViewById(R.id.add_ingredient_button).setOnClickListener(v -> {
            ingredients.add(new EditableIngredient());
            showIngredientRows();
        });
        saveButton.setOnClickListener(v -> saveMeal());

        if (editingMealId != null) {
            loadSavedMeal(editingMealId);
        } else if (getIntent().getBooleanExtra(EXTRA_MANUAL_ENTRY, false)) {
            mealPhoto.setImageResource(R.drawable.placeholder_meal);
            showIngredientRows();
            saveButton.setEnabled(true);
            showState(contentState);
        } else {
            loadPhotoAndAnalyse();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backgroundExecutor.shutdown();
    }

    private void setUpMealSlotSelector() {
        desiredSlot = presetSlotOrNull();
        if (desiredSlot == null) {
            desiredSlot = slotForTimeOfDay(LocalTime.now().getHour());
        }
        selectedSlot = desiredSlot;
        mealSlotSelect.setOnSelectionChangedListener(label -> {
            selectedSlot = slotWithLabel(label);
            desiredSlot = selectedSlot;
            updateSaveButtonLabel();
        });
        refreshSlotOptions();
    }

    /** Offers only the meals the user said they eat; the profile arrives asynchronously. */
    private void refreshSlotOptions() {
        List<MealSlot> allowed = LoggableSlots.allowedSlots(currentUser, editingSlot);
        selectedSlot = LoggableSlots.nearestAllowed(desiredSlot, allowed);
        List<String> labels = new ArrayList<>();
        for (MealSlot slot : allowed) {
            labels.add(slot.label);
        }
        mealSlotSelect.setOptions(labels, selectedSlot.label);
        updateSaveButtonLabel();
    }

    /** Nothing is preselected: an unanswered question stays null, which means unknown, not home-cooked. */
    private void setUpMealSourceSelector() {
        List<String> labels = new ArrayList<>();
        for (MealSource source : MealSource.values()) {
            labels.add(source.label);
        }
        mealSourceSelect.setOptions(labels, selectedSource != null ? selectedSource.label : null);
        mealSourceSelect.setOnSelectionChangedListener(label -> {
            for (MealSource source : MealSource.values()) {
                if (source.label.equals(label)) {
                    selectedSource = source;
                }
            }
        });
    }

    @Nullable
    private MealSlot presetSlotOrNull() {
        return EnumNames.parseOrDefault(MealSlot.class, getIntent().getStringExtra(EXTRA_PRESET_SLOT), null);
    }

    private static MealSlot slotWithLabel(String label) {
        for (MealSlot slot : MealSlot.values()) {
            if (slot.label.equals(label)) {
                return slot;
            }
        }
        return MealSlot.SNACK;
    }

    private static MealSlot slotForTimeOfDay(int hour) {
        if (hour < 11) {
            return MealSlot.BREAKFAST;
        }
        if (hour < 16) {
            return MealSlot.LUNCH;
        }
        return hour < 21 ? MealSlot.DINNER : MealSlot.SNACK;
    }

    private void updateSaveButtonLabel() {
        saveButton.setText(editingMealId != null ? "Save" : "Add to " + selectedSlot.label);
    }

    private void loadPhotoAndAnalyse() {
        showState(loadingState);
        backgroundExecutor.execute(() -> {
            try {
                photoBytes = ImageUtils.downscaleToJpeg(this, imageUri, MAX_IMAGE_DIMENSION, JPEG_QUALITY);
                Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
                runOnUiThread(() -> {
                    mealPhoto.setImageBitmap(bitmap);
                    analysePhoto();
                });
            } catch (IOException e) {
                runOnUiThread(() -> showError("Couldn't read that photo."));
            }
        });
    }

    private void analysePhoto() {
        if (photoBytes == null) {
            loadPhotoAndAnalyse();
            return;
        }
        showState(loadingState);
        visionClient.analyze(photoBytes, new VisionClient.Callback() {
            @Override
            public void onSuccess(VisionResult result) {
                showVisionResult(result);
                showState(contentState);
            }

            @Override
            public void onError(Exception e) {
                showError(e.getMessage() != null ? e.getMessage() : "Please try again.");
            }
        });
    }

    private void showVisionResult(VisionResult result) {
        ingredients.clear();
        for (VisionItem item : result.items) {
            ingredients.add(EditableIngredient.fromVision(item));
        }
        dishNameInput.setText(dishNameFrom(result));
        showIngredientRows();
        saveButton.setEnabled(true);
    }

    /** The model's dish title, or the first few ingredient names when it gave none. */
    private String dishNameFrom(VisionResult result) {
        if (result.dishTitle != null && !result.dishTitle.trim().isEmpty()) {
            return result.dishTitle.trim();
        }
        List<String> names = new ArrayList<>();
        for (EditableIngredient ingredient : ingredients.subList(0, Math.min(3, ingredients.size()))) {
            names.add(ingredient.name);
        }
        return String.join(", ", names);
    }

    private void loadSavedMeal(long mealId) {
        showState(loadingState);
        backgroundExecutor.execute(() -> {
            MealWithIngredients saved = AppDatabase.getDatabase(this).mealDao().getMealWithIngredientsSync(mealId);
            runOnUiThread(() -> {
                if (saved == null) {
                    showError("This meal could not be found.");
                    return;
                }
                showSavedMeal(saved);
                showState(contentState);
            });
        });
    }

    private void showSavedMeal(MealWithIngredients saved) {
        existingLoggedAt = saved.meal.loggedAt;
        existingPhotoPath = saved.meal.photoUri;
        editingSlot = saved.meal.mealSlot;
        desiredSlot = saved.meal.mealSlot;
        refreshSlotOptions();
        selectedSource = saved.meal.mealSource;
        setUpMealSourceSelector();
        dishNameInput.setText(saved.meal.dishName);

        ingredients.clear();
        if (saved.ingredients != null) {
            for (Ingredient ingredient : saved.ingredients) {
                ingredients.add(EditableIngredient.fromSaved(ingredient));
            }
        }
        showIngredientRows();
        saveButton.setEnabled(true);

        Bitmap photo = existingPhotoPath != null ? BitmapFactory.decodeFile(existingPhotoPath) : null;
        if (photo != null) {
            mealPhoto.setImageBitmap(photo);
        } else {
            mealPhoto.setImageResource(R.drawable.placeholder_meal);
        }
    }

    private void showIngredientRows() {
        ingredientsContainer.removeAllViews();
        for (EditableIngredient ingredient : ingredients) {
            ingredientsContainer.addView(ingredientRow(ingredient));
        }
        showNutrientTotals();
    }

    private View ingredientRow(EditableIngredient ingredient) {
        View row = getLayoutInflater().inflate(R.layout.row_ingredient, ingredientsContainer, false);
        EditText nameInput = row.findViewById(R.id.ingredient_name);
        TextView foodSelector = row.findViewById(R.id.ingredient_food);
        TextView amountText = row.findViewById(R.id.amount_text);

        nameInput.setText(ingredient.name);
        nameInput.addTextChangedListener(new AfterTextChanged(text -> ingredient.name = text));
        foodSelector.setText(ingredient.foodLabel());
        foodSelector.setOnClickListener(v -> showFoodGroupPicker(foodSelector, ingredient));
        showAmount(amountText, ingredient);

        int step = ingredient.uom == UnitOfMeasure.ML ? 10 : 5;
        row.findViewById(R.id.decrement_button).setOnClickListener(v -> changePortion(ingredient, -step, amountText));
        row.findViewById(R.id.increment_button).setOnClickListener(v -> changePortion(ingredient, step, amountText));
        row.findViewById(R.id.delete_button).setOnClickListener(v -> {
            ingredients.remove(ingredient);
            showIngredientRows();
        });
        return row;
    }

    private void changePortion(EditableIngredient ingredient, int delta, TextView amountText) {
        ingredient.adjustPortion(delta);
        showAmount(amountText, ingredient);
        showNutrientTotals();
    }

    private static void showAmount(TextView amountText, EditableIngredient ingredient) {
        String unit = ingredient.uom == UnitOfMeasure.ML ? "ml" : "g";
        amountText.setText(Math.round(ingredient.portionValue) + unit);
    }

    /** Hidden rather than showing a bare "0 kcal" nobody entered. */
    private void showNutrientTotals() {
        double calories = 0;
        double protein = 0;
        double carbs = 0;
        double fat = 0;
        for (EditableIngredient ingredient : ingredients) {
            calories += ingredient.calories;
            protein += ingredient.protein;
            carbs += ingredient.carbs;
            fat += ingredient.fat;
        }
        boolean anyNutrients = calories != 0 || protein != 0 || carbs != 0 || fat != 0;
        macroSummary.setVisibility(anyNutrients ? View.VISIBLE : View.GONE);
        macroSummary.setText(String.format(Locale.US,
                "%.0f kcal • %.0f g protein • %.0f g carbs • %.0f g fat", calories, protein, carbs, fat));
    }

    /** Groups first, then foods in the group; "Not listed" drops to category level rather than forcing a wrong food. */
    private void showFoodGroupPicker(TextView foodSelector, EditableIngredient ingredient) {
        FoodGroup[] groups = FoodGroup.values();
        List<String> labels = new ArrayList<>();
        for (FoodGroup group : groups) {
            labels.add(group.label());
        }
        labels.add(NOT_LISTED_OPTION);
        choiceDialog("Which food is this?", labels, which -> {
            if (which == groups.length) {
                showFoodCategoryPicker(foodSelector, ingredient);
            } else {
                showFoodItemPicker(foodSelector, ingredient, groups[which]);
            }
        }).show();
    }

    private void showFoodItemPicker(TextView foodSelector, EditableIngredient ingredient, FoodGroup group) {
        List<FoodItem> items = FoodTaxonomy.itemsIn(group);
        List<String> labels = new ArrayList<>();
        for (FoodItem item : items) {
            labels.add(item.label());
        }
        choiceDialog(group.label(), labels, which -> {
            ingredient.setFoodItem(items.get(which));
            foodSelector.setText(ingredient.foodLabel());
        }).setNegativeButton("Back", (dialog, which) -> showFoodGroupPicker(foodSelector, ingredient)).show();
    }

    private void showFoodCategoryPicker(TextView foodSelector, EditableIngredient ingredient) {
        FoodCategory[] categories = FoodCategory.values();
        List<String> labels = new ArrayList<>();
        for (FoodCategory category : categories) {
            labels.add(category.displayName());
        }
        choiceDialog("Closest food group", labels, which -> {
            ingredient.setCategoryOnly(categories[which]);
            foodSelector.setText(ingredient.foodLabel());
        }).show();
    }

    private AlertDialog.Builder choiceDialog(String title, List<String> labels, IntConsumer onChosen) {
        return new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(labels.toArray(new CharSequence[0]), (dialog, which) -> onChosen.accept(which));
    }

    private void saveMeal() {
        List<Ingredient> entities = new ArrayList<>();
        for (EditableIngredient ingredient : ingredients) {
            if (ingredient.name != null && !ingredient.name.trim().isEmpty()) {
                entities.add(ingredient.toEntity());
            }
        }
        if (entities.isEmpty()) {
            Toast.makeText(this, "Add at least one ingredient first.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedSource == null) {
            Toast.makeText(this, "Please choose how this was made.", Toast.LENGTH_SHORT).show();
            return;
        }
        saveButton.setEnabled(false);

        String typedDishName = dishNameInput.getText().toString().trim();
        Meal meal = new Meal();
        meal.mealSlot = selectedSlot;
        meal.mealSource = selectedSource;
        meal.dishName = typedDishName.isEmpty() ? "Logged meal" : typedDishName;
        String contributionTag = ContributionTagBuilder.buildTag(entities);
        Long mealIdBeingEdited = editingMealId;
        byte[] photoToSave = photoBytes;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            if (mealIdBeingEdited != null) {
                meal.id = mealIdBeingEdited;
                meal.loggedAt = existingLoggedAt;
                meal.photoUri = existingPhotoPath;
                db.mealDao().update(meal);
                db.ingredientDao().deleteByMealId(meal.id);
            } else {
                meal.loggedAt = LocalDateTime.now();
                meal.photoUri = photoToSave != null ? MealPhotoFile.save(getFilesDir(), photoToSave) : null;
                meal.id = db.mealDao().insert(meal);
            }
            for (Ingredient entity : entities) {
                entity.mealId = meal.id;
            }
            db.ingredientDao().insertAll(entities);
            WellScoreRepository.recomputeAll(db);
            runOnUiThread(() -> {
                Toast.makeText(this, contributionTag, Toast.LENGTH_LONG).show();
                finish();
            });
        });
    }

    /** Neutral wording: a mis-logged meal is a correction, not a lapse. */
    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete this meal?")
                .setMessage("This removes it from your log.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteMeal())
                .show();
    }

    private void deleteMeal() {
        long mealId = editingMealId;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            // Read from the row: Delete can be tapped before the async load fills the field.
            MealWithIngredients meal = db.mealDao().getMealWithIngredientsSync(mealId);
            db.mealDao().deleteById(mealId);
            MealPhotoFile.deleteIfSavedByApp(getFilesDir(), meal != null ? meal.meal.photoUri : null);
            WellScoreRepository.recomputeAll(db);
            runOnUiThread(() -> {
                Toast.makeText(this, "Meal deleted.", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void showError(String detail) {
        errorDetail.setText(detail);
        showState(errorState);
    }

    private void showState(View visibleState) {
        loadingState.setVisibility(visibleState == loadingState ? View.VISIBLE : View.GONE);
        errorState.setVisibility(visibleState == errorState ? View.VISIBLE : View.GONE);
        contentState.setVisibility(visibleState == contentState ? View.VISIBLE : View.GONE);
    }
}
