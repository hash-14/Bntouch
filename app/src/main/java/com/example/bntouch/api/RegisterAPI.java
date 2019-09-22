package com.example.bntouch.api;

public class RegisterAPI {
    private String username, email, password, country, countrycode;

    public RegisterAPI(String username, String email, String password, String country, String countrycode) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.country = country;
        this.countrycode = countrycode;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getCountry() {
        return country;
    }

    public String getCountrycode() {
        return countrycode;
    }
}
