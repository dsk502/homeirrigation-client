package com.simon.homeirrigationclient.ui.machine.dashboard;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.simon.homeirrigationclient.HICApplication;
import com.simon.homeirrigationclient.R;
import com.simon.homeirrigationclient.databinding.FragmentDashboardBinding;
import com.simon.homeirrigationclient.model.DeviceInfo;

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
        //Observe host
        dashboardViewModel.getHost().observe(getViewLifecycleOwner(), host -> {
            TextView textViewDeviceHost = root.findViewById(R.id.textView_dashboard_host_detail);
            textViewDeviceHost.setText(host);
        });
        //Observe port
        dashboardViewModel.getPort().observe(getViewLifecycleOwner(), port -> {
            TextView textViewDevicePort = root.findViewById(R.id.textView_dashboard_port_detail);
            textViewDevicePort.setText(port);
        });
        //Observe mode info
        dashboardViewModel.getMode().observe(getViewLifecycleOwner(), mode -> {
            TextView textViewDeviceMode = root.findViewById(R.id.textView_dashboard_mode_detail);
            if(mode == 1) {
                textViewDeviceMode.setText("Scheduled");
            } else if(mode == 2) {
                textViewDeviceMode.setText("Manual");
            } else {
                textViewDeviceMode.setText("");
            }

        });
        //Observe scheduled freq
        dashboardViewModel.getScheduledFreq().observe(getViewLifecycleOwner(), scheduledFreq -> {
            TextView textViewScheduledFreq = root.findViewById(R.id.textView_dashboard_scheduled_freq);
            if(dashboardViewModel.getMode().getValue() == 1) {  //Scheduled
                if(scheduledFreq == 1) {    //Every day
                    textViewScheduledFreq.setText("Scheduled Frequency: Every day");
                } else if(scheduledFreq == 2) { //Every 2 days
                    textViewScheduledFreq.setText("Scheduled Frequency: Every 2 days");
                } else if(scheduledFreq == 3) {
                    textViewScheduledFreq.setText("Scheduled Frequency: Every 3 days");
                } else if(scheduledFreq == 4) {
                    textViewScheduledFreq.setText("Scheduled Frequency: Every week");
                }
            } else {
                textViewScheduledFreq.setText("");
            }

        });
        //Observe scheduled time
        dashboardViewModel.getScheduledTime().observe(getViewLifecycleOwner(), scheduledTime -> {
            TextView textViewScheduledTime = root.findViewById(R.id.textView_dashboard_scheduled_time);
            if(dashboardViewModel.getMode().getValue() == 1) {
                textViewScheduledTime.setText("Scheduled Time: " + scheduledTime);
            } else {
                textViewScheduledTime.setText("");
            }

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

                //Set the EditText input values
                EditText editTextName = editBasicInfoDialog.findViewById(R.id.editText_dialog_edit_basic_info_name);
                EditText editTextAddress = editBasicInfoDialog.findViewById(R.id.editText_dialog_edit_basic_info_address);
                EditText editTextPort = editBasicInfoDialog.findViewById(R.id.editText_dialog_edit_basic_info_port);
                editTextName.setText(dashboardViewModel.getDeviceName().getValue());
                editTextAddress.setText(dashboardViewModel.getHost().getValue());
                editTextPort.setText(dashboardViewModel.getPort().getValue());

                //Set the ok button event (edit basic info)
                Button buttonOK = editBasicInfoDialog.findViewById(R.id.button_dialog_edit_basic_info_ok);
                buttonOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Get the EditText input values
                        String newName = editTextName.getText().toString();
                        String newHost = editTextAddress.getText().toString();
                        String newPort = editTextPort.getText().toString();

                        //Update the basic info in local database
                        DeviceInfo currentDeviceInfo = HICApplication.getInstance().servers.get(dashboardViewModel.indexOfDeviceInfo);
                        HICApplication.getInstance().deviceDatabaseHelper.updateBasicInfo(currentDeviceInfo, newName, newHost, newPort);

                        //Update the basic info in the array
                        currentDeviceInfo.name = newName;
                        currentDeviceInfo.host = newHost;
                        currentDeviceInfo.port = Integer.parseInt(newPort);
                        currentDeviceInfo.tcpClient.serverHost = newHost;
                        currentDeviceInfo.tcpClient.serverPort = Integer.parseInt(newPort);

                        //Update the basic info in the viewmodel
                        dashboardViewModel.loadData();

                        editBasicInfoDialog.dismiss();
                    }
                });

                //Set the cancel button event
                Button buttonCancel = editBasicInfoDialog.findViewById(R.id.button_dialog_edit_basic_info_cancel);
                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editBasicInfoDialog.dismiss();
                    }
                });
                editBasicInfoDialog.show();
            }
        });

        //Set the edit mode button and the dialog
        Button editModeButton = root.findViewById(R.id.button_dashboard_mode_edit);
        editModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog editModeDialog = new Dialog(requireContext());
                editModeDialog.setContentView(R.layout.dialog_edit_mode);

                //Set the spinner and edittext values
                Spinner spinnerMode = editModeDialog.findViewById(R.id.spinner_dialog_edit_mode_mode);
                String[] modeItems = {"Scheduled", "Manual"};
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, modeItems);
                modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerMode.setAdapter(modeAdapter);
                //mode - 1 = mode_index
                int defaultModeIndexSelection = dashboardViewModel.getMode().getValue() - 1;
                spinnerMode.setSelection(defaultModeIndexSelection);

                Spinner spinnerFreq = editModeDialog.findViewById(R.id.spinner_dialog_edit_mode_freq);
                String[] freqItems = {"Every day", "Every 2 days", "Every 3 days", "Every Week"};
                ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, freqItems);
                freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerFreq.setAdapter(freqAdapter);
                //freq - 1 = freq_index
                int defaultFreqIndexSelection = dashboardViewModel.getScheduledFreq().getValue() - 1;
                spinnerFreq.setSelection(defaultFreqIndexSelection);

                EditText editTextTime = editModeDialog.findViewById(R.id.editText_edit_mode_time);

                //Set the ok button event
                Button buttonOK = editModeDialog.findViewById(R.id.button_dialog_edit_mode_ok);
                buttonOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
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