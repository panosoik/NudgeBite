package com.panagiotisoikonomou.nudgebite.food.well;

import com.panagiotisoikonomou.nudgebite.food.FoodAttribute;
import com.panagiotisoikonomou.nudgebite.food.FoodGroup;
import com.panagiotisoikonomou.nudgebite.food.FoodItem;
import com.panagiotisoikonomou.nudgebite.food.FoodSubgroup;
import com.panagiotisoikonomou.nudgebite.food.FoodTaxonomy;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** What WELL scoring reads. Only unambiguous links: no component is inferred from a broad parent, judgement calls stay unmapped. */
public final class FoodWellMappingRegistry {

    private static final Map<FoodItem, Set<WellComponent>> COMPONENTS_BY_ITEM = new EnumMap<>(FoodItem.class);

    static {
        // Starchy vegetables are not WELL vegetables.
        for (FoodItem item : FoodTaxonomy.itemsIn(FoodGroup.VEGETABLES)) {
            if (item.subgroup() != FoodSubgroup.STARCHY_VEGETABLES) {
                add(item, WellComponent.VEGETABLES);
            }
        }
        addAll(FoodTaxonomy.itemsIn(FoodGroup.FRUIT), WellComponent.FRUIT);
        addAll(FoodTaxonomy.itemsWith(FoodAttribute.WHOLE_GRAIN), WellComponent.WHOLE_GRAINS);

        addAll(Arrays.asList(
                FoodItem.LENTILS, FoodItem.CHICKPEAS, FoodItem.BLACK_BEANS, FoodItem.KIDNEY_BEANS,
                FoodItem.WHITE_BEANS, FoodItem.FAVA_BEANS, FoodItem.SPLIT_PEAS, FoodItem.EDAMAME),
                WellComponent.BEANS_AND_LENTILS);

        addAll(Arrays.asList(
                FoodItem.ALMONDS, FoodItem.WALNUTS, FoodItem.CASHEWS, FoodItem.PISTACHIOS,
                FoodItem.HAZELNUTS, FoodItem.PEANUTS, FoodItem.SUNFLOWER_SEEDS,
                FoodItem.PUMPKIN_SEEDS, FoodItem.CHIA_SEEDS, FoodItem.FLAXSEED),
                WellComponent.NUTS_AND_SEEDS);

        addAll(FoodTaxonomy.itemsIn(FoodGroup.MEAT), WellComponent.RED_AND_PROCESSED_MEAT);

        addAll(FoodTaxonomy.itemsIn(FoodSubgroup.FISH), WellComponent.FISH);

        addAll(FoodTaxonomy.itemsWith(FoodAttribute.SUGAR_SWEETENED),
                WellComponent.SUGARY_BEVERAGES_AND_JUICE);
        addAll(FoodTaxonomy.itemsWith(FoodAttribute.FRUIT_JUICE_100),
                WellComponent.SUGARY_BEVERAGES_AND_JUICE);

        for (FoodItem item : FoodTaxonomy.itemsIn(FoodGroup.SWEETS_BAKED)) {
            if (item != FoodItem.HONEY && item != FoodItem.JAM) {
                add(item, WellComponent.SWEETS_AND_BAKED_GOODS);
            }
        }
        COMPONENTS_BY_ITEM.replaceAll((item, components) -> Collections.unmodifiableSet(components));
    }

    private FoodWellMappingRegistry() {
    }

    public static Set<WellComponent> componentsOf(FoodItem item) {
        return COMPONENTS_BY_ITEM.getOrDefault(item, Collections.emptySet());
    }

    private static void addAll(List<FoodItem> items, WellComponent component) {
        for (FoodItem item : items) {
            add(item, component);
        }
    }

    private static void add(FoodItem item, WellComponent component) {
        COMPONENTS_BY_ITEM.computeIfAbsent(item, key -> EnumSet.noneOf(WellComponent.class)).add(component);
    }
}
