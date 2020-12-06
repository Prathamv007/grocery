package com.example.grocery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainSellerActivity extends AppCompatActivity {
    private TextView nameTv;
    private ImageButton logoutbtn;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        logoutbtn=findViewById(R.id.logoutbtn);
        nameTv=findViewById(R.id.nameTv);
        progressDialog=new ProgressDialog(this);
          progressDialog.setTitle("please wait");
          progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth=FirebaseAuth.getInstance();
        checkUser();

        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeMeoffline();

            }
        });

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
                Toast.makeText(MainSellerActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUser() {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user==null){
            startActivity(new Intent(MainSellerActivity.this,LoginActivity.class));
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
                            String name=""+ds.child("name").getValue();
                            String accounttype=""+ds.child("accounttype").getValue();

                            nameTv.setText(name+"("+accounttype+")");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}