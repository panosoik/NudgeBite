package com.panagiotisoikonomou.nudgebite.ui.onboarding;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.ui.components.AfterTextChanged;
import com.panagiotisoikonomou.nudgebite.location.AddressLookup;
import com.panagiotisoikonomou.nudgebite.utils.DisplayUtils;

import java.util.List;

public class AddressPickerBottomSheet extends BottomSheetDialogFragment {

    public static final String RESULT_KEY = "address_picker_result";
    public static final String RESULT_PLACE = "place";
    public static final String RESULT_LABEL = "label";
    public static final String RESULT_LATITUDE = "latitude";
    public static final String RESULT_LONGITUDE = "longitude";

    private static final String ARG_PLACE = "arg_place";
    private static final String ARG_INITIAL_QUERY = "arg_initial_query";

    private static final int MIN_QUERY_LENGTH = 3;
    private static final long DEBOUNCE_MILLIS = 400;

    private AddressLookup addressLookup;
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;
    private AutocompleteSessionToken sessionToken;
    private String pendingLabel;

    private EditText queryInput;
    private TextView statusText;
    private LinearLayout resultsContainer;

    public static void show(Fragment parent, SavedPlace place, @Nullable String currentAddress) {
        AddressPickerBottomSheet sheet = new AddressPickerBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_PLACE, place.name());
        args.putString(ARG_INITIAL_QUERY, currentAddress);
        sheet.setArguments(args);
        sheet.show(parent.getChildFragmentManager(), "address_picker_" + place.name());
    }

    /** Applies a result this sheet sent to the user it describes. */
    public static void saveResultTo(User user, Bundle result) {
        SavedPlace.valueOf(result.getString(RESULT_PLACE)).saveTo(user, result.getString(RESULT_LABEL),
                result.getDouble(RESULT_LATITUDE), result.getDouble(RESULT_LONGITUDE));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottomsheet_address_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        if (AddressLookup.isConfigured()) {
            addressLookup = new AddressLookup(requireContext());
            sessionToken = addressLookup.newSessionToken();
        }

        TextView title = view.findViewById(R.id.picker_title);
        queryInput = view.findViewById(R.id.address_query_input);
        statusText = view.findViewById(R.id.address_status_text);
        resultsContainer = view.findViewById(R.id.address_results_container);

        title.setText(SavedPlace.valueOf(args.getString(ARG_PLACE)).pickerTitle);
        String initialQuery = args.getString(ARG_INITIAL_QUERY);
        if (initialQuery != null && !initialQuery.isEmpty()) {
            queryInput.setText(initialQuery);
            queryInput.setSelection(initialQuery.length());
        }

        queryInput.addTextChangedListener(new AfterTextChanged(text -> scheduleDebouncedSearch()));

        getChildFragmentManager().setFragmentResultListener(
                MapPinPickerFragment.RESULT_KEY, getViewLifecycleOwner(), (requestKey, result) ->
                        returnPick(result.getDouble(MapPinPickerFragment.RESULT_LATITUDE),
                                result.getDouble(MapPinPickerFragment.RESULT_LONGITUDE)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pendingSearch != null) {
            debounceHandler.removeCallbacks(pendingSearch);
        }
    }

    private void scheduleDebouncedSearch() {
        if (pendingSearch != null) {
            debounceHandler.removeCallbacks(pendingSearch);
        }
        String query = currentQuery();
        if (query.length() < MIN_QUERY_LENGTH) {
            resultsContainer.removeAllViews();
            hideStatus();
            return;
        }
        pendingSearch = () -> runSearch(query);
        debounceHandler.postDelayed(pendingSearch, DEBOUNCE_MILLIS);
    }

    private String currentQuery() {
        return queryInput.getText().toString().trim();
    }

    private void runSearch(String query) {
        resultsContainer.removeAllViews();
        if (addressLookup == null) {
            showStatus("Address search isn't available in this build.");
            return;
        }
        showStatus("Searching...");
        addressLookup.findPredictions(query, sessionToken, new AddressLookup.PredictionsCallback() {
            @Override
            public void onPredictions(List<AutocompletePrediction> predictions) {
                if (!isAdded() || isStale(query)) {
                    return;
                }
                if (predictions.isEmpty()) {
                    showStatus("No matches. Try a broader search.");
                    return;
                }
                hideStatus();
                for (AutocompletePrediction prediction : predictions) {
                    resultsContainer.addView(buildSuggestionRow(prediction));
                }
            }

            @Override
            public void onError() {
                if (!isAdded() || isStale(query)) {
                    return;
                }
                showStatus("Couldn't reach address search — check your connection and try again.");
            }
        });
    }

    private boolean isStale(String searchedQuery) {
        return !searchedQuery.equals(currentQuery());
    }

    private View buildSuggestionRow(AutocompletePrediction prediction) {
        TextView row = new TextView(requireContext());
        row.setText(prediction.getFullText(null));
        row.setTextColor(requireContext().getColor(R.color.nb_text_primary));
        row.setTextSize(14);
        row.setBackgroundResource(R.drawable.bg_chip_row_unselected);
        int horizontalPad = DisplayUtils.dp(requireContext(), 16);
        int verticalPad = DisplayUtils.dp(requireContext(), 14);
        row.setPadding(horizontalPad, verticalPad, horizontalPad, verticalPad);
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(v -> resolvePrediction(prediction));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = DisplayUtils.dp(requireContext(), 8);
        row.setLayoutParams(params);
        return row;
    }

    private void resolvePrediction(AutocompletePrediction prediction) {
        showStatus("Loading...");
        String label = prediction.getFullText(null).toString();
        addressLookup.fetchPlace(prediction.getPlaceId(), sessionToken, new AddressLookup.PlaceCallback() {
            @Override
            public void onPlace(double latitude, double longitude) {
                if (!isAdded()) {
                    return;
                }
                sessionToken = addressLookup.newSessionToken();
                pendingLabel = label;
                openMapPinPicker(latitude, longitude);
            }

            @Override
            public void onError() {
                if (!isAdded()) {
                    return;
                }
                sessionToken = addressLookup.newSessionToken();
                showStatus("Couldn't load that place. Try another result.");
            }
        });
    }

    private void openMapPinPicker(double initialLatitude, double initialLongitude) {
        MapPinPickerFragment.newInstance(initialLatitude, initialLongitude)
                .show(getChildFragmentManager(), "map_pin_picker");
    }

    private void returnPick(double latitude, double longitude) {
        Bundle result = new Bundle();
        result.putString(RESULT_PLACE, requireArguments().getString(ARG_PLACE));
        result.putString(RESULT_LABEL, pendingLabel);
        result.putDouble(RESULT_LATITUDE, latitude);
        result.putDouble(RESULT_LONGITUDE, longitude);
        getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
        dismiss();
    }

    private void showStatus(String message) {
        statusText.setText(message);
        statusText.setVisibility(View.VISIBLE);
    }

    private void hideStatus() {
        statusText.setVisibility(View.GONE);
    }
}
