package com.example.bookmanagement;

import static com.example.bookmanagement.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.bookmanagement.Models.ModelPdf;
import com.example.bookmanagement.adapters.AdapterPdfAdmin;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {
    public static final String TAG_DOWN = "DOWNLOAD_TAG";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static final String formatTimestamp (long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy", cal).toString();

        return date;
    }

    public static final String formatTimestamp2 (long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy HH:mm", cal).toString();

        return date;
    }

    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {
        String TAG = "DELETE_BOOK_TAG";



        Log.d(TAG, "deleteBook: Deleting....");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Đang xoá" + bookTitle +"....");
        progressDialog.show();

        Log.d(TAG, "deleteBook: Deleting from storage...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Deleted from storage");

                        Log.d(TAG, "onSuccess: Now deleting into from db");
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Delete from db");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Xoá thành công", Toast.LENGTH_SHORT).show();


                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to delete"+e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to delete from storage");
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public static void laodPdfsize(String pdfUrl, String pdfTitle, TextView sizeTv) {
        String TAG = "PDF_SIZE_TAG";

        //using url

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        //get size in bytes
                        double bytes  =storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess: "+pdfTitle +" "+bytes);

                        //convert bytes to KB, MB
                        double kb = bytes/1024;
                        double mb = kb/1024;
                        if (mb>=1){
                            sizeTv.setText(String.format("%.2f", mb)+ "MB");
                        }
                        else if (kb>=1){
                            sizeTv.setText(String.format("%.2f", kb)+ "KB");
                        }
                        else{
                            sizeTv.setText(String.format("%.2f", bytes)+ "bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                    }
                });
    }

    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar, TextView pagesTv) {
        String TAG = "PDF_LOAD_SINGLE_TAG";

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: "+pdfTitle+ "success");

                        //set to pdfview
                        pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: pdf loaded");

                                        if(pagesTv!=null){
                                            pagesTv.setText(""+nbPages);
                                        }
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onFailure: failed"+e.getMessage());
                    }
                });

    }

    public static void loadCategory(String categoryId, TextView categoryTv) {
        //get category using categoryId
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TheLoaiSach");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get category
                        String category =  ""+snapshot.child("theLoai").getValue();

                        //set to textview
                        categoryTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public static void incrementBookViewCount(String bookId){
        //get book views count
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get views count
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();

                        if(viewsCount.equals("") || viewsCount.equals("null")){
                            viewsCount="0";
                        }

                        //increment view count
                        long newViewsCount = Long.parseLong(viewsCount) +1;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewsCount", newViewsCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public static void downloadBook(Context context, String bookId, String bookTitle, String bookUrl){
        Log.d(TAG_DOWN, "downloadBook: downloading book...");

        String nameWithExtension = bookTitle +".pdf";
        Log.d(TAG_DOWN, "downloadBook: NAME:" +nameWithExtension);

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Đang tải" +nameWithExtension+"....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG_DOWN, "onSuccess: Thanh cong!");
                        Log.d(TAG_DOWN, "onSuccess: Luu sach....");
                        saveBook(context, progressDialog, bytes, nameWithExtension, bookId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWN, "onFailure: Failed"+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "Tải thất bại"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private static void saveBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId) {
        Log.d(TAG_DOWN, "saveBook: Saving download book....");
        try {
            File downFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downFolder.mkdirs();

            String filePath = downFolder.getPath() +"/"+nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Tải thành công", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWN, "saveBook: Tai ve thanh cong vao folder");
            progressDialog.dismiss();

            incrementBookDownCount(bookId);
        }catch (Exception e){
            Log.d(TAG_DOWN, "saveBook: Failed dowm"+e.getMessage());
            Toast.makeText(context, "Tải về thất bại", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    private static void incrementBookDownCount(String bookId) {
        Log.d(TAG_DOWN, "incrementBookDownCount: Dem so lan down");
        //get previous down

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String downCount = ""+snapshot.child("downloadsCount").getValue();
                        Log.d(TAG_DOWN, "onDataChange: So lan down:"+downCount);

                        if (downCount.equals("")||downCount.equals("null")){
                            downCount="0";
                        }

                        long newdownCount = Long.parseLong(downCount) +1;
                        Log.d(TAG_DOWN, "onDataChange: Luot tai ve: "+newdownCount);

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("downloadsCount", newdownCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG_DOWN, "onSuccess: So lan tai ve: ");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG_DOWN, "onFailure: failed down count"+e.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }




    public static void addFavoriteBook(Context context, String bookId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() ==null){
            Toast.makeText(context, "Đăng nhập để thưc hiện chức năng này", Toast.LENGTH_SHORT).show();
        }
        else {
            long timestamp = System.currentTimeMillis();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("bookId", ""+bookId);
            hashMap.put("timestamp", ""+timestamp);

            //save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Thêm thành công vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Thêm thất bại vào danh sách yêu thích"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static void deleteFavoriteBook(Context context, String bookId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() ==null){
            Toast.makeText(context, "Đăng nhập để thưc hiện chức năng này", Toast.LENGTH_SHORT).show();
        }
        else {

            //save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Xoá thành công khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Xoá thất bại khỏi danh sách yêu thích"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
