package com.example.bookmanagement.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.bookmanagement.databinding.ActivityDangKyBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class DangKyActivity extends AppCompatActivity {

    private ActivityDangKyBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDangKyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }

    private String name="", email="", password="";
    private void validateData() {
        //get data
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        String cpassword = binding.cpasswordEt.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(name)){
            Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Email không đúng định dạng",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Mật khẩu không được để trống", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(cpassword)){
            Toast.makeText(this, "Nhập lại mật khẩu", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(cpassword)) {
            Toast.makeText(this, "Mật khẩu không trùng nhau", Toast.LENGTH_SHORT).show();
        }
        else{
            createUserAccount();
        }
    }

    private void createUserAccount() {
        // show progress
        progressDialog.setMessage("Đang tạo tài khoản");
        progressDialog.show();

        //create firebase
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account creation success, now add in firebase realtime
                        updateUserinfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure( Exception e) {
                        //account creation failed
                        progressDialog.dismiss();
                        Toast.makeText(DangKyActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserinfo() {
        progressDialog.setMessage("Đang lưu thông tin");
        //timestamp
        long timestamp = System.currentTimeMillis();

        //get data
        String uid = firebaseAuth.getUid();

        //set data to add fb
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", "");
        hashMap.put("userType", "user");
        hashMap.put("timestamp", timestamp);

        //set data to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //data added db
                        progressDialog.dismiss();
                        Toast.makeText(DangKyActivity.this, "Tài khoản đã được tạo", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(DangKyActivity.this, DashboardUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure( Exception e) {
                        //data failed
                        progressDialog.dismiss();
                        Toast.makeText(DangKyActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}