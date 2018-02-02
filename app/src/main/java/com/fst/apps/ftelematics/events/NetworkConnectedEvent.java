package com.fst.apps.ftelematics.events;

public class NetworkConnectedEvent {
	private boolean isNetworkConnected;
    
    public NetworkConnectedEvent(boolean isNetworkConnected){          
        this.isNetworkConnected = isNetworkConnected;
    }
     
    public boolean getIsNetworkConnected(){
        return this.isNetworkConnected;
    }
}
