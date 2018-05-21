package com.admin.friendsandfamilylocator;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyDangerLocations extends AppCompatActivity {

    Toolbar toolbar;

    ListView dangerLocationsList;

    String phoneNumber;

    ArrayList<DangerLocation> dangerLocationsListByMe;

    MyDangerLocationsAdapter myDangerLocationsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_danger_locations);

        SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("phoneNumber", null);

        toolbar = (Toolbar) findViewById(R.id.mydangerLocationsToolbar);
        dangerLocationsList = (ListView) findViewById(R.id.myMarkedDangerLocationslist);

        dangerLocationsListByMe = new ArrayList<>();

        setSupportActionBar(toolbar);
        getSupportActionBar();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        myDangerLocationsAdapter = new MyDangerLocationsAdapter();
        dangerLocationsList.setAdapter(myDangerLocationsAdapter);

        new Thread(new MyDangerLocationsList()).start();


    }

    public class MyDangerLocationsList implements Runnable{

        @Override
        public void run() {

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference dangerLocationsRef = firebaseDatabase.getReference("DangerLocations");

            dangerLocationsRef.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    dangerLocationsListByMe.clear();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myDangerLocationsAdapter.notifyDataSetChanged();
                        }
                    });
                    for(DataSnapshot singleDangerLocation: dataSnapshot.getChildren()){

                        DangerLocation dangerLocation = singleDangerLocation.getValue(DangerLocation.class);
                        if(dangerLocation.getMarkedBy_PhoneNumber().equals(phoneNumber)){

                            dangerLocationsListByMe.add(dangerLocation);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    myDangerLocationsAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    public class MyDangerLocationsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return dangerLocationsListByMe.size();
        }

        @Override
        public Object getItem(int i) {
            return dangerLocationsListByMe.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i * 10;
        }

        @Override
        public View getView(final int i, View customView, ViewGroup parent) {

            if (customView == null) {
                LayoutInflater inflater = LayoutInflater.from(MyDangerLocations.this);
                customView = inflater.inflate(R.layout.mydangerlocationslist, parent, false);
            }

            TextView myLocationAddress, myReason;
            ImageView deleteMyLocation, infoOfMyLocation;

            myLocationAddress = (TextView) customView.findViewById(R.id.myLocationAddress);
            myReason = (TextView) customView.findViewById(R.id.myReason);
            deleteMyLocation = (ImageView) customView.findViewById(R.id.deleteMyLocation);
            infoOfMyLocation = (ImageView) customView.findViewById(R.id.infoOfMyLocation);

            myLocationAddress.setText(dangerLocationsListByMe.get(i).getLocationAddress());
            myReason.setText("Reason: "+dangerLocationsListByMe.get(i).getReason());

            final String code = (dangerLocationsListByMe.get(i).getLocationId());

            deleteMyLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(MyDangerLocations.this);
                    builder.setTitle("Delete Message");
                    builder.setIcon(R.drawable.ic_delete_black_24dp);
                    builder.setMessage("Are you sure you want to delete this Danger Location?");
                    builder.setCancelable(false);

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int pos) {

                            FirebaseDatabase firebase = FirebaseDatabase.getInstance();
                            DatabaseReference database = firebase.getReference("DangerLocations").child(code);
                            database.removeValue();
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int pos) {

                        }
                    });

                    AlertDialog ad = builder.create();
                    ad.show();
                }
            });

            infoOfMyLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent dangerLocationInfoDialog = new Intent(MyDangerLocations.this, DangerLocationInfoDialog.class);
                    dangerLocationInfoDialog.putExtra("code", code);
                    dangerLocationInfoDialog.putExtra("address", dangerLocationsListByMe.get(i).getLocationAddress());
                    dangerLocationInfoDialog.putExtra("latitude", dangerLocationsListByMe.get(i).getLat());
                    dangerLocationInfoDialog.putExtra("longitude", dangerLocationsListByMe.get(i).getLon());
                    startActivity(dangerLocationInfoDialog);
                }
            });

            return customView;
        }

    }


}
