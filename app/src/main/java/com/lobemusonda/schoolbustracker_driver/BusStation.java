package com.lobemusonda.schoolbustracker_driver;

public class BusStation {
    private String name;
    private double latitude, longitude;

    public BusStation() {
        this.name = "";
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
