package com.example.bntouch;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import androidx.annotation.Nullable;

public class ClosingService extends Service {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
        updateUserStatus("offline");
    }

    private void updateUserStatus(String state){
        String currentUserID;
        FirebaseAuth mAuth;
        DatabaseReference rootRef;

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStatMap =  new HashMap<>();
        onlineStatMap.put("time", saveCurrentTime);
        onlineStatMap.put("date", saveCurrentDate);
        onlineStatMap.put("state", state);
        //currentUserID = mAuth.getCurrentUser().getUid();
        /*rootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStatMap);*/

    }
}
