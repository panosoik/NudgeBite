package com.panagiotisoikonomou.nudgebite.data.entity;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.panagiotisoikonomou.nudgebite.data.enums.ActivityLevel;
import com.panagiotisoikonomou.nudgebite.data.enums.Sex;

import java.util.List;

@Entity(tableName = "user")
public class User {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @Nullable
    @ColumnInfo(name = "sex")
    public Sex sex;

    @ColumnInfo(name = "age")
    public int age;

    @ColumnInfo(name = "height")
    public Integer height;

    @ColumnInfo(name = "weight")
    public Integer weight;

    @Nullable
    @ColumnInfo(name = "activity_level")
    public ActivityLevel activityLevel;

    @ColumnInfo(name = "home_address")
    public String homeAddress;

    @ColumnInfo(name = "home_latitude")
    public Double homeLatitude;

    @ColumnInfo(name = "home_longitude")
    public Double homeLongitude;

    @ColumnInfo(name = "work_address")
    public String workAddress;

    @ColumnInfo(name = "work_latitude")
    public Double workLatitude;

    @ColumnInfo(name = "work_longitude")
    public Double workLongitude;

    @ColumnInfo(name = "usual_supermarket")
    public String usualSupermarket;

    @ColumnInfo(name = "supermarket_latitude")
    public Double supermarketLatitude;

    @ColumnInfo(name = "supermarket_longitude")
    public Double supermarketLongitude;

    @ColumnInfo(name = "usual_eating_place")
    public String usualEatingPlace;

    @ColumnInfo(name = "eating_place_latitude")
    public Double eatingPlaceLatitude;

    @ColumnInfo(name = "eating_place_longitude")
    public Double eatingPlaceLongitude;

    @ColumnInfo(name = "breakfast_time_of_day")
    public String breakfastTimeOfDay;

    @ColumnInfo(name = "lunch_time_of_day")
    public String lunchTimeOfDay;

    @ColumnInfo(name = "dinner_time_of_day")
    public String dinnerTimeOfDay;

    @ColumnInfo(name = "eats_breakfast")
    public boolean eatsBreakfast;

    @ColumnInfo(name = "eats_lunch")
    public boolean eatsLunch;

    @ColumnInfo(name = "eats_dinner")
    public boolean eatsDinner;

    @ColumnInfo(name = "eats_snacks")
    public boolean eatsSnacks;

    @ColumnInfo(name = "preferred_foods")
    public List<String> preferredFoods;

    @ColumnInfo(name = "foods_to_avoid")
    public List<String> foodsToAvoid;

    @ColumnInfo(name = "veg_servings_goal_per_day")
    public Integer vegServingsGoalPerDay;

    @ColumnInfo(name = "delivery_acceptable_per_week")
    public Integer deliveryAcceptablePerWeek;

    @ColumnInfo(name = "water_glasses_goal_per_day")
    public Integer waterGlassesGoalPerDay;
}
