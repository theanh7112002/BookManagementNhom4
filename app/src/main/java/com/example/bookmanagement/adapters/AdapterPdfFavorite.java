package com.example.bookmanagement.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookmanagement.Models.ModelPdf;
import com.example.bookmanagement.MyApplication;
import com.example.bookmanagement.activity.PdfDetailActivity;
import com.example.bookmanagement.databinding.RowPdfFavoriteBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterPdfFavorite extends RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite>{
    private Context context;
    private ArrayList<ModelPdf> pdfArrayList;
    private RowPdfFavoriteBinding binding;
    private static final String TAG ="FAVORITE_TAG";

    public AdapterPdfFavorite(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderPdfFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfFavorite holder, int position) {
        //get data
        ModelPdf model = pdfArrayList.get(position);

        loadBookDetails(model, holder);
        //handle click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", model.getId());
                context.startActivity(intent);
            }
        });
    }

    private void loadBookDetails(ModelPdf model, HolderPdfFavorite holder) {
        String bookId = model.getId();
        Log.d(TAG, "loadBookDetails: Book of book id: "+bookId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get book info
                        String bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String bookUrl = ""+snapshot.child("url").getValue();
                        String uid = ""+snapshot.child("uid").getValue();

                        //set to model
                        model.setFavorite(true);
                        model.setTitle(bookTitle);
                        model.setDescription(description);

                        MyApplication.loadPdfFromUrlSinglePage(""+bookUrl, ""+bookTitle, holder.pdfView, holder.progressBar, null);

                        //set data to views
                        holder.titleTv.setText(bookTitle);
                        holder.descriptionTv.setText(description);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    class HolderPdfFavorite extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv;


        public HolderPdfFavorite(@NonNull View itemView) {
            super(itemView);
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;

        }
    }
}
