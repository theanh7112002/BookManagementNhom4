package com.example.bookmanagement.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.bookmanagement.databinding.ActivityDangNhapBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DangNhapActivity extends AppCompatActivity {

    private ActivityDangNhapBinding binding;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDangNhapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DangNhapActivity.this, DangKyActivity.class));
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        //handle click quen mat khau
        binding.forgotTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DangNhapActivity.this, ForgotPassActivity.class));
            }
        });
    }

    private  String email= "", password="";
    private void validateData() {
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();


        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Email không đúng định dạng",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Mật khẩu không đúng định dạng", Toast.LENGTH_SHORT).show();
        }
        else{
            //data is validate, begin login
            loginUser();
        }
    }

    private void loginUser() {
        // show progress
        progressDialog.setMessage("Đang đăng nhập");
        progressDialog.show();

        //login user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //login succes
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //login failed
                        progressDialog.dismiss();
                        Toast.makeText(DangNhapActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        progressDialog.setMessage("Đang kiểm tra tài khoản");
        //check if admin or user
        //get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        //check in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        //get user type
                        String userType =""+ snapshot.child("userType").getValue();
                        if(userType.equals("user")){
                            startActivity(new Intent(DangNhapActivity.this, DashboardUserActivity.class));
                            finish();
                        } else if (userType.equals("admin")) {
                            startActivity(new Intent(DangNhapActivity.this, DashboardAdminActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });
    }
}