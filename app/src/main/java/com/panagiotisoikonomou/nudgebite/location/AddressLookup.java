package com.panagiotisoikonomou.nudgebite.location;

import android.content.Context;
import android.util.Log;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.panagiotisoikonomou.nudgebite.BuildConfig;

import java.util.Collections;
import java.util.List;

/** One session token must cover a whole search plus its details fetch, so Google bills it as one session. */
public class AddressLookup {

    private static final String TAG = "NudgeBiteAddress";
    private static final List<Place.Field> DETAIL_FIELDS = Collections.singletonList(Place.Field.LOCATION);

    public interface PredictionsCallback {
        void onPredictions(List<AutocompletePrediction> predictions);

        void onError();
    }

    public interface PlaceCallback {
        void onPlace(double latitude, double longitude);

        void onError();
    }

    private final PlacesClient placesClient;

    /** Places throws on an empty key; callers fall back to typing the address. */
    public static boolean isConfigured() {
        return BuildConfig.GOOGLE_MAPS_API_KEY != null && !BuildConfig.GOOGLE_MAPS_API_KEY.isEmpty();
    }

    public AddressLookup(Context context) {
        if (!isConfigured()) {
            throw new IllegalStateException("GOOGLE_MAPS_API_KEY missing — check isConfigured() first.");
        }
        Context appContext = context.getApplicationContext();
        if (!Places.isInitialized()) {
            Places.initialize(appContext, BuildConfig.GOOGLE_MAPS_API_KEY);
        }
        this.placesClient = Places.createClient(appContext);
    }

    public AutocompleteSessionToken newSessionToken() {
        return AutocompleteSessionToken.newInstance();
    }

    public void findPredictions(String query, AutocompleteSessionToken sessionToken,
                                PredictionsCallback callback) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setSessionToken(sessionToken)
                .build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> callback.onPredictions(response.getAutocompletePredictions()))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "findAutocompletePredictions failed for query=\"" + query + "\"", e);
                    callback.onError();
                });
    }

    public void fetchPlace(String placeId, AutocompleteSessionToken sessionToken, PlaceCallback callback) {
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, DETAIL_FIELDS)
                .setSessionToken(sessionToken)
                .build();

        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    Place place = response.getPlace();
                    if (place.getLocation() == null) {
                        callback.onError();
                        return;
                    }
                    callback.onPlace(place.getLocation().latitude, place.getLocation().longitude);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "fetchPlace failed for placeId=" + placeId, e);
                    callback.onError();
                });
    }
}
