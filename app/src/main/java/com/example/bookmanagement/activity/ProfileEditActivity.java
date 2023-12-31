package com.example.bookmanagement.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookmanagement.R;
import com.example.bookmanagement.databinding.ActivityPdfEditBinding;
import com.example.bookmanagement.databinding.ActivityProfileEditBinding;
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

import java.util.HashMap;

public class ProfileEditActivity extends AppCompatActivity {
    private ActivityProfileEditBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "PROFILE_EDIT";
    private Uri imageUri = null;
    private String name="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //set up progress
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //setup firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();

        //handle back btn
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle pick image
        binding.profileTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImage();
            }
        });

        //handle click update
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                valiDateData();
            }
        });
    }

    private void valiDateData() {
        //get data
        name = binding.nameEt.getText().toString().trim();

        //validate
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Không được để trống", Toast.LENGTH_SHORT).show();
        }else{
            if(imageUri == null){
                updateProfile("");
            }else{
                uploadImage();
            }
        }
    }

    private void updateProfile(String imageUrl) {
        Log.d(TAG, "updateProfile: Update user profile");
        progressDialog.setMessage("Cập nhật ảnh người dùng");
        progressDialog.show();

        //set up data in fb
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", ""+name);
        if(imageUri!=null){
            hashMap.put("profileImage", ""+imageUrl);
        }

        //update data to fb
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Profile Update....");
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Profile Updated...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed..."+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Failed..."+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void uploadImage() {
        Log.d(TAG, "uploadImage: upload image info...");
        progressDialog.setMessage("Cập nhật ảnh");
        progressDialog.show();

        String filePathName = "ProfileImages/" + firebaseAuth.getUid();

        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathName);
        reference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Profile image uploaded...");
                        Log.d(TAG, "onSuccess: Getting url of...");
                        Task<Uri>  uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadImageUrl = ""+uriTask.getResult();
                        Log.d(TAG, "onSuccess: Upload image url..."+uploadImageUrl);

                        updateProfile(uploadImageUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed...");
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Failed..."+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void showImage() {
        //innit setup popmenu
        PopupMenu popupMenu = new PopupMenu(this, binding.profileTv);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Thư Viện");

        popupMenu.show();

        //handle menu item
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //get id of item click
                int which = menuItem.getItemId();
                if(which==0){
                    //camera
                    pickImageCamera();
                }else if(which==1){
                    //thu vien anh
                    pickImageThuVien();
                }
                return false;
            }
        });
    }

    private void pickImageCamera() {
        // intent to pick camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Chọn ảnh mới");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Ảnh đại diện");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResult.launch(intent);
    }

    private void pickImageThuVien() {
        //intent to pick thu vien
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        thuvienActivityResult.launch(intent);
    }
    private ActivityResultLauncher<Intent> cameraActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: Pick camera"+imageUri);
                        Intent data = result.getData();
                        binding.profileTv.setImageURI(imageUri);
                    }
                    else{
                        Toast.makeText(ProfileEditActivity.this, "Huỷ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<Intent> thuvienActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: "+imageUri);
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: Pick thu vien"+imageUri);
                        binding.profileTv.setImageURI(imageUri);
                    }
                    else{
                        Toast.makeText(ProfileEditActivity.this, "Huỷ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
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
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String userType = ""+snapshot.child("userType").getValue();

                        //set data
                        binding.nameEt.setText(name);


                        //set image
                        Glide.with(ProfileEditActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person_pro)
                                .into(binding.profileTv);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}