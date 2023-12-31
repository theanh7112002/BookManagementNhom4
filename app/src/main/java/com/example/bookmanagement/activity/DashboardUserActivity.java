package com.example.bookmanagement.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.bookmanagement.BookUserFragment;
import com.example.bookmanagement.Models.ModelCategory;
import com.example.bookmanagement.databinding.ActivityDashboardUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardUserActivity extends AppCompatActivity {
    private ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;
    private ActivityDashboardUserBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        setupViewpagerAdapter(binding.viewPaper);
        binding.tabLayout.setupWithViewPager(binding.viewPaper);

        //handle log out
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                startActivity(new Intent(DashboardUserActivity.this, MainActivity.class));
                finish();
            }
        });

        //handle click, open profile
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardUserActivity.this, ProfileActivity.class));
            }
        });
    }

    private void setupViewpagerAdapter(ViewPager viewPager){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);

        categoryArrayList = new ArrayList<>();

        //load categories form fb
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TheLoaiSach");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear
                categoryArrayList.clear();

                //add model
                ModelCategory modelAll = new ModelCategory("01", "All", "", "1");
                ModelCategory modelMostView = new ModelCategory("02", "Xem Nhiều", "", "1");
                ModelCategory modelMostDown = new ModelCategory("02", "Lượt Tải Nhiều", "", "1");
                //add model to list
                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostView);
                categoryArrayList.add(modelMostDown);

                //add data to view pager
                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        ""+modelAll.getId(),
                        ""+modelAll.getTheLoai(),
                        ""+modelAll.getUid()
                ), modelAll.getTheLoai());
                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        ""+modelMostView.getId(),
                        ""+modelMostView.getTheLoai(),
                        ""+modelMostView.getUid()
                ), modelMostView.getTheLoai());
                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        ""+modelMostDown.getId(),
                        ""+modelMostDown.getTheLoai(),
                        ""+modelMostDown.getUid()
                ), modelMostDown.getTheLoai());
                //refresh list
                viewPagerAdapter.notifyDataSetChanged();

                //now load from db
                for(DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    //add data
                    categoryArrayList.add(model);

                    viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                            ""+model.getId(),
                            ""+model.getTheLoai(),
                            ""+model.getUid()),model.getTheLoai());

                    viewPagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //set adapter
        viewPager.setAdapter(viewPagerAdapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter{
        private ArrayList<BookUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;
        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context) {

            super(fm, behavior);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
        private void addFragment(BookUserFragment fragment, String title){
            //add fragment
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }
    private void checkUser() {
        //get current User
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null){
            binding.subTitleTv.setText("Tài khoản khách");

//            startActivity(new Intent(DashboardUserActivity.this, MainActivity.class));
            //finish();
        }
        else{
            //logged
            String email = firebaseUser.getEmail();
            // set text view
            binding.subTitleTv.setText(email);
        }
    }
}