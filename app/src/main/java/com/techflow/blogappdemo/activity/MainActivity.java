package com.techflow.blogappdemo.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techflow.blogappdemo.R;
import com.techflow.blogappdemo.fragment.AccountFragment;
import com.techflow.blogappdemo.fragment.HomeFragment;
import com.techflow.blogappdemo.fragment.NotificationFragment;

/**
 * Created by DILIP on 10/10/2018
 *
 * Modified by DILIP on 17/10/2018
 */

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "1";

    private Context context;
    private Toolbar toolbar;

    private FloatingActionButton fabAddPost;
    private BottomNavigationView bottomNavigationView;

    private String current_user_id;

    //Fragments
    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        initToolbar();
        findViewById();
        initComponent();
        //invokeNotification();

        //Load homeFragment by default
        //if (mAuth.getCurrentUser() != null)
        replaceFragment(homeFragment);
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Blog App Demo");
    }

    private void findViewById() {
        fabAddPost = findViewById(R.id.fab_add_post);
        bottomNavigationView = findViewById(R.id.bottom_nav_view);
    }

    private void initComponent() {

        //if (mAuth.getCurrentUser() != null) {

            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();

            fabAddPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(context, NewPostActivity.class));
                }
            });

            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {

                        case R.id.home:
                            replaceFragment(homeFragment);
                            return true;

                        case R.id.notification:
                            replaceFragment(notificationFragment);
                            return true;

                        case R.id.account:
                            replaceFragment(accountFragment);
                            return true;

                        default:
                            return false;

                    }
                }
            });
        //}

    }

    // invoke from minimize or popup from anywhere
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {

            sendToLogin();

        } else {
            //Check if user set their profile or not
            current_user_id = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {

                        if (!task.getResult().exists()) {

                            startActivity(new Intent(context, AcSetupActivity.class));

                        }

                    } else {

                        String error_msg = task.getException().toString();
                        Toast.makeText(context, "Error: " + error_msg, Toast.LENGTH_SHORT).show();

                    }

                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_logout:
                logOut();
                return true;

            case R.id.action_setting:
                startActivity(new Intent(context, AcSetupActivity.class));
                return true;
                default:
                    return false;
        }
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(context, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void logOut() {
        mAuth.signOut();
        sendToLogin();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void invokeNotification() {
        int noti_id = 0;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Dilip Suthar")
                .setContentText("This is a notification")
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        Uri path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(path);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(noti_id, builder.build());
    }
}
