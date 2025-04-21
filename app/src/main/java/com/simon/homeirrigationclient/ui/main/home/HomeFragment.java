package com.simon.homeirrigationclient.ui.main.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.simon.homeirrigationclient.HICApplication;
import com.simon.homeirrigationclient.R;
import com.simon.homeirrigationclient.databinding.FragmentHomeBinding;
import com.simon.homeirrigationclient.model.DeviceInfo;
import com.simon.homeirrigationclient.ui.machine.MachineActivity;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private DeviceCardGridViewAdapter gridViewAdapter;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        GridView gridView = root.findViewById(R.id.gridView_device_card);
        ArrayList<DeviceInfo> deviceInfoList = HICApplication.getInstance().servers;
        gridViewAdapter = new DeviceCardGridViewAdapter(requireContext(), deviceInfoList);
        gridView.setAdapter(gridViewAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(requireContext(), MachineActivity.class);
                //The index of the device info object corresponding to this device card is the position
                intent.putExtra("indexOfDeviceInfo", position);
                startActivity(intent);
            }
        });

        //Set the add device button
        Button buttonAddDevice = root.findViewById(R.id.button_add_devices);
        buttonAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), DeviceAddActivity.class);
                startActivity(intent);
            }
        });

        //Set the edit device or done button
        //Default: Edit
        Button buttonEditDone = root.findViewById(R.id.button_edit_done);
        buttonEditDone.setTag("Edit");
        buttonEditDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonTag = (String)buttonEditDone.getTag();
                if(buttonTag.equals("Edit")) {
                    //If button is edit
                    gridViewAdapter.showDeleteButton = true;
                    gridViewAdapter.notifyDataSetChanged();
                    buttonEditDone.setText(R.string.text_done);
                    buttonEditDone.setTag("Done");
                } else {    //If button is done
                    gridViewAdapter.showDeleteButton = false;
                    gridViewAdapter.notifyDataSetChanged();
                    buttonEditDone.setText(R.string.text_edit);
                    buttonEditDone.setTag("Edit");
                }

            }
        });

        return root;

    }

    @Override
    public void onResume() {
        super.onResume();
        gridViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}