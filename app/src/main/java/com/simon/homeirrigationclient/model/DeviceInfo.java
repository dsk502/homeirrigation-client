package com.simon.homeirrigationclient.model;

//Class to describe a device
public class DeviceInfo {

    //The ID of the device
    public long id;

    //The name of the device
    public String name;

    //The IP address or domain name of the device
    public String host;

    //Port number
    public int port;

    //The watering mode
    //Range: {1 == automatic, 2 == scheduled, 3 == manual}
    public int mode;

    //The amount of water (mL) used in one time of watering
    //Range: (0.0, 200.0]
    //Default: 100.0
    public double waterAmount;

    //The humidity limit of watering for automatic mode
    //This setting is only valid when mode == automatic
    //Range: [0.0, 1.0)
    //Default: 0.5
    //public double automaticHumidity;

    //The scheduled frequency
    //This setting is only valid when mode == scheduled
    //Range: {1 == every half a day, 2 == every day, 3 == every two days, 4 == every three days, 5 == once a week}
    //Default: 2
    public int scheduledFreq;

    public String scheduledTime;

    //Constructor with all parameters
    public DeviceInfo(long id, String name, String host, int port, int mode, double waterAmount, double automaticHumidity, int scheduledFreq) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
        this.mode = mode;
        this.waterAmount = waterAmount;
        //this.automaticHumidity = automaticHumidity;
        this.scheduledFreq = scheduledFreq;
    }

    //Constructor with parameters that do not have a default value
    public DeviceInfo(long id, String name, String host, int port, int mode) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
        this.mode = mode;
        this.waterAmount = 100.0;
        //this.automaticHumidity = 0.5;
        this.scheduledFreq = 2;
    }
}
