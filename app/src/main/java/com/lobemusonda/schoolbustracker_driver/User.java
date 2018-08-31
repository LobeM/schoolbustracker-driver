package com.lobemusonda.schoolbustracker_driver;

/**
 * Created by lobemusonda on 8/29/18.
 */

public class User {
    public String name, email, busNo, type;

    public User() {

    }

    public User(String busNo) {
        this.busNo = busNo;
        this.type = "driver";
    }

    public User(String name, String email, String busNo) {
        this.name = name;
        this.email = email;
        this.busNo = busNo;
    }
}
