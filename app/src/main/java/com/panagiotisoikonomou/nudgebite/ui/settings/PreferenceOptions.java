package com.panagiotisoikonomou.nudgebite.ui.settings;

import com.panagiotisoikonomou.nudgebite.food.FoodGroup;
import com.panagiotisoikonomou.nudgebite.food.FoodTaxonomy;
import com.panagiotisoikonomou.nudgebite.ui.components.ChipSection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Only whole-food groups: a stated preference would pull suggestions toward sugary or processed items. */
public final class PreferenceOptions {

    public static final String ADDITIONS_SECTION_TITLE = "Your additions";

    private static final List<FoodGroup> PREFERENCE_GROUPS = List.of(
            FoodGroup.VEGETABLES,
            FoodGroup.FRUIT,
            FoodGroup.GRAINS,
            FoodGroup.LEGUMES,
            FoodGroup.NUTS_SEEDS,
            FoodGroup.SEAFOOD,
            FoodGroup.MEAT,
            FoodGroup.POULTRY,
            FoodGroup.EGGS,
            FoodGroup.DAIRY);

    private static final Map<String, String> OFFERED_BY_KEY = new LinkedHashMap<>();

    static {
        for (FoodGroup group : PREFERENCE_GROUPS) {
            for (String label : FoodTaxonomy.labelsIn(group)) {
                OFFERED_BY_KEY.put(normalize(label), label);
            }
        }
    }

    private PreferenceOptions() {
    }

    /** Chips match by exact string, so stored spellings are rewritten to the taxonomy's own. */
    public static List<String> canonicalize(List<String> storedPreferences) {
        List<String> canonical = new ArrayList<>();
        if (storedPreferences == null) {
            return canonical;
        }
        Set<String> seen = new HashSet<>();
        for (String preference : storedPreferences) {
            if (preference == null || preference.trim().isEmpty()) {
                continue;
            }
            String key = normalize(preference);
            if (seen.add(key)) {
                canonical.add(OFFERED_BY_KEY.getOrDefault(key, preference));
            }
        }
        return canonical;
    }

    public static List<ChipSection> sections(List<String> storedPreferences) {
        List<ChipSection> sections = new ArrayList<>(PREFERENCE_GROUPS.size() + 1);
        for (FoodGroup group : PREFERENCE_GROUPS) {
            sections.add(new ChipSection(group.label(), FoodTaxonomy.labelsIn(group)));
        }
        sections.add(new ChipSection(ADDITIONS_SECTION_TITLE, additions(storedPreferences)));
        return sections;
    }

    private static List<String> additions(List<String> storedPreferences) {
        List<String> additions = new ArrayList<>();
        if (storedPreferences == null) {
            return additions;
        }
        Set<String> seen = new HashSet<>();
        for (String preference : storedPreferences) {
            if (preference == null || preference.trim().isEmpty()) {
                continue;
            }
            String key = normalize(preference);
            if (!OFFERED_BY_KEY.containsKey(key) && seen.add(key)) {
                additions.add(preference);
            }
        }
        return additions;
    }

    private static String normalize(String label) {
        return label.trim().toLowerCase(Locale.US);
    }
}
