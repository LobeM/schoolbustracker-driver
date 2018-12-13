package com.lobemusonda.schoolbustracker_driver;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class ChangePasswordActivity extends AppCompatActivity {
    private FirebaseUser mUser;

    private EditText mFullNameTxt, mEmailTxt, mOldPasswordTxt, mNewPasswordTxt, mConfirmPasswordTxt;
    private Button mSaveBtn;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mFullNameTxt = findViewById(R.id.editTextFullName);
        mEmailTxt = findViewById(R.id.editTextEmail);
        mOldPasswordTxt = findViewById(R.id.editTextOldPassword);
        mNewPasswordTxt = findViewById(R.id.editTextNewPassword);
        mConfirmPasswordTxt = findViewById(R.id.editTextConfirmPassword);
        mSaveBtn = findViewById(R.id.btn_save);
        mProgressBar = findViewById(R.id.progressBar);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUser();
            }
        });
    }

    private void updateUser() {
        String fullName = mFullNameTxt.getText().toString().trim();
        String email = mEmailTxt.getText().toString().trim();
        String oldPassword = mOldPasswordTxt.getText().toString().trim();
        final String newPassword = mNewPasswordTxt.getText().toString().trim();
        String confirmPassword = mConfirmPasswordTxt.getText().toString().trim();


        if (fullName.isEmpty()) {
            mFullNameTxt.setError("Full name is required");
            mFullNameTxt.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            mEmailTxt.setError("Email is required");
            mEmailTxt.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailTxt.setError("Please enter a valid email");
            mEmailTxt.requestFocus();
            return;
        }

        if (!oldPassword.isEmpty()) {
            if (newPassword.isEmpty()) {
                mNewPasswordTxt.setError("Password is required");
                mNewPasswordTxt.requestFocus();
                return;
            }

            if (newPassword.length() < 6) {
                mNewPasswordTxt.setError("Minimum length of password should be 6");
                mNewPasswordTxt.requestFocus();
                return;
            }

            if (!newPassword.equals(confirmPassword)){
                mConfirmPasswordTxt.setError("Passwords don't match");
                mConfirmPasswordTxt.requestFocus();
                return;
            }

            // Get auth credentials from the user for re-authentication. The example below shows
            // email and password credentials but there are multiple possible providers,
            // such as GoogleAuthProvider or FacebookAuthProvider.
            AuthCredential credential = EmailAuthProvider.getCredential(mUser.getEmail(), oldPassword);

            // Prompt the user to re-provide their sign-in credentials
            mUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Password updated");
                                } else {
                                    Log.d(TAG, "Error password not updated");
                                }
                            }
                        });
                    } else {
                        Log.d(TAG, "Error auth failed");
                    }
                }
            });
        }
        mProgressBar.setVisibility(View.VISIBLE);

        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();
        mUser.updateProfile(profileChangeRequest);
        mUser.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mProgressBar.setVisibility(View.GONE);
                Intent intent = getIntent();
                startActivity(intent);
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        loadSavedData();
    }

    private void loadSavedData() {
        mFullNameTxt.setText(mUser.getDisplayName());
        mEmailTxt.setText(mUser.getEmail());
    }
}
