package com.admin.friendsandfamilylocator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewDangerLocations extends AppCompatActivity implements OnMapReadyCallback {

    Toolbar toolbar;

    private GoogleMap mMap;
    private Circle mCircle;
    double lat,lon;
    String title="";
    ViewDangerLocationsAdapter viewDangerLocationsAdapter;

    ListView viewDangerLocationsList;

    ArrayList<DangerLocation> dangerLocationsFromDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_danger_locations);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        toolbar = (Toolbar) findViewById(R.id.dToolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        viewDangerLocationsList = (ListView) findViewById(R.id.dangerLocationsList);

        dangerLocationsFromDatabase = new ArrayList<>();

        viewDangerLocationsAdapter = new ViewDangerLocationsAdapter();
        viewDangerLocationsList.setAdapter(viewDangerLocationsAdapter);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference dangerLocationsRef = firebaseDatabase.getReference("DangerLocations");

        dangerLocationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleLocation: dataSnapshot.getChildren()){
                    DangerLocation dangerLocation = singleLocation.getValue(DangerLocation.class);

                    dangerLocationsFromDatabase.add(dangerLocation);

                    viewDangerLocationsAdapter.notifyDataSetChanged();

                    lat = dangerLocation.getLat();
                    lon = dangerLocation.getLon();
                    title = dangerLocation.getLocationAddress();
                    LatLng myLocation = new LatLng(lat, lon);
                    mMap.addMarker(new MarkerOptions().position(myLocation).title(title));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

                    double radiusInMeters = 500.0;
                    int strokeColor = 0xffff0000; //red outline
                    int shadeColor = 0x44ff0000; //opaque red fill

                    CircleOptions circleOptions = new CircleOptions().center(myLocation).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
                    mCircle = mMap.addCircle(circleOptions);
                }

                CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(6);
                mMap.animateCamera(cameraUpdate);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in my current location and move the camera
    }

    public class ViewDangerLocationsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return dangerLocationsFromDatabase.size();
        }

        @Override
        public Object getItem(int i) {
            return dangerLocationsFromDatabase.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i * 10;
        }

        @Override
        public View getView(int i, View customView, ViewGroup parent) {

            if (customView == null) {
                LayoutInflater inflater = LayoutInflater.from(ViewDangerLocations.this);
                customView = inflater.inflate(R.layout.dangerlocationsinglerow, parent, false);
            }

            TextView dAddress, dReason, dName;
            ImageView dMarkersPic;

            dAddress = (TextView) customView.findViewById(R.id.dAddress);
            dReason = (TextView) customView.findViewById(R.id.dReason);
            dName = (TextView) customView.findViewById(R.id.dMarkerName);
            dMarkersPic = (ImageView) customView.findViewById(R.id.dMarkersPic);

            dAddress.setText(dangerLocationsFromDatabase.get(i).getLocationAddress());
            dReason.setText(dangerLocationsFromDatabase.get(i).getReason());
            dName.setText(dangerLocationsFromDatabase.get(i).getMarkedBy_Name());
            Picasso.with(ViewDangerLocations.this).load(dangerLocationsFromDatabase.get(i).getMarkedBy_photo()).into(dMarkersPic);

            return customView;
        }

    }
}
