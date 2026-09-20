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

import java.util.List;

public class FoodPreferencesFragment extends Fragment {

    private UserRepository userRepository;
    @Nullable private User currentUser;
    private MultiSelectChips preferencesSelect;
    private boolean loaded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preferences, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userRepository = new UserRepository(requireActivity().getApplication());

        preferencesSelect = view.findViewById(R.id.preferences_select);
        EditText addInput = view.findViewById(R.id.add_preference_input);
        preferencesSelect.bindAddOnEnter(addInput);
        preferencesSelect.bindAddButton(addInput, view.findViewById(R.id.add_preference_button));

        view.findViewById(R.id.back_button).setOnClickListener(
                v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        userRepository.getUser().observe(getViewLifecycleOwner(), user -> {
            currentUser = user;
            if (user == null || loaded) {
                return;
            }
            loaded = true;
            List<String> preferred = PreferenceOptions.canonicalize(user.preferredFoods);
            preferencesSelect.setSections(PreferenceOptions.sections(preferred), preferred);
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (currentUser != null && loaded) {
            currentUser.preferredFoods = preferencesSelect.getSelectedList();
            userRepository.updateUser(currentUser);
        }
    }
}
