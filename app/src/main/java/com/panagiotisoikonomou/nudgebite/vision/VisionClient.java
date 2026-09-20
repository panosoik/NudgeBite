package com.panagiotisoikonomou.nudgebite.vision;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import com.panagiotisoikonomou.nudgebite.BuildConfig;
import com.panagiotisoikonomou.nudgebite.data.enums.FoodCategory;
import com.panagiotisoikonomou.nudgebite.food.FoodItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VisionClient {

    public interface Callback {
        void onSuccess(VisionResult result);

        void onError(Exception e);
    }

    private static final String ENDPOINT = "https://api.openai.com/v1/responses";
    private static final String MODEL = "gpt-5.6-luna";
    private static final String REASONING_EFFORT = "none";
    private static final String IMAGE_DETAIL = "high";
    private static final int MAX_OUTPUT_TOKENS = 500;
    private static final double TEMPERATURE = 0.2;
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private static final String PROMPT = "You are a nutrition analysis assistant. Analyze the food in the image and return\n"
            + "ONLY valid JSON matching the schema below. No prose, no markdown, no code fences.\n\n"
            + "Return one object per distinct ingredient or beverage you can identify. Treat any\n"
            + "drink (soda, juice, coffee, etc.) as a SEPARATE item, never merged into a dish.\n\n"
            + "Also return dish_title: a concise, natural name for the meal as a whole, as a person\n"
            + "would say it (e.g. \"Chicken & rice bowl\", \"Greek salad\", \"Porridge with berries\").\n"
            + "Keep it under 5 words. Do NOT list every ingredient, do not include portion sizes or\n"
            + "calories, and do not add adjectives like \"healthy\" or \"delicious\".\n\n"
            + "For each item:\n"
            + "- name: short English name of the ingredient/drink\n"
            + "- food_item: the specific food from the allowed list, when you are confident which\n"
            + "  one it is. Use null if unsure or if the food is not on the list. Be precise where\n"
            + "  the distinction is visible: brown rice vs white rice, whole-grain vs white bread,\n"
            + "  regular cola vs diet cola. Never guess between two you cannot tell apart.\n"
            + "- food_category: MUST be exactly one of the allowed enum values below\n"
            + "- uom: \"GRAMS\" for solids, \"ML\" for liquids\n"
            + "- portion_value: estimated amount in that unit (number)\n"
            + "- calories: estimated kcal for that portion (number)\n"
            + "- protein, carbs, fat: estimated grams (number)\n"
            + "- confidence: your certainty 0.0-1.0 (number)\n\n"
            + "Allowed food_category values:\n"
            + "LEAFY_GREEN, CRUCIFEROUS, ROOT_VEGETABLE, OTHER_VEGETABLE,\n"
            + "FRUIT,\n"
            + "WHOLE_GRAIN, REFINED_GRAIN,\n"
            + "LEGUME, NUT_SEED,\n"
            + "FISH, SEAFOOD,\n"
            + "RED_MEAT, PROCESSED_MEAT,\n"
            + "SUGARY_DRINK,\n"
            + "POULTRY, EGG, DAIRY, CHEESE, FAT_OIL, POTATO, SWEET_SNACK,\n"
            + "OTHER, UNKNOWN\n\n"
            + "Category rules:\n"
            + "- Rice/bread/pasta: if you cannot tell whether it is whole-grain or refined,\n"
            + "  default to REFINED_GRAIN.\n"
            + "- Potato is POTATO, never a vegetable.\n"
            + "- Fruit juice and soft drinks are SUGARY_DRINK, not FRUIT.\n"
            + "- Plain water: OTHER.\n"
            + "- If unsure of the item itself, use UNKNOWN and lower the confidence.\n\n"
            + "Calorie/macro values are rough references only; approximate reasonably.\n\n"
            + "Output schema:\n"
            + "{\n"
            + "  \"dish_title\": \"\",\n"
            + "  \"items\": [\n"
            + "    { \"name\": \"\", \"food_item\": null, \"food_category\": \"\",\n"
            + "      \"uom\": \"GRAMS|ML\",\n"
            + "      \"portion_value\": 0, \"calories\": 0, \"protein\": 0, \"carbs\": 0,\n"
            + "      \"fat\": 0, \"confidence\": 0.0 }\n"
            + "  ],\n"
            + "  \"notes\": \"\"\n"
            + "}\n";

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void analyze(byte[] jpegBytes, Callback callback) {
        executor.execute(() -> {
            try {
                VisionResult result = requestAnalysis(jpegBytes);
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    private VisionResult requestAnalysis(byte[] jpegBytes) throws IOException, JSONException {
        String base64Image = Base64.encodeToString(jpegBytes, Base64.NO_WRAP);
        String dataUrl = "data:image/jpeg;base64," + base64Image;

        JSONObject requestBody = buildRequestBody(dataUrl);

        Request request = new Request.Builder()
                .url(ENDPOINT)
                .header("Authorization", "Bearer " + BuildConfig.OPENAI_API_KEY)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), JSON_MEDIA_TYPE))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("Vision API request failed (HTTP " + response.code() + "): "
                        + extractErrorMessage(responseBody));
            }
            String outputText = extractOutputText(new JSONObject(responseBody));
            return parseResult(outputText);
        }
    }

    private JSONObject buildRequestBody(String dataUrl) throws JSONException {
        JSONObject reasoning = new JSONObject();
        reasoning.put("effort", REASONING_EFFORT);

        JSONObject format = new JSONObject();
        format.put("type", "json_schema");
        format.put("name", "nutrition_analysis");
        format.put("schema", buildResponseSchema());
        format.put("strict", true);

        JSONObject text = new JSONObject();
        text.put("verbosity", "low");
        text.put("format", format);

        JSONObject inputText = new JSONObject();
        inputText.put("type", "input_text");
        inputText.put("text", PROMPT);

        JSONObject inputImage = new JSONObject();
        inputImage.put("type", "input_image");
        inputImage.put("image_url", dataUrl);
        inputImage.put("detail", IMAGE_DETAIL);

        JSONArray content = new JSONArray();
        content.put(inputText);
        content.put(inputImage);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", content);

        JSONArray input = new JSONArray();
        input.put(userMessage);

        JSONObject body = new JSONObject();
        body.put("model", MODEL);
        body.put("reasoning", reasoning);
        body.put("max_output_tokens", MAX_OUTPUT_TOKENS);
        body.put("temperature", TEMPERATURE);
        body.put("text", text);
        body.put("input", input);
        return body;
    }

    private JSONObject buildResponseSchema() throws JSONException {
        JSONArray allowedCategories = new JSONArray();
        for (FoodCategory category : FoodCategory.values()) {
            allowedCategories.put(category.name());
        }

        JSONArray allowedItemsOrNull = new JSONArray();
        for (FoodItem item : FoodItem.values()) {
            allowedItemsOrNull.put(item.name());
        }
        allowedItemsOrNull.put(JSONObject.NULL);

        JSONObject itemProperties = new JSONObject();
        itemProperties.put("name", typeObject("string"));
        // Nullable: a model unsure of brown versus white rice should decline, falling back to the category.
        itemProperties.put("food_item", nullableEnumObject(allowedItemsOrNull));
        itemProperties.put("food_category", enumObject(allowedCategories));
        itemProperties.put("uom", enumObject(new JSONArray().put("GRAMS").put("ML")));
        itemProperties.put("portion_value", typeObject("number"));
        itemProperties.put("calories", typeObject("number"));
        itemProperties.put("protein", typeObject("number"));
        itemProperties.put("carbs", typeObject("number"));
        itemProperties.put("fat", typeObject("number"));
        itemProperties.put("confidence", typeObject("number"));

        JSONObject itemSchema = new JSONObject();
        itemSchema.put("type", "object");
        itemSchema.put("properties", itemProperties);
        itemSchema.put("required", new JSONArray(List.of(
                "name", "food_item", "food_category", "uom", "portion_value",
                "calories", "protein", "carbs", "fat", "confidence")));
        itemSchema.put("additionalProperties", false);

        JSONObject itemsArraySchema = new JSONObject();
        itemsArraySchema.put("type", "array");
        itemsArraySchema.put("items", itemSchema);

        JSONObject rootProperties = new JSONObject();
        rootProperties.put("dish_title", typeObject("string"));
        rootProperties.put("items", itemsArraySchema);
        rootProperties.put("notes", typeObject("string"));

        JSONObject rootSchema = new JSONObject();
        rootSchema.put("type", "object");
        rootSchema.put("properties", rootProperties);
        rootSchema.put("required", new JSONArray(List.of("dish_title", "items", "notes")));
        rootSchema.put("additionalProperties", false);
        return rootSchema;
    }

    private JSONObject typeObject(String type) throws JSONException {
        return new JSONObject().put("type", type);
    }

    private JSONObject enumObject(JSONArray values) throws JSONException {
        return new JSONObject().put("type", "string").put("enum", values);
    }

    /** Strict mode requires every property, so "may be absent" is spelled as null. */
    private JSONObject nullableEnumObject(JSONArray valuesIncludingNull) throws JSONException {
        return new JSONObject()
                .put("type", new JSONArray().put("string").put("null"))
                .put("enum", valuesIncludingNull);
    }

    private String extractOutputText(JSONObject response) throws JSONException {
        StringBuilder builder = new StringBuilder();
        JSONArray output = response.optJSONArray("output");
        if (output == null) {
            return builder.toString();
        }
        for (int i = 0; i < output.length(); i++) {
            JSONObject outputItem = output.getJSONObject(i);
            JSONArray content = outputItem.optJSONArray("content");
            if (content == null) {
                continue;
            }
            for (int j = 0; j < content.length(); j++) {
                JSONObject contentItem = content.getJSONObject(j);
                if ("output_text".equals(contentItem.optString("type"))) {
                    builder.append(contentItem.optString("text"));
                }
            }
        }
        return builder.toString();
    }

    private VisionResult parseResult(String outputText) throws JSONException {
        JSONObject parsed = new JSONObject(outputText);
        JSONArray itemsJson = parsed.getJSONArray("items");

        List<VisionItem> items = new ArrayList<>();
        for (int i = 0; i < itemsJson.length(); i++) {
            JSONObject itemJson = itemsJson.getJSONObject(i);
            VisionItem item = new VisionItem();
            item.name = itemJson.getString("name");
            item.foodItem = itemJson.isNull("food_item") ? null : itemJson.optString("food_item", null);
            item.foodCategory = itemJson.getString("food_category");
            item.uom = itemJson.getString("uom");
            item.portionValue = itemJson.getDouble("portion_value");
            item.calories = itemJson.getDouble("calories");
            item.protein = itemJson.getDouble("protein");
            item.carbs = itemJson.getDouble("carbs");
            item.fat = itemJson.getDouble("fat");
            items.add(item);
        }

        VisionResult result = new VisionResult();
        result.dishTitle = parsed.optString("dish_title", "");
        result.items = items;
        return result;
    }

    private String extractErrorMessage(String responseBody) {
        try {
            JSONObject error = new JSONObject(responseBody).optJSONObject("error");
            return error != null ? error.optString("message", responseBody) : responseBody;
        } catch (JSONException e) {
            return responseBody;
        }
    }
}
