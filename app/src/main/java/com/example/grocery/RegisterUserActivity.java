package com.example.grocery;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterUserActivity extends AppCompatActivity implements LocationListener {
private ImageButton backbtn,gpsbtn;
private ImageView profileiv;
private Button RegisterBtn;
private TextView RegisterSellerTv;
private EditText nameEt,phoneEt,countryEt,stateEt,cityEt,emailEt,passwordEt,confpassdEt;
    //PERMISSION CONSTANTS
    private static final int LOCATION_REQUEST_CODE=100;
    private static final int CAMERA_REQUEST_CODE=200;
    private static final int STORAGE_REQUEST_CODE=300;
    private static final int IMAGE_PICK_GALLERY_CODE=400;
    private static final int IMAGE_PICK_CAMERA_CODE=500;

    //permission arrays
    private String[] localPermission;
    private String[] cameraPermission;
    private String[] storagePermission;
    private Uri image_uri;
    private LocationManager locationManager;
    private double latitude,longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        //init ui views
        RegisterSellerTv=findViewById(R.id.RegisterSellerTv);
RegisterBtn=findViewById(R.id.RegisterBtn);
        backbtn=findViewById(R.id.backbtn);
        gpsbtn=findViewById(R.id.gpsbtn);
        profileiv=findViewById(R.id.profileiv);
        nameEt=findViewById(R.id.nameEt);
        phoneEt=findViewById(R.id.phoneEt);
        countryEt=findViewById(R.id.countryEt);
        stateEt=findViewById(R.id.stateEt);
        cityEt=findViewById(R.id.cityEt);
        emailEt=findViewById(R.id.emailEt);
        passwordEt=findViewById(R.id.passwordEt);
        confpassdEt=findViewById(R.id.confpassdEt);

        //init permission arrays
        localPermission=new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        gpsbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect current location
                if (checkPermissions()){
//already allowed
                }
                else {
                    //denied,request permission
                    requestLocationPermission();
                }
            }
        });
        profileiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //PICK IMAGE
                showImagePickDialog();
            }
        });
        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           //register user
            }
        });
        RegisterSellerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open register seller activity
                startActivity(new Intent(RegisterUserActivity.this,RegisterSellerActivity.class));
            }
        });
    }

    private void showImagePickDialog() {
        //options to display in dialog
        String[] options={"camera","gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("pick image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle clicks
                        if(which==0){
                            //camera clicks
                            if (checkCameraPermission()) {
//camera permissions allowed
                                pickfromCamera();
                            }
                            else{
//not allowed,request
                                requestCameraPermission();
                            }
                        }
                        else{
                            //gallery clicks
                            if (checkStoragePermission()) {
//storage permissions allowed
                                pickimagefromGallery();
                            }
                            else{

                            }requestStoragePermission();
                        }
                    }
                }).show();
    }

    private void pickimagefromGallery(){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }
    private void pickfromCamera(){
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Image Description");

        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private void detectLocation() {
        Toast.makeText(this,"please wait...detecting",Toast.LENGTH_LONG).show();
        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
    }
    private void findAddress() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder=new Geocoder(this, Locale.getDefault());
        try{
            addresses=geocoder.getFromLocation(latitude,longitude,1);
            String address=addresses.get(0).getAddressLine(0);//complete address
            String country=addresses.get(0).getCountryName();
            String city=addresses.get(0).getLocality();
            String state=addresses.get(0).getAdminArea();

            //set addresses
            countryEt.setText(country);
            stateEt.setText(state);
            //addressEt.setText(address);
            cityEt.setText(city);
        }
        catch (Exception e){
            Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
    private boolean checkPermissions(){

        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void  requestLocationPermission(){
        ActivityCompat.requestPermissions(this,localPermission,LOCATION_REQUEST_CODE);

    }
    private boolean checkStoragePermission(){
        boolean result=ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);

    }
    private boolean checkCameraPermission(){
        boolean result=ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1=ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
//location detected
        latitude=location.getLatitude();
        longitude=location.getLongitude();
        findAddress();
    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
//gps disabled
        Toast.makeText(this,"please enable location",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean locationAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(locationAccepted){
                        //permission allowed
                        detectLocation();
                    }
                    else{
//permission denied
                        Toast.makeText(this,"LOCATION PERMISSION DENIED",Toast.LENGTH_SHORT).
                                show();
                    }
                }
            }
            break;
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        //permission allowed
                        pickfromCamera();
                    }
                    else{
//permission denied
                        Toast.makeText(this,"CAMERA PERMISSION DENIED",Toast.LENGTH_SHORT).
                                show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){

                    boolean storageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        //permission allowed
                        pickimagefromGallery();
                    }
                    else{
//permission denied
                        Toast.makeText(this,"STORAGE PERMISSION DENIED",Toast.LENGTH_SHORT).
                                show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                //GET PICKED IMAGE
                image_uri=data.getData();
                //set to image view
                profileiv.setImageURI(image_uri);
            }
            else if(requestCode==IMAGE_PICK_CAMERA_CODE){
                //set to image view
                profileiv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}