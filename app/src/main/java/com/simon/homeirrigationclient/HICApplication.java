package com.simon.homeirrigationclient;

import android.app.Application;
import android.util.Log;

import com.simon.homeirrigationclient.model.DeviceDatabaseHelper;
import com.simon.homeirrigationclient.model.DeviceInfo;
import com.simon.homeirrigationclient.model.RSAUtils;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class HICApplication extends Application {
    private static HICApplication instance;
    //The list of all device (servers) on the phone
    public ArrayList<DeviceInfo> servers;

    //Database helper that manages devices' info
    public DeviceDatabaseHelper deviceDatabaseHelper;

    public RSAUtils rsaUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        //1. Init the common database helper (device database helper)
        deviceDatabaseHelper = new DeviceDatabaseHelper(this);

        //2. Init the rsaUtils object
        rsaUtils = new RSAUtils(this);

        if(!rsaUtils.isKeypairFileExist()) {
            try {
                Log.println(Log.ERROR, "Is keypair exist:", "False");
                rsaUtils.generateRSAKeyPair();
            } catch (NoSuchAlgorithmException e) {
                Log.println(Log.ERROR, "Error", e.toString());
            }
        }

        //3. Read all servers' info to the global variable
        servers = HICApplication.getInstance().deviceDatabaseHelper.getAllDeviceInfo();

        //4. For every device info object, create its own TCPClient and WateringRecordHelper objects
        for(int i = 0; i < servers.size(); i++) {
            servers.get(i).initNetAndRecHelper(this);
        }
    }
    public static HICApplication getInstance() {
        return instance;
    }
}
