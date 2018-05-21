package com.admin.friendsandfamilylocator;

import android.content.Intent;
import android.graphics.Color;
import android.os.TransactionTooLargeException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LiveTracking extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String phoneNumber;
    ArrayList<LocationDB> locationsList;
    Marker m;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_tracking);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        toolbar = (Toolbar) findViewById(R.id.liveTrackingToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        locationsList = new ArrayList<>();

        Intent incomingPhoneNumber = getIntent();
        phoneNumber = incomingPhoneNumber.getStringExtra("phoneNumber");

    }

    public class GetLocation implements Runnable{

        @Override
        public void run() {

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference mainRef = firebaseDatabase.getReference("Users");

            final DatabaseReference locationRef = mainRef.child(phoneNumber).child("location");

            locationRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    locationsList.clear();

                    for(DataSnapshot singleLocation: dataSnapshot.getChildren()){
                        LocationDB location = singleLocation.getValue(LocationDB.class);

                        long currentTime = System.currentTimeMillis();
                        String time = Long.toString(currentTime);
                        java.sql.Date d = new java.sql.Date(currentTime);
                        String date = d.toString();

                        if(location.getDate().equals(date)){
                            locationsList.add(location);
                        }
                    }

                    final PolylineOptions po = new PolylineOptions();
                    po.width(5);
                    po.color(Color.RED);

                    Log.d("MYMSG:", locationsList.size()+"");
                    for(int i=0; i<locationsList.size(); i++){
                        po.add(new LatLng(locationsList.get(i).getLat(), locationsList.get(i).getLon()));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(m!=null)
                            {
                                m.remove();
                            }
                            mMap.addPolyline(po);

                            if(locationsList.size()>0) {

                                LatLng latLng = new LatLng(locationsList.get(locationsList.size() - 1).getLat(), locationsList.get(locationsList.size() - 1).getLon());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                                m = mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Amritsar"));
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in my current location and move the camera

        new Thread(new GetLocation()).start();
    }
}
