package com.example.cpastone.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.cpastone.MyApplication;
import com.example.cpastone.databinding.ActivityPdfDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PdfDetailActivity extends AppCompatActivity {

    // view  binding
    private ActivityPdfDetailBinding binding;

    // pdf id, get from intent
    String bookId, bookTitle, bookUrl;
    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get data from intent
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        Log.d(bookId, "Sarthak ");
        // at start download button
        binding.downloadBookBtn.setVisibility(View.GONE);
        
        loadBookDetails();

        // increment book view count, whenever this page starts
        MyApplication.incrementBookViewCount(bookId);
        Log.d(bookId, "Sarthak1 ");
        // handles click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // handles click download pdf
        binding.downloadBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG_DOWNLOAD, "onClick: Checking permission");
                if(ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG_DOWNLOAD, "onClick: Permission already granted, can download book");
                    MyApplication.downloadBook(PdfDetailActivity.this, ""+bookId, ""+bookTitle, ""+bookUrl);
                }
                else {
                    Log.d(TAG_DOWNLOAD, "onClick: Permission was not granted, request cancellation");
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });
    }


    // request storage permission
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if(isGranted) {
                    Log.d(TAG_DOWNLOAD, ": Permission Granted");
                    MyApplication.downloadBook(this, ""+bookId, ""+bookTitle, ""+bookUrl);
                }
                else{
                    Log.d(TAG_DOWNLOAD, ": Permission was denied");
                    Toast.makeText(this, "Permission was denied", Toast.LENGTH_SHORT).show();
                }
            });

    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");

        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // getting data
                        bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        bookUrl = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();

                        // required data is loaded, show downloaded button
                        binding.downloadBookBtn.setVisibility(View.VISIBLE);

                        // formatting date
                        String date  = MyApplication.formatTimestamp(timestamp);

                        MyApplication.loadCategory(
                                ""+categoryId,
                                binding.categoryTv
                        );

                        MyApplication.loadPdfFromUrlSinglePage(
                                ""+bookUrl,
                                ""+bookTitle
                        );
                        MyApplication.loadPdfSize(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.sizeTv
                        );
                        // setting data
                        binding.titleTv.setText(bookTitle);
                        binding.descriptionTv.setText(description);
                        binding.viewsTv.setText(viewsCount.replace("null","N/A"));
                        binding.downloadsTv.setText(downloadsCount.replace("null","N/A"));
                        binding.dateTv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}