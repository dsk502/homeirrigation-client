package com.simon.homeirrigationclient;

import android.app.Application;
import com.simon.homeirrigationclient.model.Device;
import com.simon.homeirrigationclient.model.DeviceDatabaseHelper;

import java.util.ArrayList;

public class HICApplication extends Application {

    //The list of all device (servers) on the phone
    public ArrayList<Device> servers = new ArrayList<>();

    //Database helper that manages devices' info
    DeviceDatabaseHelper deviceDatabaseHelper = new DeviceDatabaseHelper();
}
