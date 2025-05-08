package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "cities.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_CITIES = "cities";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TEMP = "tem";
    public static final String COLUMN_VETER = "veter";
    public static final String COLUMN_IKV = "ikv";

    private static final String CREATE_TABLE_CITIES = "CREATE TABLE " + TABLE_CITIES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT, "
            + COLUMN_TEMP + " TEXT, "
            + COLUMN_VETER + " TEXT, "
            + COLUMN_IKV + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CITIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CITIES);
        onCreate(db);
    }

    public long addCity(String name, String tem, String veter, String ikv) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_TEMP, tem);
        values.put(COLUMN_VETER, veter);
        values.put(COLUMN_IKV, ikv);

        long id = db.insert(TABLE_CITIES, null, values);
        db.close();
        return id;
    }

    public Cursor getAllCities() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CITIES, null, null, null, null, null, null);
    }

    public int deleteCity(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_CITIES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }
}