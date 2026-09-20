package com.panagiotisoikonomou.nudgebite.engine.recommendation;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.enums.FoodCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MealRecommendation {

    public final String id;
    public final String title;
    public final List<String> tags;
    public final String bodyText;
    public final String homeBodyText;
    public final Set<FoodCategory> richIn;
    public final List<String> ingredientKeywords;
    public final Map<FoodCategory, String> ingredientByCategory;

    private MealRecommendation(String id, String title, List<String> tags, String bodyText,
                               String homeBodyText, Set<FoodCategory> richIn,
                               List<String> ingredientKeywords,
                               Map<FoodCategory, String> ingredientByCategory) {
        this.id = id;
        this.title = title;
        this.tags = tags;
        this.bodyText = bodyText;
        this.homeBodyText = homeBodyText;
        this.richIn = richIn;
        this.ingredientKeywords = ingredientKeywords;
        this.ingredientByCategory = ingredientByCategory;
    }

    static MealRecommendation of(String id, String title, String bodyText, String homeBodyText,
                                 List<String> tags, List<String> ingredientKeywords,
                                 Map<FoodCategory, String> ingredientByCategory,
                                 FoodCategory... richIn) {
        return new MealRecommendation(id, title, Collections.unmodifiableList(tags), bodyText,
                homeBodyText, Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(richIn))),
                Collections.unmodifiableList(ingredientKeywords),
                Collections.unmodifiableMap(ingredientByCategory));
    }

    static MealRecommendation of(String id, String title, String bodyText, List<String> tags,
                                 List<String> ingredientKeywords,
                                 Map<FoodCategory, String> ingredientByCategory,
                                 FoodCategory... richIn) {
        return of(id, title, bodyText, bodyText, tags, ingredientKeywords, ingredientByCategory, richIn);
    }

    @Nullable
    public static MealRecommendation byId(String id) {
        for (MealRecommendation recommendation : CATALOG) {
            if (recommendation.id.equals(id)) {
                return recommendation;
            }
        }
        return null;
    }

    private static Map<FoodCategory, String> ingredientMap(Object... categoryWordPairs) {
        Map<FoodCategory, String> map = new EnumMap<>(FoodCategory.class);
        for (int i = 0; i < categoryWordPairs.length; i += 2) {
            map.put((FoodCategory) categoryWordPairs[i], (String) categoryWordPairs[i + 1]);
        }
        return map;
    }

    /** richIn lists what a dish contains, including negative categories that are never ranked as gaps. */
    public static final List<MealRecommendation> CATALOG = Collections.unmodifiableList(Arrays.asList(

            of("grilled_chicken_brown_rice_salad",
                    "Grilled chicken with brown rice & salad",
                    "A solid pick when your recent meals could use more whole grains, vegetables, and lean protein.",
                    "Chicken with brown rice and a simple salad is an easy way to add more whole grains, vegetables, and lean protein to the day.",
                    Arrays.asList(
                            "lean protein",
                            "whole grain",
                            "fresh veg"
                    ),
                    Arrays.asList(
                            "chicken",
                            "brown rice",
                            "rice",
                            "salad"
                    ),
                    ingredientMap(
                            FoodCategory.POULTRY, "chicken",
                            FoodCategory.WHOLE_GRAIN, "brown rice",
                            FoodCategory.OTHER_VEGETABLE, "salad"
                    ),
                    FoodCategory.POULTRY,
                    FoodCategory.WHOLE_GRAIN,
                    FoodCategory.OTHER_VEGETABLE),

            of("grilled_fish_greens",
                    "Grilled fish with greens",
                    "A good option when you could use more fish and leafy greens in your recent meals.",
                    "Grilled fish with a side of greens is a simple way to bring more fish and leafy vegetables into the day.",
                    Arrays.asList(
                            "fish",
                            "leafy greens",
                            "light"
                    ),
                    Arrays.asList(
                            "fish",
                            "salmon",
                            "sea bass",
                            "sea bream",
                            "greens"
                    ),
                    ingredientMap(
                            FoodCategory.FISH, "salmon",
                            FoodCategory.LEAFY_GREEN, "greens"
                    ),
                    FoodCategory.FISH,
                    FoodCategory.LEAFY_GREEN),

            of("lentil_soup_vegetables",
                    "Lentil soup with vegetables",
                    "A hearty choice when your recent meals have been a little light on legumes and vegetables.",
                    "A bowl of lentil soup is an easy way to add more legumes and vegetables without making the meal complicated.",
                    Arrays.asList(
                            "legumes",
                            "vegetables",
                            "hearty"
                    ),
                    Arrays.asList(
                            "lentil",
                            "lentils",
                            "vegetables",
                            "soup"
                    ),
                    ingredientMap(
                            FoodCategory.LEGUME, "lentils",
                            FoodCategory.OTHER_VEGETABLE, "vegetables"
                    ),
                    FoodCategory.LEGUME,
                    FoodCategory.OTHER_VEGETABLE),

            of("grilled_chicken_broccoli",
                    "Grilled chicken with broccoli",
                    "A simple option when adding some broccoli and lean protein would round out your recent meals.",
                    "Grilled chicken and broccoli make an easy meal when you want to add more cruciferous vegetables and lean protein.",
                    Arrays.asList(
                            "lean protein",
                            "broccoli",
                            "simple"
                    ),
                    Arrays.asList(
                            "chicken",
                            "broccoli"
                    ),
                    ingredientMap(
                            FoodCategory.POULTRY, "chicken",
                            FoodCategory.CRUCIFEROUS, "broccoli"
                    ),
                    FoodCategory.POULTRY,
                    FoodCategory.CRUCIFEROUS),

            of("sweet_potato_chickpea_salad",
                    "Roasted sweet potato & chickpea salad",
                    "A filling plant-based option when your recent meals could use more legumes and root vegetables.",
                    "Roasted sweet potato and chickpeas make a filling way to add more legumes and root vegetables to the day.",
                    Arrays.asList(
                            "plant protein",
                            "root veg",
                            "filling"
                    ),
                    Arrays.asList(
                            "sweet potato",
                            "sweet potatoes",
                            "chickpea",
                            "chickpeas"
                    ),
                    ingredientMap(
                            FoodCategory.ROOT_VEGETABLE, "sweet potato",
                            FoodCategory.LEGUME, "chickpeas"
                    ),
                    FoodCategory.ROOT_VEGETABLE,
                    FoodCategory.LEGUME),

            of("shrimp_grilled_vegetables",
                    "Shrimp with grilled vegetables",
                    "A lighter choice that adds seafood and a good serving of vegetables to what you have eaten recently.",
                    "Shrimp with a mix of grilled vegetables is a quick way to add more seafood and vegetables to the day.",
                    Arrays.asList(
                            "seafood",
                            "grilled veg",
                            "light"
                    ),
                    Arrays.asList(
                            "shrimp",
                            "prawns",
                            "vegetables",
                            "grilled vegetables"
                    ),
                    ingredientMap(
                            FoodCategory.SEAFOOD, "shrimp",
                            FoodCategory.OTHER_VEGETABLE, "vegetables"
                    ),
                    FoodCategory.SEAFOOD,
                    FoodCategory.OTHER_VEGETABLE),

            of("greek_yogurt_honey_walnuts_fruit",
                    "Greek yogurt with honey, walnuts & fruit",
                    "A good lighter option when your recent choices could use more fruit, nuts, and dairy.",
                    "Greek yogurt with fruit, walnuts, and a little honey is an easy way to add fruit, nuts, and dairy to the day.",
                    Arrays.asList(
                            "fruit",
                            "nuts",
                            "yogurt"
                    ),
                    Arrays.asList(
                            "yogurt",
                            "greek yogurt",
                            "walnuts",
                            "nuts",
                            "fruit",
                            "berries",
                            "honey"
                    ),
                    ingredientMap(
                            FoodCategory.FRUIT, "fruit",
                            FoodCategory.NUT_SEED, "walnuts",
                            FoodCategory.DAIRY, "yogurt"
                    ),
                    FoodCategory.FRUIT,
                    FoodCategory.NUT_SEED,
                    FoodCategory.DAIRY),

            of("whole_wheat_pasta_vegetables",
                    "Whole-wheat pasta with tomato & vegetables",
                    "A familiar option when adding more whole grains and vegetables would help balance your recent meals.",
                    "Whole-wheat pasta with tomato and vegetables is an easy way to get more whole grains and vegetables into the day.",
                    Arrays.asList(
                            "whole grain",
                            "vegetables",
                            "filling"
                    ),
                    Arrays.asList(
                            "pasta",
                            "whole wheat pasta",
                            "whole-wheat pasta",
                            "tomato",
                            "vegetables"
                    ),
                    ingredientMap(
                            FoodCategory.WHOLE_GRAIN, "whole-wheat pasta",
                            FoodCategory.OTHER_VEGETABLE, "vegetables"
                    ),
                    FoodCategory.WHOLE_GRAIN,
                    FoodCategory.OTHER_VEGETABLE),

            of("spinach_feta_omelette_salad",
                    "Spinach & feta omelette with salad",
                    "A good pick when some eggs and leafy greens would add a bit more variety to your recent meals.",
                    "A spinach and feta omelette with salad is a simple way to add eggs, leafy greens, and vegetables to the day.",
                    Arrays.asList(
                            "eggs",
                            "leafy greens",
                            "fresh veg"
                    ),
                    Arrays.asList(
                            "egg",
                            "eggs",
                            "omelette",
                            "omelet",
                            "spinach",
                            "feta",
                            "salad"
                    ),
                    ingredientMap(
                            FoodCategory.EGG, "eggs",
                            FoodCategory.LEAFY_GREEN, "spinach",
                            FoodCategory.CHEESE, "feta",
                            FoodCategory.OTHER_VEGETABLE, "salad"
                    ),
                    FoodCategory.EGG,
                    FoodCategory.LEAFY_GREEN,
                    FoodCategory.CHEESE,
                    FoodCategory.OTHER_VEGETABLE),

            of("chickpea_salad_whole_grain_pita",
                    "Chickpea salad with whole-grain pita",
                    "A filling choice when your recent meals could use more legumes, whole grains, and vegetables.",
                    "Chickpea salad with whole-grain pita is a straightforward way to add legumes, whole grains, and vegetables to the day.",
                    Arrays.asList(
                            "plant protein",
                            "whole grain",
                            "fresh veg"
                    ),
                    Arrays.asList(
                            "chickpea",
                            "chickpeas",
                            "pita",
                            "whole grain pita",
                            "whole-grain pita",
                            "salad"
                    ),
                    ingredientMap(
                            FoodCategory.LEGUME, "chickpeas",
                            FoodCategory.WHOLE_GRAIN, "whole-grain pita",
                            FoodCategory.OTHER_VEGETABLE, "salad"
                    ),
                    FoodCategory.LEGUME,
                    FoodCategory.WHOLE_GRAIN,
                    FoodCategory.OTHER_VEGETABLE),

            of("chicken_souvlaki_pita_salad",
                    "Chicken souvlaki with pita & salad",
                    "A familiar option when your recent meals could use more vegetables and lean protein.",
                    Arrays.asList(
                            "lean protein",
                            "fresh veg",
                            "grilled"
                    ),
                    Arrays.asList(
                            "chicken",
                            "souvlaki",
                            "pita",
                            "salad"
                    ),
                    ingredientMap(
                            FoodCategory.POULTRY, "chicken",
                            FoodCategory.REFINED_GRAIN, "pita",
                            FoodCategory.OTHER_VEGETABLE, "salad"
                    ),
                    FoodCategory.POULTRY,
                    FoodCategory.REFINED_GRAIN,
                    FoodCategory.OTHER_VEGETABLE),

            of("chicken_souvlaki_potatoes_salad",
                    "Chicken souvlaki plate with potatoes & salad",
                    "A filling choice that adds lean protein and vegetables to your day.",
                    Arrays.asList(
                            "lean protein",
                            "fresh veg",
                            "filling"
                    ),
                    Arrays.asList(
                            "chicken",
                            "souvlaki",
                            "potatoes",
                            "potato",
                            "salad"
                    ),
                    ingredientMap(
                            FoodCategory.POULTRY, "chicken",
                            FoodCategory.POTATO, "potatoes",
                            FoodCategory.OTHER_VEGETABLE, "salad"
                    ),
                    FoodCategory.POULTRY,
                    FoodCategory.POTATO,
                    FoodCategory.OTHER_VEGETABLE),

            of("beef_burger_salad_potatoes",
                    "Beef burger with salad & potatoes",
                    "If you're in the mood for a burger, adding a good side of salad can help keep the meal more balanced.",
                    Arrays.asList(
                            "beef",
                            "fresh veg",
                            "filling"
                    ),
                    Arrays.asList(
                            "beef",
                            "burger",
                            "bun",
                            "potatoes",
                            "potato",
                            "salad"
                    ),
                    ingredientMap(
                            FoodCategory.RED_MEAT, "beef",
                            FoodCategory.REFINED_GRAIN, "bun",
                            FoodCategory.POTATO, "potatoes",
                            FoodCategory.OTHER_VEGETABLE, "salad"
                    ),
                    FoodCategory.RED_MEAT,
                    FoodCategory.REFINED_GRAIN,
                    FoodCategory.POTATO,
                    FoodCategory.OTHER_VEGETABLE),

            of("beef_meatballs_tomato_rice",
                    "Beef meatballs with tomato sauce & rice",
                    "A hearty option with vegetables in the mix when you want something more substantial.",
                    Arrays.asList(
                            "beef",
                            "tomato",
                            "filling"
                    ),
                    Arrays.asList(
                            "beef",
                            "meatballs",
                            "tomato",
                            "rice"
                    ),
                    ingredientMap(
                            FoodCategory.RED_MEAT, "beef",
                            FoodCategory.OTHER_VEGETABLE, "tomato",
                            FoodCategory.REFINED_GRAIN, "rice"
                    ),
                    FoodCategory.RED_MEAT,
                    FoodCategory.OTHER_VEGETABLE,
                    FoodCategory.REFINED_GRAIN),

            of("beef_stew_potatoes_carrots",
                    "Beef stew with potatoes & carrots",
                    "A hearty meal that also brings some root vegetables into the day.",
                    Arrays.asList(
                            "beef",
                            "root veg",
                            "hearty"
                    ),
                    Arrays.asList(
                            "beef",
                            "stew",
                            "potatoes",
                            "potato",
                            "carrots",
                            "carrot"
                    ),
                    ingredientMap(
                            FoodCategory.RED_MEAT, "beef",
                            FoodCategory.POTATO, "potatoes",
                            FoodCategory.ROOT_VEGETABLE, "carrots"
                    ),
                    FoodCategory.RED_MEAT,
                    FoodCategory.POTATO,
                    FoodCategory.ROOT_VEGETABLE),

            of("pork_souvlaki_pita_vegetables",
                    "Pork souvlaki with pita & vegetables",
                    "A familiar choice that works well with plenty of fresh vegetables on the side.",
                    Arrays.asList(
                            "pork",
                            "fresh veg",
                            "grilled"
                    ),
                    Arrays.asList(
                            "pork",
                            "souvlaki",
                            "pita",
                            "vegetables"
                    ),
                    ingredientMap(
                            FoodCategory.RED_MEAT, "pork",
                            FoodCategory.REFINED_GRAIN, "pita",
                            FoodCategory.OTHER_VEGETABLE, "vegetables"
                    ),
                    FoodCategory.RED_MEAT,
                    FoodCategory.REFINED_GRAIN,
                    FoodCategory.OTHER_VEGETABLE),

            of("pork_chop_potatoes_salad",
                    "Pork chop with potatoes & salad",
                    "A straightforward meal where a generous salad can add some extra vegetables to your day.",
                    Arrays.asList(
                            "pork",
                            "fresh veg",
                            "filling"
                    ),
                    Arrays.asList(
                            "pork",
                            "pork chop",
                            "potatoes",
                            "potato",
                            "salad"
                    ),
                    ingredientMap(
                            FoodCategory.RED_MEAT, "pork",
                            FoodCategory.POTATO, "potatoes",
                            FoodCategory.OTHER_VEGETABLE, "salad"
                    ),
                    FoodCategory.RED_MEAT,
                    FoodCategory.POTATO,
                    FoodCategory.OTHER_VEGETABLE),

            of("spaghetti_minced_beef_tomato",
                    "Spaghetti with minced beef & tomato sauce",
                    "A familiar pasta meal with tomato in the mix when you want something filling.",
                    Arrays.asList(
                            "beef",
                            "tomato",
                            "filling"
                    ),
                    Arrays.asList(
                            "beef",
                            "minced beef",
                            "spaghetti",
                            "pasta",
                            "tomato"
                    ),
                    ingredientMap(
                            FoodCategory.RED_MEAT, "minced beef",
                            FoodCategory.REFINED_GRAIN, "spaghetti",
                            FoodCategory.OTHER_VEGETABLE, "tomato"
                    ),
                    FoodCategory.RED_MEAT,
                    FoodCategory.REFINED_GRAIN,
                    FoodCategory.OTHER_VEGETABLE),

            of("omelette_tomato_peppers_feta",
                    "Omelette with tomato, peppers & feta",
                    "A simple option when your recent meals could use more eggs and vegetables.",
                    Arrays.asList(
                            "eggs",
                            "fresh veg",
                            "quick"
                    ),
                    Arrays.asList(
                            "egg",
                            "eggs",
                            "omelette",
                            "omelet",
                            "tomato",
                            "peppers",
                            "pepper",
                            "feta"
                    ),
                    ingredientMap(
                            FoodCategory.EGG, "eggs",
                            FoodCategory.OTHER_VEGETABLE, "peppers",
                            FoodCategory.CHEESE, "feta"
                    ),
                    FoodCategory.EGG,
                    FoodCategory.OTHER_VEGETABLE,
                    FoodCategory.CHEESE),

            of("scrambled_eggs_whole_grain_toast_tomato",
                    "Scrambled eggs with whole-grain toast & tomato",
                    "A quick meal that adds eggs, whole grains and some fresh vegetables to the day.",
                    Arrays.asList(
                            "eggs",
                            "whole grain",
                            "quick"
                    ),
                    Arrays.asList(
                            "egg",
                            "eggs",
                            "scrambled eggs",
                            "whole-grain toast",
                            "toast",
                            "bread",
                            "tomato"
                    ),
                    ingredientMap(
                            FoodCategory.EGG, "eggs",
                            FoodCategory.WHOLE_GRAIN, "whole-grain toast",
                            FoodCategory.OTHER_VEGETABLE, "tomato"
                    ),
                    FoodCategory.EGG,
                    FoodCategory.WHOLE_GRAIN,
                    FoodCategory.OTHER_VEGETABLE),

            of("greek_salad_feta_olives",
                    "Greek salad with feta & olives",
                    "A simple way to get a good mix of fresh vegetables into your meal.",
                    Arrays.asList(
                            "fresh veg",
                            "feta",
                            "light"
                    ),
                    Arrays.asList(
                            "salad",
                            "greek salad",
                            "tomato",
                            "cucumber",
                            "feta",
                            "olives",
                            "olive oil"
                    ),
                    ingredientMap(
                            FoodCategory.OTHER_VEGETABLE, "tomato",
                            FoodCategory.CHEESE, "feta",
                            FoodCategory.FAT_OIL, "olive oil"
                    ),
                    FoodCategory.OTHER_VEGETABLE,
                    FoodCategory.CHEESE,
                    FoodCategory.FAT_OIL),

            of("dakos_tomato_feta",
                    "Dakos with tomato & feta",
                    "A light option that combines whole grains with fresh tomato and a little cheese.",
                    Arrays.asList(
                            "whole grain",
                            "tomato",
                            "light"
                    ),
                    Arrays.asList(
                            "dakos",
                            "barley rusk",
                            "rusk",
                            "tomato",
                            "feta",
                            "olive oil"
                    ),
                    ingredientMap(
                            FoodCategory.WHOLE_GRAIN, "barley rusk",
                            FoodCategory.OTHER_VEGETABLE, "tomato",
                            FoodCategory.CHEESE, "feta",
                            FoodCategory.FAT_OIL, "olive oil"
                    ),
                    FoodCategory.WHOLE_GRAIN,
                    FoodCategory.OTHER_VEGETABLE,
                    FoodCategory.CHEESE,
                    FoodCategory.FAT_OIL),

            of("spinach_pie_feta",
                    "Spinach pie with feta",
                    "A familiar option when adding some leafy greens to your day would help.",
                    Arrays.asList(
                            "leafy greens",
                            "feta",
                            "familiar"
                    ),
                    Arrays.asList(
                            "spinach",
                            "spinach pie",
                            "spanakopita",
                            "feta",
                            "phyllo",
                            "pie"
                    ),
                    ingredientMap(
                            FoodCategory.LEAFY_GREEN, "spinach",
                            FoodCategory.CHEESE, "feta",
                            FoodCategory.REFINED_GRAIN, "phyllo"
                    ),
                    FoodCategory.LEAFY_GREEN,
                    FoodCategory.CHEESE,
                    FoodCategory.REFINED_GRAIN),

            of("stuffed_tomatoes_peppers_rice",
                    "Stuffed tomatoes & peppers with rice",
                    "A vegetable-based meal that can add more variety to your usual choices.",
                    Arrays.asList(
                            "vegetables",
                            "rice",
                            "filling"
                    ),
                    Arrays.asList(
                            "tomatoes",
                            "tomato",
                            "peppers",
                            "pepper",
                            "rice",
                            "gemista"
                    ),
                    ingredientMap(
                            FoodCategory.OTHER_VEGETABLE, "tomatoes",
                            FoodCategory.REFINED_GRAIN, "rice"
                    ),
                    FoodCategory.OTHER_VEGETABLE,
                    FoodCategory.REFINED_GRAIN),

            of("briam_roasted_vegetables",
                    "Briam roasted vegetables",
                    "A good pick when your recent meals have been a little light on vegetables.",
                    Arrays.asList(
                            "vegetables",
                            "roasted",
                            "simple"
                    ),
                    Arrays.asList(
                            "briam",
                            "vegetables",
                            "zucchini",
                            "courgette",
                            "aubergine",
                            "eggplant",
                            "peppers",
                            "potatoes",
                            "potato",
                            "olive oil"
                    ),
                    ingredientMap(
                            FoodCategory.OTHER_VEGETABLE, "zucchini",
                            FoodCategory.POTATO, "potatoes",
                            FoodCategory.FAT_OIL, "olive oil"
                    ),
                    FoodCategory.OTHER_VEGETABLE,
                    FoodCategory.POTATO,
                    FoodCategory.FAT_OIL),

            of("green_beans_tomato_potatoes",
                    "Green beans with tomato & potatoes",
                    "A simple vegetable-based meal when you'd like to get more vegetables into the week.",
                    Arrays.asList(
                            "vegetables",
                            "tomato",
                            "simple"
                    ),
                    Arrays.asList(
                            "green beans",
                            "fasolakia",
                            "tomato",
                            "potatoes",
                            "potato",
                            "olive oil"
                    ),
                    ingredientMap(
                            FoodCategory.OTHER_VEGETABLE, "green beans",
                            FoodCategory.POTATO, "potatoes",
                            FoodCategory.FAT_OIL, "olive oil"
                    ),
                    FoodCategory.OTHER_VEGETABLE,
                    FoodCategory.POTATO,
                    FoodCategory.FAT_OIL),

            // Green peas count as a starchy vegetable, not a legume, matching FoodItem.GREEN_PEAS.
            of("peas_carrots_potatoes",
                    "Peas with carrots & potatoes",
                    "A comforting option that brings peas and root vegetables together in one meal.",
                    Arrays.asList(
                            "vegetables",
                            "root veg",
                            "hearty"
                    ),
                    Arrays.asList(
                            "peas",
                            "arakas",
                            "carrots",
                            "carrot",
                            "potatoes",
                            "potato"
                    ),
                    ingredientMap(
                            FoodCategory.OTHER_VEGETABLE, "peas",
                            FoodCategory.ROOT_VEGETABLE, "carrots",
                            FoodCategory.POTATO, "potatoes"
                    ),
                    FoodCategory.OTHER_VEGETABLE,
                    FoodCategory.ROOT_VEGETABLE,
                    FoodCategory.POTATO),

            of("chickpea_soup_lemon",
                    "Chickpea soup with lemon",
                    "A filling choice when your recent meals could use another serving of legumes.",
                    Arrays.asList(
                            "legumes",
                            "soup",
                            "filling"
                    ),
                    Arrays.asList(
                            "chickpea",
                            "chickpeas",
                            "revithia",
                            "soup",
                            "lemon"
                    ),
                    ingredientMap(
                            FoodCategory.LEGUME, "chickpeas"
                    ),
                    FoodCategory.LEGUME),

            of("bean_soup_vegetables",
                    "Bean soup with vegetables",
                    "A hearty way to add more legumes and vegetables to your week.",
                    Arrays.asList(
                            "legumes",
                            "vegetables",
                            "hearty"
                    ),
                    Arrays.asList(
                            "bean",
                            "beans",
                            "white beans",
                            "fasolada",
                            "soup",
                            "vegetables",
                            "carrots",
                            "celery"
                    ),
                    ingredientMap(
                            FoodCategory.LEGUME, "beans",
                            FoodCategory.OTHER_VEGETABLE, "vegetables"
                    ),
                    FoodCategory.LEGUME,
                    FoodCategory.OTHER_VEGETABLE),

            of("giant_beans_tomato_sauce",
                    "Giant beans in tomato sauce",
                    "A filling legume-based option when you'd like something simple and satisfying.",
                    Arrays.asList(
                            "legumes",
                            "tomato",
                            "filling"
                    ),
                    Arrays.asList(
                            "beans",
                            "giant beans",
                            "gigantes",
                            "butter beans",
                            "tomato"
                    ),
                    ingredientMap(
                            FoodCategory.LEGUME, "giant beans",
                            FoodCategory.OTHER_VEGETABLE, "tomato"
                    ),
                    FoodCategory.LEGUME,
                    FoodCategory.OTHER_VEGETABLE),

            of("lentil_salad_tomato_cucumber_feta",
                    "Lentil salad with tomato, cucumber & feta",
                    "A fresh option that combines legumes with plenty of vegetables.",
                    Arrays.asList(
                            "legumes",
                            "fresh veg",
                            "light"
                    ),
                    Arrays.asList(
                            "lentil",
                            "lentils",
                            "salad",
                            "tomato",
                            "cucumber",
                            "feta"
                    ),
                    ingredientMap(
                            FoodCategory.LEGUME, "lentils",
                            FoodCategory.OTHER_VEGETABLE, "tomato",
                            FoodCategory.CHEESE, "feta"
                    ),
                    FoodCategory.LEGUME,
                    FoodCategory.OTHER_VEGETABLE,
                    FoodCategory.CHEESE),

            of("tuna_salad_vegetables",
                    "Tuna salad with vegetables",
                    "A light option when your recent meals could use more fish and vegetables.",
                    Arrays.asList(
                            "fish",
                            "fresh veg",
                            "light"
                    ),
                    Arrays.asList(
                            "tuna",
                            "fish",
                            "salad",
                            "tomato",
                            "cucumber",
                            "vegetables"
                    ),
                    ingredientMap(
                            FoodCategory.FISH, "tuna",
                            FoodCategory.OTHER_VEGETABLE, "vegetables"
                    ),
                    FoodCategory.FISH,
                    FoodCategory.OTHER_VEGETABLE),

            of("grilled_sardines_salad",
                    "Grilled sardines with salad",
                    "A simple way to include some fish alongside a good serving of vegetables.",
                    Arrays.asList(
                            "fish",
                            "fresh veg",
                            "grilled"
                    ),
                    Arrays.asList(
                            "sardines",
                            "sardine",
                            "fish",
                            "salad",
                            "vegetables"
                    ),
                    ingredientMap(
                            FoodCategory.FISH, "sardines",
                            FoodCategory.OTHER_VEGETABLE, "salad"
                    ),
                    FoodCategory.FISH,
                    FoodCategory.OTHER_VEGETABLE),

            of("baked_sea_bream_potatoes",
                    "Baked sea bream with potatoes",
                    "A familiar fish meal when adding some more fish to your week would be useful.",
                    Arrays.asList(
                            "fish",
                            "baked",
                            "filling"
                    ),
                    Arrays.asList(
                            "sea bream",
                            "tsipoura",
                            "fish",
                            "potatoes",
                            "potato"
                    ),
                    ingredientMap(
                            FoodCategory.FISH, "sea bream",
                            FoodCategory.POTATO, "potatoes"
                    ),
                    FoodCategory.FISH,
                    FoodCategory.POTATO),

            of("shrimp_pasta_tomato_sauce",
                    "Shrimp pasta with tomato sauce",
                    "A filling seafood option when you want something different from your usual meat-based meals.",
                    Arrays.asList(
                            "seafood",
                            "tomato",
                            "filling"
                    ),
                    Arrays.asList(
                            "shrimp",
                            "prawns",
                            "seafood",
                            "pasta",
                            "spaghetti",
                            "tomato"
                    ),
                    ingredientMap(
                            FoodCategory.SEAFOOD, "shrimp",
                            FoodCategory.REFINED_GRAIN, "pasta",
                            FoodCategory.OTHER_VEGETABLE, "tomato"
                    ),
                    FoodCategory.SEAFOOD,
                    FoodCategory.REFINED_GRAIN,
                    FoodCategory.OTHER_VEGETABLE),

            of("seafood_risotto",
                    "Seafood risotto",
                    "A satisfying option that can help bring some more seafood into your week.",
                    Arrays.asList(
                            "seafood",
                            "rice",
                            "filling"
                    ),
                    Arrays.asList(
                            "seafood",
                            "shrimp",
                            "mussels",
                            "squid",
                            "calamari",
                            "risotto",
                            "rice"
                    ),
                    ingredientMap(
                            FoodCategory.SEAFOOD, "seafood",
                            FoodCategory.REFINED_GRAIN, "rice"
                    ),
                    FoodCategory.SEAFOOD,
                    FoodCategory.REFINED_GRAIN),

            of("yogurt_banana_walnuts_honey",
                    "Yogurt with banana, walnuts & honey",
                    "A quick option when your recent meals could use more fruit and nuts.",
                    Arrays.asList(
                            "fruit",
                            "nuts",
                            "yogurt"
                    ),
                    Arrays.asList(
                            "yogurt",
                            "greek yogurt",
                            "banana",
                            "walnuts",
                            "nuts",
                            "honey"
                    ),
                    ingredientMap(
                            FoodCategory.DAIRY, "yogurt",
                            FoodCategory.FRUIT, "banana",
                            FoodCategory.NUT_SEED, "walnuts"
                    ),
                    FoodCategory.DAIRY,
                    FoodCategory.FRUIT,
                    FoodCategory.NUT_SEED),

            of("oatmeal_yogurt_apple_walnuts",
                    "Oatmeal with yogurt, apple & walnuts",
                    "A filling breakfast or snack that adds whole grains, fruit and nuts to the day.",
                    Arrays.asList(
                            "whole grain",
                            "fruit",
                            "nuts"
                    ),
                    Arrays.asList(
                            "oats",
                            "oatmeal",
                            "porridge",
                            "yogurt",
                            "apple",
                            "walnuts",
                            "nuts"
                    ),
                    ingredientMap(
                            FoodCategory.WHOLE_GRAIN, "oats",
                            FoodCategory.DAIRY, "yogurt",
                            FoodCategory.FRUIT, "apple",
                            FoodCategory.NUT_SEED, "walnuts"
                    ),
                    FoodCategory.WHOLE_GRAIN,
                    FoodCategory.DAIRY,
                    FoodCategory.FRUIT,
                    FoodCategory.NUT_SEED),

            of("whole_grain_toast_tahini_banana",
                    "Whole-grain toast with tahini & banana",
                    "A quick choice that brings together whole grains, fruit and seeds.",
                    Arrays.asList(
                            "whole grain",
                            "fruit",
                            "quick"
                    ),
                    Arrays.asList(
                            "whole-grain toast",
                            "toast",
                            "bread",
                            "tahini",
                            "sesame",
                            "banana"
                    ),
                    ingredientMap(
                            FoodCategory.WHOLE_GRAIN, "whole-grain toast",
                            FoodCategory.NUT_SEED, "tahini",
                            FoodCategory.FRUIT, "banana"
                    ),
                    FoodCategory.WHOLE_GRAIN,
                    FoodCategory.NUT_SEED,
                    FoodCategory.FRUIT),

            of("chicken_sandwich_cheese_tomato_lettuce",
                    "Chicken sandwich with cheese, tomato & lettuce",
                    "A practical option when you want something quick with lean protein and some vegetables.",
                    Arrays.asList(
                            "lean protein",
                            "fresh veg",
                            "quick"
                    ),
                    Arrays.asList(
                            "chicken",
                            "sandwich",
                            "bread",
                            "cheese",
                            "tomato",
                            "lettuce"
                    ),
                    ingredientMap(
                            FoodCategory.POULTRY, "chicken",
                            FoodCategory.REFINED_GRAIN, "bread",
                            FoodCategory.CHEESE, "cheese",
                            FoodCategory.OTHER_VEGETABLE, "tomato"
                    ),
                    FoodCategory.POULTRY,
                    FoodCategory.REFINED_GRAIN,
                    FoodCategory.CHEESE,
                    FoodCategory.OTHER_VEGETABLE)
    ));

    private static final Set<String> LIGHT_IDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "greek_yogurt_honey_walnuts_fruit",
            "yogurt_banana_walnuts_honey",
            "whole_grain_toast_tahini_banana",
            "greek_salad_feta_olives",
            "dakos_tomato_feta",
            "lentil_salad_tomato_cucumber_feta",
            "tuna_salad_vegetables",
            "chickpea_salad_whole_grain_pita",
            "chicken_sandwich_cheese_tomato_lettuce",
            "oatmeal_yogurt_apple_walnuts",
            "omelette_tomato_peppers_feta",
            "scrambled_eggs_whole_grain_toast_tomato",
            "spinach_feta_omelette_salad",
            "grilled_fish_greens",
            "shrimp_grilled_vegetables",
            "grilled_chicken_broccoli")));

    public boolean isLight() {
        return LIGHT_IDS.contains(id);
    }

    public static final List<MealRecommendation> LIGHT_CATALOG = lightOnly(CATALOG);

    private static List<MealRecommendation> lightOnly(List<MealRecommendation> catalog) {
        List<MealRecommendation> light = new ArrayList<>();
        for (MealRecommendation recommendation : catalog) {
            if (recommendation.isLight()) {
                light.add(recommendation);
            }
        }
        return Collections.unmodifiableList(light);
    }
}
