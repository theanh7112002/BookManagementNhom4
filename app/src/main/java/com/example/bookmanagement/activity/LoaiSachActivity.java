package com.example.bookmanagement.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.bookmanagement.databinding.ActivityLoaiSachBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoaiSachActivity extends AppCompatActivity {

    private ActivityLoaiSachBinding binding;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoaiSachBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Pleast wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //nut back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private String category = "";
    private void validateData() {
        //before add

        //get data
        category = binding.categoryEt.getText().toString().trim();
        //validate
        if(TextUtils.isEmpty(category)){
            Toast.makeText(this, "Vui lòng nhập loại sách!", Toast.LENGTH_SHORT).show();
        }
        else {
            addCategoryFirebase();
        }
    }

    private void addCategoryFirebase() {
        // show progress
        progressDialog.setMessage("Đang thêm mới...");
        progressDialog.show();

        //get timestamp
        long timestamp = System.currentTimeMillis();

        //setup to firebase
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("theLoai", ""+category);
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("uid", ""+firebaseAuth.getUid());

        //add db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TheLoaiSach");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //success
                        progressDialog.dismiss();
                        Toast.makeText(LoaiSachActivity.this, "Thêm thành công!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                        public void onFailure(@NonNull Exception e) {
                        //failed
                        progressDialog.dismiss();
                        Toast.makeText(LoaiSachActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}