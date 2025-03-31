package com.simon.homeirrigationclient.model;

import android.content.Context;

//Class for a device
public class Device {

    //Device information
    public DeviceInfo deviceInfo;

    //TCP Client customized for the device
    public TCPClient tcpClient;

    //Watering record database manager
    public WateringRecordDatabaseHelper wateringRecordDatabaseHelper;
    public Device(DeviceInfo deviceInfo, Context context) {
        this.deviceInfo = deviceInfo;
        tcpClient = new TCPClient(context, deviceInfo.host, deviceInfo.port);
    }
}
