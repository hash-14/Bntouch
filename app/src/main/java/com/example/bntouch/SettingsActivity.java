package com.example.bntouch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private Button update_settings_button;
    private EditText set_user_name, set_profile_status;
    private CircleImageView profile_image;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference userProfileImagesRef;

    private ProgressDialog loadingBar;

    private Uri resultUri = null;
    private String imageUriOnLoad="";

    private static final int GALLERY_PICK=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        set_user_name.setVisibility(View.INVISIBLE);

        update_settings_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.startPickImageActivity(SettingsActivity.this);
            }
        });
    }

    private void RetrieveUserInfo() {
        rootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists() && (dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image"))){
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveUserimage = dataSnapshot.child("image").getValue().toString();
                            profile_image.setVisibility(View.VISIBLE);
                            profile_image.setEnabled(true);
                            imageUriOnLoad = retrieveUserimage;

                            Picasso.get().load(retrieveUserimage).placeholder(R.drawable.profile_image).into(profile_image);

                            set_user_name.setText(retrieveUserName);
                            set_profile_status.setText(retrieveUserStatus);
                        } else if(dataSnapshot.exists() && dataSnapshot.hasChild("name")){
                            profile_image.setVisibility(View.VISIBLE);
                            profile_image.setEnabled(true);
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();

                            set_user_name.setText(retrieveUserName);
                            set_profile_status.setText(retrieveUserStatus);
                        } else{
                            set_user_name.setVisibility(View.VISIBLE);
                            profile_image.setVisibility(View.INVISIBLE);
                            profile_image.setEnabled(false);
                            Toast.makeText(SettingsActivity.this, "Please update your profile information", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void UpdateSettings() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        String setUserName = set_user_name.getText().toString();
        String setStatus = set_profile_status.getText().toString();
        if(TextUtils.isEmpty(setUserName) || TextUtils.isEmpty(setStatus)){
            Toast.makeText(SettingsActivity.this, "Please fill required fields", Toast.LENGTH_LONG).show();
        } else {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("status", setStatus);
            if(imageUriOnLoad != null && imageUriOnLoad !="") {
                profileMap.put("image", imageUriOnLoad);
            }

            rootRef.child("Users").child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(SettingsActivity.this, "Error " + task.getException().toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE:
                    Uri imageUri = data.getData();
                    CropImage.activity(imageUri)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setMultiTouchEnabled(true)
                            .setAspectRatio(1, 1)
                            .start(this);
                    break;
                case CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE:

                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == RESULT_OK) {
                        loadingBar.setTitle("Set Profile Image");
                        loadingBar.setMessage("Please wait, your profile image is updating...");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();
                        resultUri = result.getUri();
                        imageUriOnLoad = resultUri.toString();
                        final StorageReference filePath = userProfileImagesRef.child(currentUserID + ".jpg");
                        filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful()){
                                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Toast.makeText(SettingsActivity.this, "Profile Image Uploaded Successfully", Toast.LENGTH_LONG).show();
                                            rootRef.child("Users").child(currentUserID).child("image")
                                                    .setValue(uri.toString())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()) {
                                                                Toast.makeText(SettingsActivity.this, "Profile Image Saved in DB Successfully", Toast.LENGTH_LONG).show();
                                                            } else {
                                                                Toast.makeText(SettingsActivity.this, "Error " + task.getException().toString(), Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    });

                                } else{
                                    Toast.makeText(SettingsActivity.this, "Error " + task.getException().toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Exception error = result.getError();
                    }
                    loadingBar.dismiss();
                    break;
                default:
                    break;
            }
        }
    }

    private void InitializeFields() {
        update_settings_button = (Button)findViewById(R.id.update_settings_button);
        set_user_name = (EditText)findViewById(R.id.set_user_name);
        set_profile_status = (EditText)findViewById(R.id.set_profile_status);
        profile_image = (CircleImageView)findViewById(R.id.profile_image);
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        loadingBar = new ProgressDialog(this);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//handle back button by setting MainActivity as first activity we do so by clearing and calling new task;
        startActivity(mainIntent);
        finish();
    }
}
