package com.fst.apps.ftelematics.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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

public class RefreshParkingServiceOreo extends Service {

    DatabaseHelper dbHelper;
    public final String NOTIFICATION_CHANNEL_ID = "10001";
    public final String NOTIFICATION_CHANNEL_ID_OREO = "10002";
    private int NOTIFICATION_ID_FOREGROUND_SERVICE = 101010;
    private NotificationManager mNotificationManager;
    private NotificationManagerCompat notificationManager;
    NetworkUtility networkUtility;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        networkUtility = new NetworkUtility();

        dbHelper = new DatabaseHelper(this);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        intent.putExtra("ListFragment", true);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager = NotificationManagerCompat.from(this);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Parking")
                .setTicker("Parking Service")
                .setContentText("Service Running")
                .setSmallIcon(R.mipmap.parking_sign)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);

            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert notificationManager != null;
            notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID_OREO);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }


        Notification notification = notificationBuilder.build();
        mNotificationManager.notify(NOTIFICATION_ID_FOREGROUND_SERVICE, notificationBuilder.build());


        startForeground(NOTIFICATION_ID_FOREGROUND_SERVICE,
                notification);

        new ParkingTask().execute();


        return START_STICKY;
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
        dist = dist * 60 * 1.1515 * 1000;

        Log.e("Distance",""+dist);
        //Toast.makeText(this,""+dist,Toast.LENGTH_LONG).show();
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    class ParkingTask extends AsyncTask <Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                HashMap<String, LatLong> parkingList = dbHelper.getParkingMap();
                if (parkingList.size() > 0) {
                    AppUtils appUtils = new AppUtils(RefreshParkingServiceOreo.this);
                    String url = appUtils.getLastLocationUrl();
                    String response = networkUtility.sendGet(url);
                    List<LastLocation> lastLocationList = new Gson().fromJson(response, new TypeToken<List<LastLocation>>() {
                    }.getType());


                    if (lastLocationList != null && lastLocationList.size() > 0) {
                        for (LastLocation lastLocation : lastLocationList) {
                            LatLong fromLatLong = parkingList.get(lastLocation.getDeviceID());

                            if (fromLatLong != null && distance(Double.parseDouble(fromLatLong.getLatitude()), Double.parseDouble(fromLatLong.getLongitude()), Double.parseDouble(lastLocation.getLatitude()), Double.parseDouble(fromLatLong.getLongitude())) > 100f) {
                                dbHelper.deleteParking(lastLocation.getDeviceID());

                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(RefreshParkingServiceOreo.this)
                                        .setSmallIcon(R.mipmap.parking_sign)
                                        .setContentTitle(lastLocation.getDisplayName())
                                        .setContentText("Vehicle is out of its Parking Geofence.")
                                        .setAutoCancel(true)
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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
                    PendingIntent pendingIntentCancel = PendingIntent.getService(RefreshParkingServiceOreo.this, 196, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    alarmManager.cancel(pendingIntentCancel);

                    return "stop";

                }

            }
            catch (Exception e)
            {
                e.printStackTrace();

            }
            return "";
        }



        @Override
        protected void onPostExecute(String s) {

            if(s.equalsIgnoreCase("stop"))
            {
                //mNotificationManager.cancel(NOTIFICATION_ID_FOREGROUND_SERVICE);
                stopForeground(true);
                stopSelf();
            }

        }
    }


}
