package com.techflow.blogappdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techflow.blogappdemo.R;
import com.techflow.blogappdemo.utils.Tools;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

/**
 * Created by DILIP on 17/10/2018
 *
 * Modified by DILIP on 22/10/2018
 */

public class NewPostActivity extends AppCompatActivity {

    private Context context;
    private Toolbar toolbar;

    private ImageView newPostImage;
    private EditText newPostDesc;
    private Button postBtn;
    private ProgressBar progressBar;

    private Uri postImageUri =  null;
    private String current_user_id;

    private Bitmap compressedImageFile;

    // Firebase
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        context = NewPostActivity.this;
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        current_user_id = mAuth.getCurrentUser().getUid();

        initToolbar();
        findViewById();
        initComponent();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Post New Blog");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void findViewById() {
        newPostImage = findViewById(R.id.new_post_image);
        newPostDesc = findViewById(R.id.new_post_desc);
        postBtn = findViewById(R.id.post_btn);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void initComponent() {

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openImagePicker();

            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String desc = newPostDesc.getText().toString();

                if (!TextUtils.isEmpty(desc) && postImageUri != null) {

                    progressBar.setVisibility(View.VISIBLE);
                    postBtn.setEnabled(false);

                    // It generate random string
                    final String random_name = UUID.randomUUID().toString();

                    // Create new posted image path & upload image to SERVER
                    final StorageReference file_path = storageReference.child("post_images").child(random_name + ".jpg");

                    file_path.putFile(postImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                            if (!task.isSuccessful()) {

                                String error_msg = task.getException().toString();
                                Toast.makeText(context, "Image Error: " + error_msg, Toast.LENGTH_SHORT).show();
                                postBtn.setEnabled(true);

                            }

                            return file_path.getDownloadUrl();

                        }

                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            if (task.isSuccessful()) {

                                //get uploaded image Url from SERVER---
                                final String download_uri = task.getResult().toString();

                                // compress image to lowResolution
                                byte[] thumbImgData = Tools.imageCompressor(postImageUri, context);

                                // after compression it upload image to SERVER
                                // 2nd method by UploadTask---
                                UploadTask uploadTaskThumb = storageReference.child("post_images/thumbs")
                                        .child(random_name + ".jpg").putBytes(thumbImgData);

                                final StorageReference thumb_file_path = storageReference.child("post_images/thumbs")
                                        .child(random_name + ".jpg");

                                uploadTaskThumb.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                                        if (!task.isSuccessful()) {
                                            postBtn.setEnabled(true);
                                            throw task.getException();
                                        }

                                        // Continue with the task to get the download URL
                                        return thumb_file_path.getDownloadUrl();

                                    }

                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {

                                        if (task.isSuccessful()) {

                                            //get uploaded thumbImage url from SERVER---
                                            String download_thumb_uri = task.getResult().toString();
                                            //Toast.makeText(context, "Thumb Url: " + download_thumb_uri, Toast.LENGTH_SHORT).show();

                                            //Upload post doc. to the SERVER---
                                            Map<String, Object> postMap = new HashMap<>();
                                            postMap.put("image_url", download_uri);
                                            postMap.put("thumb_url", download_thumb_uri);
                                            postMap.put("desc", desc);
                                            postMap.put("user_id", current_user_id);
                                            postMap.put("time_stamp", FieldValue.serverTimestamp());

                                            firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {

                                                    if (task.isSuccessful()) {

                                                        Toast.makeText(context, "Post added successfully", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(context, MainActivity.class));
                                                        finish();

                                                    }

                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    postBtn.setEnabled(true);

                                                }
                                            });

                                        } else {

                                            Toast.makeText(context, "Error: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                            postBtn.setEnabled(true);

                                        }

                                    }
                                });

                            } else {

                                // Error msg here if !task.isSuccessful() for download_uri

                            }

                        }
                    });

                } else {

                    Toast.makeText(context, "Please check above field", Toast.LENGTH_LONG).show();

                }

            }
        });

    }

    private void openImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setMinCropResultSize(512, 512)
                .setAspectRatio(2, 1)
                .start(NewPostActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                newPostImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }
}
