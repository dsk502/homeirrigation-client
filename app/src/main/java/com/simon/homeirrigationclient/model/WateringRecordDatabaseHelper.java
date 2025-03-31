package com.simon.homeirrigationclient.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

public class WateringRecordDatabaseHelper extends SQLiteOpenHelper {

    private String databaseName;

    private static final int DATABASE_VERSION = 1;
    public WateringRecordDatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
        this.databaseName = databaseName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
