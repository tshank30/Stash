package com.fst.apps.ftelematics.utils;

import com.fst.apps.ftelematics.events.NetworkConnectedEvent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkChangeReceiver extends BroadcastReceiver {
	

	@Override
	public void onReceive(Context context, Intent intent) {
			NetworkConnectedEvent ne=new NetworkConnectedEvent(true);
	}

}
