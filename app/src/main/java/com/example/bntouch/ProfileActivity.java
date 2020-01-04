package com.example.bntouch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.bntouch.functionshelper.ChatRequestsHelper;
import com.example.bntouch.functionshelper.StaticVariales;
import com.example.bntouch.interfaces.ChatRequestsInterface;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {


    ChatRequestsHelper chatRequestsHelper;
    private String receiver_user_id, sender_user_id, current_state;

    private CircleImageView visit_profile_image;
    private TextView visit_profile_user_name, visit_profile_status;
    private Button send_message_request_button, decline_message_request_button;

    private DatabaseReference userRef, contactsRef, notificationRef;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiver_user_id = getIntent().getStringExtra("visit_user_id");
        InittializeFields();
        LoadVisitedUserInfo(receiver_user_id);
        ManageChatRequests();
    }

    private void InittializeFields(){
        chatRequestsHelper = new ChatRequestsHelper(getResources().getString(R.string.request_type));
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        sender_user_id = mAuth.getCurrentUser().getUid();
        visit_profile_image = (CircleImageView)findViewById(R.id.visit_profile_image);
        visit_profile_user_name = (TextView)findViewById(R.id.visit_profile_user_name);
        visit_profile_status = (TextView)findViewById(R.id.visit_profile_status);
        send_message_request_button = (Button)findViewById(R.id.send_message_request_button);
        decline_message_request_button = (Button)findViewById(R.id.decline_message_request_button);
        current_state = StaticVariales.CURRENT_STATE_NEW;
    }

    private void LoadVisitedUserInfo(String visit_user_id){
        userRef.child(visit_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    if(dataSnapshot.hasChild("image")) {
                        String userImage = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(visit_profile_image);
                    }

                    String userName=  dataSnapshot.child("name").getValue().toString();
                    String userStatus =  dataSnapshot.child("status").getValue().toString();

                    visit_profile_user_name.setText(userName);
                    visit_profile_status.setText(userStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequests() {
        chatRequestsHelper.getConnectionHandler().getDatabaseReference().child(sender_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild(receiver_user_id)){
                    String request_type = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();
                    switch (request_type){
                        case StaticVariales.RECEIVED:
                            current_state = StaticVariales.CURRENT_STATE_NEW;
                            send_message_request_button.setText("Accept Chat Request");
                            decline_message_request_button.setVisibility(View.VISIBLE);
                            decline_message_request_button.setEnabled(true);
                            decline_message_request_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CancelChatRequest();
                                }
                            });
                            break;
                        case StaticVariales.SENT:
                            current_state = StaticVariales.CURRENT_STATE_REQUEST_SENT;
                            send_message_request_button.setText("Cancel Chat Request");
                            break;
                        default:
                            break;
                    }
                } else {
                    contactsRef.child(sender_user_id)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(receiver_user_id)){
                                            current_state = StaticVariales.CURRENT_STATE_FRIENDS;
                                            send_message_request_button.setText("Unfriend");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(!sender_user_id.equals(receiver_user_id)) {
            send_message_request_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    send_message_request_button.setEnabled(false);
                    if(current_state.equals(StaticVariales.CURRENT_STATE_NEW)) {
                        SendChatRequest();
                    }
                    else if(current_state.equals(StaticVariales.CURRENT_STATE_REQUEST_SENT)) {
                        CancelChatRequest();
                    }
                    else if(current_state.equals(StaticVariales.CURRENT_STATE_REQUEST_RECEIVED)){
                        AcceptChatRequest();
                    }
                    else if(current_state.equals(StaticVariales.CURRENT_STATE_FRIENDS)){
                        RemoveSpecificContact();
                    }
                }
            });
        } else {
            send_message_request_button.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact() {
        contactsRef.child(sender_user_id).child(receiver_user_id)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactsRef.child(receiver_user_id).child(sender_user_id)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                send_message_request_button.setEnabled(true);
                                                current_state="new";
                                                send_message_request_button.setText("Send Message");
                                            }
                                        }
                                    });
                        }
                    }
                });
        decline_message_request_button.setVisibility(View.INVISIBLE);
        decline_message_request_button.setEnabled(false);
    }

    private void AcceptChatRequest() {
        contactsRef.child(sender_user_id).child(receiver_user_id)
                                         .child("Contacts").setValue("Saved")
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {
                                                        contactsRef.child(receiver_user_id).child(sender_user_id)
                                                                .child("Contacts").setValue("Saved")
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()) {
                                                                                chatRequestsHelper.deleteChatRequest(sender_user_id, receiver_user_id, new ChatRequestsInterface() {
                                                                                    @Override
                                                                                    public void onSuccess() {
                                                                                        send_message_request_button.setEnabled(true);
                                                                                        current_state = "friends";
                                                                                        send_message_request_button.setText("Unfriend");

                                                                                        decline_message_request_button.setVisibility(View.INVISIBLE);
                                                                                        decline_message_request_button.setEnabled(false);
                                                                                    }
                                                                                });
                                                                                chatRequestsHelper.deleteChatRequest(sender_user_id, receiver_user_id, null);
                                                                            }
                                                                        }
                                                                    });
                                                    }
                                                }
                                            });
    }

    private void CancelChatRequest() {
        chatRequestsHelper.deleteChatRequest(sender_user_id, receiver_user_id, new ChatRequestsInterface() {
            @Override
            public void onSuccess() {
                chatRequestsHelper.deleteChatRequest(receiver_user_id, sender_user_id, new ChatRequestsInterface() {
                    @Override
                    public void onSuccess() {
                        send_message_request_button.setEnabled(true);
                        current_state="new";
                        send_message_request_button.setText("Send Message");
                    }
                });
            }
        });
        decline_message_request_button.setVisibility(View.INVISIBLE);
        decline_message_request_button.setEnabled(false);
    }

    private void SendChatRequest() {
        chatRequestsHelper.addChatRequest(sender_user_id, receiver_user_id, StaticVariales.SENT, new ChatRequestsInterface() {
            @Override
            public void onSuccess() {
                send_message_request_button.setText("Cancel Chat Request");
                chatRequestsHelper.addChatRequest(receiver_user_id, sender_user_id, StaticVariales.RECEIVED, new ChatRequestsInterface() {
                    @Override
                    public void onSuccess() {
                        HashMap<String, String> chatNotificationMap = new HashMap<>();
                        chatNotificationMap.put("from", sender_user_id);
                        chatNotificationMap.put("type", "request");
                        notificationRef.child(receiver_user_id).push()
                                .setValue(chatNotificationMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            send_message_request_button.setEnabled(true);
                                            current_state = "request_sent";
                                            send_message_request_button.setText("Cancel Chat Request");
                                        }
                                    }
                                });
                    };
                });
            }
        });

    }
}

/*  */
