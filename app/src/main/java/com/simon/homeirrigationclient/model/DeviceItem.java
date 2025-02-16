package com.simon.homeirrigationclient.model;

//Class to describe a device
public class DeviceItem {
    //The name of the device
    public String name;

    //The address on the network (IP address + port or domain + port)
    public String address;

    //The watering mode
    public String mode;

    public DeviceItem(String name, String address, String mode) {
        this.name = name;
        this.address = address;
        this.mode = mode;
    }
}
