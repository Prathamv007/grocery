package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocery.R;
import com.example.grocery.constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {



    //ui views
    private SwitchCompat fcmSwitch;
    private TextView notificationStatusTv;
    private ImageButton backBtn;

    private static final String enableMessage = "Notifications are enabled";
    private static final String disableMessage = "Notifications are disabled";

    private boolean isChecked = false;

    private FirebaseAuth firebaseAuth;

    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //init ui views
        fcmSwitch = findViewById(R.id.fcmSwitch);
        notificationStatusTv = findViewById(R.id.notificationStatusTv);
        backBtn = findViewById(R.id.backBtn);

        firebaseAuth = FirebaseAuth.getInstance();

        //init shared preferences
        sp = getSharedPreferences("SETTINGS_SP", MODE_PRIVATE);
        //check last selected option; true/false
        isChecked = sp.getBoolean("FCM_ENABLED" ,false);
        fcmSwitch.setChecked(isChecked);
        if(isChecked){
            //was enabled
            notificationStatusTv.setText(enableMessage);
        }
        else{
            //was disabled
            notificationStatusTv.setText(enableMessage);
        }


        //handle click; go back
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //add switch check change listener to enable disable notifications
        fcmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    //checked, enabled notifications
                    subscribeToTopic();

                }
                else{
                    //unchecked, disable notifications
                    unSubscribeToTopic();

                }
            }
        });

    }

    private void subscribeToTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic(constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //subscribed successfully
                        //save setting ins shared preferences
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED",true );
                        spEditor.apply();

                        Toast.makeText(SettingsActivity.this, ""+enableMessage, Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(enableMessage);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed subscribing
                        Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unSubscribeToTopic(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //unsubscribed
                        //save setting ins shared preferences
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED",false );
                        spEditor.apply();

                        Toast.makeText(SettingsActivity.this, ""+disableMessage, Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(disableMessage);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //failed unsubscribed
                        Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }
}

