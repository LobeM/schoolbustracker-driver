package com.lobemusonda.schoolbustracker_driver;

/**
 * Created by lobemusonda on 8/29/18.
 */

public class User {
    public String name, email, busNo, type, status;
    public double latitude, longitude;

    public User() {

    }

    public User(String busNo) {
        this.busNo = busNo;
        this.type = "driver";
        this.status = "offline";
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public User (double latitude, double longitude, String status) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }

    public User(String name, String email, String busNo) {
        this.name = name;
        this.email = email;
        this.busNo = busNo;
    }
}
