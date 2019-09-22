package com.example.bntouch;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.androidfung.geoip.api.ApiManager;
import com.androidfung.geoip.model.GeoIpResponseModel;
import com.example.bntouch.Database.UserInformationDB;
import com.example.bntouch.api.API;
import com.example.bntouch.api.RegisterAPI;
import com.example.bntouch.api.ResponseAPI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private Button createAccountButton;
    private EditText userEmail, userPassword;
    private TextView alreadyHaveAccountLink;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private ProgressDialog loadingBar;
    private String deviceToken;

    private API api;
    private static final String SIGNUP = API.BASE_URL + "signup";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //FirebaseApp.initializeApp(this);

        InitializeFields();

        alreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void InitializeFields() {
        api = new API(this);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        createAccountButton = (Button)findViewById(R.id.register_button);
        userEmail = (EditText)findViewById(R.id.register_email);
        userPassword = (EditText)findViewById(R.id.register_password);
        alreadyHaveAccountLink = (TextView)findViewById(R.id.already_have_account_link);
        loadingBar = new ProgressDialog(this);
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void CreateNewAccount(){
        final String email = userEmail.getText().toString();
        final String password = userPassword.getText().toString();
        final String username = "anon";
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            Toast.makeText(RegisterActivity.this, "Please fill required fields", Toast.LENGTH_LONG).show();
        } else{
            ApiManager apiManager = new ApiManager(Volley.newRequestQueue(this));
            apiManager.getGeoIpInfo(new com.android.volley.Response.Listener<GeoIpResponseModel>() {
                @Override
                public void onResponse(GeoIpResponseModel response) {
                    String country = response.getCountry();
                    String city = response.getCity();
                    String countryCode = response.getCountryCode();
                    double latitude = response.getLatitude();
                    double longtidue = response.getLongitude();
                    String region = response.getRegion();
                    String timezone = response.getTimezone();
                    String isp = response.getIsp();
                    String id = android.provider.Settings.System.getString(RegisterActivity.super.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

                    RegisterAPI registerAPI = new RegisterAPI(username, email, password, country, countryCode);
                    api.POST(registerAPI, SIGNUP, new ResponseAPI() {
                        @Override
                        public void onSuccess(String response)  {
                            try {
                                JSONObject res = new JSONObject(response);

                                UserInformationDB userInformationDB = new UserInformationDB();
                                userInformationDB.username = res.getString("username");
                                userInformationDB.email = res.getString("email");
                                userInformationDB.uid = res.getString("uid");
                                userInformationDB.isloggedin = true;
                                userInformationDB.save();
                                System.out.println("Response: " + response);
                                System.out.println("Saved to DB: " + userInformationDB.toString());
                                SendUserToMainActivity();
                            }
                            catch (Exception ex) {

                            }
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(RegisterActivity.this, "Cannot sign in. Please check the form and try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String errorMessage = error.toString();
                }
            });
        }
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
