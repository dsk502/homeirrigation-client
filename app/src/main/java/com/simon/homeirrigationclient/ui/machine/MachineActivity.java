package com.simon.homeirrigationclient.ui.machine;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.simon.homeirrigationclient.HICApplication;
import com.simon.homeirrigationclient.R;
import com.simon.homeirrigationclient.databinding.ActivityMachineBinding;
import com.simon.homeirrigationclient.model.DeviceInfo;
import com.simon.homeirrigationclient.ui.machine.dashboard.DashboardViewModel;

public class MachineActivity extends AppCompatActivity {

    private ActivityMachineBinding binding;
    private DashboardViewModel dashboardViewModel;
    private DeviceInfo currentDeviceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMachineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Get the index of the DeviceInfo object in the application Arraylist
        Intent intent = getIntent();
        int indexOfDeviceInfo = intent.getIntExtra("indexOfDeviceInfo", 0);

        //Get DashboardViewModel and set the param
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        dashboardViewModel.indexOfDeviceInfo = indexOfDeviceInfo;

        //Set Toolbar as action bar
        Toolbar toolbar = findViewById(R.id.toolbar_machine);
        setSupportActionBar(toolbar);

        BottomNavigationView navView = findViewById(R.id.nav_view_machine);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_statistics)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_machine);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navViewMachine, navController);

        //Create the bundle and parse the index by navigating to dash
        //Bundle indexBundle = new Bundle();
        //indexBundle.putInt("indexOfDeviceInfo", indexOfDeviceInfo);
        //navController.navigate(R.id.navigation_dashboard, indexBundle);

        //Get the DeviceInfo object from the application ArrayList
        //currentDeviceInfo = HICApplication.getInstance().servers.get(indexOfDeviceInfo);

        //Show the device information on the page


    }

}