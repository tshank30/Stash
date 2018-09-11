package com.fst.apps.ftelematics.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;


import com.fst.apps.ftelematics.MainActivity;
import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.entities.LastLocation;
import com.fst.apps.ftelematics.entities.LatLong;
import com.fst.apps.ftelematics.restclient.NetworkUtility;
import com.fst.apps.ftelematics.utils.AppUtils;
import com.fst.apps.ftelematics.utils.DatabaseHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;

public class RefreshServiceForParking extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public RefreshServiceForParking(String name) {
        super(name);
    }

    public RefreshServiceForParking()
    {
        super("refreshParking");
    }

    DatabaseHelper dbHelper;
    public final String NOTIFICATION_CHANNEL_ID = "10001";
    private  int NOTIFICATION_ID_FOREGROUND_SERVICE = 101;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        NetworkUtility networkUtility = new NetworkUtility();
        AppUtils appUtils = new AppUtils(this);
        String url = appUtils.getLastLocationUrl();
        dbHelper = new DatabaseHelper(this);

        try {

            HashMap<String, LatLong> parkingList = dbHelper.getParkingMap();
            if (parkingList.size() > 0) {
                String response = networkUtility.sendGet(url);
                List<LastLocation> lastLocationList = new Gson().fromJson(response, new TypeToken<List<LastLocation>>() {
                }.getType());

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);



                if (lastLocationList != null && lastLocationList.size() > 0) {
                    for (LastLocation lastLocation : lastLocationList) {
                        LatLong fromLatLong = parkingList.get(lastLocation.getDeviceID());

                        if (fromLatLong!=null && distance(Double.parseDouble(fromLatLong.getLatitude()), Double.parseDouble(fromLatLong.getLongitude()), Double.parseDouble(lastLocation.getLatitude()), Double.parseDouble(fromLatLong.getLongitude())) > 100f) {
                            dbHelper.deleteParking(lastLocation.getDeviceID());
                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.mipmap.parking_sign)
                                    .setContentTitle(lastLocation.getDisplayName())
                                    .setContentText("Vehicle is out of its Parking Geofence.")
                                    .setAutoCancel(true)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                            {
                                int importance = NotificationManager.IMPORTANCE_HIGH;
                                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
                                notificationChannel.enableLights(true);
                                notificationChannel.setLightColor(Color.RED);
                                notificationChannel.enableVibration(true);
                                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                                assert notificationManager != null;
                                mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                                mNotificationManager.createNotificationChannel(notificationChannel);
                            }

                            mNotificationManager.notify(fromLatLong.getId(), mBuilder.build());


                        }
                    }
                }

            } else {
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent myIntent = new Intent(getApplicationContext(), RefreshServiceForParking.class);
                PendingIntent pendingIntent = PendingIntent.getService(this, 196, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager.cancel(pendingIntent);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515*1000;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
