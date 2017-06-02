package com.fst.apps.ftelematics.entities;

/**
 * Created by welcome on 2/4/2016.
 */
public class HistoryData {

    private String Sno;
    private String Timestamp;
    private String PushpinPath;
    private String StatusCode;
    private String Latitude;
    private String Longitude;
    private String VehicleNumber;
    private String SpeedKph;
    private String Heading;
    private String Address;
    private String StayedDuration;
    private String DistanceCovered;
    private String StatusCodeString;
    private String TimestampString;

    public String getSno() {
        return Sno;
    }

    public void setSno(String sno) {
        Sno = sno;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }

    public String getPushpinPath() {
        return PushpinPath;
    }

    public void setPushpinPath(String pushpinPath) {
        PushpinPath = pushpinPath;
    }

    public String getStatusCode() {
        return StatusCode;
    }

    public void setStatusCode(String statusCode) {
        StatusCode = statusCode;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getVehicleNumber() {
        return VehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        VehicleNumber = vehicleNumber;
    }

    public String getSpeedKph() {
        return SpeedKph;
    }

    public void setSpeedKph(String speedKph) {
        SpeedKph = speedKph;
    }

    public String getHeading() {
        return Heading;
    }

    public void setHeading(String heading) {
        Heading = heading;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getStayedDuration() {
        return StayedDuration;
    }

    public void setStayedDuration(String stayedDuration) {
        StayedDuration = stayedDuration;
    }

    public String getDistanceCovered() {
        return DistanceCovered;
    }

    public void setDistanceCovered(String distanceCovered) {
        DistanceCovered = distanceCovered;
    }

    public String getStatusCodeString() {
        return StatusCodeString;
    }

    public void setStatusCodeString(String statusCodeString) {
        StatusCodeString = statusCodeString;
    }

    public String getTimestampString() {
        return TimestampString;
    }

    public void setTimestampString(String timestampString) {
        TimestampString = timestampString;
    }
}
