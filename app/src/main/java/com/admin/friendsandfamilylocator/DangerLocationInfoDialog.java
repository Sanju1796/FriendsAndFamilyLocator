package com.admin.friendsandfamilylocator;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class DangerLocationInfoDialog extends AppCompatActivity implements OnMapReadyCallback {

    String code, address;

    private GoogleMap mMap;

    double lat, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danger_location_info_dialog);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent incomingCode = getIntent();
        code = incomingCode.getStringExtra("code");
        address = incomingCode.getStringExtra("address");
        lat = incomingCode.getDoubleExtra("latitude", 0.0);
        lon = incomingCode.getDoubleExtra("longitude", 0.0);

    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in my current location and move the camera
        LatLng myLocation = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(myLocation).title(address));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(14);
        mMap.animateCamera(cameraUpdate);

    }
}
