package com.devanant.bee.UI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devanant.bee.Database.TinyDB;
import com.devanant.bee.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class PhoneLogin extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView editPhone, editOTP,state,logo_name,group;
    private LinearLayout emailLinear;
    private Button button,skip;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mPhone;
    private View parentLayout;
    private ProgressBar progressBar;
    private CountryCodePicker codePicker;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken token;
    private Boolean verificationInProgress=false;
    private int METHOD=0;
    private AuthCredential authCredential;
    private TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        //transition Time period
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getSharedElementEnterTransition().setDuration(800);
            getWindow().getSharedElementReturnTransition().setDuration(800)
                    .setInterpolator(new DecelerateInterpolator());
        }

        tinyDB=new TinyDB(getApplicationContext());

        parentLayout = findViewById(android.R.id.content);
        editPhone=findViewById(R.id.EditPhone);
        editOTP=findViewById(R.id.EditOTP);
        button=findViewById(R.id.btnOTP);
        state=findViewById(R.id.OTPStatus);
        codePicker=findViewById(R.id.textView_code);
        progressBar=findViewById(R.id.progressBar);
        logo_name=findViewById(R.id.logo_name);
        skip=findViewById(R.id.btnSkip);

        METHOD=getIntent().getIntExtra("Method",0);
        if(METHOD==0){
            authCredential= EmailAuthProvider.getCredential(getIntent().getStringExtra("email"), getIntent().getStringExtra("password"));
        }else{
            authCredential= GoogleAuthProvider.getCredential(getIntent().getStringExtra("GoogleIDToken"),null);
        }

        skip.setOnClickListener(v -> {
            Intent i=new Intent(PhoneLogin.this, CreateProfile.class);
            startActivity(i);
        });

        progressBar.setVisibility(View.INVISIBLE);

        //FirebaseAuth initialize
        mAuth= FirebaseAuth.getInstance();

        button.setOnClickListener(v -> {
            //phone get number
            mPhone=editPhone.getText().toString();
            if(!verificationInProgress){
                if(mPhone.length() == 10){

                    String phoneNo = "+"+codePicker.getSelectedCountryCode()+mPhone;
                    progressBar.setVisibility(View.VISIBLE);
                    state.setText("Sending OTP");
                    state.setVisibility(View.VISIBLE);
                    requestOTP(phoneNo);

                }else
                {
                    editPhone.setError("Phone number is not Valid");
                }
            }else {
                String userOTP = editOTP.getText().toString();

                if(userOTP.length() == 6){

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,userOTP);
                    linkCredential(credential);

                }else {
                    editOTP.setError("Valid OTP is required");
                }
            }
        });

    }

    public void linkCredential(AuthCredential credential) {
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            tinyDB.putString("PhoneNumber",mPhone);
                            Log.d("LinkCredential", "linkWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            Snackbar.make(parentLayout, "Authentication Successful", Snackbar.LENGTH_SHORT).show();
                            Intent i=new Intent(PhoneLogin.this, CreateProfile.class);
                            startActivity(i);

                        } else {
                            Snackbar.make(parentLayout, "Authentication Failed", Snackbar.LENGTH_SHORT).show();
                            Log.w("LinkCredential", "linkWithCredential:failure", task.getException());
                            Toast.makeText(PhoneLogin.this, "Failed to merge" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void requestOTP(String phoneNo) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNo, 60L, TimeUnit.SECONDS, this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                progressBar.setVisibility(View.GONE);
                state.setText("OTP sent");
                editOTP.setVisibility(View.VISIBLE);
                verificationId =  s;
                token = forceResendingToken;
                button.setText("Verify");
                verificationInProgress = true;
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                Toast.makeText(PhoneLogin.this, "Cannot create acount" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i=new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}