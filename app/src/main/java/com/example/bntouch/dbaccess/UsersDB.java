package com.example.bntouch.dbaccess;

public class UsersDB {

    private String deviceid, name, status, uid;

    public UsersDB(){}

    public UsersDB(String deviceid, String name, String status, String uid) {
        this.deviceid = deviceid;
        this.name = name;
        this.status = status;
        this.uid = uid;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
