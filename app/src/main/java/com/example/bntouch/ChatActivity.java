package com.example.bntouch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private String messageRecieverID, messageRecieverName, messageRecieverImage, messageSenderID;
    private TextView custom_profile_name, custom_profile_last_seen;
    private CircleImageView custom_profile_image;

    private Toolbar chat_toolbar;
    private ImageButton send_chat_message_button, send_files_button;
    private EditText input_chat_message;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private RecyclerView private_messages_list_of_users;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagerAdapter messagerAdapter;

    private String saveCurrentTime, saveCurrentDate, checker="", myUrl="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageRecieverID = getIntent().getStringExtra("visit_user_id");
        messageRecieverName = getIntent().getStringExtra("visit_user_name");
        messageRecieverImage = getIntent().getStringExtra("visit_user_image");

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        custom_profile_name.setText(messageRecieverName);
        Picasso.get().load(messageRecieverImage).placeholder(R.drawable.profile_image).into(custom_profile_image);
        DisplayLastSeen();

        EventMessageAddedtoDB();

        send_chat_message_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

        send_files_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Ms Word Files"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select The File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                checker = "image";
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                                break;
                            case 1:
                                checker = "pdf";
                                Intent intentPDF = new Intent();
                                intentPDF.setAction(Intent.ACTION_GET_CONTENT);
                                intentPDF.setType("application/pdf");
                                startActivityForResult(intentPDF.createChooser(intentPDF, "Select PDF file"), 438);
                                break;
                            case 2:
                                checker = "docx";
                                Intent intentDocx = new Intent();
                                intentDocx.setAction(Intent.ACTION_GET_CONTENT);
                                intentDocx.setType("application/msword");
                                startActivityForResult(intentDocx.createChooser(intentDocx, "Select MS Word file"), 438);
                                break;
                        }
                    }
                });
                builder.show();
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 438:
                if(resultCode == RESULT_OK && data != null && data.getData() != null) {
                    loadingBar.setTitle("Sending File");
                    loadingBar.setMessage("Please wait, we are sending that file...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    fileUri = data.getData();
                    if(!checker.equals("image")) {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                        final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageRecieverID;
                        final String messageRecieverRef = "Messages/" + messageRecieverID+ "/" + messageSenderID ;

                        DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                                .child(messageSenderID).child(messageRecieverID).push();

                        final String messagePushID = userMessageKeyRef.getKey();

                        final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

                        filePath.putFile(fileUri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                                loadingBar.setMessage((int) progress + "% Uploading...");
                            }
                        }).continueWithTask(new Continuation() {
                            @Override
                            public Task<? extends Object> then(@NonNull Task task) throws Exception {
                                if(!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if(task.isSuccessful()) {
                                    Map messageTextBody = new HashMap();
                                    messageTextBody.put("message", task.getResult().toString());
                                    messageTextBody.put("name", fileUri.getLastPathSegment());
                                    messageTextBody.put("type", checker);
                                    messageTextBody.put("from", messageSenderID);
                                    messageTextBody.put("to", messageRecieverID);
                                    messageTextBody.put("messageID", messagePushID);
                                    messageTextBody.put("time", saveCurrentTime);
                                    messageTextBody.put("date", saveCurrentDate);

                                    Map messageBodyDetails = new HashMap();
                                    messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                    messageBodyDetails.put(messageRecieverRef + "/" + messagePushID, messageTextBody);

                                    rootRef.updateChildren(messageBodyDetails);
                                    loadingBar.dismiss();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if(checker.equals("image")) {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                        final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageRecieverID;
                        final String messageRecieverRef = "Messages/" + messageRecieverID+ "/" + messageSenderID ;

                        DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                                .child(messageSenderID).child(messageRecieverID).push();

                        final String messagePushID = userMessageKeyRef.getKey();

                        final StorageReference filePath = storageReference.child(messagePushID + ".jpg");
                        uploadTask = filePath.putFile(fileUri);

                        uploadTask.continueWithTask(new Continuation() {
                            @Override
                            public Object then(@NonNull Task task) throws Exception {
                                if(!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if(task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    myUrl = downloadUri.toString();
                                    Map messageTextBody = new HashMap();
                                    messageTextBody.put("message", myUrl);
                                    messageTextBody.put("name", fileUri.getLastPathSegment());
                                    messageTextBody.put("type", checker);
                                    messageTextBody.put("from", messageSenderID);
                                    messageTextBody.put("to", messageRecieverID);
                                    messageTextBody.put("messageID", messagePushID);
                                    messageTextBody.put("time", saveCurrentTime);
                                    messageTextBody.put("date", saveCurrentDate);

                                    Map messageBodyDetails = new HashMap();
                                    messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                    messageBodyDetails.put(messageRecieverRef + "/" + messagePushID, messageTextBody);

                                    rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if(task.isSuccessful()) {
                                                //Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(ChatActivity.this, "Error " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                            }
                                            loadingBar.dismiss();
                                            input_chat_message.setText("");
                                        }
                                    });
                                }
                            }
                        });

                    } else {
                        loadingBar.dismiss();
                        Toast.makeText(this, "Nothing selected, Error", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    private void InitializeFields() {
        chat_toolbar = (Toolbar)findViewById(R.id.chat_toolbar);
        setSupportActionBar(chat_toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);


        custom_profile_name = (TextView)findViewById(R.id.custom_profile_name);
        custom_profile_last_seen = (TextView)findViewById(R.id.custom_profile_last_seen);
        custom_profile_image = (CircleImageView)findViewById(R.id.custom_profile_image);

        send_chat_message_button = (ImageButton)findViewById(R.id.send_chat_message_button);
        send_files_button = (ImageButton)findViewById(R.id.send_files_button);
        input_chat_message = (EditText)findViewById(R.id.input_chat_message);

        private_messages_list_of_users = (RecyclerView)findViewById(R.id.private_messages_list_of_users);

        messagerAdapter = new MessagerAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        private_messages_list_of_users.setLayoutManager(linearLayoutManager);
        private_messages_list_of_users.setAdapter(messagerAdapter);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        loadingBar = new ProgressDialog(this);
    }

    private void EventMessageAddedtoDB() {
        messagesList.clear();
        rootRef.child("Messages").child(messageSenderID).child(messageRecieverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);
                System.out.println("ID " + messages.toString());
                messagesList.add(messages);

                messagerAdapter.notifyDataSetChanged();
                private_messages_list_of_users.smoothScrollToPosition(private_messages_list_of_users.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void SendMessage() {
        String messageText = input_chat_message.getText().toString();

        if(TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Please type a message...", Toast.LENGTH_SHORT).show();
        } else{
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageRecieverID;
            String messageRecieverRef = "Messages/" + messageRecieverID+ "/" + messageSenderID ;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                                                            .child(messageSenderID).child(messageRecieverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageRecieverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageRecieverRef + "/" + messagePushID, messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()) {
                        //Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatActivity.this, "Error " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                    input_chat_message.setText("");
                }
            });

        }
    }

    private void DisplayLastSeen(){
        rootRef.child("Users").child(messageRecieverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child("userState").hasChild("state")){
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();
                            if(state.equals("online")) {
                                custom_profile_last_seen.setText("online");
                            } else if(state.equals("offline")) {
                                custom_profile_last_seen.setText("Last seen: " + date + " " + time);
                            }
                        } else {
                            custom_profile_last_seen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
