package com.example.grocery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
//ui views
    private EditText emailEt,passwordEt;
    private TextView forgotTv,NoaccountTv;
    private Button LoginBtn;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init ui
        setContentView(R.layout.activity_login);
        emailEt=findViewById(R.id.emailEt);
        passwordEt=findViewById(R.id.passwordEt);
        forgotTv=findViewById(R.id.forgotTv);
        NoaccountTv=findViewById(R.id.NoaccountTv);
        LoginBtn=findViewById(R.id.LoginBtn);

        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("PLEASE WAIT");
        progressDialog.setCanceledOnTouchOutside(false);

        NoaccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterUserActivity.class));
            }
        });

        forgotTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));
            }
        });

        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

    }
    private String email,password;
    private void loginUser() {
        email=emailEt.getText().toString().trim();
        password=passwordEt.getText().toString().trim();
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"invalid email format",Toast.LENGTH_SHORT).show();
            return;

        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"enter password bitch",Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("logging in......");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password).
                addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //logged in successfully
                        makemeOnline();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //logging in failed
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makemeOnline() {
        //after login make user online
        progressDialog.setMessage("checking user...");

        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("online","true");

        //update value to db
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                                    //update successfully
                        checkUsertype();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //updatig failed
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUsertype() {
        //if user open user activity
        //if seller open seller activity
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            String accounttype=""+ds.child("account type").getValue();
                            if(accounttype.equals("seller")){
                                progressDialog.dismiss();
                                //user is seller
                                startActivity(new Intent(LoginActivity.this,MainSellerActivity.class));
                                finish();
                            }
                            else{
                                progressDialog.dismiss();
                                //user is buyer
                                startActivity(new Intent(LoginActivity.this,MainUserActivity.class));
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}