package com.fst.apps.ftelematics.soapclient;

import java.util.Hashtable;
import java.util.Random;
import java.util.Timer;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;


public class IMService extends Service implements IAppManager 
{
	public ConnectivityManager conManager = null; 
	ISocketOperator socketOperator = new WebserviceCall(this);

	private final IBinder mBinder = new IMBinder();
	private Timer timer;
	Hashtable< String , Object> param=null;
	String result=null;
	public class IMBinder extends Binder 
	{
		public IAppManager getService() {
			return IMService.this;
		}
	}

	@Override
	public void onCreate() 
	{   	
		// Display a notification about us starting.  We put an icon in the status bar.
		//   showNotification();
		conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		// Timer is used to get info every UPDATE_TIME_PERIOD;
		timer = new Timer();   



		Thread thread = new Thread()
		{
			@Override
			public void run() {			

				Random random = new Random();
				int tryCount = 0;
				while (socketOperator.startListening(10000 + random.nextInt(20000) )  == 0 )
				{		
					tryCount++; 
					if (tryCount > 10)
					{
						// if it can't listen a port after trying 10 times, give up...
						break;
					}
				}
			}
		};		
		thread.start();
	}

	@Override
	public IBinder onBind(Intent intent) 
	{
		return mBinder;
	}



	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void exit() 
	{
		socketOperator.exit(); 
		socketOperator = null;
		this.stopSelf();
	}

	@Override
	public String getTotalDistance(String accountID,String deviceID) {
		param=new Hashtable<String, Object>();
		param.put("accountId", accountID);
		param.put("deviceId", deviceID);
		String result = socketOperator.sendHttpRequest("GetDistance", param, "GetDistanceResult");
		return result;
	}

	@Override
	public String getFuel(String accountID, String deviceID, String calibrationValue, String maxTankCapacity) {

		param=new Hashtable<String, Object>();
		param.put("accountId", accountID);
		param.put("deviceId", deviceID);
		param.put("calibrationValues", calibrationValue);
		param.put("maxTankCapacity", maxTankCapacity);
		String result = socketOperator.sendHttpRequest("GetFuel", param, "GetFuelResult");
		return result;
	}

	@Override
	public String getHistoryData(Hashtable<String, Object> param) {
		String result = socketOperator.sendHttpRequest("GetHistoryData", param, "GetHistoryDataResult");
		return result;
	}

}