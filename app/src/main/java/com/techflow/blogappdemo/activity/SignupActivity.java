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

public class SignupActivity extends AppCompatActivity {

    private Context context;

    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btSignup, btLogin;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        context = this;
        mAuth = FirebaseAuth.getInstance();
        findViewById();
        initComponent();

    }

    private void findViewById() {
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etConfirmPassword = findViewById(R.id.confirm_passsword);
        btSignup = findViewById(R.id.signup_btn);
        btLogin = findViewById(R.id.login_btn);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void initComponent() {
        btSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpWithEmailAndPassword();
            }
        });

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

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

    private void signUpWithEmailAndPassword() {
        String email = etEmail.getText().toString();
        String pass = etPassword.getText().toString();
        String confirm_pass = etConfirmPassword.getText().toString();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) & !TextUtils.isEmpty(confirm_pass)) {

            if (pass.equals(confirm_pass)) {

                progressBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            startActivity(new Intent(context, AcSetupActivity.class));
                            finish();

                        } else {

                            String error_msg = task.getException().toString();
                            Toast.makeText(context, "Error: " + error_msg, Toast.LENGTH_SHORT).show();

                        }
                        progressBar.setVisibility(View.INVISIBLE);

                    }
                });

            } else {

                Toast.makeText(context, "Please check the password", Toast.LENGTH_SHORT).show();

            }

        } else {

            Toast.makeText(context, "Fill all", Toast.LENGTH_SHORT).show();

        }

    }

}
