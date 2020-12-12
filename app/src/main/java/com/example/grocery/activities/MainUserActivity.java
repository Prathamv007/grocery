package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterOrderUser;
import com.example.grocery.adapters.AdapterShop;
import com.example.grocery.models.ModelOrderUser;
import com.example.grocery.models.ModelShop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainUserActivity extends AppCompatActivity {
    private TextView nameTv,emailTv,phoneTv,tabShopsTv,tabsOrdersTv;
    private ImageButton logoutBtn,editProfileBtn,settingsBtn;
    private RelativeLayout shopsRl,ordersRl;
    private ImageView profileIv;
    private RecyclerView shopsRv,ordersRv;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelShop> shopsList;
    private AdapterShop adapterShop;

    private  ArrayList<ModelOrderUser>ordersList;
    private AdapterOrderUser adapterOrderUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        logoutBtn=findViewById(R.id.logoutBtn);
        editProfileBtn=findViewById(R.id.editProfileBtn);
        emailTv=findViewById(R.id.emailTv);
        nameTv=findViewById(R.id.nameTv);
        phoneTv=findViewById(R.id.phoneTv);
        tabShopsTv=findViewById(R.id.tabShopsTv);
        tabsOrdersTv=findViewById(R.id.tabsOrdersTv);
        shopsRl=findViewById(R.id.shopsRl);
        shopsRv=findViewById(R.id.shopsRv);
        ordersRl=findViewById(R.id.ordersRl);
        ordersRv=findViewById(R.id.ordersRv);
        settingsBtn = findViewById(R.id.settingsBtn);

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth=FirebaseAuth.getInstance();

        checkUser();


                //at start show shops ui
        showShopsUI();
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeMeoffline();
            }
        });
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainUserActivity.this,ProfileEditUserActivity.class));
            }
        });
        tabShopsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShopsUI();
            }
        });
            tabsOrdersTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOrdersUI();
                }
            });




        //start settings screen
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainUserActivity.this,SettingsActivity.class));

            }
        });


    }




    private void showShopsUI() {
        //show ORDERS ui,hide SHOP ui
        shopsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);
        tabShopsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabsOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabsOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));


    }


    private void showOrdersUI() {
        //show shops ui,hide ordsers ui
        shopsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);
        tabShopsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabsOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabsOrdersTv.setBackgroundResource(R.drawable.shape_rect04);


    }
    private void makeMeoffline() {
        //after login make user online
        progressDialog.setMessage("logging out user...");

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("online","false");

        //update value to db
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //update successfully
                        firebaseAuth.signOut();
                        checkUser();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //updatig failed
                progressDialog.dismiss();
                Toast.makeText(MainUserActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUser() {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user==null){
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();
        }
        else{
            loadMyinfo();
        }
    }

    private void loadMyinfo() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            //get user data

                            String name=""+ds.child("name").getValue();
                            String email=""+ds.child("email").getValue();
                            String phone=""+ds.child("phone").getValue();
                            String profileImage=""+ds.child("profileImage").getValue();
                            String accountType=""+ds.child("accountType").getValue();
                            String city=""+ds.child("city").getValue();

                            //set user data
                            nameTv.setText(name);
                            emailTv.setText(email);
                            phoneTv.setText(phone);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(profileIv);
                            }
                            catch (Exception e){
                            //profileIv.setImageResource(R.drawable.ic_person_gray);
                            }
                            //load only those shops that are in city of user
                            loadShops(city);
                            loadOrders();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrders() {
        //init order list
        ordersList=new ArrayList<>();

        //get orders
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                ordersList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    String uid=""+ds.getRef().getKey();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                               if(dataSnapshot.exists()){
                                   for(DataSnapshot ds:dataSnapshot.getChildren()){
                                       ModelOrderUser modelOrderUser=ds.getValue(ModelOrderUser.class);
                                      ordersList.add(modelOrderUser);
                                   }
                                   //setup adapter
                                   adapterOrderUser=new AdapterOrderUser(MainUserActivity.this,ordersList);
                                   //set to recycler view
                                   ordersRv.setAdapter(adapterOrderUser);
                               }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops(final String myCity) {
        //INIT LIST
        shopsList =new ArrayList<>();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//clear lists before adding
                        shopsList.clear();
                        for (DataSnapshot ds:dataSnapshot.getChildren() ){
                            ModelShop modelShop=ds.getValue(ModelShop.class);
                            String shopCity=""+ds.child("city").getValue();
                            //SHOW ONLY USER CITY STORE
//                            if(shopCity.equals(myCity)){
//                                shopsList.add(modelShop);
//                                //if we want to display all shops ,skip the if part and
//                                //this shopsList.add(modelShop);
//                            }
                            //set up adapter
                            adapterShop=new AdapterShop(MainUserActivity.this,shopsList);
                            //set adapter to recycler view
                            shopsRv.setAdapter(adapterShop);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}