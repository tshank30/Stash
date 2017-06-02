package com.fst.apps.ftelematics.utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.fst.apps.ftelematics.entities.Alerts;
import com.fst.apps.ftelematics.entities.LastLocation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = DatabaseHelper.class.getName();

    public static final String DATABASE_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static final String FOLDER_NAME = "Rottweiler";
    // Database Version
    private static final int DATABASE_VERSION = 3;

    // Database Name
    private static final String DATABASE_NAME = DATABASE_FILE_PATH + File.separator + FOLDER_NAME + File.separator + "rottweilertrackers";

    // Table Names
    private static final String TABLE_ALERTS = "alerts";
    private static final String TABLE_VEHICLE_LIST = "VehicleList";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    // Alerts Table - column names
    private static final String KEY_ALERT_TYPE = "alert_type";
    private static final String KEY_ALERT_SUB_TYPE = "alert_sub_type";
    private static final String KEY_ALERT_TEXT = "alert_text";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_VEHICLE_NUMBER = "vehicle_number";

    // Vehicle List Table - column names
    private static final String KEY_ACCOUNT_ID = "accountID";
    private static final String KEY_DEVICE_ID = "deviceID";
    private static final String KEY_UNIX_TIME = "unixTime";
    private static final String KEY_TIMESTAMP = "TIMESTAMP";
    private static final String KEY_STATUS_CODE = "statusCode";
    private static final String KEY_TEMPERATURE = "temperature";
    private static final String KEY_INPUT_MASK = "inputMask";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_SPEED_KPH = "speedKPH";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_HEADING = "heading";
    private static final String KEY_DISPLAY_NAME = "displayName";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_VEHICLE_TYPE = "vehicleType";
    private static final String KEY_EQUIPMENT_TYPE = "equipmentType";
    private static final String KEY_VOLTAGE_AT_EMPTY = "voltageAtEmpty";
    private static final String KEY_FUEL_PER_VOLT = "fuelPerVolt";
    private static final String KEY_FUEL_VOLTAGE_DIRECTION = "fuelVoltageDirection";
    private static final String KEY_MAX_TANK_CAPACITY = "maxTankCapicity";
    private static final String KEY_VOLTAGE_PATTERN = "voltagePattern";
    private static final String KEY_SIM_PHONE_NUMBER = "simPhoneNumber";
    private static final String KEY_DEVICE_TYPE = "deviceType";
    private static final String KEY_CURRENT_TIME = "currentTime";
    private static final String KEY_STATUS_SINCE = "statusSince";
    private static final String KEY_DRIVER_NAME = "driverName";
    private static final String KEY_DRIVER_NUMBER = "driverNumber";

    private static final String TAG = "DatabaseHelper";

    // Table Create Statements
    // Alert table create statement
    private static final String CREATE_TABLE_ALERTS = "CREATE TABLE "
            + TABLE_ALERTS + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_ALERT_TYPE
            + " INTEGER," + KEY_ALERT_SUB_TYPE + " TEXT," + KEY_ALERT_TEXT + " TEXT," + KEY_USER_ID + " TEXT," +
            KEY_VEHICLE_NUMBER + " TEXT," + KEY_CREATED_AT + " DATETIME" + ")";

    private static final String CREATE_TABLE_VEHICLE_LIST = "CREATE TABLE " + TABLE_VEHICLE_LIST + "(" +
            KEY_ACCOUNT_ID + " TEXT," +
            KEY_DEVICE_ID + " TEXT," +
            KEY_UNIX_TIME + " TEXT," +
            KEY_TIMESTAMP + " TEXT," +
            KEY_STATUS_CODE + " TEXT," +
            KEY_TEMPERATURE + " TEXT," +
            KEY_INPUT_MASK + " TEXT," +
            KEY_LATITUDE + " TEXT," +
            KEY_LONGITUDE + " TEXT," +
            KEY_SPEED_KPH + " TEXT," +
            KEY_ADDRESS + " TEXT," +
            KEY_HEADING + " TEXT," +
            KEY_DISPLAY_NAME + " TEXT," +
            KEY_DESCRIPTION + " TEXT," +
            KEY_VEHICLE_TYPE + " TEXT," +
            KEY_EQUIPMENT_TYPE + " TEXT," +
            KEY_VOLTAGE_AT_EMPTY + " TEXT," +
            KEY_FUEL_PER_VOLT + " TEXT," +
            KEY_FUEL_VOLTAGE_DIRECTION + " TEXT," +
            KEY_MAX_TANK_CAPACITY + " TEXT," +
            KEY_VOLTAGE_PATTERN + " TEXT," +
            KEY_SIM_PHONE_NUMBER + " TEXT," +
            KEY_DEVICE_TYPE + " TEXT," +
            KEY_CURRENT_TIME + " TEXT," +
            KEY_STATUS_SINCE + " TEXT," +
            KEY_DRIVER_NAME + " TEXT," +
            KEY_DRIVER_NUMBER + " TEXT" +
            ")";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.e(TAG, "DH:Constructor:start");
        try {
            this.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        Log.e(TAG, "DH:onCreate:start");
        db.execSQL(CREATE_TABLE_ALERTS);
        db.execSQL(CREATE_TABLE_VEHICLE_LIST);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        Log.e(TAG, "DH:onUpgrade:start");
        Log.e(TAG, "DH:onUpgrade:oldVersion:" + oldVersion + ", newVersion:" + newVersion);
        switch (oldVersion) {
            case 1:
                Log.e(TAG, "DH:onUpgrade:case:1");
                try {
                    db.execSQL(CREATE_TABLE_VEHICLE_LIST);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            case 2:
                db.execSQL("ALTER TABLE VehicleList ADD driverName TEXT");
                db.execSQL("ALTER TABLE VehicleList ADD driverNumber TEXT");

        }
    }

    // ------------------------ "alerts" table methods ----------------//


    public long createAlert(Alerts alerts) {
        Log.e(TAG, "DH:createAlert:start");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(alerts.getCreatedAt())) {
            values.put(KEY_CREATED_AT, getDateTime(alerts.getCreatedAt()));
        } else {
            values.put(KEY_CREATED_AT, getDateTime());
        }
        values.put(KEY_ALERT_TYPE, alerts.getAlertType());
        values.put(KEY_ALERT_SUB_TYPE, alerts.getAlertSubType());
        values.put(KEY_ALERT_TEXT, alerts.getAlertText());
        values.put(KEY_USER_ID, alerts.getUserId());
        values.put(KEY_VEHICLE_NUMBER, alerts.getVehicleNumber());
        // insert row
        long alertid = db.insert(TABLE_ALERTS, null, values);
        return alertid;
    }


    public List<Alerts> getAlertsByAlertType(int alertType, String userId) {
        Log.e(TAG, "DH:getAlertsByAlertType:start");
        SQLiteDatabase db = this.getReadableDatabase();
        List<Alerts> alertsList = new ArrayList<Alerts>();

        String selectQuery = "SELECT  * FROM " + TABLE_ALERTS + " WHERE "
                + KEY_ALERT_TYPE + " = " + alertType + " AND " + KEY_USER_ID + "='" + String.valueOf(userId) + "'"
                + " ORDER BY datetime(" + KEY_CREATED_AT + ") DESC";

        Log.i(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Alerts alert = new Alerts();
                alert.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                alert.setCreatedAt(c.getString(c.getColumnIndex(KEY_CREATED_AT)));
                alert.setAlertType((c.getInt(c.getColumnIndex(KEY_ALERT_TYPE))));
                alert.setAlertSubType(c.getString(c.getColumnIndex(KEY_ALERT_SUB_TYPE)));
                alert.setAlertText(c.getString(c.getColumnIndex(KEY_ALERT_TEXT)));
                alert.setUserId(c.getString(c.getColumnIndex(KEY_USER_ID)));
                alert.setUserId(c.getString(c.getColumnIndex(KEY_VEHICLE_NUMBER)));

                // adding to alert list
                alertsList.add(alert);
            } while (c.moveToNext());
        }

        return alertsList;
    }


    public int getToDoCount() {
        String countQuery = "SELECT  * FROM " + TABLE_ALERTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }


    public int deleteAlertsByType(int alertType, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ALERTS, KEY_ALERT_TYPE + " = ? AND " + KEY_USER_ID + " = ?", new String[]{String.valueOf(alertType), userId});
    }


    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    /**
     * get datetime
     */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private String getDateTime(String dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        try {
            date = dateFormat.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateFormat.format(date);
    }

    /**
     * Stores vehile list data from service
     *
     * @return index of row saved (-1 in case of failiure)
     */
    synchronized public void storeVehicleListData(List<LastLocation> lastLocationList) {
        Log.e(TAG, "DH:storeVehicleListData:start");
        if (isVehicleListDataInDB())
            clearVehicleList();
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        for (LastLocation lastLocation : lastLocationList) {
            values.put(KEY_ACCOUNT_ID, lastLocation.getAccountID());
            values.put(KEY_DEVICE_ID, lastLocation.getDeviceID());
            values.put(KEY_UNIX_TIME, lastLocation.getUnixTime());
            values.put(KEY_TIMESTAMP, lastLocation.getTIMESTAMP());
            values.put(KEY_STATUS_CODE, lastLocation.getStatusCode());
            values.put(KEY_TEMPERATURE, lastLocation.getTemperature());
            values.put(KEY_INPUT_MASK, lastLocation.getInputMask());
            values.put(KEY_LATITUDE, lastLocation.getLatitude());
            values.put(KEY_LONGITUDE, lastLocation.getLongitude());
            values.put(KEY_SPEED_KPH, lastLocation.getSpeedKPH());
            values.put(KEY_ADDRESS, lastLocation.getAddress());
            values.put(KEY_HEADING, lastLocation.getHeading());
            values.put(KEY_DISPLAY_NAME, lastLocation.getDisplayName());
            values.put(KEY_DESCRIPTION, lastLocation.getDescription());
            values.put(KEY_VEHICLE_TYPE, lastLocation.getVehicleType());
            values.put(KEY_EQUIPMENT_TYPE, lastLocation.getEquipmentType());
            values.put(KEY_VOLTAGE_AT_EMPTY, lastLocation.getVoltageAtEmpty());
            values.put(KEY_FUEL_PER_VOLT, lastLocation.getFuelPerVolt());
            values.put(KEY_FUEL_VOLTAGE_DIRECTION, lastLocation.getFuelVoltageDirection());
            values.put(KEY_MAX_TANK_CAPACITY, lastLocation.getMaxTankCapicity());
            values.put(KEY_VOLTAGE_PATTERN, lastLocation.getVoltagePattern());
            values.put(KEY_SIM_PHONE_NUMBER, lastLocation.getSimPhoneNumber());
            values.put(KEY_DEVICE_TYPE, lastLocation.getDeviceType());
            values.put(KEY_CURRENT_TIME, lastLocation.getCurrentTime());
            values.put(KEY_STATUS_SINCE, lastLocation.getStatusSince());
            values.put(KEY_DRIVER_NAME, lastLocation.getDriverName());
            values.put(KEY_DRIVER_NUMBER, lastLocation.getDriverNumber());

            long id = db.insert(TABLE_VEHICLE_LIST, null, values);
            Log.e(TAG, "DH:storeVehicleListData:id:" + id);
        }
    }

    synchronized public List<LastLocation> getVehicleListData() {
        Log.e(TAG, "DH:getVehicleListData:start");
        SQLiteDatabase db = this.getReadableDatabase();
        List<LastLocation> lastLocationList = new ArrayList<LastLocation>() {
        };

        String selectQuery = "SELECT * FROM " + TABLE_VEHICLE_LIST;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                LastLocation lastLocation = new LastLocation();

                lastLocation.setAccountID(c.getString(c.getColumnIndex(KEY_ACCOUNT_ID)));
                lastLocation.setDeviceID(c.getString(c.getColumnIndex(KEY_DEVICE_ID)));
                lastLocation.setUnixTime(c.getString(c.getColumnIndex(KEY_UNIX_TIME)));
                lastLocation.setTIMESTAMP(c.getString(c.getColumnIndex(KEY_TIMESTAMP)));
                lastLocation.setStatusCode(c.getString(c.getColumnIndex(KEY_STATUS_CODE)));
                lastLocation.setTemperature(c.getString(c.getColumnIndex(KEY_TEMPERATURE)));
                lastLocation.setInputMask(c.getString(c.getColumnIndex(KEY_INPUT_MASK)));
                lastLocation.setLatitude(c.getString(c.getColumnIndex(KEY_LATITUDE)));
                lastLocation.setLongitude(c.getString(c.getColumnIndex(KEY_LONGITUDE)));
                lastLocation.setSpeedKPH(c.getString(c.getColumnIndex(KEY_SPEED_KPH)));
                lastLocation.setAddress(c.getString(c.getColumnIndex(KEY_ADDRESS)));
                lastLocation.setHeading(c.getString(c.getColumnIndex(KEY_HEADING)));
                lastLocation.setDisplayName(c.getString(c.getColumnIndex(KEY_DISPLAY_NAME)));
                lastLocation.setDescription(c.getString(c.getColumnIndex(KEY_DESCRIPTION)));
                lastLocation.setVehicleType(c.getString(c.getColumnIndex(KEY_VEHICLE_TYPE)));
                lastLocation.setEquipmentType(c.getString(c.getColumnIndex(KEY_EQUIPMENT_TYPE)));
                lastLocation.setVoltageAtEmpty(c.getString(c.getColumnIndex(KEY_VOLTAGE_AT_EMPTY)));
                lastLocation.setFuelPerVolt(c.getString(c.getColumnIndex(KEY_FUEL_PER_VOLT)));
                lastLocation.setFuelVoltageDirection(c.getString(c.getColumnIndex(KEY_FUEL_VOLTAGE_DIRECTION)));
                lastLocation.setMaxTankCapicity(c.getString(c.getColumnIndex(KEY_MAX_TANK_CAPACITY)));
                lastLocation.setVoltagePattern(c.getString(c.getColumnIndex(KEY_VOLTAGE_PATTERN)));
                lastLocation.setSimPhoneNumber(c.getString(c.getColumnIndex(KEY_SIM_PHONE_NUMBER)));
                lastLocation.setDeviceType(c.getString(c.getColumnIndex(KEY_DEVICE_TYPE)));
                lastLocation.setCurrentTime(c.getString(c.getColumnIndex(KEY_CURRENT_TIME)));
                lastLocation.setStatusSince(c.getString(c.getColumnIndex(KEY_STATUS_SINCE)));
                lastLocation.setDriverName(c.getString(c.getColumnIndex(KEY_DRIVER_NAME)));
                lastLocation.setDriverNumber(c.getString(c.getColumnIndex(KEY_DRIVER_NUMBER)));

                lastLocationList.add(lastLocation);
            } while (c.moveToNext());
        }


        return lastLocationList;
    }

    synchronized public void clearVehicleList() {
        if (isVehicleListDataInDB()) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_VEHICLE_LIST, null, null);
            Log.e(TAG, "DH:clearVehicleList:tableClearedSuccessfully");
        }
    }

    synchronized public boolean isVehicleListDataInDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_VEHICLE_LIST;
        Cursor c = db.rawQuery(selectQuery, null);
        Log.e(TAG, "DH:isVehicleListDataInDB:countInDb:" + c.getCount());
        if (c.getCount() > 0)
            return true;
        return false;
    }
}
