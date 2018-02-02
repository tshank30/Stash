package com.fst.apps.ftelematics.events;

public class AlertEvent {
	
	private String alertType;
	private String alertSubType;
	private String alertText;
	private String createdAt;
	public String getAlertType() {
		return alertType;
	}
	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}
	public String getAlertSubType() {
		return alertSubType;
	}
	public void setAlertSubType(String alertSubType) {
		this.alertSubType = alertSubType;
	}
	public String getAlertText() {
		return alertText;
	}
	public void setAlertText(String alertText) {
		this.alertText = alertText;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	
	
}
