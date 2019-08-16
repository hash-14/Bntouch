package com.example.bntouch.dbaccess;

public class ContactsDB {
    private String status;

    public ContactsDB() {}

    public ContactsDB(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
