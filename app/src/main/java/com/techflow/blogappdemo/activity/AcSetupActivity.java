package com.techflow.blogappdemo.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techflow.blogappdemo.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by DILIP on 11/10/2018
 *
 * Modified by DILIP on 17/10/2018
 */

public class AcSetupActivity extends AppCompatActivity {

    private Context context;
    private View parentView;

    private CircleImageView profileImage;
    private EditText profileName;
    private Button btSave;
    private ProgressBar progressBarSave;
    private Uri mainImageUri = null;

    private boolean isChanged = false;

    private String user_name;
    private String user_id;

    // Firebase
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private UploadTask uploadTask;
    private FirebaseFirestore firebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ac_setup);
        context = AcSetupActivity.this;
        parentView = findViewById(android.R.id.content);
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        user_id = mAuth.getCurrentUser().getUid();

        initToolbar();
        findViewById();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Setup");

    }

    private void findViewById() {
        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        btSave = findViewById(R.id.save_btn);
        progressBarSave = findViewById(R.id.progress_bar);
    }

    // This is a Main Part
    private void initComponent() {

        progressBarSave.setVisibility(View.VISIBLE);
        btSave.setEnabled(false);


        // check That the Data is available or not
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mainImageUri = Uri.parse(image);

                        profileName.setText(name);

                        RequestOptions placeHolderRequest = new RequestOptions();
                        placeHolderRequest.placeholder(R.drawable.default_image);
                        Glide.with(context).setDefaultRequestOptions(placeHolderRequest).load(image).into(profileImage);

                    }

                } else {

                    String error_msg = task.getException().toString();
                    Toast.makeText(context, "FireStore Retrieve Error: " + error_msg, Toast.LENGTH_SHORT).show();

                }

                progressBarSave.setVisibility(View.INVISIBLE);
                btSave.setEnabled(true);

            }
        });


        // set image to profile picture
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check android version > MARSHMALLOW
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // check if permission is granted or not
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(AcSetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {

                        openImagePicker();

                    }

                } else {

                    openImagePicker();

                }
            }
        });


        // save image and name to server
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                user_name = profileName.getText().toString();

                if (!TextUtils.isEmpty(user_name) && mainImageUri != null) {

                    progressBarSave.setVisibility(View.VISIBLE);

                    // check if Image changed or not
                    if (isChanged) {

                        user_id = mAuth.getCurrentUser().getUid();

                        final StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");

                        image_path.putFile(mainImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                                if (task.isSuccessful()) {

                                    Toast.makeText(context, "The image is Uploaded", Toast.LENGTH_SHORT).show();

                                } else {

                                    String error_msg = task.getException().toString();
                                    Toast.makeText(context, "Image Error: " + error_msg, Toast.LENGTH_SHORT).show();

                                }

                                // returning DownloadLink of uploaded image
                                return image_path.getDownloadUrl();

                            }

                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                if (task.isSuccessful()) {

                                    storeFirestoreData(task);

                                } else {

                                    String error_msg = task.getException().toString();
                                    Toast.makeText(context, "Download Link Error: " + error_msg, Toast.LENGTH_SHORT).show();

                                    progressBarSave.setVisibility(View.INVISIBLE);

                                }

                            }
                        });

                    } else {
                        storeFirestoreData(null);
                    }

                } else {

                    Snackbar.make(parentView, "Please check above fields.", Snackbar.LENGTH_SHORT).show();

                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageUri = result.getUri();
                profileImage.setImageURI(mainImageUri);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }

    private void openImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(AcSetupActivity.this);
    }

    // method for storing data to Database
    private void storeFirestoreData(@NonNull Task<Uri> task) {

        Uri downloadUri;

        if (task != null) {

            downloadUri = task.getResult();

        } else {

            downloadUri = mainImageUri;

        }

        // Upload User name and imageLink to Database
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", user_name);
        userMap.put("image", downloadUri.toString());

        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Toast.makeText(context, "The user settings are updated", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(context, MainActivity.class));
                    finish();

                } else {

                    String error_msg = task.getException().toString();
                    Toast.makeText(context, "FireStore Error: " + error_msg, Toast.LENGTH_SHORT).show();

                }

                progressBarSave.setVisibility(View.INVISIBLE);

            }
        });

    }
}
