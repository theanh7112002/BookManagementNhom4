package com.example.bookmanagement.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookmanagement.Filters.FilterCategory;
import com.example.bookmanagement.Models.ModelCategory;
import com.example.bookmanagement.activity.PdfListAdminActivity;
import com.example.bookmanagement.databinding.RowCategoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory> implements Filterable {
    private Context context;
    public ArrayList<ModelCategory> categoryArrayList, filterList;

    //view binding
    private RowCategoryBinding binding;

    private FilterCategory filter;

    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = categoryArrayList;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind row_category.xml
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false);
        HolderCategory holderCategory = new HolderCategory(binding.getRoot());
        return holderCategory;
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        //get data
        ModelCategory model = categoryArrayList.get(position);
        String id = model.getId();
        String theLoai = model.getTheLoai();
        String uid = model.getUid();
        String timestamp = model.getTimestamp();

        //set data
        holder.categoryTv.setText(theLoai);

        //handle click delete
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Xoá loại sách")
                        .setMessage("Bạn có muốn xoá loại sách này không?")
                        .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //begin delete
                                Toast.makeText(context, "Đang xoá....", Toast.LENGTH_SHORT).show();
                                deleteCategory(model, holder);

                            }
                        })
                        .setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });
        //handle item click, go to Pdf list admin
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfListAdminActivity.class);
                intent.putExtra("categoryId", id);
                intent.putExtra("categoryTitle", theLoai);
                context.startActivity(intent);
            }
        });

    }

    private void deleteCategory(ModelCategory model, HolderCategory holder) {
        //get id of category to delete
        String id = model.getId();
        //DFirebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TheLoaiSach");
        ref.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //delete success
                        Toast.makeText(context, "Xoá thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed delete
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterCategory(filterList, this);
        }
        return filter;
    }

    class HolderCategory extends RecyclerView.ViewHolder{

        TextView categoryTv;
        ImageButton deleteBtn;

        public HolderCategory(@NonNull View itemView) {
            super(itemView);

            categoryTv = binding.categoryTv;
            deleteBtn = binding.deleteBtn;
        }
    }
}
