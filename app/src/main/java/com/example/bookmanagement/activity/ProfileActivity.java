package com.example.bookmanagement.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.bookmanagement.Models.ModelPdf;
import com.example.bookmanagement.MyApplication;
import com.example.bookmanagement.R;
import com.example.bookmanagement.adapters.AdapterPdfFavorite;
import com.example.bookmanagement.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfFavorite adapterPdfFavorite;

    private static final String TAG = "PROFILE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //set up fbauth
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();
        loadFavoriteBooks();

        //handle click edit Btn
        binding.profileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class));
            }
        });

        //handle back Btn
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: Loading user..."+firebaseAuth.getUid());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get all info
                        String email = ""+snapshot.child("email").getValue();
                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();
                        long timestamp = Long.parseLong(""+snapshot.child("timestamp").getValue());
                        String uid = ""+snapshot.child("uid").getValue();
                        String userType = ""+snapshot.child("userType").getValue();

                        String formattedDate = MyApplication.formatTimestamp(timestamp);
                        //set data
                        binding.emailTv.setText(email);
                        binding.nameTv.setText(name);
                        binding.accountTypeTv.setText(userType);
                        binding.memberDate.setText(formattedDate);

                        //set image
                        Glide.with(ProfileActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person_pro)
                                .into(binding.profileTv);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadFavoriteBooks() {
        //init list
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String bookId = ""+ds.child("bookId").getValue();

                            //set id to model
                            ModelPdf modelPdf = new ModelPdf();
                            modelPdf.setId(bookId);
                            //add model to list
                            pdfArrayList.add(modelPdf);
                        }
                        //set number of favorite books
                        binding.favoriteCount.setText(""+pdfArrayList.size());
                        //setup adapter
                        adapterPdfFavorite = new AdapterPdfFavorite(ProfileActivity.this, pdfArrayList);
                        //set adapter to rc
                        binding.booksRv.setAdapter(adapterPdfFavorite);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}