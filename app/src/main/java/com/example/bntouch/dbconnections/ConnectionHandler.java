package com.example.bntouch.dbconnections;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConnectionHandler {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String currentUserid;

    public ConnectionHandler(String databaseReference) {
        this.mAuth = FirebaseAuth.getInstance();
        this.currentUserid = this.mAuth.getCurrentUser().getUid();
        this.databaseReference = FirebaseDatabase.getInstance().getReference().child(databaseReference);
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public void setmAuth(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }

    public void setDatabaseReference(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    public String getCurrentUserid() {
        return currentUserid;
    }

    public void setCurrentUserid(String currentUserid) {
        this.currentUserid = currentUserid;
    }
}
