package com.example.bookmanagement.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookmanagement.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {
    private ActivityPdfEditBinding binding;
    private String bookId;
    private ProgressDialog progressDialog;
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;
    private static final String TAG ="BOOK_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId = getIntent().getStringExtra("bookId");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadCategories();
        loadBooksInfo();

        //handle pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });

        //handle back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle upload
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private String title="", description="";

    private void validateData() {
        //get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Không được để trống", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(description)){
            Toast.makeText(this, "Không được để trống", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(selectedCategoryId)){
            Toast.makeText(this, "Không được để trống", Toast.LENGTH_SHORT).show();
        }else{
            updatePdf();
        }
    }

    private void updatePdf() {
        Log.d(TAG, "updatePdf: Starting updating pdf into to db....");

        //show progress
        progressDialog.setMessage("Đang cập nhật lại thông tin sách");
        progressDialog.show();

        //set up data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("categoryId", ""+selectedCategoryId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Update success");
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed update"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void loadBooksInfo() {
        Log.d(TAG, "loadBooksInfo: Loading Books Info");

        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get books info
                        selectedCategoryId = ""+snapshot.child("categoryId").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String title = ""+snapshot.child("title").getValue();
                        //set to views
                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);
                        Log.d(TAG, "onDataChange: Loading Books Category Info");
                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("TheLoaiSach");
                        refBookCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //get category
                                        String category = ""+snapshot.child("theLoai").getValue();
                                        //set to category
                                        binding.categoryTv.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String selectedCategoryId="", selectedCategoryTitle="";

    private void categoryDialog(){
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for(int i=0; i<categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn loại sách")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        selectedCategoryId = categoryIdArrayList.get(which);
                        selectedCategoryTitle = categoryTitleArrayList.get(which);

                        //set to Text View
                        binding.categoryTv.setText(selectedCategoryTitle);

                    }
                })
                .show();
    }
    private void loadCategories() {
        Log.d(TAG, "loadCategories: Loading categories....");

        categoryIdArrayList = new ArrayList<>();
        categoryTitleArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TheLoaiSach");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArrayList.clear();
                categoryTitleArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    String id = ""+ds.child("id").getValue();
                    String category = ""+ds.child("theLoai").getValue();
                    categoryIdArrayList.add(id);
                    categoryTitleArrayList.add(category);

                    Log.d(TAG, "onDataChange: ID: "+id);
                    Log.d(TAG, "onDataChange: Category"+category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}