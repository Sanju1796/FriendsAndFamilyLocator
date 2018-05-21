package com.admin.friendsandfamilylocator;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Home extends AppCompatActivity implements OnMapReadyCallback {

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    ImageView profile;
    TextView name, members;
    String phoneNumber, names, user="", phnNumbers="", locationAddress;
    private GoogleMap mMap;
    double lat, lon, sendLat, sendLon;;
    Spinner spinner;
    SpinnerAdapter spinnerAdapter;
    LocationReceiver myreceiver;
    Marker m;
    Uri uri;
    int count=0, p=0;
    ArrayList<String> al_ids = new ArrayList<>();
    ArrayList<ViewGroups> groupsFromDatabase;
    ArrayList<String> grpCodesFromUsers;
    ArrayList<String> grpNamesFromUsers;
    ArrayList<String> listOfGroupMembers;
    ArrayList<Users> membersOfGroupOnMapList;
    ArrayList<String> lastGroupCodesList;

    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.homeMap);
        mapFragment.getMapAsync(this);

        SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("phoneNumber", null);

        groupsFromDatabase = new ArrayList<>();
        grpCodesFromUsers = new ArrayList<>();
        grpNamesFromUsers = new ArrayList<>();
        membersOfGroupOnMapList = new ArrayList<>();
        lastGroupCodesList = new ArrayList<>();

        toolbar = (Toolbar) findViewById(R.id.Hometoolbar);
        spinner = (Spinner) findViewById(R.id.groupSpinner);
        members = (TextView) findViewById(R.id.members);

        //replace action bar with toolbar
        setSupportActionBar(toolbar);

        getSupportActionBar();
        progress= new ProgressDialog(this);
        progress.setTitle("");
        progress.setMessage("Loading...");
        progress.setCancelable(false);
        progress.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseDatabase firebase = FirebaseDatabase.getInstance();
                DatabaseReference userRef = firebase.getReference("Users").child(phoneNumber).child("userName");

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        user = (String)dataSnapshot.getValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        }).start();


        spinnerAdapter = new SpinnerAdapter();
        spinner.setAdapter(spinnerAdapter);

        new Thread(new MyGroupsList()).start();

        // Create Navigation drawer and inflate layout
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        View inflatedView = navigationView.inflateHeaderView(R.layout.header);
        name = (TextView) inflatedView.findViewById(R.id.profileName);
        profile = (ImageView) inflatedView.findViewById(R.id.profilePic);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference mainRef = firebaseDatabase.getReference("Users");

                DatabaseReference myRef = mainRef.child(phoneNumber);

                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Users u = dataSnapshot.getValue(Users.class);
                                name.setText(u.getUserName());
                                toolbar.setTitle("Welcome "+ (u.getUserName()).toUpperCase());
                                Picasso.with(Home.this).load(u.getPhoto()).into(profile);
                            }
                        });

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        }).start();

        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.menuicon);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

// Set behavior of Navigation drawer
        navigationView.setNavigationItemSelectedListener
                (
                        new NavigationView.OnNavigationItemSelectedListener() {
                            // This method will trigger on item Click of navigation menu
                            @Override
                            public boolean onNavigationItemSelected(MenuItem menuItem) {
                                // Set item in checked state
                                // menuItem.setChecked(true);
                                // TODO: handle navigation
                                // Closing drawer on item click

                                if (menuItem.getItemId() == R.id.newGroup) {
                                    //Toast.makeText(Home.this, "New Group", Toast.LENGTH_SHORT).show();
                                    Intent newGroup = new Intent(Home.this, NewGroup.class);
                                    startActivity(newGroup);

                                } else if (menuItem.getItemId() == R.id.myInvitations) {
                                    //Toast.makeText(Home.this, "My Invitations Clicked!!", Toast.LENGTH_SHORT).show();
                                    Intent myInvitations = new Intent(Home.this, MyInvitations.class);
                                    startActivity(myInvitations);
                                } else if (menuItem.getItemId() == R.id.myGroups) {
                                    //Toast.makeText(Home.this, "My Groups Clicked!!", Toast.LENGTH_SHORT).show();
                                    Intent myGroups = new Intent(Home.this, MyGroups.class);
                                    startActivity(myGroups);
                                }
                                else if(menuItem.getItemId() == R.id.meetingPlan){
                                    // Toast.makeText(Home.this, "Meeting Clicked!!", Toast.LENGTH_SHORT).show();
                                    Intent meeting = new Intent(Home.this, MeetingPlan.class);
                                    startActivity(meeting);
                                }
                                else if(menuItem.getItemId() == R.id.viewMeetings){
                                    //Toast.makeText(Home.this, "View Meetings Clicked!!", Toast.LENGTH_SHORT).show();
                                    Intent viewMeetings = new Intent(Home.this, ViewMeetings.class);
                                    startActivity(viewMeetings);
                                }
                                else if(menuItem.getItemId() == R.id.share) {
                                    // Toast.makeText(Home.this, "Share Clicked!!", Toast.LENGTH_SHORT).show();
                                    Intent sendIntent = new Intent();
                                    sendIntent.setAction(Intent.ACTION_SEND);
                                    sendIntent.putExtra(Intent.EXTRA_TEXT,
                                            "Hey check out my app at: https://play.google.com/store/apps/details?id=com.google.android.apps.plus");
                                    sendIntent.setType("text/plain");
                                    startActivity(Intent.createChooser(sendIntent, "share using"));
                                }
                                else if(menuItem.getItemId() == R.id.markDangerous){
                                    //Toast.makeText(Home.this, "Dangerous Clicked!!", Toast.LENGTH_SHORT).show();
                                    Intent markDangerous = new Intent(Home.this, MarkDangerousLocation.class);
                                    startActivity(markDangerous);
                                }
                                else if(menuItem.getItemId() == R.id.viewDangerLocations){
                                    // Toast.makeText(Home.this, "View Danger Locations Clicked!!", Toast.LENGTH_SHORT).show();
                                    Intent viewDangerLocations = new Intent(Home.this, ViewDangerLocations.class);
                                    startActivity(viewDangerLocations);
                                }
                                else if(menuItem.getItemId() == R.id.panicAlert){
                                    Toast.makeText(Home.this, "Message Sent!!", Toast.LENGTH_SHORT).show();

                                    locationAddress="";

                                    FirebaseDatabase firebaseRef = FirebaseDatabase.getInstance();
                                    DatabaseReference latRef = firebaseRef.getReference("Users").child(phoneNumber).child("lastLatitude");
                                    DatabaseReference lngRef = firebaseRef.getReference("Users").child(phoneNumber).child("lastLongitude");
                                    latRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            sendLat = (double)dataSnapshot.getValue();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    lngRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            sendLon = (double) dataSnapshot.getValue();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                    Geocoder geocoder = new Geocoder(Home.this, Locale.getDefault());
                                    try {
                                        List<Address> addresses = geocoder.getFromLocation(sendLat, sendLon, 1);
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

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    FirebaseDatabase gfirebase = FirebaseDatabase.getInstance();
                                    DatabaseReference grpCodeRef = gfirebase.getReference("Users").child(phoneNumber).child("groupCodes");

                                    lastGroupCodesList.clear();
                                    grpCodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for(DataSnapshot singleCode: dataSnapshot.getChildren()){
                                                lastGroupCodesList.add((String)singleCode.getValue());
                                            }
                                            count=0;
                                            for(int i=0; i<lastGroupCodesList.size(); i++){
                                                FirebaseDatabase fire = FirebaseDatabase.getInstance();
                                                DatabaseReference dataRef = fire.getReference("Groups").child(lastGroupCodesList.get(i)).child("members");

                                                dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        count++;
                                                        for (DataSnapshot singleMember: dataSnapshot.getChildren()){
                                                            String no = (String)singleMember.getValue();
                                                            Log.d("MYMSG",no+" "+phoneNumber);
                                                            if(no.equals(phoneNumber)){

                                                            }
                                                            else if(phnNumbers.contains(no)){

                                                            }
                                                            else{
                                                                Log.d("MYMSG","in else");
                                                                phnNumbers += (String)singleMember.getValue()+",";
                                                            }
                                                        }
                                                        if(count==lastGroupCodesList.size())
                                                        {

                                                            phnNumbers = phnNumbers.substring(0,phnNumbers.length()-1);
                                                            Log.d("MYMSG: ", phnNumbers);

                                                            new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {

                                                                    try
                                                                    {
                                                                        String urlParameters = "username=danish_sid&password=RKGZCH0B&message="+ user+" is in danger!!\n Location: "+ locationAddress + "&phone_numbers=" + phnNumbers;
                                                                        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
                                                                        int postDataLength = postData.length;
                                                                        String request = "http://server1.vmm.education/VMMCloudMessaging/AWS_SMS_Sender";
                                                                        URL url = new URL(request);
                                                                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                                                        conn.setDoOutput(true);
                                                                        conn.setInstanceFollowRedirects(false);
                                                                        conn.setRequestMethod("POST");
                                                                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                                                        conn.setRequestProperty("charset", "utf-8");
                                                                        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                                                                        conn.setUseCaches(false);
                                                                        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream()))
                                                                        {
                                                                            wr.write(postData);
                                                                        }

                                                                        DataInputStream dis = new DataInputStream(conn.getInputStream());
                                                                        String s = "";
                                                                        while (true)
                                                                        {
                                                                            s = dis.readLine();
                                                                            if (s == null)
                                                                            {
                                                                                break;
                                                                            }
                                                                            System.out.println(s);

                                                                        }
                                                                    } catch (Exception e)
                                                                    {
                                                                        e.printStackTrace();
                                                                    }

                                                                }
                                                            }).start();

                                                        }

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
                                //Logic
                                drawerLayout.closeDrawers();
                                return true;
                            }
                        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {


                final String code = groupsFromDatabase.get(pos).getCode();
                new Thread() {
                    public void run(){
                        FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
                        final DatabaseReference databaseReference = firebaseDatabase1.getReference("Groups");
                        final DatabaseReference membersRef = databaseReference.child(code).child("members");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(m!=null)
                                {
                                    m.remove();
                                }
                            }
                        });

                        membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                listOfGroupMembers = (ArrayList<String>) dataSnapshot.getValue();
                                if(listOfGroupMembers == null){
                                    listOfGroupMembers = new ArrayList<>();
                                }

                                for(int i=0; i<listOfGroupMembers.size();i++){

                                    p=i;
                                    String number = listOfGroupMembers.get(i);

                                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                    DatabaseReference databaseReference1 = firebaseDatabase.getReference("Users");

                                    DatabaseReference name = databaseReference1.child(number).child("userName");

                                    names = "";
                                    name.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String membNames = (String)dataSnapshot.getValue();
                                            names += membNames+", ";
                                            if(p==listOfGroupMembers.size()-1){
                                                names = names.substring(0,names.length()-1);
                                            }
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    members.setText(names);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }

                                al_ids.clear();
                                membersOfGroupOnMapList.clear();
                                Log.d("MYMSG","before loop : "+membersOfGroupOnMapList.size()+" "+listOfGroupMembers.size());
                                for(int i=0; i<listOfGroupMembers.size(); i++){
                                    String contactNumber = listOfGroupMembers.get(i);
                                    Log.d("MYMSG",contactNumber);
                                    FirebaseDatabase firebaseDatabase2 = FirebaseDatabase.getInstance();
                                    DatabaseReference mainRef = firebaseDatabase2.getReference("Users");
                                    DatabaseReference memberDetailRef = mainRef.child(contactNumber);

                                    memberDetailRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Users user = dataSnapshot.getValue(Users.class);
                                            Log.d("MYMSG","before add");
                                            membersOfGroupOnMapList.add(user);


                                            if(membersOfGroupOnMapList.size()==listOfGroupMembers.size())
                                            {
                                                Log.d("MYMSG","after loop : "+membersOfGroupOnMapList.size());

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mMap.clear();

                                                    }
                                                });


                                                Log.d("MYMSG","in thread");
                                                for(int j = 0; j<membersOfGroupOnMapList.size();j++)
                                                {
                                                    double latitude = membersOfGroupOnMapList.get(j).getLastLatitude();
                                                    double longitude = membersOfGroupOnMapList.get(j).getLastLongitude();
                                                    String photo = membersOfGroupOnMapList.get(j).getPhoto();
                                                    final String name = membersOfGroupOnMapList.get(j).getUserName();
                                                    //final String number = membersOfGroupOnMapList.get(j).getPhoneNumber();

                                                    final LatLng frndLocation = new LatLng(latitude, longitude);

                                                    Thread t = new Thread(new loadImage(photo,name,frndLocation));
                                                    t.start();
                                                    try {
                                                        t.join();
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }

                                                }
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                                            @Override
                                                            public boolean onMarkerClick(Marker marker) {
                                                                int pos = -1;
                                                                String id = marker.getId();
                                                                for(int i=0;i<al_ids.size();i++)
                                                                {
                                                                    if(id.equals(al_ids.get(i)))
                                                                    {
                                                                        pos = i;
                                                                        break;
                                                                    }
                                                                }
                                                                final int newPos = pos;
                                                                if(newPos!=-1) {

                                                                    final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(Home.this);
                                                                    View parentView = getLayoutInflater().inflate(R.layout.bottomsheet, null);
                                                                    bottomSheetDialog.setContentView(parentView);
                                                                    BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) parentView.getParent());
                                                                    bottomSheetBehavior.setPeekHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                                            160, getResources().getDisplayMetrics()));
                                                                    bottomSheetDialog.show();

                                                                    ImageView liveTrackingImage = (ImageView) parentView.findViewById(R.id.liveTrackingimage);
                                                                    ImageView locationHistoryImage = (ImageView) parentView.findViewById(R.id.locationHistoryImage);
                                                                    ImageView viewProfile = (ImageView) parentView.findViewById(R.id.viewProfile);

                                                                    liveTrackingImage.setOnClickListener(new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View view) {
                                                                            //Toast.makeText(Home.this, membersOfGroupOnMapList.get(newPos).getPhoneNumber(), Toast.LENGTH_SHORT).show();
                                                                            Intent liveTracking = new Intent(Home.this, LiveTracking.class);
                                                                            liveTracking.putExtra("phoneNumber", membersOfGroupOnMapList.get(newPos).getPhoneNumber());
                                                                            startActivity(liveTracking);
                                                                            bottomSheetDialog.hide();
                                                                        }
                                                                    });

                                                                    locationHistoryImage.setOnClickListener(new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View view) {
                                                                            //Toast.makeText(Home.this, membersOfGroupOnMapList.get(newPos).getPhoneNumber(), Toast.LENGTH_SHORT).show();
                                                                            Intent viewHistory = new Intent(Home.this, ViewHistory.class);
                                                                            viewHistory.putExtra("phoneNumber", membersOfGroupOnMapList.get(newPos).getPhoneNumber());
                                                                            startActivity(viewHistory);
                                                                            bottomSheetDialog.hide();
                                                                        }
                                                                    });

                                                                    viewProfile.setOnClickListener(new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View view) {
                                                                            //Toast.makeText(Home.this, membersOfGroupOnMapList.get(newPos).getPhoneNumber(), Toast.LENGTH_SHORT).show();
                                                                            Intent viewProfile = new Intent(Home.this, ViewProfile.class);
                                                                            viewProfile.putExtra("phoneNumber", membersOfGroupOnMapList.get(newPos).getPhoneNumber());
                                                                            startActivity(viewProfile);
                                                                            bottomSheetDialog.hide();
                                                                        }
                                                                    });
                                                                }
                                                                return true;
                                                            }
                                                        });

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

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Inflate menu, adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.homemenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //handles action bar item selected
        int id = item.getItemId();

        if (id == R.id.changePhoto) {

            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(Home.this);
            View parentView = getLayoutInflater().inflate(R.layout.changephotobottomsheet, null);
            bottomSheetDialog.setContentView(parentView);
            BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) parentView.getParent());
            bottomSheetBehavior.setPeekHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    160, getResources().getDisplayMetrics()));
            bottomSheetDialog.show();

            ImageView gallery = (ImageView) parentView.findViewById(R.id.galleryPic);
            ImageView camera = (ImageView) parentView.findViewById(R.id.cameraPic);

            gallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent gallery = new Intent(Intent.ACTION_PICK);
                    gallery.setType("image/*");
                    startActivityForResult(gallery, 90);
                    bottomSheetDialog.hide();

                }
            });

            camera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(camera, 80);
                    bottomSheetDialog.hide();

                }
            });
        }
        else if (id == R.id.logOut) {

            Intent myService = new Intent(Home.this, LocationService.class);
            stopService(myService);

            SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
            sharedPreferences.edit().clear().commit();

            Intent loginIntent = new Intent(Home.this, MainActivity.class);
            startActivity(loginIntent);
            finish();

        }

        else if (id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == 80){
            if(resultCode ==RESULT_OK){
                Bitmap bitmap = (Bitmap) (data.getExtras().get("data"));
                uri = getImageUri(Home.this, bitmap);
                Log.d("MYMSG:", uri.toString());
                Intent changePhotoIntent = new Intent(Home.this, ChangePhotoDialogActivity.class);
                changePhotoIntent.putExtra("uri", uri.toString());
                startActivity(changePhotoIntent);
            }
        }
        else if(requestCode == 90){
            if(resultCode == RESULT_OK){
                uri = data.getData();
                Intent changePhotoIntent = new Intent(Home.this, ChangePhotoDialogActivity.class);
                changePhotoIntent.putExtra("uri", uri.toString());
                startActivity(changePhotoIntent);
            }
        }
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        Log.d("MYMSG:", inImage.toString());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    protected void onResume() {
        super.onResume();
        myreceiver = new LocationReceiver();
        IntentFilter inf = new IntentFilter("location");
        registerReceiver(myreceiver, inf);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(myreceiver);
    }

    public class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            lat = intent.getDoubleExtra("latitude", 0.0);
            lon = intent.getDoubleExtra("longitude", 0.0);
            LatLng myLocation = new LatLng(lat, lon);
            if(mMap!=null) {
                m = mMap.addMarker(new MarkerOptions().position(myLocation).title("Me"));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
//                CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(14);
//                mMap.animateCamera(cameraUpdate);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,14.0f));
            }



        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in my current location and move the camera
    }

    public class MyGroupsList implements Runnable {

        @Override
        public void run() {

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference userRef = firebaseDatabase.getReference("Users");

            final DatabaseReference groupCodeRef = userRef.child(phoneNumber).child("groupCodes");
            final DatabaseReference groupNameRef = userRef.child(phoneNumber).child("groupNames");

            groupCodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    grpCodesFromUsers = (ArrayList<String>) dataSnapshot.getValue();
                    if (grpCodesFromUsers == null) {
                        grpCodesFromUsers = new ArrayList<>();
                    }

                    groupNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            grpNamesFromUsers = (ArrayList<String>) dataSnapshot.getValue();
                            if (grpNamesFromUsers == null) {
                                grpNamesFromUsers = new ArrayList<>();
                            }

                            groupsFromDatabase.add(new ViewGroups("--Select--","--Select--"));
                            for (int i = 0; i < grpCodesFromUsers.size(); i++) {
                                String grpCode = grpCodesFromUsers.get(i);
                                String grpName = grpNamesFromUsers.get(i);
                                ViewGroups viewGroups = new ViewGroups(grpCode, grpName);
                                groupsFromDatabase.add(viewGroups);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    spinnerAdapter.notifyDataSetChanged();
                                    progress.dismiss();
                                }
                            });
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

    public class SpinnerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return groupsFromDatabase.size();
        }

        @Override
        public Object getItem(int i) {
            return groupsFromDatabase.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i * 10;
        }

        @Override
        public View getView(int i, View customView, ViewGroup parent) {

            if (customView == null) {
                LayoutInflater inflater = LayoutInflater.from(Home.this);
                customView = inflater.inflate(R.layout.memberslist, parent, false);
            }

            TextView groupName;
            final String grpCode = groupsFromDatabase.get(i).getCode();
            final String grpName = groupsFromDatabase.get(i).getName();


            groupName = (TextView) customView.findViewById(R.id.grpmembers);

            groupName.setText(groupsFromDatabase.get(i).getName()+"");

            return customView;
        }

    }


    class loadImage implements Runnable
    {
        String photo,name;
        LatLng frndLocation;

        public loadImage(String photo,String name, LatLng frndLocation) {
            this.photo = photo;
            this.frndLocation = frndLocation;
        }

        @Override
        public void run() {

            Bitmap bmp = null;
            try {
                URL url = new URL(photo);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            final Bitmap finalBmp = bmp;

            final Bitmap smallMarker = Bitmap.createScaledBitmap(finalBmp, 100, 100, false);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    al_ids.add(mMap.addMarker(new MarkerOptions().position(frndLocation).title(name).icon(BitmapDescriptorFactory.fromBitmap(smallMarker))).getId());
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(frndLocation));
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(frndLocation,14);
                    mMap.animateCamera(cameraUpdate);
                }
            });

        }
    }
}