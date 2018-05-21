package com.admin.friendsandfamilylocator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NewUser extends AppCompatActivity {

    EditText email, userName;
    String incomingNumber;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        email = (EditText) findViewById(R.id.userEmailText);
        userName = (EditText) findViewById(R.id.userNameText);
        email.setText("");
        userName.setText("");

        Intent incomingIntent = getIntent();
        incomingNumber = incomingIntent.getStringExtra("number");
        Log.d("Phone Number:", incomingNumber+"");
    }

    public void createAccount(View view){

        if(email.getText().toString().equals("") || userName.getText().toString().equals("")){

            Toast.makeText(this, "All fields are required!!", Toast.LENGTH_LONG).show();

        }
        else{

            // Uploading to the cloud (firebase storage)
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReference();
            StorageReference myFile = storageReference.child("1");

            uri = Uri.parse("android.resource://com.admin.friendsandfamilylocator/drawable/dpicon");
            UploadTask myTask = myFile.putFile(uri);

            myTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //Inserting in the database

                    String photoUrlFromCloud = taskSnapshot.getDownloadUrl().toString();
                    Log.d("URL:", photoUrlFromCloud+"");

                    String eMail = email.getText().toString();
                    String user = userName.getText().toString();
                    Log.d("MYMSG",incomingNumber+" ");
                    final Users users = new Users(incomingNumber, eMail, user, photoUrlFromCloud, null, null, null, null, 0.0, 0.0);

                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference mainRef = firebaseDatabase.getReference("Users");

                    final DatabaseReference newUserForCloud = mainRef.child(incomingNumber);

                    newUserForCloud.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.getValue()==null){

                                newUserForCloud.setValue(users);

                                SharedPreferences tokenPreference = getSharedPreferences("mypref1", MODE_PRIVATE);
                                final String refreshedToken = tokenPreference.getString("devicetoken", null);

                                try
                                {
                                    final String packagenameofapp = getPackageName();


                                    final String cloudserverip = "server1.vmm.education";

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                URL url = new URL("http://" + cloudserverip + "/VMMCloudMessaging/RecordDeviceInfo?devicetoken=" + refreshedToken + "&packagenameofapp=" + packagenameofapp + "&mobileno=" + incomingNumber);
                                                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                                                int status = urlConnection.getResponseCode();
                                                Log.d("MYMESSAGE", "Response status " + status);

                                                if (status == 200) {
                                                    InputStream inputStream = urlConnection.getInputStream();

                                                    int conlength = urlConnection.getContentLength();

                                                    byte b[] = new byte[conlength];

                                                    inputStream.read(b, 0, conlength);

                                                    String ans = new String(b);
                                                    Log.d("MYMESSAGE", "ans from server " + ans);

                                                } else {
                                                    Log.d("MYMESSAGE", "ERROR -> " + status + " " + urlConnection.getResponseMessage());
                                                }
                                            }
                                            catch(Exception e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();
                                                                    }
                                catch(Exception ex)
                                {
                                    ex.printStackTrace();
                                }
runOnUiThread(new Runnable() {
    @Override
    public void run() {
        Toast.makeText(NewUser.this, "Account Created!!", Toast.LENGTH_LONG).show();

    }
});

                            }
                            else{
runOnUiThread(new Runnable() {
    @Override
    public void run() {
        Toast.makeText(NewUser.this, "Number already Exists!!", Toast.LENGTH_LONG).show();

    }
});

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });

            myTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(NewUser.this, "Uploading Failed!!", Toast.LENGTH_LONG).show();
                }
            });

            Intent mainAct = new Intent(this, MainActivity.class);
            startActivity(mainAct);

        }

    }
}
