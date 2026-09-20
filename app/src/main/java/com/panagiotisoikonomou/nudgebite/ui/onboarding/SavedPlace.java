package com.panagiotisoikonomou.nudgebite.ui.onboarding;

import androidx.annotation.Nullable;

import com.panagiotisoikonomou.nudgebite.data.entity.User;

/** The four places a user can pick an address for, and where each is stored on the User. */
public enum SavedPlace {
    HOME("Home address"),
    WORK("Work / school address"),
    SUPERMARKET("Usual supermarket"),
    EATING_PLACE("Usual eating place");

    public final String pickerTitle;

    SavedPlace(String pickerTitle) {
        this.pickerTitle = pickerTitle;
    }

    @Nullable
    public String addressOf(User user) {
        switch (this) {
            case HOME: return user.homeAddress;
            case WORK: return user.workAddress;
            case SUPERMARKET: return user.usualSupermarket;
            default: return user.usualEatingPlace;
        }
    }

    public boolean isSetOn(User user) {
        String address = addressOf(user);
        return address != null && !address.isEmpty();
    }

    public void saveTo(User user, String address, double latitude, double longitude) {
        switch (this) {
            case HOME:
                user.homeAddress = address;
                user.homeLatitude = latitude;
                user.homeLongitude = longitude;
                return;
            case WORK:
                user.workAddress = address;
                user.workLatitude = latitude;
                user.workLongitude = longitude;
                return;
            case SUPERMARKET:
                user.usualSupermarket = address;
                user.supermarketLatitude = latitude;
                user.supermarketLongitude = longitude;
                return;
            default:
                user.usualEatingPlace = address;
                user.eatingPlaceLatitude = latitude;
                user.eatingPlaceLongitude = longitude;
        }
    }
}
