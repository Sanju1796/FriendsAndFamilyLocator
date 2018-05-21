package com.admin.friendsandfamilylocator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class ChangePhotoDialogActivity extends AppCompatActivity {

    ImageView previewImage;
    Uri uri;
    String iuri;
    String phoneNumber;
    Button cancel, ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_change_photo_dialog);

        previewImage = (ImageView) findViewById(R.id.preview);
        cancel = (Button) findViewById(R.id.cancelButton);
        ok = (Button) findViewById(R.id.okbutton);

        Intent incominguri = getIntent();
        iuri = incominguri.getStringExtra("uri");
        uri = Uri.parse(iuri);
        Picasso.with(ChangePhotoDialogActivity.this).load(uri).into(previewImage);

        SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("phoneNumber", null);

    }

    public void ok(View view){

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        StorageReference myFile = storageReference.child(phoneNumber+".jpg");

        UploadTask myTask = myFile.putFile(uri);

        myTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //Inserting in the database

                String photoUrlFromCloud = taskSnapshot.getDownloadUrl().toString();
                Log.d("URL:", photoUrlFromCloud+"");

                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference mainRef = firebaseDatabase.getReference("Users");

                DatabaseReference userFromCloud = mainRef.child(phoneNumber);

                DatabaseReference changeUserPhoto = userFromCloud.child("photo");

                changeUserPhoto.setValue(photoUrlFromCloud);

                Toast.makeText(ChangePhotoDialogActivity.this, "Photo Updated!!", Toast.LENGTH_LONG).show();
                finish();
            }
        });

    }

    public void cancel(View view){
        finish();
    }

}
