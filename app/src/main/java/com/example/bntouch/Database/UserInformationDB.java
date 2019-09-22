package com.example.bntouch.Database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "UserInformationDB")
public class UserInformationDB extends Model {
    @Column(name = "username")
    public String username;
    @Column(name = "email")
    public String email;
    @Column(name = "uid")
    public String uid;
    @Column(name = "isloggedin")
    public boolean isloggedin;

    @Override
    public String toString() {
        return "UserInformationDB{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", uid='" + uid + '\'' +
                ", isloggedin='" + isloggedin + '\'' +
                '}';
    }
}
