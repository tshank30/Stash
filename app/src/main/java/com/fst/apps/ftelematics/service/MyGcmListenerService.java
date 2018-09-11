/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fst.apps.ftelematics.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.fst.apps.ftelematics.AppConstants;
import com.fst.apps.ftelematics.MainActivity;
import com.fst.apps.ftelematics.R;
import com.fst.apps.ftelematics.entities.Alerts;
import com.fst.apps.ftelematics.utils.AlertTypes;
import com.fst.apps.ftelematics.utils.DatabaseHelper;
import com.fst.apps.ftelematics.utils.SharedPreferencesManager;
import com.fst.apps.ftelematics.utils.TtsProviderFactory;
import com.google.android.gms.gcm.GcmListenerService;


public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private static int NOTIFICATION_ID = 0;
    String message, type, time;
    TtsProviderFactory ttsProviderImpl = TtsProviderFactory.getInstance();
    public static final String NOTIFICATION_CHANNEL_ID = "10002";

    @Override
    public void onCreate() {
        super.onCreate();

    }


    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.i(TAG, "Received message!!");
        message = data.getString("message");
        type = data.getString("type");
        time = data.getString("time");
        SharedPreferencesManager sf = new SharedPreferencesManager(this);

        if (sf != null) {
            if (sf.getIsLoggedIn()) {
                if (sf.getAlertsMode()) {
                    generateNotification(this, message, type);
                }
                saveAlertInDB(message, type, time, this, sf.getSpeechMode());
            }
        }

    }
    // [END receive_message]

    private static void generateNotification(Context context, String message, String parking) {

        int icon = 0;
        if ("parking".equalsIgnoreCase(parking))
            icon = R.mipmap.parking_sign;
        else
            icon = R.drawable.ic_notification;

        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(icon)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(message)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentIntent(intent)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message));


        Notification notification = builder.build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notification.defaults |= Notification.DEFAULT_SOUND;


        notification.defaults |= Notification.DEFAULT_VIBRATE;

        if (NOTIFICATION_ID > 1073741824) {
            NOTIFICATION_ID = 0;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert notificationManager != null;
            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(NOTIFICATION_ID++, notification);

    }


    public void saveAlertInDB(String message, String type, String time, Context context, boolean isTtsEnabled) {
        DatabaseHelper db = new DatabaseHelper(context);
        SharedPreferencesManager sf = new SharedPreferencesManager(context);
        String userId = sf.getUserId();
        String alertText = message;
        String alertType = null;
        String alertSubType = null;
        StringBuilder alertSpeechText = new StringBuilder();
        if (!TextUtils.isEmpty(type)) {
            if (type.contains("#")) {
                String parts[] = type.split("#");
                alertType = parts[0];
                alertSubType = parts[1];
            } else {
                alertType = type;
            }

            if (!TextUtils.isEmpty(alertType)) {
                alertSpeechText.append(alertType);
            }

            if (!TextUtils.isEmpty(alertSubType)) {
                alertSpeechText.append(" " + alertSubType);
            }

            alertSpeechText.append(" alert received!");

            if (isTtsEnabled && ttsProviderImpl != null) {
                ttsProviderImpl.init(getApplicationContext());
                ttsProviderImpl.say(alertSpeechText.toString());
            }
        }

        Alerts alert = new Alerts();
        alert.setUserId(userId);
        alert.setAlertText(alertText);
        alert.setCreatedAt(time);

        if (!TextUtils.isEmpty(alertType)) {
            if (alertType.equalsIgnoreCase("Ignition")) {
                alert.setAlertType(AlertTypes.IGNITION);
                if (!TextUtils.isEmpty(alertSubType)) {
                    if (alertSubType.equalsIgnoreCase(AppConstants.ALERT_SUB_TYPES.ON.name())) {
                        alert.setAlertSubType(AppConstants.ALERT_SUB_TYPES.ON.name());
                    } else {
                        alert.setAlertSubType(AppConstants.ALERT_SUB_TYPES.OFF.name());
                    }
                }
            } else if (alertType.equalsIgnoreCase("Overspeed")) {
                alert.setAlertType(AlertTypes.OVERSPEED);
                alert.setAlertSubType(AppConstants.ALERT_SUB_TYPES.OVERSPEED.name());
            } else if (alertType.equalsIgnoreCase("Geofence")) {
                alert.setAlertType(AlertTypes.GEOFENCE);
                if (!TextUtils.isEmpty(alertSubType)) {
                    if (alertSubType.equalsIgnoreCase(AppConstants.ALERT_SUB_TYPES.IN.name())) {
                        alert.setAlertSubType(AppConstants.ALERT_SUB_TYPES.IN.name());
                    } else {
                        alert.setAlertSubType(AppConstants.ALERT_SUB_TYPES.OUT.name());
                    }
                }
            }

        }

        db.createAlert(alert);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
