package com.simon.homeirrigationclient.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WateringRecordDatabaseHelper extends SQLiteOpenHelper {

    private String databaseName;

    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "watering_record";

    public WateringRecordDatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
        this.databaseName = databaseName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Do nothing
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Do nothing
    }

    public HashMap<String, String> getTimesOfWatering() {
        HashMap<String, String> timesOfWatering = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{"day", "times_of_watering"}, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // 获取字段 a 和字段 b 的值
                String key = cursor.getString(cursor.getColumnIndexOrThrow("day"));
                String value = cursor.getString(cursor.getColumnIndexOrThrow("times_of_watering"));

                // 将键值对存入 Map
                timesOfWatering.put(key, value);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return timesOfWatering;
    }

    public HashMap<String, String> getAmountOfWatering() {
        HashMap<String, String> amountOfWatering = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{"day", "amount_of_watering"}, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // 获取字段 a 和字段 b 的值
                String key = cursor.getString(cursor.getColumnIndexOrThrow("day"));
                String value = cursor.getString(cursor.getColumnIndexOrThrow("amount_of_watering"));

                // 将键值对存入 Map
                amountOfWatering.put(key, value);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return amountOfWatering;
    }

    public HashMap<String, String> getSoilMoisturePercentage() {
        HashMap<String, String> soilMoisturePercentage = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{"day", "soil_moisture_percentage"}, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // 获取字段 a 和字段 b 的值
                String key = cursor.getString(cursor.getColumnIndexOrThrow("day"));
                String value = cursor.getString(cursor.getColumnIndexOrThrow("soil_moisture_percentage"));

                // 将键值对存入 Map
                soilMoisturePercentage.put(key, value);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return soilMoisturePercentage;
    }


}
