package sg.com.commute_solutions.bustracker.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import sg.com.commute_solutions.bustracker.common.Constants;

/**
 * Created by Kyle on 6/9/16.
 */
public class DBUtil extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "locationManager";

    // Contacts table name
    private static final String TABLE_LOCATION = "USER_LOCATION ";

    // Contacts Table Columns names


    private SharedPreferences prefs;

    public DBUtil(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOCATION_TABLE = "CREATE TABLE " + TABLE_LOCATION + "("
                + Constants.KEY_NO + " INTEGER PRIMARY KEY," + Constants.KEY_ID + " TEXT,"  + Constants.KEY_LONGTITUDE + " TEXT,"
                + Constants.KEY_LATITUDE + " TEXT,"  + Constants.KEY_ALTITUDE + " TEXT," + Constants.KEY_ACCURACY + " TEXT,"
                + Constants.KEY_SPEED + " TEXT," + Constants.KEY_DATE + " TEXT" + ")";
        db.execSQL(CREATE_LOCATION_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new row of location values
    public void addLocation(String id, String longitude, String latitude, String altitude, String accuracy, String speed, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.KEY_NO, "" + getTableCount() + 1);
        contentValues.put(Constants.KEY_ID, id);
        contentValues.put(Constants.KEY_LONGTITUDE, longitude);
        contentValues.put(Constants.KEY_LATITUDE, latitude);
        contentValues.put(Constants.KEY_ALTITUDE, altitude);
        contentValues.put(Constants.KEY_ACCURACY, accuracy);
        contentValues.put(Constants.KEY_SPEED, speed);
        contentValues.put(Constants.KEY_DATE, date);

        // Inserting Row
        db.beginTransaction();
        db.insert(TABLE_LOCATION, null, contentValues);
        db.close();
    }

    // Getting single location
    public List<String> getLocation(int index) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_LOCATION, new String[] {Constants.KEY_NO,
                        Constants.KEY_ID, Constants.KEY_LONGTITUDE, Constants.KEY_LATITUDE, Constants.KEY_ALTITUDE,
                        Constants.KEY_ACCURACY, Constants.KEY_SPEED, Constants.KEY_DATE }, Constants.KEY_NO + "="+ index,
                null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        List<String> mLocation = new ArrayList<>();

        for (int i = 1; i < cursor.getColumnCount(); i++) {
            mLocation.add(cursor.getString(i));
        }

        return mLocation;
    }

    //Getting all locations
    public ArrayList<ArrayList<String>> getAllLocations() {
        ArrayList<ArrayList<String>> locationList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_LOCATION;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ArrayList<String> mLocation = new ArrayList<>();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    mLocation.add(cursor.getString(i));
                }

                // Adding location details to list
                locationList.add(mLocation);
            } while (cursor.moveToNext());
        }

        // return contact list
        return locationList;
    }

    public int updateContact(int index, String row, String data ) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.KEY_NO, "" + index);
        contentValues.put(row, data);

        db.beginTransaction();
        int rowsUpdated = db.update(TABLE_LOCATION, contentValues, Constants.KEY_NO + "="+ index, null);
        db.close();
        // updating row
        return rowsUpdated;
    }

    // Deleting single location
    public void deleteLocation (int index) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        db.delete(TABLE_LOCATION, Constants.KEY_NO + "="+ index, null);
        db.close();
    }

    // Getting current stored count
    public int getTableCount() {
        String countQuery = "SELECT  * FROM " + TABLE_LOCATION;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }
}
