package com.lobemusonda.schoolbustracker_driver;

/**
 * Created by lobemusonda on 10/22/18.
 */

public class ChildLocation {
    private String parentID;
    private BusStation pickUp, dropOff;

    public ChildLocation() {

    }

    public ChildLocation(String parentID, BusStation pickUp, BusStation dropOff) {
        this.parentID = parentID;
        this.pickUp = pickUp;
        this.dropOff = dropOff;
    }

    public String getParentID() {
        return parentID;
    }

    public BusStation getPickUp() {
        return pickUp;
    }

    public BusStation getDropOff() {
        return dropOff;
    }
}
