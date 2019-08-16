package com.example.bntouch.dbaccess;

public class GroupsDB {
    private String groupName, date, message, name, time;

    public GroupsDB() {}

    public GroupsDB(String groupName, String date, String message, String name, String time) {
        this.groupName = groupName;
        this.date = date;
        this.message = message;
        this.name = name;
        this.time = time;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
