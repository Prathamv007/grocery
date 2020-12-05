package com.example.grocery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ImageButton backbtn;
    private EditText emailEt;
    private Button recoverbtn;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        backbtn = findViewById(R.id.backbtn);
        emailEt = findViewById(R.id.emailEt);
        recoverbtn = findViewById(R.id.recoverbtn);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("PLEASE WAIT");
        progressDialog.setCanceledOnTouchOutside(false);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        recoverbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoverPassword();
            }
        });
    }

    private String email;

    private void recoverPassword() {
        email = emailEt.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "invalid email ", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("sending instructions to reset password");
        progressDialog.show();

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                //instructions send
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this,"instructions sent to your email",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //instructions failed
                progressDialog.dismiss();
                Toast.makeText(ForgotPasswordActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}