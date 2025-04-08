package com.simon.homeirrigationclient.ui.machine.dashboard;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.simon.homeirrigationclient.R;
import com.simon.homeirrigationclient.databinding.FragmentDashboardBinding;
import com.simon.homeirrigationclient.model.DeviceInfo;
import com.simon.homeirrigationclient.ui.machine.MachineActivity;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //Observe device name
        dashboardViewModel.getDeviceName().observe(getViewLifecycleOwner(), deviceName -> {
            TextView textViewDeviceName = root.findViewById(R.id.textView_dashboard_name_detail);
            textViewDeviceName.setText(deviceName);
        });
        //Observe address and port
        dashboardViewModel.getAddressPort().observe(getViewLifecycleOwner(), addressPort -> {
            TextView textViewDeviceAddressPort = root.findViewById(R.id.textView_dashboard_address_detail);
            textViewDeviceAddressPort.setText(addressPort);
        });
        //Observe mode info
        dashboardViewModel.getMode().observe(getViewLifecycleOwner(), mode -> {
            TextView textViewDeviceAddressPort = root.findViewById(R.id.textView_dashboard_mode_detail);

            textViewDeviceAddressPort.setText(mode);
        });
        //Observe scheduled freq hint
        dashboardViewModel.getScheduledFreqHint().observe(getViewLifecycleOwner(), scheduledFreqHint -> {
            TextView textViewScheduledFreq = root.findViewById(R.id.textView_dashboard_scheduled_freq);
            textViewScheduledFreq.setText(scheduledFreqHint);
        });
        //Observe scheduled time hint
        dashboardViewModel.getScheduledTimeHint().observe(getViewLifecycleOwner(), scheduledTimeHint -> {
            TextView textViewScheduledTime = root.findViewById(R.id.textView_dashboard_scheduled_time);
            textViewScheduledTime.setText(scheduledTimeHint);
        });

        //Load the data into the viewmodel
        dashboardViewModel.loadData();

        //Set the edit (basic info) button to show the edit basic info dialog
        Button editBasicInfoButton = root.findViewById(R.id.button_dashboard_basic_info_edit);
        editBasicInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog editBasicInfoDialog = new Dialog(requireContext());
                editBasicInfoDialog.setContentView(R.layout.dialog_edit_basic_info);
                editBasicInfoDialog.show();
            }
        });

        //Set the edit mode button
        Button editModeButton = root.findViewById(R.id.button_dashboard_mode_edit);
        editModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog editModeDialog = new Dialog(requireContext());
                editModeDialog.setContentView(R.layout.dialog_edit_mode);
                editModeDialog.show();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}