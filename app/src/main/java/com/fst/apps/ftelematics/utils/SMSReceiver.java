package com.fst.apps.ftelematics.utils;

import com.fst.apps.ftelematics.AppConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(AppConstants.ACTION_SMS_SENT))
		{
			switch (getResultCode())
			{
			case -1: //Activity.RESULT_OK
			Toast.makeText(context, "Message Sent", Toast.LENGTH_SHORT).show();
			break;

			case SmsManager.RESULT_ERROR_NO_SERVICE:
				break;

			case SmsManager.RESULT_ERROR_NULL_PDU:
				break;

			case SmsManager.RESULT_ERROR_RADIO_OFF:
				break;

			case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				break;

			default:

			}
		}
		else if (action.equals(AppConstants.ACTION_SMS_DELIVERED))
		{
			switch (getResultCode())
			{
			case -1: //Activity.RESULT_OK
				Toast.makeText(context, "Message Delivered", Toast.LENGTH_SHORT).show();
				break;

			case 0: //Activity.RESULT_CANCELED
				break;

			default:
			}
		}
	}
}
