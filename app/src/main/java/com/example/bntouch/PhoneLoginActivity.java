package com.example.bntouch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button send_verification_code, verify_code;
    private EditText phone_number_input, verification_code_input;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        InitializeFields();

        send_verification_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendVerificationCode();
            }
        });

        verify_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_verification_code.setVisibility(View.INVISIBLE);
                phone_number_input.setVisibility(View.INVISIBLE);

                String verificationCode = verification_code_input.getText().toString();
                if(TextUtils.isEmpty(verificationCode)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please write verification code", Toast.LENGTH_LONG).show();
                } else{

                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("please wait, while authenticating your phone");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(PhoneLoginActivity.this, "Invalid phone number " + e.toString(), Toast.LENGTH_LONG).show();
                send_verification_code.setVisibility(View.VISIBLE);
                phone_number_input.setVisibility(View.VISIBLE);

                verify_code.setVisibility(View.INVISIBLE);
                verification_code_input.setVisibility(View.INVISIBLE);
                loadingBar.dismiss();
            }
            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(PhoneLoginActivity.this, "Code sent, please verify", Toast.LENGTH_LONG).show();
                send_verification_code.setVisibility(View.INVISIBLE);
                phone_number_input.setVisibility(View.INVISIBLE);

                verify_code.setVisibility(View.VISIBLE);
                verification_code_input.setVisibility(View.VISIBLE);
                loadingBar.dismiss();
            }
        };
    }

    private void SendVerificationCode() {
        String phoneNumber = phone_number_input.getText().toString();
        loadingBar.setTitle("Verification Code");
        loadingBar.setMessage("please wait, while authenticating your phone");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        if(TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(PhoneLoginActivity.this, "Require phone field", Toast.LENGTH_LONG).show();
        } else{
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber, //user phone number to be verified
                    60,  //Timeout duration
                    TimeUnit.SECONDS, //Timeout unit
                    PhoneLoginActivity.this,// Activity (for callback binding)
                    callbacks);// OnVerificationStateChangedCallbacks
            }
    }

    private void InitializeFields() {
        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode("lb");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        loadingBar = new ProgressDialog(this);
        send_verification_code = (Button)findViewById(R.id.send_verification_code);
        verify_code = (Button)findViewById(R.id.verify_code);
        phone_number_input = (EditText)findViewById(R.id.phone_number_input);
        verification_code_input = (EditText)findViewById(R.id.verification_code_input);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            String currentUserID = mAuth.getCurrentUser().getUid();

                            usersRef.child(currentUserID).child("device_token")
                                    .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        SendUserToMainActivity();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(PhoneLoginActivity.this, "Invalid phone number " + task.getException().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//handle back button by setting MainActivity as first activity we do so by clearing and calling new task;
        startActivity(mainIntent);
        finish();
    }
}
