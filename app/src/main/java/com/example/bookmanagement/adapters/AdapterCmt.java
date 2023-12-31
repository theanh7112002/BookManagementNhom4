package com.example.bookmanagement.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookmanagement.Models.ModelCmt;
import com.example.bookmanagement.MyApplication;
import com.example.bookmanagement.R;
import com.example.bookmanagement.databinding.RowCmtBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterCmt extends RecyclerView.Adapter<AdapterCmt.HolderComment> {
    private Context context;
    private ArrayList<ModelCmt> cmtArrayList;
    private RowCmtBinding binding;
    private FirebaseAuth firebaseAuth;
    public AdapterCmt(Context context, ArrayList<ModelCmt> cmtArrayList) {
        this.context = context;
        this.cmtArrayList = cmtArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    class HolderComment extends RecyclerView.ViewHolder{

        ShapeableImageView profileTv;
        TextView nameTv, cmtTv, dateTv;
        public HolderComment(@NonNull View itemView) {
            super(itemView);

            profileTv = binding.profileTv;
            nameTv = binding.nameTv;
            cmtTv = binding.cmtTv;
            dateTv = binding.dateTv;
        }
    }

    @NonNull
    @Override
    public AdapterCmt.HolderComment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCmtBinding.inflate(LayoutInflater.from(context), parent, false);


        return new HolderComment(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterCmt.HolderComment holder, int position) {
        //get data
        ModelCmt modelCmt = cmtArrayList.get(position);
        String id = modelCmt.getId();
        String bookId = modelCmt.getBookId();
        String comment = modelCmt.getComment();
        String uid = modelCmt.getUid();
        long timestamp = Long.parseLong(modelCmt.getTimestamp());

        String formattedDate = MyApplication.formatTimestamp2(timestamp);

        //set data
        holder.cmtTv.setText(comment);
        holder.dateTv.setText(formattedDate);

        loadUserDetails(modelCmt, holder);

        //handle click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseAuth.getCurrentUser() !=null && uid.equals(firebaseAuth.getUid())){
                    deleteCmt(modelCmt, holder);
                }
            }
        });
    }

    private void deleteCmt(ModelCmt modelCmt, HolderComment holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Xoá bình luận")
                .setMessage("Bạn muốn xoá bình luận này không?")
                .setPositiveButton("Xoá", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                        ref.child(modelCmt.getBookId())
                                .child("Comments")
                                .child(modelCmt.getId())
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "Xoá thành công", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Xoá thất bại"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
    }

    private void loadUserDetails(ModelCmt modelCmt, HolderComment holder) {
        String uid = modelCmt.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();

                        //set data
                        holder.nameTv.setText(name);
                        try{
                            Glide.with(context)
                                    .load(profileImage)
                                    .placeholder(R.drawable.ic_person_pro)
                                    .into(holder.profileTv);
                        }catch (Exception e){
                            holder.profileTv.setImageResource(R.drawable.ic_person_pro);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return cmtArrayList.size();
    }
}
