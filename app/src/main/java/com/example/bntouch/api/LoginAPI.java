package com.example.bntouch.api;

public class LoginAPI {
    private String email, password;

    public LoginAPI(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

}
