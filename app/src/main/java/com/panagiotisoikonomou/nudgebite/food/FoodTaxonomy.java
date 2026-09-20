package com.panagiotisoikonomou.nudgebite.food;

import androidx.annotation.Nullable;


import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FoodTaxonomy {

    private static final Map<FoodGroup, List<FoodItem>> ITEMS_BY_GROUP = new EnumMap<>(FoodGroup.class);
    private static final Map<FoodSubgroup, List<FoodItem>> ITEMS_BY_SUBGROUP = new EnumMap<>(FoodSubgroup.class);
    private static final Map<String, FoodItem> BY_LABEL = new LinkedHashMap<>();

    static {
        for (FoodGroup group : FoodGroup.values()) {
            ITEMS_BY_GROUP.put(group, new ArrayList<>());
        }
        for (FoodSubgroup subgroup : FoodSubgroup.values()) {
            ITEMS_BY_SUBGROUP.put(subgroup, new ArrayList<>());
        }
        for (FoodItem item : FoodItem.values()) {
            ITEMS_BY_GROUP.get(item.group()).add(item);
            if (item.subgroup() != null) {
                ITEMS_BY_SUBGROUP.get(item.subgroup()).add(item);
            }
            BY_LABEL.put(normalize(item.label()), item);
        }
        freeze(ITEMS_BY_GROUP);
        freeze(ITEMS_BY_SUBGROUP);
    }

    private FoodTaxonomy() {
    }

    public static List<FoodItem> itemsIn(FoodGroup group) {
        return ITEMS_BY_GROUP.get(group);
    }

    public static List<FoodItem> itemsIn(FoodSubgroup subgroup) {
        return ITEMS_BY_SUBGROUP.get(subgroup);
    }

    @Nullable
    public static FoodItem byLabel(String label) {
        return label == null ? null : BY_LABEL.get(normalize(label));
    }

    public static List<String> labelsIn(FoodGroup group) {
        List<FoodItem> items = itemsIn(group);
        List<String> labels = new ArrayList<>(items.size());
        for (FoodItem item : items) {
            labels.add(item.label());
        }
        return Collections.unmodifiableList(labels);
    }

    public static List<FoodItem> itemsWith(FoodAttribute attribute) {
        List<FoodItem> matching = new ArrayList<>();
        for (FoodItem item : FoodItem.values()) {
            if (item.has(attribute)) {
                matching.add(item);
            }
        }
        return Collections.unmodifiableList(matching);
    }

    private static String normalize(String label) {
        return label.trim().toLowerCase(Locale.US);
    }

    private static <K, V> void freeze(Map<K, List<V>> map) {
        for (Map.Entry<K, List<V>> entry : map.entrySet()) {
            entry.setValue(Collections.unmodifiableList(entry.getValue()));
        }
    }
}
