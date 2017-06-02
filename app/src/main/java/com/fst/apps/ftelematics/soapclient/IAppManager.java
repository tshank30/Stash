package com.fst.apps.ftelematics.soapclient;

import java.util.Hashtable;

public interface IAppManager {
	public void exit();
	public String getTotalDistance(String accountID, String deviceID);
	public String getHistoryData(Hashtable< String , Object> param);
}
