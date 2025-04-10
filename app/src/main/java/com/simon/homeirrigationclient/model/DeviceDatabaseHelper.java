package com.simon.homeirrigationclient.model;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DeviceDatabaseHelper extends SQLiteOpenHelper {
    //Database name
    private static final String DATABASE_NAME = "device_database.db";
    //Database version
    private static final int DATABASE_VERSION = 1;

    public DeviceDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create database tables
        String createDevicesTableSQL = "CREATE TABLE servers (server_id TEXT PRIMARY KEY, name TEXT, server_pubkey TEXT, client_add_time INTEGER, host TEXT, port INTEGER, mode INTEGER, water_amount REAL, scheduled_freq INTEGER, scheduled_time TEXT)";
        db.execSQL(createDevicesTableSQL);
        /*
        String createWateringDataTableSQL = "CREATE TABLE watering_data (id INTEGER PRIMARY KEY)";
        db.execSQL(createWateringDataTableSQL);
        */
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //When upgrading the database
        //Do nothing
        db.execSQL("DROP TABLE IF EXISTS servers");
        onCreate(db);
    }

    //Add a device to the database
    @SuppressLint("DefaultLocale")
    public int insertDevice(DeviceInfo deviceInfo) {
        SQLiteDatabase db = this.getWritableDatabase();

        //Generate the record for the device
        ContentValues deviceRecord = new ContentValues();
        deviceRecord.put("server_id", deviceInfo.serverId);
        deviceRecord.put("name", deviceInfo.name);
        deviceRecord.put("server_pubkey", deviceInfo.serverPubkey);
        deviceRecord.put("client_add_time", deviceInfo.clientAddTime);
        deviceRecord.put("host", deviceInfo.host);
        deviceRecord.put("port", deviceInfo.port);
        deviceRecord.put("mode", deviceInfo.mode);
        deviceRecord.put("water_amount", deviceInfo.waterAmount);
        deviceRecord.put("scheduled_freq", deviceInfo.scheduledFreq);
        deviceRecord.put("scheduled_time", deviceInfo.scheduledTime);

        //Insert the record to the table "devices"
        if(db.insert("servers", null, deviceRecord) == -1L) {
            return -1;
        }

        //Create a record in the watering_data table on the client side, in order to temp the data from the server side (Raspberry Pi)
        //The timezone of the statistics will be server's
        //String createWateringDataTempSQL = String.format("CREATE TABLE watering_record_%s (day INTEGER PRIMARY KEY, time_of_watering INTEGER amount_of_watering REAL)", serverId);
        //db.execSQL(createWateringDataTempSQL);

        db.close();
        return 0;   //Success
    }

    //Delete a device from the local database
    public void deleteDevice(String serverId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String deleteDeviceInfoSQL = String.format("DELETE FROM servers WHERE server_id='%s'", serverId);
        db.execSQL(deleteDeviceInfoSQL);
        //String deleteWateringDataSQL = String.format("DROP TABLE IF EXISTS watering_record_%s", serverId);
        //db.execSQL(deleteWateringDataSQL);
        db.close();
    }

    //Get the basic information (server_id, name, host, port, mode and mode-related information) of all devices, to show device cards
    @SuppressLint("Range")
    public ArrayList<DeviceInfo> getAllDeviceInfo() {
        ArrayList<DeviceInfo> deviceInfoList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("servers",new String[]{"server_id", "name", "server_pubkey", "client_add_time", "host", "port", "mode", "water_amount", "scheduled_freq","scheduled_time"}, null, null, null, null, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                do {
                    //For each record
                    String serverId = cursor.getString(cursor.getColumnIndex("server_id"));
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    String serverPubkey = cursor.getString(cursor.getColumnIndex("server_pubkey"));
                    long clientAddTime = cursor.getLong(cursor.getColumnIndex("client_add_time"));
                    String host = cursor.getString(cursor.getColumnIndex("host"));
                    int port = cursor.getInt(cursor.getColumnIndex("port"));
                    int mode = cursor.getInt(cursor.getColumnIndex("mode"));
                    double waterAmount = cursor.getDouble(cursor.getColumnIndex("water_amount"));
                    int scheduledFreq = cursor.getInt(cursor.getColumnIndex("scheduled_freq"));
                    String scheduledTime = cursor.getString(cursor.getColumnIndex("scheduled_time"));
                    DeviceInfo deviceInfo = new DeviceInfo(serverId, name, serverPubkey, clientAddTime, host, port, mode, waterAmount, scheduledFreq, scheduledTime);
                    deviceInfoList.add(deviceInfo);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return deviceInfoList;
    }

    //Get the statistics of a single device from local database
    public void getDeviceStat(String serverId) {

    }

    //Sync watering data from the server
    public void syncServerWateringData() {

    }

    //Get the timestamp of the client adding the Raspberry Pi
    //The timestamp is not related to timezone
    //SQLite's INTEGER corresponds to Java's long
    public static long getAddTime() {
        return System.currentTimeMillis();
    }

    public void updateBasicInfo(DeviceInfo deviceInfo, String newName, String newHost, String newPort) {
        SQLiteDatabase db = this.getWritableDatabase();
        String serverId = deviceInfo.serverId;
        ContentValues values = new ContentValues();
        values.put("name", newName);
        values.put("host", newHost);
        values.put("port", newPort);
        db.update("servers", values, "server_id = ?", new String[]{serverId});
        db.close();

    }
    public void updateMode() {

    }
}
