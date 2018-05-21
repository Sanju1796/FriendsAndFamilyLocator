package com.admin.friendsandfamilylocator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MeetingDetails extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    TextView mTitle, mCreatedBy, mDate, mTime, mMembers;
    ArrayList<String> members;
    String names, locationName, ownerName, owner;
    String meetingcode;
    double lat, lon;
    Toolbar meetingDetailsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_details);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        meetingDetailsToolbar = (Toolbar) findViewById(R.id.meetingDetailsToolbar);

        setSupportActionBar(meetingDetailsToolbar);

        getSupportActionBar();

        meetingDetailsToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
        final String num = sharedPreferences.getString("phoneNumber", null);

        mTitle = (TextView) findViewById(R.id.mTitle);
        mCreatedBy = (TextView) findViewById(R.id.mCreatedBy);
        mDate = (TextView) findViewById(R.id.mDate);
        mTime = (TextView) findViewById(R.id.mTime);
        mMembers = (TextView) findViewById(R.id.mMembers);

        mMembers.setMovementMethod(new ScrollingMovementMethod());

        Intent incomingCode = getIntent();
        meetingcode = incomingCode.getStringExtra("code");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference meetingRef = firebaseDatabase.getReference("Meetings").child(meetingcode);

        meetingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Meetings meeting = dataSnapshot.getValue(Meetings.class);

                lat = meeting.getLocationLat();
                lon = meeting.getLocationLon();

                locationName = meeting.getLocationName();

                mTitle.setText(meeting.getTitle());
                owner = meeting.getOwner();

                if (num.equals(owner)) {
                    mCreatedBy.setText("Me");
                } else {
                    FirebaseDatabase firebaseRef = FirebaseDatabase.getInstance();
                    DatabaseReference ownerRef = firebaseRef.getReference("Users").child(owner).child("userName");

                    ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ownerName = (String) dataSnapshot.getValue();
                            mCreatedBy.setText(ownerName);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                mDate.setText(meeting.getDate());
                mTime.setText(meeting.getTime());

                members = meeting.getMeetingMembers();

                for (int i = 0; i < members.size(); i++) {

                    String number = members.get(i);

                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference1 = firebaseDatabase.getReference("Users");

                    DatabaseReference name = databaseReference1.child(number).child("userName");

                    names = "";
                    name.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String membNames = (String) dataSnapshot.getValue();
                            names += membNames + ", ";
                            mMembers.setText(names);

                            LatLng myLocation = new LatLng(lat, lon);
                            mMap.addMarker(new MarkerOptions().position(myLocation).title(locationName));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                            CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(16);
                            mMap.animateCamera(cameraUpdate);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Inflate menu, adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.messageicon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //handles action bar item selected
        int id = item.getItemId();

        if (id == R.id.messages) {
            Intent sendMessage = new Intent(this, InboxActivity.class);
            sendMessage.putExtra("meetingCode", meetingcode);
            startActivity(sendMessage);

        }
        return true;
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in my current location and move the camera
    }
}
