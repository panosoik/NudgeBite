package com.panagiotisoikonomou.nudgebite.food;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.enums.FoodCategory;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** Every item bridges to the FoodCategory it scores as; the bridge need not follow the tree. */
public enum FoodItem {

    SPINACH("Spinach", FoodSubgroup.LEAFY_VEGETABLES, FoodCategory.LEAFY_GREEN),
    LETTUCE("Lettuce", FoodSubgroup.LEAFY_VEGETABLES, FoodCategory.LEAFY_GREEN),
    ROCKET("Rocket", FoodSubgroup.LEAFY_VEGETABLES, FoodCategory.LEAFY_GREEN),
    SWISS_CHARD("Swiss chard", FoodSubgroup.LEAFY_VEGETABLES, FoodCategory.LEAFY_GREEN),
    WATERCRESS("Watercress", FoodSubgroup.LEAFY_VEGETABLES, FoodCategory.LEAFY_GREEN),

    TOMATO("Tomato", FoodSubgroup.FRUITING_VEGETABLES, FoodCategory.OTHER_VEGETABLE),
    BELL_PEPPER("Bell pepper", FoodSubgroup.FRUITING_VEGETABLES, FoodCategory.OTHER_VEGETABLE),
    CHILLI_PEPPER("Chilli pepper", FoodSubgroup.FRUITING_VEGETABLES, FoodCategory.OTHER_VEGETABLE),
    ZUCCHINI("Zucchini", FoodSubgroup.FRUITING_VEGETABLES, FoodCategory.OTHER_VEGETABLE),
    AUBERGINE("Aubergine", FoodSubgroup.FRUITING_VEGETABLES, FoodCategory.OTHER_VEGETABLE),
    CUCUMBER("Cucumber", FoodSubgroup.FRUITING_VEGETABLES, FoodCategory.OTHER_VEGETABLE),
    OKRA("Okra", FoodSubgroup.FRUITING_VEGETABLES, FoodCategory.OTHER_VEGETABLE),
    PUMPKIN("Pumpkin", FoodSubgroup.FRUITING_VEGETABLES, FoodCategory.OTHER_VEGETABLE),

    CARROT("Carrot", FoodSubgroup.ROOT_VEGETABLES, FoodCategory.ROOT_VEGETABLE),
    BEETROOT("Beetroot", FoodSubgroup.ROOT_VEGETABLES, FoodCategory.ROOT_VEGETABLE),
    PARSNIP("Parsnip", FoodSubgroup.ROOT_VEGETABLES, FoodCategory.ROOT_VEGETABLE),
    TURNIP("Turnip", FoodSubgroup.ROOT_VEGETABLES, FoodCategory.ROOT_VEGETABLE),
    RADISH("Radish", FoodSubgroup.ROOT_VEGETABLES, FoodCategory.ROOT_VEGETABLE),
    CELERIAC("Celeriac", FoodSubgroup.ROOT_VEGETABLES, FoodCategory.ROOT_VEGETABLE),

    BROCCOLI("Broccoli", FoodSubgroup.CRUCIFEROUS_VEGETABLES, FoodCategory.CRUCIFEROUS),
    CAULIFLOWER("Cauliflower", FoodSubgroup.CRUCIFEROUS_VEGETABLES, FoodCategory.CRUCIFEROUS),
    CABBAGE("Cabbage", FoodSubgroup.CRUCIFEROUS_VEGETABLES, FoodCategory.CRUCIFEROUS),
    BRUSSELS_SPROUTS("Brussels sprouts", FoodSubgroup.CRUCIFEROUS_VEGETABLES, FoodCategory.CRUCIFEROUS),
    KALE("Kale", FoodSubgroup.CRUCIFEROUS_VEGETABLES, FoodCategory.CRUCIFEROUS),
    BOK_CHOY("Bok choy", FoodSubgroup.CRUCIFEROUS_VEGETABLES, FoodCategory.CRUCIFEROUS),

    ONION("Onion", FoodSubgroup.ALLIUM_VEGETABLES, FoodCategory.OTHER_VEGETABLE),
    GARLIC("Garlic", FoodSubgroup.ALLIUM_VEGETABLES, FoodCategory.OTHER_VEGETABLE),
    LEEK("Leek", FoodSubgroup.ALLIUM_VEGETABLES, FoodCategory.OTHER_VEGETABLE),
    SPRING_ONION("Spring onion", FoodSubgroup.ALLIUM_VEGETABLES, FoodCategory.OTHER_VEGETABLE),

    // Potato keeps its own category so it stays out of the vegetable component; sweet potato scores as a root vegetable.
    POTATO("Potato", FoodSubgroup.STARCHY_VEGETABLES, FoodCategory.POTATO),
    SWEET_POTATO("Sweet potato", FoodSubgroup.STARCHY_VEGETABLES, FoodCategory.ROOT_VEGETABLE),
    CORN("Corn", FoodSubgroup.STARCHY_VEGETABLES, FoodCategory.OTHER_VEGETABLE),
    GREEN_PEAS("Green peas", FoodSubgroup.STARCHY_VEGETABLES, FoodCategory.OTHER_VEGETABLE),

    MUSHROOM("Mushroom", FoodSubgroup.OTHER_VEGETABLES, FoodCategory.OTHER_VEGETABLE),
    ASPARAGUS("Asparagus", FoodSubgroup.OTHER_VEGETABLES, FoodCategory.OTHER_VEGETABLE),
    CELERY("Celery", FoodSubgroup.OTHER_VEGETABLES, FoodCategory.OTHER_VEGETABLE),
    ARTICHOKE("Artichoke", FoodSubgroup.OTHER_VEGETABLES, FoodCategory.OTHER_VEGETABLE),
    GREEN_BEANS("Green beans", FoodSubgroup.OTHER_VEGETABLES, FoodCategory.OTHER_VEGETABLE),
    MIXED_VEGETABLES("Mixed vegetables", FoodSubgroup.OTHER_VEGETABLES, FoodCategory.OTHER_VEGETABLE),

    STRAWBERRY("Strawberry", FoodSubgroup.BERRIES, FoodCategory.FRUIT),
    BLUEBERRY("Blueberry", FoodSubgroup.BERRIES, FoodCategory.FRUIT),
    RASPBERRY("Raspberry", FoodSubgroup.BERRIES, FoodCategory.FRUIT),
    BLACKBERRY("Blackberry", FoodSubgroup.BERRIES, FoodCategory.FRUIT),
    GRAPES("Grapes", FoodSubgroup.BERRIES, FoodCategory.FRUIT),

    ORANGE("Orange", FoodSubgroup.CITRUS_FRUIT, FoodCategory.FRUIT),
    MANDARIN("Mandarin", FoodSubgroup.CITRUS_FRUIT, FoodCategory.FRUIT),
    LEMON("Lemon", FoodSubgroup.CITRUS_FRUIT, FoodCategory.FRUIT),
    GRAPEFRUIT("Grapefruit", FoodSubgroup.CITRUS_FRUIT, FoodCategory.FRUIT),

    APPLE("Apple", FoodSubgroup.POME_STONE_FRUIT, FoodCategory.FRUIT),
    PEAR("Pear", FoodSubgroup.POME_STONE_FRUIT, FoodCategory.FRUIT),
    PEACH("Peach", FoodSubgroup.POME_STONE_FRUIT, FoodCategory.FRUIT),
    NECTARINE("Nectarine", FoodSubgroup.POME_STONE_FRUIT, FoodCategory.FRUIT),
    APRICOT("Apricot", FoodSubgroup.POME_STONE_FRUIT, FoodCategory.FRUIT),
    PLUM("Plum", FoodSubgroup.POME_STONE_FRUIT, FoodCategory.FRUIT),
    CHERRY("Cherry", FoodSubgroup.POME_STONE_FRUIT, FoodCategory.FRUIT),

    BANANA("Banana", FoodSubgroup.TROPICAL_FRUIT, FoodCategory.FRUIT),
    MANGO("Mango", FoodSubgroup.TROPICAL_FRUIT, FoodCategory.FRUIT),
    PINEAPPLE("Pineapple", FoodSubgroup.TROPICAL_FRUIT, FoodCategory.FRUIT),
    KIWI("Kiwi", FoodSubgroup.TROPICAL_FRUIT, FoodCategory.FRUIT),
    POMEGRANATE("Pomegranate", FoodSubgroup.TROPICAL_FRUIT, FoodCategory.FRUIT),
    AVOCADO("Avocado", FoodSubgroup.TROPICAL_FRUIT, FoodCategory.FRUIT),
    FIG("Fig", FoodSubgroup.TROPICAL_FRUIT, FoodCategory.FRUIT),

    WATERMELON("Watermelon", FoodSubgroup.MELONS, FoodCategory.FRUIT),
    MELON("Melon", FoodSubgroup.MELONS, FoodCategory.FRUIT),

    RAISINS("Raisins", FoodSubgroup.DRIED_FRUIT, FoodCategory.FRUIT),
    DATES("Dates", FoodSubgroup.DRIED_FRUIT, FoodCategory.FRUIT),
    DRIED_APRICOTS("Dried apricots", FoodSubgroup.DRIED_FRUIT, FoodCategory.FRUIT),
    PRUNES("Prunes", FoodSubgroup.DRIED_FRUIT, FoodCategory.FRUIT),

    BROWN_RICE("Brown rice", FoodSubgroup.RICE, FoodCategory.WHOLE_GRAIN, FoodAttribute.WHOLE_GRAIN),
    WHITE_RICE("White rice", FoodSubgroup.RICE, FoodCategory.REFINED_GRAIN),

    WHOLE_GRAIN_BREAD("Whole-grain bread", FoodSubgroup.BREAD, FoodCategory.WHOLE_GRAIN, FoodAttribute.WHOLE_GRAIN),
    RYE_BREAD("Rye bread", FoodSubgroup.BREAD, FoodCategory.WHOLE_GRAIN, FoodAttribute.WHOLE_GRAIN),
    WHOLE_GRAIN_PITA("Whole-grain pita", FoodSubgroup.BREAD, FoodCategory.WHOLE_GRAIN, FoodAttribute.WHOLE_GRAIN),
    WHITE_BREAD("White bread", FoodSubgroup.BREAD, FoodCategory.REFINED_GRAIN),
    PITA_BREAD("Pita bread", FoodSubgroup.BREAD, FoodCategory.REFINED_GRAIN),
    TORTILLA_WRAP("Tortilla wrap", FoodSubgroup.BREAD, FoodCategory.REFINED_GRAIN),

    WHOLE_WHEAT_PASTA("Whole-wheat pasta", FoodSubgroup.PASTA, FoodCategory.WHOLE_GRAIN, FoodAttribute.WHOLE_GRAIN),
    PASTA("Pasta", FoodSubgroup.PASTA, FoodCategory.REFINED_GRAIN),
    COUSCOUS("Couscous", FoodSubgroup.PASTA, FoodCategory.REFINED_GRAIN),
    NOODLES("Noodles", FoodSubgroup.PASTA, FoodCategory.REFINED_GRAIN),

    OATS("Oats", FoodSubgroup.BREAKFAST_CEREAL, FoodCategory.WHOLE_GRAIN, FoodAttribute.WHOLE_GRAIN),
    MUESLI("Muesli", FoodSubgroup.BREAKFAST_CEREAL, FoodCategory.WHOLE_GRAIN, FoodAttribute.WHOLE_GRAIN),
    SWEETENED_CEREAL("Sweetened breakfast cereal", FoodSubgroup.BREAKFAST_CEREAL, FoodCategory.REFINED_GRAIN),
    CORNFLAKES("Cornflakes", FoodSubgroup.BREAKFAST_CEREAL, FoodCategory.REFINED_GRAIN),

    QUINOA("Quinoa", FoodSubgroup.OTHER_GRAINS, FoodCategory.WHOLE_GRAIN, FoodAttribute.WHOLE_GRAIN),
    BULGUR("Bulgur", FoodSubgroup.OTHER_GRAINS, FoodCategory.WHOLE_GRAIN, FoodAttribute.WHOLE_GRAIN),
    BARLEY("Barley", FoodSubgroup.OTHER_GRAINS, FoodCategory.WHOLE_GRAIN, FoodAttribute.WHOLE_GRAIN),
    BUCKWHEAT("Buckwheat", FoodSubgroup.OTHER_GRAINS, FoodCategory.WHOLE_GRAIN, FoodAttribute.WHOLE_GRAIN),
    POLENTA("Polenta", FoodSubgroup.OTHER_GRAINS, FoodCategory.REFINED_GRAIN),

    LENTILS("Lentils", FoodGroup.LEGUMES, FoodCategory.LEGUME),
    CHICKPEAS("Chickpeas", FoodGroup.LEGUMES, FoodCategory.LEGUME),
    BLACK_BEANS("Black beans", FoodGroup.LEGUMES, FoodCategory.LEGUME),
    KIDNEY_BEANS("Kidney beans", FoodGroup.LEGUMES, FoodCategory.LEGUME),
    WHITE_BEANS("White beans", FoodGroup.LEGUMES, FoodCategory.LEGUME),
    FAVA_BEANS("Fava beans", FoodGroup.LEGUMES, FoodCategory.LEGUME),
    SPLIT_PEAS("Split peas", FoodGroup.LEGUMES, FoodCategory.LEGUME),
    EDAMAME("Edamame", FoodGroup.LEGUMES, FoodCategory.LEGUME),
    TOFU("Tofu", FoodGroup.LEGUMES, FoodCategory.LEGUME),
    TEMPEH("Tempeh", FoodGroup.LEGUMES, FoodCategory.LEGUME),
    HUMMUS("Hummus", FoodGroup.LEGUMES, FoodCategory.LEGUME),

    ALMONDS("Almonds", FoodGroup.NUTS_SEEDS, FoodCategory.NUT_SEED),
    WALNUTS("Walnuts", FoodGroup.NUTS_SEEDS, FoodCategory.NUT_SEED),
    CASHEWS("Cashews", FoodGroup.NUTS_SEEDS, FoodCategory.NUT_SEED),
    PISTACHIOS("Pistachios", FoodGroup.NUTS_SEEDS, FoodCategory.NUT_SEED),
    HAZELNUTS("Hazelnuts", FoodGroup.NUTS_SEEDS, FoodCategory.NUT_SEED),
    PEANUTS("Peanuts", FoodGroup.NUTS_SEEDS, FoodCategory.NUT_SEED),
    SUNFLOWER_SEEDS("Sunflower seeds", FoodGroup.NUTS_SEEDS, FoodCategory.NUT_SEED),
    PUMPKIN_SEEDS("Pumpkin seeds", FoodGroup.NUTS_SEEDS, FoodCategory.NUT_SEED),
    CHIA_SEEDS("Chia seeds", FoodGroup.NUTS_SEEDS, FoodCategory.NUT_SEED),
    FLAXSEED("Flaxseed", FoodGroup.NUTS_SEEDS, FoodCategory.NUT_SEED),
    PEANUT_BUTTER("Peanut butter", FoodGroup.NUTS_SEEDS, FoodCategory.NUT_SEED),
    ALMOND_BUTTER("Almond butter", FoodGroup.NUTS_SEEDS, FoodCategory.NUT_SEED),
    TAHINI("Tahini", FoodGroup.NUTS_SEEDS, FoodCategory.NUT_SEED),

    SALMON("Salmon", FoodSubgroup.FISH, FoodCategory.FISH),
    SARDINES("Sardines", FoodSubgroup.FISH, FoodCategory.FISH),
    COD("Cod", FoodSubgroup.FISH, FoodCategory.FISH),
    TUNA("Tuna", FoodSubgroup.FISH, FoodCategory.FISH),
    MACKEREL("Mackerel", FoodSubgroup.FISH, FoodCategory.FISH),
    ANCHOVIES("Anchovies", FoodSubgroup.FISH, FoodCategory.FISH),
    SEA_BASS("Sea bass", FoodSubgroup.FISH, FoodCategory.FISH),
    TROUT("Trout", FoodSubgroup.FISH, FoodCategory.FISH),

    OCTOPUS("Octopus", FoodSubgroup.CEPHALOPODS, FoodCategory.SEAFOOD),
    SQUID("Squid", FoodSubgroup.CEPHALOPODS, FoodCategory.SEAFOOD),
    CUTTLEFISH("Cuttlefish", FoodSubgroup.CEPHALOPODS, FoodCategory.SEAFOOD),

    SHRIMP("Shrimp", FoodSubgroup.CRUSTACEANS, FoodCategory.SEAFOOD),
    CRAB("Crab", FoodSubgroup.CRUSTACEANS, FoodCategory.SEAFOOD),
    LOBSTER("Lobster", FoodSubgroup.CRUSTACEANS, FoodCategory.SEAFOOD),

    MUSSELS("Mussels", FoodSubgroup.MOLLUSCS, FoodCategory.SEAFOOD),
    CLAMS("Clams", FoodSubgroup.MOLLUSCS, FoodCategory.SEAFOOD),
    OYSTERS("Oysters", FoodSubgroup.MOLLUSCS, FoodCategory.SEAFOOD),
    SCALLOPS("Scallops", FoodSubgroup.MOLLUSCS, FoodCategory.SEAFOOD),

    BEEF("Beef", FoodSubgroup.RED_MEAT, FoodCategory.RED_MEAT),
    BEEF_MINCE("Beef mince", FoodSubgroup.RED_MEAT, FoodCategory.RED_MEAT),
    STEAK("Steak", FoodSubgroup.RED_MEAT, FoodCategory.RED_MEAT),
    PORK("Pork", FoodSubgroup.RED_MEAT, FoodCategory.RED_MEAT),
    LAMB("Lamb", FoodSubgroup.RED_MEAT, FoodCategory.RED_MEAT),
    GOAT("Goat", FoodSubgroup.RED_MEAT, FoodCategory.RED_MEAT),
    VEAL("Veal", FoodSubgroup.RED_MEAT, FoodCategory.RED_MEAT),

    BACON("Bacon", FoodSubgroup.PROCESSED_MEAT, FoodCategory.PROCESSED_MEAT),
    SAUSAGE("Sausage", FoodSubgroup.PROCESSED_MEAT, FoodCategory.PROCESSED_MEAT),
    HAM("Ham", FoodSubgroup.PROCESSED_MEAT, FoodCategory.PROCESSED_MEAT),
    SALAMI("Salami", FoodSubgroup.PROCESSED_MEAT, FoodCategory.PROCESSED_MEAT),
    HOT_DOG("Hot dog", FoodSubgroup.PROCESSED_MEAT, FoodCategory.PROCESSED_MEAT),
    PROSCIUTTO("Prosciutto", FoodSubgroup.PROCESSED_MEAT, FoodCategory.PROCESSED_MEAT),
    PEPPERONI("Pepperoni", FoodSubgroup.PROCESSED_MEAT, FoodCategory.PROCESSED_MEAT),

    CHICKEN("Chicken", FoodGroup.POULTRY, FoodCategory.POULTRY),
    CHICKEN_BREAST("Chicken breast", FoodGroup.POULTRY, FoodCategory.POULTRY),
    TURKEY("Turkey", FoodGroup.POULTRY, FoodCategory.POULTRY),
    TURKEY_BREAST("Turkey breast", FoodGroup.POULTRY, FoodCategory.POULTRY),
    DUCK("Duck", FoodGroup.POULTRY, FoodCategory.POULTRY),

    EGG("Egg", FoodGroup.EGGS, FoodCategory.EGG),
    EGG_WHITES("Egg whites", FoodGroup.EGGS, FoodCategory.EGG),

    WHOLE_MILK("Whole milk", FoodSubgroup.MILK, FoodCategory.DAIRY),
    SEMI_SKIMMED_MILK("Semi-skimmed milk", FoodSubgroup.MILK, FoodCategory.DAIRY),
    SKIMMED_MILK("Skimmed milk", FoodSubgroup.MILK, FoodCategory.DAIRY),

    GREEK_YOGURT("Greek yogurt", FoodSubgroup.YOGURT, FoodCategory.DAIRY),
    PLAIN_YOGURT("Plain yogurt", FoodSubgroup.YOGURT, FoodCategory.DAIRY),
    FLAVOURED_YOGURT("Flavoured yogurt", FoodSubgroup.YOGURT, FoodCategory.DAIRY),

    FETA("Feta", FoodSubgroup.CHEESE, FoodCategory.CHEESE),
    MOZZARELLA("Mozzarella", FoodSubgroup.CHEESE, FoodCategory.CHEESE),
    CHEDDAR("Cheddar", FoodSubgroup.CHEESE, FoodCategory.CHEESE),
    PARMESAN("Parmesan", FoodSubgroup.CHEESE, FoodCategory.CHEESE),
    COTTAGE_CHEESE("Cottage cheese", FoodSubgroup.CHEESE, FoodCategory.CHEESE),
    CREAM_CHEESE("Cream cheese", FoodSubgroup.CHEESE, FoodCategory.CHEESE),
    HALLOUMI("Halloumi", FoodSubgroup.CHEESE, FoodCategory.CHEESE),

    OLIVE_OIL("Olive oil", FoodGroup.FATS_OILS, FoodCategory.FAT_OIL),
    SUNFLOWER_OIL("Sunflower oil", FoodGroup.FATS_OILS, FoodCategory.FAT_OIL),
    COCONUT_OIL("Coconut oil", FoodGroup.FATS_OILS, FoodCategory.FAT_OIL),
    BUTTER("Butter", FoodGroup.FATS_OILS, FoodCategory.FAT_OIL),
    MARGARINE("Margarine", FoodGroup.FATS_OILS, FoodCategory.FAT_OIL),
    MAYONNAISE("Mayonnaise", FoodGroup.FATS_OILS, FoodCategory.FAT_OIL),
    CREAM("Cream", FoodGroup.FATS_OILS, FoodCategory.FAT_OIL),

    WATER("Water", FoodSubgroup.UNSWEETENED_DRINKS, FoodCategory.OTHER),
    SPARKLING_WATER("Sparkling water", FoodSubgroup.UNSWEETENED_DRINKS, FoodCategory.OTHER),
    COFFEE("Coffee", FoodSubgroup.UNSWEETENED_DRINKS, FoodCategory.OTHER),
    TEA("Tea", FoodSubgroup.UNSWEETENED_DRINKS, FoodCategory.OTHER),
    HERBAL_TEA("Herbal tea", FoodSubgroup.UNSWEETENED_DRINKS, FoodCategory.OTHER),

    COLA("Cola", FoodSubgroup.SUGAR_SWEETENED_DRINKS, FoodCategory.SUGARY_DRINK, FoodAttribute.SUGAR_SWEETENED),
    SOFT_DRINK("Soft drink", FoodSubgroup.SUGAR_SWEETENED_DRINKS, FoodCategory.SUGARY_DRINK, FoodAttribute.SUGAR_SWEETENED),
    SWEETENED_ICED_TEA("Sweetened iced tea", FoodSubgroup.SUGAR_SWEETENED_DRINKS, FoodCategory.SUGARY_DRINK, FoodAttribute.SUGAR_SWEETENED),
    ENERGY_DRINK("Energy drink", FoodSubgroup.SUGAR_SWEETENED_DRINKS, FoodCategory.SUGARY_DRINK, FoodAttribute.SUGAR_SWEETENED),
    SPORTS_DRINK("Sports drink", FoodSubgroup.SUGAR_SWEETENED_DRINKS, FoodCategory.SUGARY_DRINK, FoodAttribute.SUGAR_SWEETENED),
    JUICE_DRINK("Sweetened juice drink", FoodSubgroup.SUGAR_SWEETENED_DRINKS, FoodCategory.SUGARY_DRINK, FoodAttribute.SUGAR_SWEETENED),

    // Diet drinks bridge to OTHER so they never feed the sugary-drink component.
    DIET_COLA("Diet cola", FoodSubgroup.DIET_DRINKS, FoodCategory.OTHER),
    ZERO_SUGAR_SOFT_DRINK("Zero-sugar soft drink", FoodSubgroup.DIET_DRINKS, FoodCategory.OTHER),

    // 100% juice is neither sugary drink nor fruit for the scorer, so it bridges to OTHER.
    ORANGE_JUICE_100("100% orange juice", FoodSubgroup.JUICE, FoodCategory.OTHER, FoodAttribute.FRUIT_JUICE_100),
    APPLE_JUICE_100("100% apple juice", FoodSubgroup.JUICE, FoodCategory.OTHER, FoodAttribute.FRUIT_JUICE_100),

    CHOCOLATE("Chocolate", FoodGroup.SWEETS_BAKED, FoodCategory.SWEET_SNACK),
    COOKIES("Cookies", FoodGroup.SWEETS_BAKED, FoodCategory.SWEET_SNACK),
    CAKE("Cake", FoodGroup.SWEETS_BAKED, FoodCategory.SWEET_SNACK),
    DOUGHNUT("Doughnut", FoodGroup.SWEETS_BAKED, FoodCategory.SWEET_SNACK),
    CROISSANT("Croissant", FoodGroup.SWEETS_BAKED, FoodCategory.SWEET_SNACK),
    ICE_CREAM("Ice cream", FoodGroup.SWEETS_BAKED, FoodCategory.SWEET_SNACK),
    CANDY("Candy", FoodGroup.SWEETS_BAKED, FoodCategory.SWEET_SNACK),
    CHOCOLATE_SPREAD("Chocolate spread", FoodGroup.SWEETS_BAKED, FoodCategory.SWEET_SNACK),
    HONEY("Honey", FoodGroup.SWEETS_BAKED, FoodCategory.SWEET_SNACK),
    JAM("Jam", FoodGroup.SWEETS_BAKED, FoodCategory.SWEET_SNACK),

    CRISPS("Crisps", FoodGroup.SALTY_SNACKS, FoodCategory.OTHER),
    TORTILLA_CHIPS("Tortilla chips", FoodGroup.SALTY_SNACKS, FoodCategory.OTHER),
    SALTED_CRACKERS("Salted crackers", FoodGroup.SALTY_SNACKS, FoodCategory.REFINED_GRAIN),
    PRETZELS("Pretzels", FoodGroup.SALTY_SNACKS, FoodCategory.REFINED_GRAIN),
    INSTANT_NOODLES("Instant noodles", FoodGroup.SALTY_SNACKS, FoodCategory.REFINED_GRAIN),
    CANNED_SOUP("Canned soup", FoodGroup.SALTY_SNACKS, FoodCategory.OTHER),
    FROZEN_PIZZA("Frozen pizza", FoodGroup.SALTY_SNACKS, FoodCategory.OTHER),
    POPCORN("Popcorn", FoodGroup.SALTY_SNACKS, FoodCategory.OTHER);

    private final String label;
    private final FoodGroup group;
    private final FoodSubgroup subgroup;
    private final FoodCategory scoringCategory;
    private final Set<FoodAttribute> attributes;

    FoodItem(String label, FoodSubgroup subgroup, FoodCategory scoringCategory,
             FoodAttribute... attributes) {
        this(label, subgroup.group(), subgroup, scoringCategory, attributes);
    }

    FoodItem(String label, FoodGroup group, FoodCategory scoringCategory,
             FoodAttribute... attributes) {
        this(label, group, (FoodSubgroup) null, scoringCategory, attributes);
    }

    FoodItem(String label, FoodGroup group, FoodSubgroup subgroup, FoodCategory scoringCategory,
             FoodAttribute... attributes) {
        this.label = label;
        this.group = group;
        this.subgroup = subgroup;
        this.scoringCategory = scoringCategory;
        this.attributes = attributes.length == 0
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(attributes)));
    }

    /** Null for a blank name, the literal "null", or a name this build no longer has. */
    @Nullable
    public static FoodItem parseOrNull(@Nullable String storedName) {
        if (storedName == null || storedName.trim().isEmpty() || "null".equalsIgnoreCase(storedName.trim())) {
            return null;
        }
        try {
            return valueOf(storedName.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String label() {
        return label;
    }

    public FoodGroup group() {
        return group;
    }

    public FoodSubgroup subgroup() {
        return subgroup;
    }

    public FoodCategory scoringCategory() {
        return scoringCategory;
    }

    public boolean has(FoodAttribute attribute) {
        return attributes.contains(attribute);
    }

}
