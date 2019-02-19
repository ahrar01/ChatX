package com.appswedevelop.mychat.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.appswedevelop.mychat.MainActivity;
import com.appswedevelop.mychat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private EditText mDisplayName;
    private EditText mEmail;
    private EditText mPassword, mConfirmPass;
    private String check, display_name, email, password, confirmedPass;


    private Button mCreateBtn;

    private Toolbar mToolbar;

    private DatabaseReference mDatabase;
    TextWatcher nameWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //none
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //none
        }

        @Override
        public void afterTextChanged(Editable s) {

            check = s.toString();

            if (check.length() < 4 || check.length() > 20) {
                mDisplayName.setError("Name Must consist of 4 to 20 characters");
            }
        }

    };

    //Firebase Auth
    private FirebaseAuth mAuth;
    TextWatcher emailWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //none
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //none
        }

        @Override
        public void afterTextChanged(Editable s) {

            check = s.toString();

            if (check.length() < 4 || check.length() > 40) {
                mEmail.setError("Email Must consist of 4 to 20 characters");
            } else if (!check.matches("^[A-za-z0-9.@]+")) {
                mEmail.setError("Only . and @ characters allowed");
            } else if (!check.contains("@") || !check.contains(".")) {
                mEmail.setError("Enter Valid Email");
            }

        }

    };
    TextWatcher passWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //none
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //none
        }

        @Override
        public void afterTextChanged(Editable s) {

            check = s.toString();

            if (check.length() < 4 || check.length() > 20) {
                mPassword.setError("Password Must consist of 4 to 20 characters");
            } else if (!check.matches("^[A-za-z0-9@]+")) {
                mPassword.setError("Only @ special character allowed");
            }
        }

    };
    TextWatcher cnfpassWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //none
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //none
        }

        @Override
        public void afterTextChanged(Editable s) {

            check = s.toString();

            if (!check.equals(mPassword.getText().toString())) {
                mConfirmPass.setError("Both the passwords do not match");
            }
        }

    };
    //ProgressDialog
    private KProgressHUD mRegProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Toolbar Set
        mToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Firebase Auth

        mAuth = FirebaseAuth.getInstance();


        mDisplayName = findViewById(R.id.register_display_name);
        mEmail = findViewById(R.id.register_email);
        mPassword = findViewById(R.id.reg_password);
        mConfirmPass = findViewById(R.id.confirmPassword);
        mCreateBtn = findViewById(R.id.reg_create_btn);

        mDisplayName.addTextChangedListener(nameWatcher);
        mEmail.addTextChangedListener(emailWatcher);
        mPassword.addTextChangedListener(passWatcher);
        mConfirmPass.addTextChangedListener(cnfpassWatcher);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                display_name = mDisplayName.getText().toString();
                email = mEmail.getText().toString();
                password = mPassword.getText().toString();
                confirmedPass = mConfirmPass.getText().toString();

                if (validateName() && validateEmail() && validatePass() && validateCnfPass()) {

                    //Progress Bar while connection establishes

                    mRegProgress = KProgressHUD.create(RegisterActivity.this)
                            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                            .setLabel("Please wait")
                            .setCancellable(false)
                            .setAnimationSpeed(2)
                            .setDimAmount(0.5f)
                            .show();

                    register_user(display_name, email, password);

                }
            }
        });

    }

    private void register_user(final String display_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = Objects.requireNonNull(current_user).getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    String device_token = FirebaseInstanceId.getInstance().getToken();


                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", display_name);
                    userMap.put("status", "Hi there I'm using  ChatX App.");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", "default");
                    userMap.put("device_token", device_token);

                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                mRegProgress.dismiss();

                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();

                            }

                        }
                    });

                } else {

                    mRegProgress.dismiss();
                    String error;
                    try {
                        throw Objects.requireNonNull(task.getException());
                    } catch (FirebaseAuthWeakPasswordException e) {
                        error = "Weak Password!";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        error = "Invalid Email";
                    } catch (FirebaseAuthUserCollisionException e) {
                        error = "Existing account!";
                    } catch (Exception e) {
                        error = "Unknown error!";
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private boolean validateCnfPass() {

        check = mConfirmPass.getText().toString();

        return check.equals(mPassword.getText().toString());
    }

    private boolean validatePass() {


        check = mConfirmPass.getText().toString();

        if (check.length() < 4 || check.length() > 20) {
            return false;
        } else if (!check.matches("^[A-za-z0-9@]+")) {
            return false;
        }
        return true;
    }

    private boolean validateEmail() {

        check = mEmail.getText().toString();

        if (check.length() < 4 || check.length() > 40) {
            return false;
        } else if (!check.matches("^[A-za-z0-9.@]+")) {
            return false;
        } else if (!check.contains("@") || !check.contains(".")) {
            return false;
        }

        return true;
    }

    private boolean validateName() {

        check = mDisplayName.getText().toString();

        return !(check.length() < 4 || check.length() > 20);

    }
}
