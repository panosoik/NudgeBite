package com.panagiotisoikonomou.nudgebite.ui.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;
import com.panagiotisoikonomou.nudgebite.dashboard.ContributionTagBuilder;
import com.panagiotisoikonomou.nudgebite.data.entity.Ingredient;
import com.panagiotisoikonomou.nudgebite.data.entity.relation.MealWithIngredients;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.LongConsumer;

public class MealSlotCard extends LinearLayout {

    private final TextView slotLabel;
    private final LinearLayout rowsContainer;

    public MealSlotCard(Context context) {
        this(context, null);
    }

    public MealSlotCard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setBackgroundResource(R.drawable.bg_chip_row_unselected);
        int pad = DisplayUtils.dp(getContext(), 16);
        setPadding(pad, pad, pad, pad);

        slotLabel = new TextView(context);
        slotLabel.setTextColor(ContextCompat.getColor(context, R.color.nb_text_secondary));
        slotLabel.setTextSize(12);
        slotLabel.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        LayoutParams labelParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        labelParams.bottomMargin = DisplayUtils.dp(getContext(), 10);
        addView(slotLabel, labelParams);

        rowsContainer = new LinearLayout(context);
        rowsContainer.setOrientation(VERTICAL);
        addView(rowsContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public void bind(String slotDisplayName, List<MealWithIngredients> meals,
                      LongConsumer onMealTapped, Runnable onAddTapped,
                      boolean allowAdding) {
        slotLabel.setText(slotDisplayName.toUpperCase(Locale.US));
        rowsContainer.removeAllViews();
        if (meals.isEmpty()) {
            rowsContainer.addView(allowAdding ? buildEmptyRow(onAddTapped) : buildNotLoggedRow());
            return;
        }
        for (int i = 0; i < meals.size(); i++) {
            rowsContainer.addView(buildMealRow(meals.get(i), onMealTapped, i > 0));
        }
        if (allowAdding) {
            rowsContainer.addView(buildAddLink(onAddTapped));
        }
    }

    private View buildNotLoggedRow() {
        TextView label = new TextView(getContext());
        label.setText("Not logged");
        label.setTextColor(ContextCompat.getColor(getContext(), R.color.nb_text_secondary));
        label.setTextSize(15);
        return label;
    }

    private View buildEmptyRow(Runnable onAddTapped) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setClickable(true);
        row.setFocusable(true);

        View placeholder = new View(getContext());
        placeholder.setBackgroundResource(R.drawable.bg_input_field);
        row.addView(placeholder, new LinearLayout.LayoutParams(DisplayUtils.dp(getContext(), 56), DisplayUtils.dp(getContext(), 56)));

        TextView label = new TextView(getContext());
        label.setText("Tap to add");
        label.setTextColor(ContextCompat.getColor(getContext(), R.color.nb_text_secondary));
        label.setTextSize(15);
        LinearLayout.LayoutParams labelParams =
                new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        labelParams.leftMargin = DisplayUtils.dp(getContext(), 12);
        row.addView(label, labelParams);

        row.setOnClickListener(v -> onAddTapped.run());
        return row;
    }

    private View buildMealRow(MealWithIngredients meal, LongConsumer onMealTapped, boolean topMargin) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setClickable(true);
        row.setFocusable(true);

        ImageView photo = new ImageView(getContext());
        photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photo.setBackgroundResource(R.drawable.bg_input_field);
        Bitmap bitmap = meal.meal.photoUri != null ? BitmapFactory.decodeFile(meal.meal.photoUri) : null;
        photo.setImageBitmap(bitmap);
        row.addView(photo, new LinearLayout.LayoutParams(DisplayUtils.dp(getContext(), 56), DisplayUtils.dp(getContext(), 56)));

        LinearLayout textColumn = new LinearLayout(getContext());
        textColumn.setOrientation(VERTICAL);

        TextView dishName = new TextView(getContext());
        dishName.setText(meal.meal.dishName);
        dishName.setTextColor(ContextCompat.getColor(getContext(), R.color.nb_text_primary));
        dishName.setTextSize(16);
        dishName.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textColumn.addView(dishName);

        TextView tag = new TextView(getContext());
        List<Ingredient> ingredients = meal.ingredients != null ? meal.ingredients : new ArrayList<>();
        tag.setText(ContributionTagBuilder.buildTag(ingredients));
        tag.setTextColor(ContextCompat.getColor(getContext(), R.color.nb_primary_green));
        tag.setTextSize(12);
        LinearLayout.LayoutParams tagParams =
                new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tagParams.topMargin = DisplayUtils.dp(getContext(), 2);
        textColumn.addView(tag, tagParams);

        LinearLayout.LayoutParams textParams =
                new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        textParams.leftMargin = DisplayUtils.dp(getContext(), 12);
        row.addView(textColumn, textParams);

        row.setOnClickListener(v -> onMealTapped.accept(meal.meal.id));

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        if (topMargin) {
            rowParams.topMargin = DisplayUtils.dp(getContext(), 12);
        }
        row.setLayoutParams(rowParams);
        return row;
    }

    private View buildAddLink(Runnable onAddTapped) {
        TextView link = new TextView(getContext());
        link.setText("+ Add dish");
        link.setTextColor(ContextCompat.getColor(getContext(), R.color.nb_primary_green));
        link.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        link.setTextSize(14);
        link.setClickable(true);
        link.setFocusable(true);
        link.setPadding(0, DisplayUtils.dp(getContext(), 10), 0, 0);
        link.setOnClickListener(v -> onAddTapped.run());
        return link;
    }
}
