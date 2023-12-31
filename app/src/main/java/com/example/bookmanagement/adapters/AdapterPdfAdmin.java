package com.example.bookmanagement.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookmanagement.Filters.FilterPdfAdmin;
import com.example.bookmanagement.Models.ModelPdf;
//import com.example.bookmanagement.MyApplication;
import com.example.bookmanagement.MyApplication;
import com.example.bookmanagement.activity.PdfDetailActivity;
import com.example.bookmanagement.activity.PdfEditActivity;
import com.example.bookmanagement.databinding.RowPdfAdminBinding;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {
    private Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    private RowPdfAdminBinding binding;
    private FilterPdfAdmin filter;
    private static final String TAG = "PDF_ADAPTER_TAG";
    private ProgressDialog progressDialog;

    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;

        //init progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //binding layout
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        //Get data, set data, ...

        //get data
        ModelPdf model = pdfArrayList.get(position);
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        long timestamp = Long.parseLong(model.getTimestamp());

        //convert timestamp to dd//MM//yyyy
        String formattedDate = MyApplication.formatTimestamp(timestamp);

        //set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        //load further
        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTv
        );
        MyApplication.loadPdfFromUrlSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar,
                null
        );
        MyApplication.laodPdfsize(
                "" +pdfUrl,
                "" +title,
                holder.sizeTv
        );

        //handle click options 1,Edit, 2 Delete
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsDialog(model, holder);
            }
        });

        //handle open pdf detail
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", pdfId);
                context.startActivity(intent);
            }
        });
    }

    private void moreOptionsDialog(ModelPdf model, HolderPdfAdmin holder) {
        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();
        //option to show in dialog
        String[] options = {"Sửa", "Xoá"};

        //Alert dialog
        AlertDialog.Builder builder =  new AlertDialog.Builder(context);
        builder.setTitle("Chọn chức năng")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        //handle dialog option click
                        if(which==0){
                            //Edit
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId", bookId);
                            context.startActivity(intent);

                        } else if (which==1) {
                            //Delete
                            MyApplication.deleteBook(
                                    context,
                                    ""+bookId,
                                    ""+bookUrl,
                                    ""+bookTitle
                            );
                            //deleteBook(model,holder);
                        }
                    }
                })
                .show();
    }




    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfAdmin(filterList, this);

        }
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder{

        //UI view of row_admin_pdf
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton moreBtn;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;
        }
    }
}
