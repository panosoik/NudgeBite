package com.panagiotisoikonomou.nudgebite.ui.onboarding.steps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.AddressPickerBottomSheet;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.OnboardingActivity;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.OnboardingStep;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.SavedPlace;

import java.util.EnumMap;
import java.util.Map;

public class LocationFragment extends Fragment implements OnboardingStep {

    private static final String EMPTY_HINT = "Tap to search";

    private final Map<SavedPlace, TextView> valueViewsByPlace = new EnumMap<>(SavedPlace.class);
    private User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        user = ((OnboardingActivity) requireActivity()).getAnswers().user;

        bindPlace(view, SavedPlace.HOME, R.id.home_address_field, R.id.home_address_value);
        bindPlace(view, SavedPlace.WORK, R.id.work_address_field, R.id.work_address_value);
        bindPlace(view, SavedPlace.SUPERMARKET, R.id.supermarket_field, R.id.supermarket_value);
        bindPlace(view, SavedPlace.EATING_PLACE, R.id.eating_place_field, R.id.eating_place_value);
        renderAll();

        getChildFragmentManager().setFragmentResultListener(
                AddressPickerBottomSheet.RESULT_KEY, getViewLifecycleOwner(), (requestKey, result) -> {
                    AddressPickerBottomSheet.saveResultTo(user, result);
                    renderAll();
                    ((OnboardingActivity) requireActivity()).refreshNextButtonState();
                });
    }

    private void bindPlace(View root, SavedPlace place, int fieldId, int valueId) {
        valueViewsByPlace.put(place, root.findViewById(valueId));
        root.findViewById(fieldId).setOnClickListener(
                v -> AddressPickerBottomSheet.show(this, place, place.addressOf(user)));
    }

    private void renderAll() {
        for (Map.Entry<SavedPlace, TextView> entry : valueViewsByPlace.entrySet()) {
            boolean isSet = entry.getKey().isSetOn(user);
            TextView valueView = entry.getValue();
            valueView.setText(isSet ? entry.getKey().addressOf(user) : EMPTY_HINT);
            valueView.setTextColor(requireContext().getColor(
                    isSet ? R.color.nb_text_primary : R.color.nb_text_secondary));
        }
    }

    @Override
    public String primaryButtonLabel() {
        return "Next";
    }

    @Override
    public boolean isStepValid() {
        return user != null && SavedPlace.HOME.isSetOn(user) && SavedPlace.WORK.isSetOn(user);
    }
}
