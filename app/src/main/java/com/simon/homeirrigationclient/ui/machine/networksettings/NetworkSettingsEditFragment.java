package com.simon.homeirrigationclient.ui.machine.networksettings;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.simon.homeirrigationclient.R;

public class NetworkSettingsEditFragment extends Fragment {

    private NetworkSettingsEditViewModel mViewModel;

    public static NetworkSettingsEditFragment newInstance() {
        return new NetworkSettingsEditFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_network_settings_edit, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(NetworkSettingsEditViewModel.class);
        // TODO: Use the ViewModel
    }

}