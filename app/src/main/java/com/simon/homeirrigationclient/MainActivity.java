package com.simon.homeirrigationclient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.simon.homeirrigationclient.databinding.ActivityMainBinding;
import com.simon.homeirrigationclient.model.DeviceInfo;
import com.simon.homeirrigationclient.ui.main.home.DeviceAddActivity;
import com.simon.homeirrigationclient.ui.main.home.DeviceCardGridViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Set toolbar as actionbar
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        //Set bottom navigation bar
        BottomNavigationView navView = findViewById(R.id.nav_view_main);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_about)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navViewMain, navController);

        //Set the add device button
        Button buttonAddDevice = findViewById(R.id.button_add_devices);
        buttonAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeviceAddActivity.class);
                startActivity(intent);
            }
        });

        //Set GridView
        GridView gridView = findViewById(R.id.gridView_device_card);
        ArrayList<DeviceInfo> deviceInfoList = HICApplication.getInstance().servers;
        DeviceCardGridViewAdapter gridViewAdapter = new DeviceCardGridViewAdapter(this, deviceInfoList);
        gridView.setAdapter(gridViewAdapter);
    }

}