package com.example.bookmanagement.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.bookmanagement.Models.ModelCmt;
import com.example.bookmanagement.Models.ModelPdf;
import com.example.bookmanagement.MyApplication;
import com.example.bookmanagement.R;
import com.example.bookmanagement.adapters.AdapterCmt;
import com.example.bookmanagement.adapters.AdapterPdfFavorite;
import com.example.bookmanagement.databinding.ActivityPdfDetailBinding;
import com.example.bookmanagement.databinding.DialogCommentBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {
    private ActivityPdfDetailBinding binding;
    public static final String TAG_DOWN = "DOWNLOAD_TAG";
    String bookId, bookTitle, bookUrl;
    boolean isInMyFavo = false;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ArrayList<ModelCmt> cmtArrayList;
    private AdapterCmt adapterCmt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        binding.downloadBtn.setVisibility(View.GONE);

        //init progressbar
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            checkisFavorite();
        }

        loadBookDetails();
        loadCmtBooks();
        MyApplication.incrementBookViewCount(bookId);



        //handle click back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle click read book
        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(PdfDetailActivity.this, PdfViewActivity.class);
                intent1.putExtra("bookId", bookId);
                startActivity(intent1);
            }
        });

        //handle click tai ve
        binding.downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG_DOWN, "onClick: Checking...");
                if(ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG_DOWN, "onClick: Already, CAN DOWN");
                    MyApplication.downloadBook(PdfDetailActivity.this, ""+bookId, ""+bookTitle, ""+bookUrl);

                }else{
                    Log.d(TAG_DOWN, "onClick: Failed request");
                    resultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });
        //handle favorite click
        binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser()==null){
                    Toast.makeText(PdfDetailActivity.this, "Đăng nhập để thực hiện chức năng này", Toast.LENGTH_SHORT).show();

                }else {
                    if (isInMyFavo){
                        MyApplication.deleteFavoriteBook(PdfDetailActivity.this, bookId);
                    }else {
                        MyApplication.addFavoriteBook(PdfDetailActivity.this, bookId);
                    }
                }
            }
        });

        //handle click, show cmt
        binding.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (firebaseAuth.getCurrentUser()==null){
//                    Toast.makeText(PdfDetailActivity.this, "Chưa đăng nhập...", Toast.LENGTH_SHORT).show();
//                }
//                else{
                    commentDialog();
//                }
            }
        });
    }

    private void loadCmtBooks() {
        cmtArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cmtArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelCmt model = ds.getValue(ModelCmt.class);

                            cmtArrayList.add(model);
                        }
                        adapterCmt = new AdapterCmt(PdfDetailActivity.this, cmtArrayList);
                        binding.cmtRv.setAdapter(adapterCmt);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String comment= "";
    private void commentDialog() {
        DialogCommentBinding commentBinding = DialogCommentBinding.inflate(LayoutInflater.from(this));

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setView(commentBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //handle back
        commentBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        //handle add cmt
        commentBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get data
                comment = commentBinding.cmtEt.getText().toString().trim();
                //validate
                if(TextUtils.isEmpty(comment)){
                    Toast.makeText(PdfDetailActivity.this, "Bình luận không được để trống", Toast.LENGTH_SHORT).show();

                }else{
                    alertDialog.dismiss();
                    themComment();
                }
            }
        });
    }

    private void themComment() {
        //show progress
        progressDialog.setMessage("Đang thêm bình luận");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();

        //set up data comment firebase
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("bookId", ""+bookId);
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("comment", ""+comment);
        hashMap.put("uid", ""+firebaseAuth.getUid());

        //DB
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(PdfDetailActivity.this, "Thêm bình luận thành công", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PdfDetailActivity.this, "Failed cmt..."+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //request storage permission
    private ActivityResultLauncher<String> resultLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->{
                if(isGranted){
                    Log.d(TAG_DOWN, "Permission...");
                    MyApplication.downloadBook(this, ""+bookId, ""+bookTitle, ""+bookUrl);
                }
                else{
                    Log.d(TAG_DOWN, "Permisson was denied...");
                    Toast.makeText(this, "Thất bại", Toast.LENGTH_SHORT).show();

                }
    });

    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        bookTitle= ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        bookUrl = ""+snapshot.child("url").getValue();
                        long timestamp = Long.parseLong(""+snapshot.child("timestamp").getValue());

                        String formattedDate = MyApplication.formatTimestamp(timestamp);

                        //required data is load
                        binding.downloadBtn.setVisibility(View.VISIBLE);

                        MyApplication.loadCategory(
                                ""+categoryId,
                                binding.categoryTv
                        );
                        MyApplication.loadPdfFromUrlSinglePage(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.pdfView,
                                binding.progressBar,
                                binding.pageTv
                        );
                        MyApplication.laodPdfsize(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.sizeTv
                        );

                        //set data
                        binding.titleTv.setText(bookTitle);
                        binding.descriptionTv.setText(description);
                        binding.dateTv.setText(formattedDate);
                        binding.viewsTv.setText(viewsCount.replace("null", "N/A"));
                        binding.downloadsTv.setText(downloadsCount.replace("null", "N/A"));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void checkisFavorite(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavo = snapshot.exists();
                        //if(isInMyFavo){
                            //binding.favoriteBtn.setText("Bỏ yêu thích");

                        //}//else {
                            //binding.favoriteBtn.setText("Yêu thích");
                        //}
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}