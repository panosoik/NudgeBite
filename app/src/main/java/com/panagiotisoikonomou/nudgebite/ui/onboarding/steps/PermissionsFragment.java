package com.panagiotisoikonomou.nudgebite.ui.onboarding.steps;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.panagiotisoikonomou.nudgebite.R;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.OnboardingActivity;
import com.panagiotisoikonomou.nudgebite.ui.onboarding.OnboardingStep;

public class PermissionsFragment extends Fragment implements OnboardingStep {

    private ActivityResultLauncher<String[]> permissionLauncher;

    private TextView locationAction;
    private TextView backgroundLocationAction;
    private TextView usageAction;
    private TextView notificationsAction;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> refreshStatuses());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_permissions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationAction = view.findViewById(R.id.location_action);
        backgroundLocationAction = view.findViewById(R.id.background_location_action);
        usageAction = view.findViewById(R.id.usage_action);
        notificationsAction = view.findViewById(R.id.notifications_action);

        locationAction.setOnClickListener(v ->
                permissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}));
        backgroundLocationAction.setOnClickListener(v -> {
            // Android 11+ only grants background location after foreground location.
            if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                permissionLauncher.launch(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION});
            }
        });
        notificationsAction.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(new String[]{Manifest.permission.POST_NOTIFICATIONS});
            }
        });
        usageAction.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));

        refreshStatuses();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatuses();
    }

    private void refreshStatuses() {
        if (locationAction == null) {
            return;
        }
        setRowState(locationAction, locationGranted());
        setRowState(backgroundLocationAction, backgroundLocationGranted());
        setRowState(notificationsAction, notificationsGranted());
        setRowState(usageAction, hasUsageAccess());
        ((OnboardingActivity) requireActivity()).refreshNextButtonState();
    }

    private boolean locationGranted() {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean backgroundLocationGranted() {
        return hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
    }

    private boolean notificationsGranted() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || hasPermission(Manifest.permission.POST_NOTIFICATIONS);
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(requireContext(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasUsageAccess() {
        AppOpsManager appOps = (AppOpsManager) requireContext().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), requireContext().getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void setRowState(TextView action, boolean granted) {
        if (granted) {
            action.setText("Granted");
            action.setTextColor(ContextCompat.getColor(requireContext(), R.color.nb_text_secondary));
            action.setClickable(false);
        } else {
            action.setText("Allow");
            action.setTextColor(ContextCompat.getColor(requireContext(), R.color.nb_primary_green));
            action.setClickable(true);
        }
    }

    @Override
    public String primaryButtonLabel() {
        return "Finish";
    }

    @Override
    public boolean isStepValid() {
        return locationGranted() && backgroundLocationGranted() && notificationsGranted() && hasUsageAccess();
    }
}
