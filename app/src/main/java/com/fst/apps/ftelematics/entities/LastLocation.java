package com.fst.apps.ftelematics.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class LastLocation implements Parcelable{

	private String accountID;
	private String deviceID;
	private String unixTime;
	private String TIMESTAMP;
	private String statusCode;
	private String temperature;
	private String inputMask;
	private String latitude;
	private String longitude;
	private String speedKPH;
	private String address;
	private String heading;
	private String displayName;
	private String description;
	private String vehicleType;
	private String equipmentType;
	private String voltageAtEmpty;
	private String fuelPerVolt;
	private String fuelVoltageDirection;
	private String maxTankCapicity;
	private String voltagePattern;
	private String simPhoneNumber;
	private String deviceType;
	private String StringTimestamp;
	private String statusSince;
	private String currentTime;
	private String signal;
	private String battery;
	private String driverNumber;
	private String driverName;

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getDriverName() {

		return driverName;
	}

	public void setDriverNumber(String driverNumber) {

		this.driverNumber = driverNumber;
	}

	public String getDriverNumber() {

		return driverNumber;
	}

	public String getAccountID() {
		return accountID;
	}
	public void setAccountID(String accountID) {
		this.accountID = accountID;
	}
	public String getDeviceID() {
		return deviceID;
	}
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	public String getUnixTime() {
		return unixTime;
	}
	public void setUnixTime(String unixTime) {
		this.unixTime = unixTime;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public String getTemperature() {
		return temperature;
	}
	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}
	public String getInputMask() {
		return inputMask;
	}
	public void setInputMask(String inputMask) {
		this.inputMask = inputMask;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getSpeedKPH() {
		return speedKPH;
	}
	public void setSpeedKPH(String speedKPH) {
		this.speedKPH = speedKPH;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getHeading() {
		return heading;
	}
	public void setHeading(String heading) {
		this.heading = heading;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getVehicleType() {
		return vehicleType;
	}
	public void setVehicleType(String vehicleType) {
		this.vehicleType = vehicleType;
	}
	public String getEquipmentType() {
		return equipmentType;
	}
	public void setEquipmentType(String equipmentType) {
		this.equipmentType = equipmentType;
	}
	public String getVoltageAtEmpty() {
		return voltageAtEmpty;
	}
	public void setVoltageAtEmpty(String voltageAtEmpty) {
		this.voltageAtEmpty = voltageAtEmpty;
	}
	public String getFuelPerVolt() {
		return fuelPerVolt;
	}
	public void setFuelPerVolt(String fuelPerVolt) {
		this.fuelPerVolt = fuelPerVolt;
	}
	public String getFuelVoltageDirection() {
		return fuelVoltageDirection;
	}
	public void setFuelVoltageDirection(String fuelVoltageDirection) {
		this.fuelVoltageDirection = fuelVoltageDirection;
	}
	public String getMaxTankCapicity() {
		return maxTankCapicity;
	}
	public void setMaxTankCapicity(String maxTankCapicity) {
		this.maxTankCapicity = maxTankCapicity;
	}
	public String getVoltagePattern() {
		return voltagePattern;
	}
	public void setVoltagePattern(String voltagePattern) {
		this.voltagePattern = voltagePattern;
	}
	public String getSimPhoneNumber() {
		return simPhoneNumber;
	}
	public void setSimPhoneNumber(String simPhoneNumber) {
		this.simPhoneNumber = simPhoneNumber;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	
	public String getTIMESTAMP() {
		return TIMESTAMP;
	}
	public void setTIMESTAMP(String tIMESTAMP) {
		TIMESTAMP = tIMESTAMP;
	}


	public String getStringTimestamp() {
		return StringTimestamp;
	}
	public void setStringTimestamp(String stringTimestamp) {
		StringTimestamp = stringTimestamp;
	}

	public String getStatusSince() {
		return statusSince;
	}

	public void setStatusSince(String statusSince) {
		this.statusSince = statusSince;
	}

	public String getCurrentTime() {
		return currentTime;
	}

	public String getSignal() {
		return signal;
	}

	public void setSignal(String signal) {
		this.signal = signal;
	}

	public String getBattery() {
		return battery;
	}

	public void setBattery(String battery) {
		this.battery = battery;
	}

	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}

	public static final Creator<LastLocation> CREATOR
	= new Creator<LastLocation>() {
		public LastLocation createFromParcel(Parcel in) {
			return new LastLocation(in);
		}

		public LastLocation[] newArray(int size) {
			return new LastLocation[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	public LastLocation(){}
	
	private LastLocation(Parcel in) {
        accountID = in.readString();
        deviceID = in.readString();
        speedKPH = in.readString();
        statusCode = in.readString();
        latitude = in.readString();
        longitude = in.readString();
        displayName = in.readString();
        TIMESTAMP=in.readString();
        StringTimestamp=in.readString();
		statusSince=in.readString();
		currentTime=in.readString();
		signal=in.readString();
		battery=in.readString();
    }
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(accountID);
		dest.writeString(deviceID);
		dest.writeString(speedKPH);
		dest.writeString(statusCode);
		dest.writeString(latitude);
		dest.writeString(longitude);
		dest.writeString(displayName);
		dest.writeString(TIMESTAMP);
		dest.writeString(StringTimestamp);
		dest.writeString(statusSince);
		dest.writeString(currentTime);
		dest.writeString(signal);
		dest.writeString(battery);
	}

}
