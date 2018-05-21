package com.admin.friendsandfamilylocator;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_GROUP_PERMISSION = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M)

        {

            //Check If Permissions are already granted, otherwise show Ask Permission Dialog

            if(checkPermission())

            {

                Toast.makeText(this, "All Permissions Already Granted", Toast.LENGTH_SHORT).show();

            }

            else

            {

                Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_SHORT).show();

                requestPermission();

            }

        }
        SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);

        String phoneNumber = sharedPreferences.getString("phoneNumber", null);

        if(phoneNumber == null || phoneNumber.equals("")){

        }
        else{

            finish();
            Intent homeActivity = new Intent(this, Home.class);
            startActivity(homeActivity);

        }

    }

    public void signUpButton(View view){

        Intent signUp = new Intent(this, SignUp.class);
        startActivity(signUp);

    }

    public void signInButton(View view){

        Intent signIn = new Intent(this, SignIn.class);
        startActivity(signIn);

    }


    public boolean checkPermission()

    {

        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED;

        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;

        boolean result3 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)== PackageManager.PERMISSION_GRANTED;

        boolean result4 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED;

        boolean result5 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED;

        return result1 && result2 && result3 && result4 && result5;

    }

    public void requestPermission()

    {

        //Show ASK FOR PERSMISSION DIALOG (passing array of permissions that u want to ask)

        ActivityCompat.requestPermissions(this,

                new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);

    }



    // After User Selects Desired Permissions, thid method is automatically called

    // It has request code, permissions array and corresponding grantresults array

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)

    {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);



        if(requestCode==1)

        {

            if (grantResults.length > 0)

            {


                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)

                {

                    Toast.makeText(this, "All PERMISSON GRANTED", Toast.LENGTH_SHORT).show();

                }

            }
        }

    }
}
