package com.moonactive.assignment.dao;

import java.sql.Timestamp;

public class LicensePlate {
    String plateID;
    String type;
    Timestamp timeStamp;

    public void setId(String id) {
        this.plateID = id;
    }

    public String getId() {
        return plateID;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }
}
