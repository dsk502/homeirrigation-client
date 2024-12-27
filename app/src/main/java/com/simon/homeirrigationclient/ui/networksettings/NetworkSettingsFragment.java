package com.simon.homeirrigationclient.ui.networksettings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.simon.homeirrigationclient.databinding.FragmentNetworkSettingsBinding;

/**

 */
public class NetworkSettingsFragment extends Fragment {

    private FragmentNetworkSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NetworkSettingsViewModel networkSettingsViewModel =
                new ViewModelProvider(this).get(NetworkSettingsViewModel.class);

        binding = FragmentNetworkSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSettings;
        networkSettingsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}