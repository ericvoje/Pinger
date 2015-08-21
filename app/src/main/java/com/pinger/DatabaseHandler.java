package com.pinger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Eric on 7/31/2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper
{
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "pingManager";

    // Contacts table name
    private static final String TABLE_PINGS = "pings";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TIME = "time";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_CHARGE = "charge";
    private static final String KEY_ISCHARGING = "isCharging";
    private static final String KEY_ISCONNECTED = "isConnected";
    private static final String KEY_NETTYPE = "nettype";
    private static final String KEY_SSID = "ssid";
    private static final String KEY_IPADDR = "ipaddr";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_PINGS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIME + " DATETIME,"
                + KEY_LONGITUDE + " FLOAT," + KEY_LATITUDE + " FLOAT,"
                + KEY_CHARGE + " FLOAT," + KEY_ISCHARGING + " BOOLEAN,"
                + KEY_ISCONNECTED + " BOOLEAN," + KEY_NETTYPE + " VARCHAR(63),"
                + KEY_SSID + " VARCHAR(255)," + KEY_IPADDR + " VARCHAR(63)" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PINGS);

        // Create tables again
        onCreate(db);
    }

    /**
     * Add a new Entry to the database
     * @param e - Entry to add to database
     */
    public void addEntry(Entry e)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");

        // Populate values
        values.put(KEY_TIME, sdf.format(e.getTime()));
        values.put(KEY_LONGITUDE, e.getLocation().getLongitude());
        values.put(KEY_LATITUDE, e.getLocation().getLatitude());
        values.put(KEY_CHARGE, e.getCharge());
        values.put(KEY_ISCHARGING, e.isCharging());
        values.put(KEY_ISCONNECTED, e.isConnected());
        values.put(KEY_NETTYPE, e.getNetworkType());
        values.put(KEY_SSID, e.getSsid());
        values.put(KEY_IPADDR, e.getIpaddr());

        // Inserting Row
        db.insert(TABLE_PINGS, null, values);
        db.close(); // Closing database connection
    }

    /**
     * Get entry by ID
     * @param id
     * @return
     */
    public Entry getEntry(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PINGS, new String[] { KEY_ID,
                        KEY_LONGITUDE, KEY_LATITUDE, KEY_CHARGE, KEY_ISCHARGING,
                        KEY_ISCONNECTED, KEY_NETTYPE, KEY_SSID, KEY_IPADDR}, KEY_ID + "=?",

                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");


        Date time;
        try {
            time = sdf.parse(cursor.getString(0));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Location location = new Location("database");
        location.setLongitude(cursor.getDouble(1));
        location.setLatitude(cursor.getDouble(2));

        float charge = (float) cursor.getDouble(3);
        boolean isCharging = cursor.getInt(4) == 1;
        boolean isConnected = cursor.getInt(5) == 1;
        String netType = cursor.getString(6);
        String ssid = cursor.getString(7);
        String ipaddr = cursor.getString(8);

        Entry entry = new Entry(time, location, charge, isCharging, isConnected, netType,
                ssid, ipaddr);

        // return contact
        return entry;
    }

    /**
     * Return the number of rows in database
     * @return
     */
    public int nEntries()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from your_table_name",null);
        return c.getCount();
    }

}
