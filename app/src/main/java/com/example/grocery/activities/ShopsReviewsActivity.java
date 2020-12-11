package com.example.grocery.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.grocery.R;

public class ShopsReviewsActivity extends AppCompatActivity {
    //ui views
    private ImageButton backBtn;
    private ImageView profileIv;
    private TextView shopNameTv,ratingsTv;
    private RatingBar ratingBar;
private RecyclerView reviewsRv;

    private String shopUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shops_reviews);

        //init ui views
        backBtn=findViewById(R.id.backBtn);
        profileIv=findViewById(R.id.profileIv);
        shopNameTv=findViewById(R.id.shopNameTv);
        ratingBar=findViewById(R.id.ratingBar);
        ratingsTv=findViewById(R.id.ratingsTv);
        reviewsRv=findViewById(R.id.reviewsRv);

        //get shop uid from intent
        shopUid=getIntent().getStringExtra("shopUid");
    }
}