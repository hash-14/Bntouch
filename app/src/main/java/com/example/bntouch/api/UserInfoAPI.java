package com.example.bntouch.api;

public class UserInfoAPI {
    String uid, username, profileimage;

    public UserInfoAPI(String uid, String username, String profileimage) {
        this.uid = uid;
        this.username = username;
        this.profileimage = profileimage;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileimage() {
        return profileimage;
    }
}
