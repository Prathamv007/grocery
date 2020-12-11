package com.example.grocery.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.grocery.R;

public class OrderDetailsSellerActivity extends AppCompatActivity {

    String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_seller);
    }
}