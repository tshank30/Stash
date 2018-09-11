package com.fst.apps.ftelematics.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

	Context mContext;

	SharedPreferences settings;
	SharedPreferences.Editor editor;

	public SharedPreferencesManager(Context mContext) {

		settings = mContext.getSharedPreferences("rottweiler_sharedpreferences",0);
		editor = settings.edit();

	}

	public String getAccountId() {
		return settings.getString("accoundId", null);
	}

	public void setAccountId(String accoundId) {
		editor.putString("accoundId", accoundId);
		editor.commit();
	}

	public String getUserId() {
		return settings.getString("userId", null);
	}

	public void setUserId(String accoundId) {
		editor.putString("userId", accoundId);
		editor.commit();
	}

	public void setSchoolAccount(boolean schoolAccount) {
		editor.putBoolean("schoolAccount", schoolAccount);
		editor.commit();
	}

	public boolean getSchoolAccount() {
		return settings.getBoolean("schoolAccount", false);
	}

	public String getRole() {
		return settings.getString("role", null);
	}

	public void setRole(String role) {
		editor.putString("role", role);
		editor.commit();
	}

	public String getIsMobAppUser() {
		return settings.getString("isMobAppUser", null);
	}

	public void setIsMobAppUser(String accoundId) {
		editor.putString("isMobAppUser", accoundId);
		editor.commit();
	}

	public String getContactName() {
		return settings.getString("contactName", null);
	}

	public void setContactName(String accoundId) {
		editor.putString("contactName", accoundId);
		editor.commit();
	}

	public String getDealerName() {
		return settings.getString("dealerName", null);
	}

	public void setDealerName(String dealerName) {
		editor.putString("dealerName", dealerName);
		editor.commit();
	}

	public void setIsLoggedIn(boolean flag) {
		editor.putBoolean("isloggedIn", flag);
		editor.commit();
	}
	
	public boolean getRegisteredForPush(){
		return settings.getBoolean("isRegisteredForPush",false);
	}

	public void setRegisteredForPush(boolean flag) {
		editor.putBoolean("isRegisteredForPush", flag);
		editor.commit();
	}

	public boolean getIsLoggedIn(){
		return settings.getBoolean("isloggedIn",false);
	}


	public void setIgnitionMode(boolean flag) {
		editor.putBoolean("ignition", flag);
		editor.commit();
	}
	
	public boolean getIgnitionMode(){
		return settings.getBoolean("ignition",false);
	}
	
	public void setAutoRefresh(long time) {
		editor.putLong("autoRefresh", time*1000);
		editor.commit();
	}
	
	public long getAutoRefresh(){
		return settings.getLong("autoRefresh",0);
	}
	
	public void setAlertsMode(boolean flag) {
		editor.putBoolean("notifications", flag);
		editor.commit();
	}
	
	public boolean getAlertsMode(){
		return settings.getBoolean("notifications",true);
	}

	public void setSpeechMode(boolean flag) {
		editor.putBoolean("tts", flag);
		editor.commit();
	}

	public boolean getSpeechMode(){
		return settings.getBoolean("tts",true);
	}
	
	public void clearSharedPreferences(){
		editor.clear();
		editor.commit();
	}
	
	

}
