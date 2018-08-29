package com.lobemusonda.schoolbustracker_driver;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    EditText mEditTextEmail, mEditTextPassword, mEditTextFullName, mEditTextBusNo;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        mEditTextFullName = findViewById(R.id.editTextFullName);
        mEditTextEmail = findViewById(R.id.editTextEmail);
        mEditTextPassword = findViewById(R.id.editTextPassword);
        mEditTextBusNo = findViewById(R.id.editTextBusNo);
        mProgressBar = findViewById(R.id.progressBar);

        Button signUp = findViewById(R.id.buttonSignUp);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
//            Handle the already Logged in user

        }
    }

    private void registerUser() {
        final String fullName = mEditTextFullName.getText().toString().trim();
        final String email = mEditTextEmail.getText().toString().trim();
        String password = mEditTextPassword.getText().toString().trim();
        final String busNo = mEditTextBusNo.getText().toString().trim();

        if (fullName.isEmpty()) {
            mEditTextFullName.setError("Full name is required");
            mEditTextFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            mEditTextEmail.setError("Email is required");
            mEditTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEditTextEmail.setError("Please enter a valid email");
            mEditTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            mEditTextPassword.setError("Password is required");
            mEditTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            mEditTextPassword.setError("Minimum length of password should be 6");
            mEditTextPassword.requestFocus();
            return;
        }

        if (busNo.isEmpty()) {
            mEditTextBusNo.setError("Bus number is required");
            mEditTextBusNo.requestFocus();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
//                    We store additional fields in firebase database
                    Driver driver = new Driver(fullName, email, busNo);
                    mDatabase.getReference("Drivers")
                            .child(mAuth.getCurrentUser().getUid())
                            .setValue(driver).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mProgressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignUpActivity.this, MapActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
