package com.simon.homeirrigationclient.model;

//Class to describe a device (server)
public class DeviceInfo {

    //The ID of the device
    public long serverId;

    //The name of the device
    public String name;

    public String serverPubkey;

    public long clientAddTime;

    //The IP address or domain name of the device
    public String host;

    //Port number
    public int port;

    //The watering mode
    //Range: {1 == scheduled, 2 == manual}
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
    //Range: {1 == every day, 2 == every two days, 3 == every three days, 4 == once a week}
    //Default: 1
    public int scheduledFreq;

    public String scheduledTime;

    //TCP Client customized for the device
    public TCPClient tcpClient;

    //Watering record database manager
    public WateringRecordDatabaseHelper wateringRecordDatabaseHelper;

    //Constructor with all parameters
    public DeviceInfo(long id, String name, String serverPubkey, long clientAddTime, String host, int port, int mode, double waterAmount, int scheduledFreq, String scheduledTime) {
        this.serverId = id;
        this.name = name;
        this.serverPubkey = serverPubkey;
        this.clientAddTime = clientAddTime;
        this.host = host;
        this.port = port;
        this.mode = mode;
        this.waterAmount = waterAmount;
        this.scheduledFreq = scheduledFreq;
        this.scheduledTime = scheduledTime;
    }

    //Constructor with parameters that do not have a default value
    public DeviceInfo(long id, String name, String serverPubkey, long clientAddTime, String host, int port, int mode) {
        this.serverId = id;
        this.name = name;
        this.serverPubkey = serverPubkey;
        this.clientAddTime = clientAddTime;
        this.host = host;
        this.port = port;
        this.mode = mode;
        this.waterAmount = 100.0;
        this.scheduledFreq = 2;
    }
}
