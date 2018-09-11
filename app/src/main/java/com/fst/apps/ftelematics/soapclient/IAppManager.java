package com.fst.apps.ftelematics.soapclient;

import java.util.Hashtable;

public interface IAppManager {
	public void exit();
	public String getTotalDistance(String accountID, String deviceID);
	public String getFuel(String accountID, String deviceID, String calibrationValue, String maxTankCapacity);
	public String setParking(String accountID, String deviceID, String status, String lat,String longitude,String address, String vehNo);
	public String disableParking(String accountID, String deviceID);
	public String getHistoryData(Hashtable< String , Object> param);
	public String setLiveUrlParams(String accountID, String deviceID, String currentTime, String activationTime);
}
