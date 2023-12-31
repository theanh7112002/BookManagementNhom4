package com.example.bookmanagement.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.bookmanagement.Models.ModelPdf;
import com.example.bookmanagement.adapters.AdapterPdfAdmin;
import com.example.bookmanagement.databinding.ActivityPdfListAdminBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfListAdminActivity extends AppCompatActivity {
    private ActivityPdfListAdminBinding binding;
    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfAdmin adapterPdfAdmin;
    private String categoryId, categoryTitle;
    private static final String TAG = "PDF_LIST_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle = intent.getStringExtra("categoryTitle");

        //set pdf
        binding.subTitleTv.setText(categoryTitle);

        loadPdfList();

        //search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterPdfAdmin.getFilter().filter(s);


                }catch (Exception e){
                    Log.d(TAG, "onTextChanged: "+e.getMessage());
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //handle click black
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadPdfList() {
        //init list before add
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            //get data
                            ModelPdf model = ds.getValue(ModelPdf.class);
                            //add to list
                            pdfArrayList.add(model);

                            Log.d(TAG, "onDataChange: "+model.getId()+" "+model.getTitle());
                        }
                        //setup adapter
                        adapterPdfAdmin = new AdapterPdfAdmin(PdfListAdminActivity.this, pdfArrayList);
                        binding.bookRv.setAdapter(adapterPdfAdmin);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}