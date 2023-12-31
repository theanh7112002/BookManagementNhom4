package com.example.bookmanagement.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookmanagement.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {
    private ActivityPdfAddBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;
    private Uri pdfUri = null;
    private static final int PDF_PICK_CODE = 1000;

    //TAG for debugging
    private static final String TAG = "ADD_PDF_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init fb
        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

        //setup dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle attach pdf
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfPickIntent();
            }
        });

        //handle pick loai sach
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });

        //upload pdf
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }
    private String title ="", description ="";
    private void validateData() {
        //step 1
        Log.d(TAG, "validateData: validating data...");
        //get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        //validate data
        if(TextUtils.isEmpty(title)){
            Toast.makeText(this, "Tiêu đề không được để trống", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(description)){
            Toast.makeText(this, "Mô tả không được để trống", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(selectedCategoryTitle)) {
            Toast.makeText(this, "Chưa chọn loại sách", Toast.LENGTH_SHORT).show();
        }else if(pdfUri == null){
            Toast.makeText(this, "Chưa gán link pdf của sách", Toast.LENGTH_SHORT).show();
        }
        else{
            uploadPdfToStorage();
        }

    }

    private void uploadPdfToStorage() {
        //step 2: upload to fb
        Log.d(TAG, "uploadPdfToStorage: uploading to fb....");

        //show progress
        progressDialog.setMessage("Đang tải Pdf lên...");
        progressDialog.show();

        //timestamp
        long timestamp = System.currentTimeMillis();

        String filePathAndName = "Books/" + timestamp;
        //storage ref
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: PDF uploaded to storage...");
                        Log.d(TAG, "onSuccess: getting pdf url");

                        //get pdf url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = ""+uriTask.getResult();

                        //upload to fb
                        uploadPdfInfoToDb(uploadedPdfUrl, timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: PDF upload failed due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "PDF upload failed due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadPdfInfoToDb(String uploadedPdfUrl, long timestamp) {
        //step 3: upload to fb
        Log.d(TAG, "uploadPdfToStorage: uploading pdf info to fb....");

        progressDialog.setMessage("Uploading pdf info...");

        String uid = firebaseAuth.getUid();

        //setup data to upload
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("categoryId", ""+selectedCategoryId);
        hashMap.put("url", ""+uploadedPdfUrl);
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Successfully uploaded... ");
                        Toast.makeText(PdfAddActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload to db"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Failed to upload to db"+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf categories...");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList =new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TheLoaiSach");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    //get id and theloai
                    String categoryId =""+ ds.child("id").getValue();
                    String categoryTitle =""+ ds.child("theLoai").getValue();

                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //selected category
    private String selectedCategoryId,  selectedCategoryTitle;

    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for(int i = 0; i< categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chon loai sach")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        selectedCategoryTitle = categoryTitleArrayList.get(which);
                        selectedCategoryId = categoryIdArrayList.get(which);
                        binding.categoryTv.setText(selectedCategoryTitle);

                        Log.d(TAG, "onClick: Selected Category: "+selectedCategoryId+" "+selectedCategoryTitle);
                    }
                })
                .show();
    }

    private void pdfPickIntent() {

        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == PDF_PICK_CODE){
                Log.d(TAG, "onActivityResult: PDF Picked");

                pdfUri = data.getData();

                Log.d(TAG, "onActivityResult: URI"+pdfUri);
            }
        }
        else{
            Log.d(TAG, "onActivityResult: cancelled picking pdf");
            Toast.makeText(this, "cancelled picking pdf", Toast.LENGTH_SHORT).show();
        }
    }
}