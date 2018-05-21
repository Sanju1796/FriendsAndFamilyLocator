package com.admin.friendsandfamilylocator;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ViewHistory extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    Toolbar toolbar;

    EditText dateSelect;
    Button selectButton;
    String phoneNumber;
    ArrayList<LocationDB> locationHistoryList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_history);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        toolbar = (Toolbar) findViewById(R.id.locationHistoryToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent incomingPhoneNumber = getIntent();
        phoneNumber = incomingPhoneNumber.getStringExtra("phoneNumber");

        locationHistoryList = new ArrayList<>();

        selectButton = (Button) findViewById(R.id.selectButton);
        dateSelect = (EditText) findViewById(R.id.dateSelect);

        dateSelect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(ViewHistory.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }

    Calendar myCalendar = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        dateSelect.setText(sdf.format(myCalendar.getTime()));
    }


    public class GetHistory implements Runnable{

        @Override
        public void run() {

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference mainRef = firebaseDatabase.getReference("Users");

            final DatabaseReference locationRef = mainRef.child(phoneNumber).child("location");

            locationRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    locationHistoryList.clear();
                    mMap.clear();

                    for(DataSnapshot singleLocation: dataSnapshot.getChildren()){
                        LocationDB location = singleLocation.getValue(LocationDB.class);

                        if(location.getDate().equals(dateSelect.getText().toString())){
                            locationHistoryList.add(location);
                        }
                    }

                    if(locationHistoryList.size()>0) {

                        final PolylineOptions po = new PolylineOptions();
                        po.width(5);
                        po.color(Color.RED);

                        Log.d("MYMSG:", locationHistoryList.size() + "");
                        for (int i = 0; i < locationHistoryList.size(); i++) {
                            po.add(new LatLng(locationHistoryList.get(i).getLat(), locationHistoryList.get(i).getLon()));
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                mMap.addPolyline(po);

                                LatLng latLng = new LatLng(locationHistoryList.get(locationHistoryList.size() - 1).getLat(), locationHistoryList.get(locationHistoryList.size() - 1).getLon());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                                mMap.addMarker(new MarkerOptions().position(latLng));

                            }
                        });
                    }
                    else{

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ViewHistory.this, "No Path tread on this day!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    public void showHistory(View view){
        new Thread(new GetHistory()).start();
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


       // Add a marker in my current location and move the camera

    }

}
