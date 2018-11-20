package com.lobemusonda.schoolbustracker_driver;
/**
 * Created by lobemusonda on 9/8/18.
 */

public class Child {
    private String childId, firstName, lastName, driverID, status;

    public Child() {

    }

    public Child(String childId, String firstName, String lastName, String driverID, String status) {
        this.childId = childId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.driverID = driverID;
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChildId() {
        return childId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDriverID() {
        return driverID;
    }

    public String getStatus() {
        return status;
    }
}
