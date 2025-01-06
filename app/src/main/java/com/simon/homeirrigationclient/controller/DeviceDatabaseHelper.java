package com.simon.homeirrigationclient.controller;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DeviceDatabaseHelper extends SQLiteOpenHelper {
    //Database name
    private static final String DATABASE_NAME = "deviceDatabase.db";
    //Database version
    private static final int DATABASE_VERSION = 1;

    public DeviceDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create database table
        String createTableSQL = "CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, age INTEGER)";
        db.execSQL(createTableSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //When upgrading the database
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}
