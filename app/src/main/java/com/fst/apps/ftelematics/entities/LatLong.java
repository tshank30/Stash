package com.fst.apps.ftelematics.entities;

public class LatLong {
    String latitude;

    public LatLong(String latitude, String longitude,int id)
    {
        this.latitude=latitude;
        this.longitude=longitude;
        this.id=id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    String longitude;
    int id;
}
