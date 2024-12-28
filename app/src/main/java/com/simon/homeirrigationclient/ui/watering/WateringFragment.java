package com.simon.homeirrigationclient.ui.watering;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.simon.homeirrigationclient.databinding.FragmentWateringBinding;

public class WateringFragment extends Fragment {

    private FragmentWateringBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        WateringViewModel wateringViewModel =
                new ViewModelProvider(this).get(WateringViewModel.class);

        binding = FragmentWateringBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textWatering;
        //wateringViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}