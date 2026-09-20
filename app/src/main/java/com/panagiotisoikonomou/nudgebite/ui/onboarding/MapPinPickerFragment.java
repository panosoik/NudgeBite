package com.panagiotisoikonomou.nudgebite.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.panagiotisoikonomou.nudgebite.R;

public class MapPinPickerFragment extends DialogFragment {

    public static final String RESULT_KEY = "map_pin_picker_result";
    public static final String RESULT_LATITUDE = "latitude";
    public static final String RESULT_LONGITUDE = "longitude";

    private static final String ARG_INITIAL_LAT = "arg_initial_lat";
    private static final String ARG_INITIAL_LON = "arg_initial_lon";

    private static final float PLACE_ZOOM = 17f;

    @Nullable
    private GoogleMap googleMap;

    public static MapPinPickerFragment newInstance(double initialLatitude, double initialLongitude) {
        MapPinPickerFragment fragment = new MapPinPickerFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_INITIAL_LAT, initialLatitude);
        args.putDouble(ARG_INITIAL_LON, initialLongitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_pin_picker, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this::onMapReady);
        }

        view.findViewById(R.id.map_pin_confirm_button).setOnClickListener(v -> confirm());
        view.findViewById(R.id.map_pin_cancel_button).setOnClickListener(v -> dismiss());
    }

    private void onMapReady(GoogleMap map) {
        googleMap = map;
        map.getUiSettings().setZoomControlsEnabled(true);

        Bundle args = requireArguments();
        LatLng target = new LatLng(args.getDouble(ARG_INITIAL_LAT), args.getDouble(ARG_INITIAL_LON));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(target, PLACE_ZOOM));
    }

    private void confirm() {
        if (googleMap == null) {
            return;
        }
        LatLng center = googleMap.getCameraPosition().target;
        Bundle result = new Bundle();
        result.putDouble(RESULT_LATITUDE, center.latitude);
        result.putDouble(RESULT_LONGITUDE, center.longitude);
        getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
        dismiss();
    }
}
