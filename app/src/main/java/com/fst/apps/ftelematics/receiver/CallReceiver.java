package com.fst.apps.ftelematics.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.fst.apps.ftelematics.utils.SharedPreferencesManager;

import java.util.Date;

/**
 * Created by shashanktiwari on 10/17/16.
 */

public class CallReceiver extends BroadcastReceiver {

    //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;  //because the passed incoming is only valid in ringing
    private SharedPreferencesManager sharedPrefs;
    private static boolean speech_bool=true;

    @Override
    public void onReceive(Context context, Intent intent) {

        sharedPrefs=new SharedPreferencesManager(context);
        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
            Log.e("Call","outgoing");
        }
        else{
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;
            if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                state = TelephonyManager.CALL_STATE_IDLE;
                Log.e("Call","idle");
                if(!speech_bool) {
                    speech_bool=true;
                    sharedPrefs.setSpeechMode(speech_bool);
                }


            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                state = TelephonyManager.CALL_STATE_OFFHOOK;
                Log.e("Call","offhook");
                if(sharedPrefs.getSpeechMode() && speech_bool) {
                    speech_bool = false;
                    sharedPrefs.setSpeechMode(speech_bool);
                }

            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                state = TelephonyManager.CALL_STATE_RINGING;
                Log.e("Call","ringing");
            }


            //onCallStateChanged(context, state, number);
        }
    }


}