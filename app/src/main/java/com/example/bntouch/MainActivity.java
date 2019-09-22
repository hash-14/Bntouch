package com.example.bntouch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.example.bntouch.Database.UserInformationDB;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private Toolbar main_page_toolbar;
    private ViewPager main_tabs_pager;
    private TabLayout main_tabs;
    private TabsAccessorAdapter tabsAccessorAdapter;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(getBaseContext(), ClosingService.class));

        initializeDB();
        //Set main title for the app;
        main_page_toolbar = (Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(main_page_toolbar);
        getSupportActionBar().setTitle("BnTouch");
        //end

        //set fragments to pageviewr
        main_tabs_pager = (ViewPager)findViewById(R.id.main_tabs_pager);
        tabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        main_tabs_pager.setAdapter(tabsAccessorAdapter);
        main_tabs = (TabLayout)findViewById(R.id.main_tabs);
        main_tabs.setupWithViewPager(main_tabs_pager);
        //end
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    protected void initializeDB() {
        Configuration.Builder configurationBuilder = new Configuration.Builder(this);
        configurationBuilder.addModelClasses(UserInformationDB.class);

        ActiveAndroid.initialize(configurationBuilder.create());
    }

    @Override
    protected void onStart(){
        super.onStart();
        VerifyUserExistance();
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);

        // check if the app is still visible
        if (!tasks.get(0).topActivity.getPackageName().equals(getPackageName())) {
            // for some reason(HOME, BACK, RECENT APPS, etc.) the app is no longer visible
            // do your thing here
            updateUserStatus("offline");
        } else {
            // app is still visible, switched to other activity
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            updateUserStatus("offline");
        }*/
    }

    private void VerifyUserExistance() {
        UserInformationDB userInformationDB = new Select()
                .from(UserInformationDB.class)
                .executeSingle();
        if(userInformationDB != null) {
            if (!userInformationDB.uid.isEmpty() && userInformationDB.uid != null && userInformationDB.isloggedin) {
                currentUserID = userInformationDB.uid;
                if(userInformationDB.username.equals("anon")) {
                    System.out.println("User " + userInformationDB.uid  + " didnt set username");
                    SendUserToSettingsActivity(currentUserID);
                    return;
                }
                //Stay in MainActivity;
                System.out.println("UserInformation: " + userInformationDB.toString());
            } else {//user didnt register yet!
                System.out.println("UserInformation: Logged out! " + userInformationDB.toString());
                SendUserToLoginActivity();//if user is not logged in
            }
        } else {//user didnt register yet!
            System.out.println("UserInformation: Not Registered Yet");
            SendUserToLoginActivity();//if user is not logged in
        }
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//handle back button by setting MainActivity as first activity we do so by clearing and calling new task;
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToSettingsActivity(String uid) {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        settingsIntent.putExtra("uid", uid);
        startActivity(settingsIntent);
    }

    //create the menuitmes the three dots in the app
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }
    //end

    //handle clicking menuitmes
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        super.onOptionsItemSelected(menuItem);
        switch (menuItem.getItemId()) {
            case R.id.main_logout_option:
                updateUserStatus("offline");
                mAuth.signOut();
                new Update(UserInformationDB.class)
                        .set("isloggedin = ?", false)
                        .where("uid = ?", currentUserID)
                        .execute();
                SendUserToLoginActivity();
                break;

            case R.id.main_settings_option:
                SendUserToSettingsActivity(currentUserID);
                break;
            case R.id.main_find_friends_option:
                SendUserToFindFriendsActivity();
                break;
            case R.id.main_new_group_option:
                RequestNewGroup();
                break;

        }
        return true;
    }
    //end
    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g. Group Name");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please fill group name" , Toast.LENGTH_LONG).show();
                } else{
                    CreateNewGroup(groupName);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void CreateNewGroup(final String groupName){
        rootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, groupName + " Created Successfully..." , Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void SendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void updateUserStatus(String state){
        /*String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStatMap =  new HashMap<>();
        onlineStatMap.put("time", saveCurrentTime);
        onlineStatMap.put("date", saveCurrentDate);
        onlineStatMap.put("state", state);
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStatMap);
*/
    }
}