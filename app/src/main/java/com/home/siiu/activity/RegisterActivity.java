package com.home.siiu.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.home.siiu.R;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {


    /*............................................................*/
    /*..........................Variables.........................*/
    /*............................................................*/


    /*..........................Constant..........................*/
    public static final String PREFS_EMAIL = "PREFERENCE_EMAIL";
    public static final String PREFS_PASSWORD = "PREFERENCE_PASWORD";
    public static final String PREFS_AUTO = "PREFERENCES_AUTO";

    /*.....................Valid Email Pattern....................*/
    private final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    /*..........................Firebase..........................*/
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth auth;

    /*..........................Component.........................*/
    private FloatingActionButton fabBtn;
    private CardView cvAdd;
    private EditText emailEditText;
    private EditText userNameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;

    /*..........................Wait Dialog.......................*/
    private LovelyProgressDialog waitingDialog;


    /*............................................................*/
    /*..........................Functions.........................*/
    /*............................................................*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initComponent();
        initFabBtn();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ShowEnterAnimation();
        }
    }

    private void initComponent() {

        cvAdd = findViewById(R.id.cv_add);
        emailEditText = findViewById(R.id.et_email);
        userNameEditText = findViewById(R.id.et_username);
        passwordEditText = findViewById(R.id.et_password);
        confirmPasswordEditText = findViewById(R.id.et_repeatpassword);
        waitingDialog = new LovelyProgressDialog(this).setCancelable(false);
    }

    private void initFabBtn() {

        fabBtn = findViewById(R.id.fabBtn);
        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateRevealClose();
            }
        });
    }

    private void ShowEnterAnimation() {
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.fabtransition);
        getWindow().setSharedElementEnterTransition(transition);

        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                cvAdd.setVisibility(View.GONE);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
                animateRevealShow();
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }


        });
    }

    public void animateRevealShow() {
        Animator mAnimator = ViewAnimationUtils.createCircularReveal(cvAdd, cvAdd.getWidth()/2,0, fabBtn.getWidth() / 2, cvAdd.getHeight());
        mAnimator.setDuration(500);
        mAnimator.setInterpolator(new AccelerateInterpolator());
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                cvAdd.setVisibility(View.VISIBLE);
                super.onAnimationStart(animation);
            }
        });
        mAnimator.start();
    }

    public void animateRevealClose() {
        Animator mAnimator = ViewAnimationUtils.createCircularReveal(cvAdd,cvAdd.getWidth()/2,0, cvAdd.getHeight(), fabBtn.getWidth() / 2);
        mAnimator.setDuration(500);
        mAnimator.setInterpolator(new AccelerateInterpolator());
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                cvAdd.setVisibility(View.INVISIBLE);
                super.onAnimationEnd(animation);
                fabBtn.setImageResource(R.drawable.ic_signup);
                RegisterActivity.super.onBackPressed();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
        mAnimator.start();
    }

    public void clickRegister(View view) {

        final String emailStr = emailEditText.getText().toString();
        final String userName = userNameEditText.getText().toString();
        final String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();
        if (isValidate(emailStr, userName, password, confirmPassword)) {

            //Get Firebase auth instance...
            auth = FirebaseAuth.getInstance();
            waitingDialog.setIcon(R.drawable.ic_add_friend)
                    .setTitle("Registering....")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();
            auth.createUserWithEmailAndPassword(emailStr, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    waitingDialog.dismiss();
                    if (!task.isSuccessful()) {

                        new LovelyInfoDialog(RegisterActivity.this) {
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
                                .setIcon(R.drawable.ic_add_friend)
                                .setTitle("Register Failed")
                                .setMessage("Email exist or weak password!")
                                .setConfirmButtonText("ok")
                                .setCancelable(false)
                                .show();
                    } else {

                        Map<String, Object> result = new HashMap<>();
                        result.put("userEmail", emailStr);
                        result.put("userName", userName);
                        dbRef.child("users").child(auth.getCurrentUser().getUid()).updateChildren(result);

                        SharedPreferences userEmail = getSharedPreferences(PREFS_EMAIL, Context.MODE_PRIVATE);
                        userEmail.edit().putString("UserEmail", emailStr).commit();
                        SharedPreferences userPasword = getSharedPreferences(PREFS_PASSWORD, Context.MODE_PRIVATE);
                        userPasword.edit().putString("UserPassword", password).commit();
                        SharedPreferences autoLogin = getSharedPreferences(PREFS_AUTO, Context.MODE_PRIVATE);
                        autoLogin.edit().putBoolean("AutoLogin", true).commit();

                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    waitingDialog.dismiss();
                }
            });
        } else {

            Toast.makeText(RegisterActivity.this, "Invalid email or Password is not match.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidate(String emailStr, String userName, String pw, String confirmPw) {

        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return pw.length() > 6 && userName.length() > 0 && confirmPw.equals(pw) && matcher.find();
    }
}
