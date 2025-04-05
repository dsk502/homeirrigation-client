package com.simon.homeirrigationclient;

import android.app.Application;

import com.simon.homeirrigationclient.model.DeviceDatabaseHelper;
import com.simon.homeirrigationclient.model.DeviceInfo;

import java.util.ArrayList;

public class HICApplication extends Application {
    private static HICApplication instance;
    //The list of all device (servers) on the phone
    public ArrayList<DeviceInfo> servers = new ArrayList<>();

    //Database helper that manages devices' info
    public DeviceDatabaseHelper deviceDatabaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        deviceDatabaseHelper = new DeviceDatabaseHelper(this);
    }
    public static HICApplication getInstance() {
        return instance;
    }
}
