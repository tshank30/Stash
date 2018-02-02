package com.fst.apps.ftelematics.entities;

public class Alerts {
	
	private int id;
	private int alertType;
	private String alertText;
	private String alertSubType;
	private String createdAt;
	private String userId;
	private String vehicleNumber;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getAlertType() {
		return alertType;
	}
	public void setAlertType(int alertType) {
		this.alertType = alertType;
	}
	public String getAlertText() {
		return alertText;
	}
	public void setAlertText(String alertText) {
		this.alertText = alertText;
	}
	public String getAlertSubType() {
		return alertSubType;
	}
	public void setAlertSubType(String alertSubType) {
		this.alertSubType = alertSubType;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getVehicleNumber() {
		return vehicleNumber;
	}
	public void setVehicleNumber(String vehicleNumber) {
		this.vehicleNumber = vehicleNumber;
	}
	
	
}
