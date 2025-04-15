package com.simon.homeirrigationclient.ui.machine.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.simon.homeirrigationclient.HICApplication;
import com.simon.homeirrigationclient.R;
import com.simon.homeirrigationclient.databinding.FragmentStatisticsBinding;
import com.simon.homeirrigationclient.model.DeviceInfo;
import com.simon.homeirrigationclient.ui.machine.dashboard.DashboardViewModel;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        StatisticsViewModel statisticsViewModel =
                new ViewModelProvider(this).get(StatisticsViewModel.class);

        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textNotifications;
        //statisticsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        Button buttonRefresh = root.findViewById(R.id.button_stat_refresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Download stat
                DeviceInfo currentDeviceInfo = HICApplication.getInstance().servers.get(dashboardViewModel.indexOfDeviceInfo);
                currentDeviceInfo.tcpClient.downloadStatRequest(currentDeviceInfo.serverId, currentDeviceInfo.serverPubkey);
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