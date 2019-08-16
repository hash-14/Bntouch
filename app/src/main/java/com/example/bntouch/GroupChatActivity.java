package com.example.bntouch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar group_chat_bar_layout;
    private ImageButton send_message_button;
    private EditText input_group_message;
    private TextView group_chat_text_display;
    private ScrollView group_chat_scroll_view;

    private String clickedGroupName, currentUserID, currentUserName, currentDate, currentTime;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, groupRef, groupMessageKeyRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        clickedGroupName = getIntent().getExtras().getString("groupname");
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(clickedGroupName);
        InitializeFields();

        GetUserInfo();

        send_message_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfoToDatabase();
                input_group_message.setText("");
                group_chat_scroll_view.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        groupRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields(){
        group_chat_bar_layout = (Toolbar)findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(group_chat_bar_layout);
        getSupportActionBar().setTitle(clickedGroupName);

        send_message_button = (ImageButton)findViewById(R.id.send_message_button);
        input_group_message = (EditText)findViewById(R.id.input_group_message);
        group_chat_text_display = (TextView)findViewById(R.id.group_chat_text_display);
        group_chat_scroll_view = (ScrollView)findViewById(R.id.group_chat_scroll_view);

    }


    private void GetUserInfo() {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void SaveMessageInfoToDatabase() {
        String message = input_group_message.getText().toString();
        String messageKey = groupRef.push().getKey();
        if(TextUtils.isEmpty(message)){
            Toast.makeText(GroupChatActivity.this, "Please write a message...", Toast.LENGTH_SHORT).show();
        } else{
            Calendar calendarForDate = Calendar.getInstance();
            SimpleDateFormat currenDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            currentDate = currenDateFormat.format(calendarForDate.getTime());

            Calendar calendarForTime = Calendar.getInstance();
            SimpleDateFormat currenTimeFormat = new SimpleDateFormat("hh:mm:ss a");
            currentTime = currenTimeFormat.format(calendarForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupRef.updateChildren(groupMessageKey);

            groupMessageKeyRef = groupRef.child(messageKey);
            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            groupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }


    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            group_chat_text_display.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "        " + chatDate + "\n\n\n");
            group_chat_scroll_view.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

}
