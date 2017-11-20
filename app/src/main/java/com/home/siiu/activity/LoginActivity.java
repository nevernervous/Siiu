package com.home.siiu.activity;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.home.siiu.R;
import com.home.siiu.data.StaticConfig;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {


    /*............................................................*/
    /*..........................Variables.........................*/
    /*............................................................*/


    /*..........................Constant..........................*/
    public static final String PREFS_EMAIL = "PREFERENCE_EMAIL";
    public static final String PREFS_PASSWORD = "PREFERENCE_PASWORD";
    public static final String PREFS_AUTO = "PREFERENCES_AUTO";

    /*..........................Firebase..........................*/
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private Boolean mAllowNavigation = false;

    /*..........................Component.........................*/
    private FloatingActionButton fabBtn;
    private EditText emailEditText, passwordEditText;

    /*..........................Wait Dialog.......................*/
    private LovelyProgressDialog waitingDialog;

    /*.....................Valid Email Pattern....................*/
    private final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);


    /*............................................................*/
    /*..........................Functions.........................*/
    /*............................................................*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initComponent();
        setEmailText();
        FirebaseAuthChange();
        autoLogin();
    }

    private void initComponent() {

        fabBtn = findViewById(R.id.fabBtn);
        emailEditText = findViewById(R.id.et_username);
        passwordEditText = findViewById(R.id.et_password);
    }

    private void setEmailText() {

        //Get User Email Preferences...
        SharedPreferences userEmail = getSharedPreferences(PREFS_EMAIL, Context.MODE_PRIVATE);
        emailEditText.setText(userEmail.getString("UserEmail", "").toString());
    }

    private void FirebaseAuthChange() {

        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d("", "onAuthStateChanged:signed_in:" + user.getUid());

                    if (mAllowNavigation) {
                        mAllowNavigation = false;

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                } else {
                    Log.d("", "onAuthStateChanged:signed_out");
                }
            }
        };
        waitingDialog = new LovelyProgressDialog(this).setCancelable(false);
    }

    public void clickResetPassword(View view) {

        final String email = emailEditText.getText().toString();
        if (isValidate(email, ";")) {

            waitingDialog.setIcon(R.drawable.ic_person_low)
                    .setTitle("Reset Password...")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();
            auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    waitingDialog.dismiss();
                    new LovelyInfoDialog(LoginActivity.this) {

                        public LovelyInfoDialog setConfirmButtonText(String text) {
                            findView(com.yarolegovich.lovelydialog.R.id.ld_btn_confirm).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    dismiss();
                                }
                            });
                            return super.setConfirmButtonText(text);
                        }
                    }
                            .setTopColorRes(R.color.colorPrimary)
                            .setIcon(R.drawable.ic_pass_reset)
                            .setTitle("Password Recovery")
                            .setMessage("Sent email to " + email)
                            .setConfirmButtonText("OK")
                            .show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    waitingDialog.dismiss();
                    new LovelyInfoDialog(LoginActivity.this) {
                        @Override
                        public LovelyInfoDialog setConfirmButtonText(String text) {
                            findView(com.yarolegovich.lovelydialog.R.id.ld_btn_confirm).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dismiss();
                                }
                            });
                            return super.setConfirmButtonText(text);
                        }
                    }
                            .setTopColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.ic_pass_reset)
                            .setTitle("Failed")
                            .setMessage("Failed to sent email to " + email)
                            .setConfirmButtonText("Ok")
                            .show();
                }
            });
        } else {

            Toast.makeText(LoginActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
        }
    }

    private void autoLogin() {

        SharedPreferences autoLogin = getSharedPreferences(PREFS_AUTO, Context.MODE_PRIVATE);
        if (autoLogin.getBoolean("AutoLogin", false)) {

            SharedPreferences emailPref = getSharedPreferences(PREFS_EMAIL, Context.MODE_PRIVATE);
            String userEmail = emailPref.getString("UserEmail", "");
            SharedPreferences passwordPref = getSharedPreferences(PREFS_PASSWORD, Context.MODE_PRIVATE);
            String userPassword = passwordPref.getString("UserPassword", "");
            passwordEditText.setText(userPassword);

            waitingDialog.setIcon(R.drawable.ic_person_low)
                    .setTitle("Login...")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();
            auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    waitingDialog.dismiss();
                    if (!task.isSuccessful()) {

                        new LovelyInfoDialog(LoginActivity.this) {

                            public LovelyInfoDialog setConfirmButtonText(String text) {
                                findView(com.yarolegovich.lovelydialog.R.id.ld_btn_confirm).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        dismiss();
                                    }
                                });
                                return super.setConfirmButtonText(text);
                            }
                        }
                                .setTopColorRes(R.color.colorAccent)
                                .setIcon(R.drawable.ic_person_low)
                                .setTitle("Login Failed")
                                .setMessage("Authentication Failed")
                                .setCancelable(false)
                                .setConfirmButtonText("OK")
                                .show();
                    } else {

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    waitingDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Authentication Failed: " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void clickLogin(View view) {

        final String email = emailEditText.getText().toString();
        final String password = passwordEditText.getText().toString();
        if (isValidate(email, password)) {

            waitingDialog.setIcon(R.drawable.ic_person_low)
                    .setTitle("Login...")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    waitingDialog.dismiss();
                    if (!task.isSuccessful()) {

                        new LovelyInfoDialog(LoginActivity.this) {

                            public LovelyInfoDialog setConfirmButtonText(String text) {
                                findView(com.yarolegovich.lovelydialog.R.id.ld_btn_confirm).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        dismiss();
                                    }
                                });
                                return super.setConfirmButtonText(text);
                            }
                        }
                            .setTopColorRes(R.color.colorAccent)
                            .setIcon(R.drawable.ic_person_low)
                            .setTitle("Login Failed")
                            .setMessage("Email not exist or wrong password!")
                            .setCancelable(false)
                            .setConfirmButtonText("OK")
                            .show();
                    } else {

                        SharedPreferences userEmail = getSharedPreferences(PREFS_EMAIL, Context.MODE_PRIVATE);
                        userEmail.edit().putString("UserEmail", email).commit();
                        SharedPreferences userPassword = getSharedPreferences(PREFS_PASSWORD, Context.MODE_PRIVATE);
                        userPassword.edit().putString("UserPassword", password).commit();
                        SharedPreferences autoLogin = getSharedPreferences(PREFS_AUTO, Context.MODE_PRIVATE);
                        autoLogin.edit().putBoolean("AutoLogin", true).commit();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    waitingDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Authentication Failed: " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {

            Toast.makeText(LoginActivity.this, "Invalid email or password must be more than 6 letters.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == StaticConfig.REQUEST_CODE_REGISTER && resultCode == RESULT_OK) {

        }
    }

    public void clickRegisterLayout(View view) {

        getWindow().setExitTransition(null);
        getWindow().setEnterTransition(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, fabBtn, fabBtn.getTransitionName());
            startActivityForResult(new Intent(this, RegisterActivity.class), StaticConfig.REQUEST_CODE_REGISTER, options.toBundle());
        } else {

            startActivityForResult(new Intent(this, RegisterActivity.class), StaticConfig.REQUEST_CODE_REGISTER);
        }

    }

    private boolean isValidate(String emailStr, String pw) {

        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return (pw.length() > 6 || pw.equals(";")) && matcher.find();
    }
}
