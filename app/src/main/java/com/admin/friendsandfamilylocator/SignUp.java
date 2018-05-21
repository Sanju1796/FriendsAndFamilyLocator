package com.admin.friendsandfamilylocator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

public class SignUp extends AppCompatActivity {

    EditText number, otpText;
    String newUserNumber;
    TextView codeText;
    Button verifyButton;
    String verificationId;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    String userPhoneNumber;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        number = (EditText) findViewById(R.id.number);
        otpText = (EditText) findViewById(R.id.otp);
        codeText = (TextView) findViewById(R.id.codeText);
        verifyButton = (Button) findViewById(R.id.verifyButton);

        codeText.setVisibility(View.INVISIBLE);
        otpText.setVisibility(View.INVISIBLE);
        verifyButton.setVisibility(View.INVISIBLE);

        progress= new ProgressDialog(this);
        progress.setTitle("");
        progress.setMessage("");
        progress.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // onVerificationCompleted is Auto Called if Auto Detection of SMS is done

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                    }
                });
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.setTitle("OTP");
                        progress.setMessage("Getting OTP...");
                    }
                });
                otpText.setText(credential.getSmsCode());
                //et2.setVisibility(View.INVISIBLE);
                codeText.setVisibility(View.INVISIBLE);
                verifyButton.setVisibility(View.INVISIBLE);
                Log.d("MYMSG", "verification completed");

                Intent newUser = new Intent(SignUp.this, NewUser.class);
                newUserNumber = number.getText().toString();
                newUser.putExtra("number", newUserNumber);
                number.setText("");
                otpText.setText("");
                startActivity(newUser);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                    }
                });
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.d("MYMSG", "onVerificationFailed");
                Toast.makeText(SignUp.this, "Number Invalid!!", Toast.LENGTH_LONG);
            }

            @Override
            public void onCodeSent(String VerificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                    }
                });
                Log.d("MYMSG", "code sent " + verificationId);
                verificationId = VerificationId;
                otpText.setVisibility(View.VISIBLE);
                codeText.setVisibility(View.VISIBLE);
                verifyButton.setVisibility(View.VISIBLE);
            }
        };
    }

    public void send(View view) {
        otpText.setText("");

        if (number.getText().toString().equals("") || (number.getText().toString().length()!=10)) {
            Toast.makeText(this, "Enter a valid Phone Number!!", Toast.LENGTH_SHORT).show();
        } else {
                if (isOnline()) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.setTitle("Verify");
                            progress.setMessage("Verifying your Number...");
                            progress.show();
                        }
                    });

                    userPhoneNumber = number.getText().toString();

                    // We Register mCallbacks which are attached to verification process
                    // Which will try to Authenticate Automatically
                    // Otherwise we might need to fill code manually and Then Click Verify

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            userPhoneNumber,        // Phone number to verify
                            120,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            this,               // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks


                    Log.d("MYMSG", "Phone Number Veification Started");
                }
                else{
                    Toast.makeText(this, "You are not connected to Internet!!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    public void verify(View view) {

        if (otpText.getText().toString().equals("")) {
            Toast.makeText(SignUp.this, "Field cannot be Empty!!", Toast.LENGTH_LONG).show();
        }
        else {
            String code = otpText.getText().toString();

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithPhoneAuthCredential(credential);

            Intent newUser = new Intent(this, NewUser.class);
            newUserNumber = number.getText().toString();
            newUser.putExtra("number", newUserNumber);
            number.setText("");
            otpText.setText("");
            startActivity(newUser);
        }
    }


    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progress.dismiss();
                                }
                            });
                            //Toast.makeText(MainActivity.this, "Phone Verified", Toast.LENGTH_SHORT).show();

                            otpText.setText(credential.getSmsCode());
                            //et2.setVisibility(View.INVISIBLE);
                            codeText.setVisibility(View.INVISIBLE);
                            verifyButton.setVisibility(View.INVISIBLE);


                        } else {
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(SignUp.this, "Invalid code", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }

    }
}
