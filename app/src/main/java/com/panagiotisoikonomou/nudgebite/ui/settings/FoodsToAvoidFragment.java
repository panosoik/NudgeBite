package com.panagiotisoikonomou.nudgebite.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.data.entity.User;
import com.panagiotisoikonomou.nudgebite.data.repository.UserRepository;
import com.panagiotisoikonomou.nudgebite.ui.components.MultiSelectChips;

import java.util.ArrayList;
import java.util.List;

public class FoodsToAvoidFragment extends Fragment {

    private static final List<String> COMMON_ALLERGENS = List.of(
            "Peanuts",
            "Tree nuts",
            "Fish",
            "Shellfish",
            "Dairy",
            "Eggs",
            "Gluten",
            "Sesame"
    );

    private UserRepository userRepository;
    @Nullable private User currentUser;
    private MultiSelectChips allergiesSelect;
    private boolean loaded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_allergies, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userRepository = new UserRepository(requireActivity().getApplication());

        allergiesSelect = view.findViewById(R.id.allergies_select);
        EditText addInput = view.findViewById(R.id.add_allergy_input);
        allergiesSelect.bindAddOnEnter(addInput);
        allergiesSelect.bindAddButton(addInput, view.findViewById(R.id.add_allergy_button));

        view.findViewById(R.id.back_button).setOnClickListener(
                v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        userRepository.getUser().observe(getViewLifecycleOwner(), user -> {
            currentUser = user;
            if (user == null || loaded) {
                return;
            }
            loaded = true;
            List<String> stored = user.foodsToAvoid == null ? new ArrayList<>() : user.foodsToAvoid;
            List<String> options = new ArrayList<>(COMMON_ALLERGENS);
            for (String food : stored) {
                if (!options.contains(food)) {
                    options.add(food);
                }
            }
            allergiesSelect.setOptions(options, stored);
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (currentUser != null && loaded) {
            currentUser.foodsToAvoid = allergiesSelect.getSelectedList();
            userRepository.updateUser(currentUser);
        }
    }
}
