package com.admin.friendsandfamilylocator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.cast.CastRemoteDisplayLocalService.getInstance;

public class SignIn extends AppCompatActivity {

    EditText number, otpText;
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
        setContentView(R.layout.activity_sign_in);

        number = (EditText) findViewById(R.id.number);
        otpText = (EditText) findViewById(R.id.otp);
        codeText = (TextView) findViewById(R.id.codeText);
        verifyButton = (Button) findViewById(R.id.verifyButton);

        number.setText("");
        otpText.setText("");

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
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        progress.dismiss();
                        Toast.makeText(SignIn.this, "Verification Complete!!", Toast.LENGTH_SHORT).show();

                    }
                });

                otpText.setText(credential.getSmsCode());
                //et2.setVisibility(View.INVISIBLE);
                codeText.setVisibility(View.INVISIBLE);
                verifyButton.setVisibility(View.INVISIBLE);
                Log.d("MYMSG", "verification completed");

                Toast.makeText(SignIn.this, "Account Verified!!", Toast.LENGTH_LONG).show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        editor.putString("phoneNumber", number.getText().toString());
                        editor.commit();

                        SharedPreferences tokenPreference = getSharedPreferences("mypref1", MODE_PRIVATE);
                        String refreshedToken = tokenPreference.getString("devicetoken", null);

                        try
                        {
                            String packagenameofapp = getPackageName();


                            String cloudserverip = "server1.vmm.education";

                            URL url = new URL("http://"+ cloudserverip +"/VMMCloudMessaging/RecordDeviceInfo?devicetoken="+refreshedToken+"&packagenameofapp="+packagenameofapp+"&mobileno="+userPhoneNumber);
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                            int status = urlConnection.getResponseCode();
                            Log.d("MYMESSAGE","Response status "+ status);

                            if(status==200)
                            {
                                InputStream inputStream = urlConnection.getInputStream();

                                int conlength = urlConnection.getContentLength();

                                byte b[]=new byte[conlength];

                                inputStream.read(b,0,conlength);

                                String ans=new String(b);
                                Log.d("MYMESSAGE","ans from server "+ans);


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent homeActivity = new Intent(SignIn.this, Home.class);

                                        Intent locationService = new Intent(getApplicationContext(), LocationService.class);
                                        startService(locationService);

                                        startActivity(homeActivity);
                                        finish();
                                    }
                                });
                            }
                            else
                            {
                                Log.d("MYMESSAGE","ERROR -> "+status+" "+urlConnection.getResponseMessage());
                            }
                        }
                        catch(Exception ex)
                        {
                            ex.printStackTrace();
                        }

                    }
                }).start();

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                progress.dismiss();
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.d("MYMSG", "onVerificationFailed");
                Toast.makeText(SignIn.this, "Number Invalid!!", Toast.LENGTH_LONG);
            }

            @Override
            public void onCodeSent(String VerificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SignIn.this, "Code Sent!!", Toast.LENGTH_SHORT).show();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                            }
                        });
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

    public void send() {
        otpText.setText("");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setTitle("OTP");
                progress.setMessage("Sending OTP!!");
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


        Log.d("MYMSG", "Phone Number Verification Started");

    }

    public void verify(View view) {
        if(otpText.getText().equals("")){
            Toast.makeText(this, "Enter a valid OTP pin!", Toast.LENGTH_SHORT).show();
        }
        else {

            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = false;
            boolean network_enabled = false;

            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch(Exception ex) {}

            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch(Exception ex) {}

            Log.d("MYMSG",gps_enabled+" "+network_enabled+" -----------");
            if(!gps_enabled && !network_enabled) {
                // notify user
                AlertDialog.Builder dialog = new AlertDialog.Builder(SignIn.this);
                dialog.setMessage("Location Service is not Enabled, Do you want to Enable it?");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                        Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                        //get gps
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub

                    }
                });

                        dialog.show();

            }
            else {

                String code = otpText.getText().toString();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.setTitle("Verify");
                        progress.setMessage("Verifying your OTP!!");
                        progress.show();
                    }
                });

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                signInWithPhoneAuthCredential(credential);
            }
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

                            otpText.setText(credential.getSmsCode());
                            codeText.setVisibility(View.INVISIBLE);
                            verifyButton.setVisibility(View.INVISIBLE);

                            Toast.makeText(SignIn.this, "Account Verified!!", Toast.LENGTH_LONG).show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();

                                    editor.putString("phoneNumber", number.getText().toString());
                                    editor.commit();

                                    SharedPreferences tokenPreference = getSharedPreferences("mypref1", MODE_PRIVATE);
                                    String refreshedToken = tokenPreference.getString("devicetoken", null);

                                    try
                                    {
                                        String packagenameofapp = getPackageName();


                                        String cloudserverip = "server1.vmm.education";

                                        URL url = new URL("http://"+ cloudserverip +"/VMMCloudMessaging/RecordDeviceInfo?devicetoken="+refreshedToken+"&packagenameofapp="+packagenameofapp+"&mobileno="+userPhoneNumber);
                                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                                        int status = urlConnection.getResponseCode();
                                        Log.d("MYMESSAGE","Response status "+ status);

                                        if(status==200)
                                        {
                                            InputStream inputStream = urlConnection.getInputStream();

                                            int conlength = urlConnection.getContentLength();

                                            byte b[]=new byte[conlength];

                                            inputStream.read(b,0,conlength);

                                            String ans=new String(b);
                                            Log.d("MYMESSAGE","ans from server "+ans);

                                        }
                                        else
                                        {
                                            Log.d("MYMESSAGE","ERROR -> "+status+" "+urlConnection.getResponseMessage());
                                        }
                                    }
                                    catch(Exception ex)
                                    {
                                        ex.printStackTrace();
                                    }


                                }
                            }).start();

                            Intent homeActivity = new Intent(SignIn.this, Home.class);

                            Intent locationService = new Intent(getApplicationContext(), LocationService.class);
                            startService(locationService);

                            startActivity(homeActivity);
                            finish();

                        } else {
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(SignIn.this, "Invalid code", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });
    }

    public void firebaseCheck(View view) {

        if (number.getText().toString().equals("")) {
            Log.d("MYMSG: ", "button Clicked!!");
            Toast.makeText(SignIn.this, "Enter Phone Number!!", Toast.LENGTH_SHORT).show();
        } else {

            if (isOnline()) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.setTitle("Verify");
                        progress.setMessage("Verifying your Mobile Number!!");
                        progress.show();
                    }
                });
                //Toast.makeText(SignIn.this, "You are connected to Internet", Toast.LENGTH_SHORT).show();

                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference mainRef = firebaseDatabase.getReference("Users");

                final DatabaseReference alreadyExistingUserOnCloud = mainRef.child(number.getText().toString());

                alreadyExistingUserOnCloud.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() == null) {

                            Toast.makeText(SignIn.this, "Invalid Number!! SignUp First!!", Toast.LENGTH_LONG).show();

                        } else {
                            send();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else {
                Toast.makeText(SignIn.this, "You are not connected to Internet", Toast.LENGTH_SHORT).show();
            }

        }
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
