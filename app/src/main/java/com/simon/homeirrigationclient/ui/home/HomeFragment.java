package com.simon.homeirrigationclient.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.simon.homeirrigationclient.controller.DeviceCardGridViewAdapter;
import com.simon.homeirrigationclient.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private GridView deviceCardContainer;
    private DeviceCardGridViewAdapter deviceCardGridViewAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        /*

        gridView = findViewById(R.id.gridView);
        items = new ArrayList<>();
        adapter = new CardViewAdapter(this, items);

        // 添加数据到列表
        for (int i = 0; i < 20; i++) {
            items.add("Item " + (i + 1));
        }

        gridView.setAdapter(adapter);
         */

        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}