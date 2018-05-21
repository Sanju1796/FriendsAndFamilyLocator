package com.admin.friendsandfamilylocator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ReverseGeocodingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Marker m;
    String locationName, meetingId, owner;
    double locationLat, locationLon;

    String groupCode, groupName, meetingTitle, meetingDate, meetingTime;

    ArrayList<String> meetingMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reverse_geocoding);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
        owner = sharedPreferences.getString("phoneNumber", null);

        Intent incomingIntent = getIntent();
        groupCode = incomingIntent.getStringExtra("grpCode");
        groupName = incomingIntent.getStringExtra("grpName");
        meetingTitle = incomingIntent.getStringExtra("title");
        meetingDate = incomingIntent.getStringExtra("date");
        meetingTime = incomingIntent.getStringExtra("time");

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if(m!=null)
                {
                    locationName = place.getName().toString();
                    LatLng latLng = place.getLatLng();
                    locationLat = latLng.latitude;
                    locationLon = latLng.longitude;
                    m = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(locationName));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 12.0f));
                }

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("MYMSG", "An error occurred: " + status);
            }
        });

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference membersRef = firebaseDatabase.getReference("Groups").child(groupCode).child("members");

        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                meetingMembers = (ArrayList<String>) dataSnapshot.getValue();
                if(meetingMembers == null){
                    meetingMembers = new ArrayList<>();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void schedule(View view){

        FirebaseDatabase fD = FirebaseDatabase.getInstance();
        DatabaseReference mainRef = fD.getReference("Meetings");

        meetingId = mainRef.push().getKey();

        DatabaseReference meetingRef = mainRef.child(meetingId);

        Meetings meeting = new Meetings(meetingId, meetingTitle, meetingDate, meetingTime, locationName, groupCode, locationLat, locationLon, meetingMembers, owner, null);

        meetingRef.setValue(meeting);

        for(int i=0; i<meetingMembers.size(); i++){

            String memberNumber = meetingMembers.get(i);

            FirebaseDatabase firebase = FirebaseDatabase.getInstance();
            DatabaseReference userRef = firebase.getReference("Users");

            final DatabaseReference meetingCodesRef = userRef.child(memberNumber).child("meetingCodes");
            final DatabaseReference meetingTitlesRef = userRef.child(memberNumber).child("meetingTitles");

            meetingCodesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    ArrayList<String> codes = (ArrayList<String>) dataSnapshot.getValue();
                    if(codes == null){
                        codes = new ArrayList<>();
                    }
                    codes.add(meetingId);

                    meetingCodesRef.setValue(codes);

                    meetingTitlesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            ArrayList<String> titles = (ArrayList<String>) dataSnapshot.getValue();
                            if(titles == null){
                                titles = new ArrayList<>();
                            }
                            titles.add(meetingTitle);

                            meetingTitlesRef.setValue(titles);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(31.6340, 74.8723);
        m = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(sydney.latitude,sydney.longitude), 12.0f));
    }
}

