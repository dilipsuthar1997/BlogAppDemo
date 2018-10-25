package com.techflow.blogappdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.techflow.blogappdemo.R;

/**
 * Created by DILIP on 10/10/2018
 */

public class LoginActivity extends AppCompatActivity {

    private Context context;
    private EditText loginEmailTxt, loginPasswordTxt;
    private Button loginButton, registerButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;
        mAuth = FirebaseAuth.getInstance();
        findViewById();
        initComponent();
    }

    private void findViewById() {
        loginEmailTxt = findViewById(R.id.email);
        loginPasswordTxt = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_btn);
        registerButton = findViewById(R.id.register_btn);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void initComponent() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithEmailAndPassword();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToRegister();
            }
        });
    }

    // invoke from minimize or popup from anywhere
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            sendToMain();
        }

    }

    private void sendToMain() {
        Intent mainIntent = new Intent(context, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void sendToRegister() {
        Intent registerIntent = new Intent(context, SignupActivity.class);
        startActivity(registerIntent);
    }

    private void signInWithEmailAndPassword() {
        String email = loginEmailTxt.getText().toString();
        String password = loginPasswordTxt.getText().toString();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            progressBar.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {

                        sendToMain();

                    } else {

                        String error_msg = task.getException().toString();
                        Toast.makeText(context, "Error: " + error_msg, Toast.LENGTH_SHORT).show();

                    }
                    progressBar.setVisibility(View.INVISIBLE);

                }
            });

        } else
            Toast.makeText(context, "Fill all", Toast.LENGTH_SHORT).show();
    }

}
