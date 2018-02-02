package com.fst.apps.ftelematics.soapclient;

import java.util.Hashtable;

public interface ISocketOperator 
{
	public String sendHttpRequest(String method, Hashtable<String, Object> param, String resultNode);
	public int startListening(int port);
	public void stopListening();
	public void exit();
	public int getListeningPort();
}
