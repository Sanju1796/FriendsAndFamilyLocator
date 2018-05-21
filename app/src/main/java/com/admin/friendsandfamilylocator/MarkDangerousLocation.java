package com.admin.friendsandfamilylocator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MarkDangerousLocation extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    double lat, lon, lastLat, lastLon;

    Toolbar toolbar;

    String locationAddress, phoneNumber, userName, userPhoto;

    EditText reason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_dangerous_location);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        toolbar = (Toolbar) findViewById(R.id.markDangerousLocationToolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        reason = (EditText) findViewById(R.id.reason);

        SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("phoneNumber", null);

    }

        @Override
        public boolean onCreateOptionsMenu (Menu menu){

            //Inflate menu, adds items to the action bar if it is present
            getMenuInflater().inflate(R.menu.dangerlocationmenu, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item){

            //handles action bar item selected
            int id = item.getItemId();

            if (id == R.id.dangerLocationMenu) {
                Intent myDangerLocations = new Intent(this, MyDangerLocations.class);
                startActivity(myDangerLocations);
            }
            return true;
        }

    public void dangerLocationButton(View view){

        if(reason.getText().toString().equals("")){
            Toast.makeText(this, "Enter a Reason!!", Toast.LENGTH_SHORT).show();
        }
        else {

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference dangerLocationRef = firebaseDatabase.getReference("DangerLocations");

            final String key = dangerLocationRef.push().getKey();
            final DatabaseReference groupRef = dangerLocationRef.child(key);

            DangerLocation dangerLocation = new DangerLocation(key, locationAddress, phoneNumber, userName, userPhoto, reason.getText().toString(), lat, lon);

            groupRef.setValue(dangerLocation);

            Toast.makeText(this, "Danger Location Added!!", Toast.LENGTH_SHORT).show();

            reason.setText("");
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in my current location and move the camera
        FirebaseDatabase firebase = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = firebase.getReference("Users").child(phoneNumber);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                userName = user.getUserName();
                userPhoto = user.getPhoto();
                lastLat = user.getLastLatitude();
                lastLon = user.getLastLongitude();

                LatLng myLocation = new LatLng(lastLat, lastLon);
                mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location!!"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(16);
                mMap.animateCamera(cameraUpdate);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Setting a click event handler for the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                // This will be displayed on taping the marker
                lat = latLng.latitude;
                lon = latLng.longitude;

                locationAddress="";

                Geocoder geocoder = new Geocoder(MarkDangerousLocation.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                    if(addresses.size()>0) {
                        Address obj = addresses.get(0);
                        locationAddress = obj.getAddressLine(0);
                        locationAddress = locationAddress + "\n" + obj.getCountryName();
                        locationAddress = locationAddress + "\n" + obj.getCountryCode();
                        locationAddress = locationAddress + "\n" + obj.getAdminArea();
                        locationAddress = locationAddress + "\n" + obj.getPostalCode();
                        locationAddress = locationAddress + "\n" + obj.getSubAdminArea();
                        locationAddress = locationAddress + "\n" + obj.getLocality();
                        locationAddress = locationAddress + "\n" + obj.getSubThoroughfare();
                    }

                    Log.v("IGA", "Address" + locationAddress);
                    // Toast.makeText(this, "Address=>" + add,
                    // Toast.LENGTH_SHORT).show();

                    // TennisAppActivity.showDialog(add);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                markerOptions.title(locationAddress);

                // Clears the previously touched position
                mMap.clear();

                // Animating to the touched position
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);
            }
        });
    }
}
