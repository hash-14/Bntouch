package com.example.bntouch.functionshelper;

import com.example.bntouch.dbaccess.UsersDB;
import com.example.bntouch.dbconnections.ConnectionHandler;
import com.example.bntouch.interfaces.ChatRequestsInterface;
import com.example.bntouch.interfaces.UsersInterface;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;

public class UsersHelper extends UsersDB {

    private ConnectionHandler connectionHandler;

    public UsersHelper(String deviceid, String name, String status, String uid) {
        super(deviceid, name, status, uid);
        this.connectionHandler = new ConnectionHandler("Users");
    }

    public void addUser(String userid, final ChatRequestsInterface callback){
        try {
            this.getDeviceID(new UsersInterface() {
                @Override
                public void callBack(UsersHelper usersHelper) {
                    usersHelper = UsersHelper.this;
                    usersHelper.connectionHandler.getDatabaseReference();
                }
            });
        }

        catch (Exception ex) {

        }

        finally {

        }
    }

    private void getDeviceID(final UsersInterface usersInterface){
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                UsersHelper.this.setDeviceid(task.getResult().getToken());
                usersInterface.callBack(UsersHelper.this);
            }
        });
    }
}
