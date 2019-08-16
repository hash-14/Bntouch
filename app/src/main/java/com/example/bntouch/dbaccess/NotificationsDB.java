package com.example.bntouch.dbaccess;

public class NotificationsDB {
    private String from, type;

    public NotificationsDB(){}

    public NotificationsDB(String from, String type) {
        this.from = from;
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
