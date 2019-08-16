package com.example.bntouch.dbaccess;

public class ChatRequestsDB {
    String requestType;

    public ChatRequestsDB(){}

    public ChatRequestsDB(String requestType) {
        this.requestType = requestType;
    }

    public String getRequest_type() {
        return requestType;
    }

    public void setRequest_type(String requestType) {
        this.requestType = requestType;
    }
}
