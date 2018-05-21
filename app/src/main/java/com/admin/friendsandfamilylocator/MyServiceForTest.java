package com.admin.friendsandfamilylocator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyServiceForTest extends Service {
    double lat, lon;
    double dist;
    myLocationListener mylocationlistenerobj;

    LocationManager lm;
    Handler h;
    public MyServiceForTest() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        h= new Handler();
        Log.d("MYMSG", "service started");
        new Thread(new Start()).start();
        return START_STICKY;
    }


    class Start implements Runnable
    {

        @Override
        public void run() {
            lm = (LocationManager) getSystemService(LOCATION_SERVICE);


            ////////   Logic to get CURRENT LOCATIONS /////////////////

            //---check if GPS_PROVIDER is enabled---

            boolean gpsStatus = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);


            //---check if NETWORK_PROVIDER is enabled---

            boolean networkStatus = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


            mylocationlistenerobj = new myLocationListener();

            // check which provider is enabled

            if (gpsStatus == false && networkStatus == false) {

//                Toast.makeText(this, "Both GPS and Newtork are disabled", Toast.LENGTH_LONG).show();

                //---display the "LocationDB services" settings page---

                Intent in = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(in);
            }


            if (gpsStatus == true) {
//                Toast.makeText(this, "GPS is Enabled, using it", Toast.LENGTH_LONG).show();
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, mylocationlistenerobj);

                    }
                });

            }


            if (networkStatus == true) {

//                Toast.makeText(this, "Network LocationDB is Enabled, using it", Toast.LENGTH_LONG).show();
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, mylocationlistenerobj);

                    }
                });

            }

//            new Thread(new CheckDangerLocation()).start();
//            new Thread(new checkGPS()).start();
        }
    }


    public class CheckDangerLocation implements Runnable{
        @Override
        public void run() {


            while(true) {
                FirebaseDatabase firebase = FirebaseDatabase.getInstance();
                final DatabaseReference database = firebase.getReference("DangerLocations");

                database.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot singleLocation : dataSnapshot.getChildren()) {

                            DangerLocation dangerLocation = singleLocation.getValue(DangerLocation.class);
                            double nlat, nlon;

                            nlat = dangerLocation.getLat();
                            nlon = dangerLocation.getLon();

                            final int Rd = 6371; // Radius of the earth

                            double latDistance = Math.toRadians(nlat - lat);
                            double lonDistance = Math.toRadians(nlon - lon);
                            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                                    + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(nlat))
                                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                            dist = Rd * c * 1000;

                            Log.d("MYMSG: ", dist+ " mtrs");

                            if(dist < 100) {
                                Log.d("MYMSG","in notification : "+dist+" "+nlat+" "+nlon+" "+lat+" "+lon+" "+dangerLocation.getLocationAddress());
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(MyServiceForTest.this);
                                builder.setContentTitle("Danger!!");
                                builder.setContentText("This Location is marked as dangerous by a user");
                                builder.setSmallIcon(R.drawable.ic_add_alert_black_24dp);

                                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.header);
                                builder.setLargeIcon(bmp);

                                Notification myNotification = builder.build();
                                NotificationManager notificationManager = (NotificationManager) (getSystemService(NOTIFICATION_SERVICE));
                                notificationManager.notify(20, myNotification);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public class checkGPS implements Runnable{

        @Override
        public void run() {
            while(true){

                boolean gpsStatus = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

                boolean networkStatus = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (gpsStatus || networkStatus){

                    lm = (LocationManager) getSystemService(LOCATION_SERVICE);

                    mylocationlistenerobj = new myLocationListener();

                    h.post(new Runnable() {
                        @Override
                        public void run() {

                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, mylocationlistenerobj);

                            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, mylocationlistenerobj);


                        }
                    });

                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    ///////  Logic that runs inside service//////
    class myLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            lat = location.getLatitude();
            lon = location.getLongitude();

            Log.d("MYMSG: ", "Latitude: " + lat + " Longitude:" + lon);

            Intent broadcastLocation = new Intent("location");
            broadcastLocation.putExtra("latitude", lat);
            broadcastLocation.putExtra("longitude", lon);
            sendBroadcast(broadcastLocation);

/*h.post(new Runnable() {
    @Override
    public void run() {

        SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
        final String phoneNumber = sharedPreferences.getString("phoneNumber", null);

        final long currentTime = System.currentTimeMillis();
        final String time = Long.toString(currentTime);
        java.sql.Date d = new java.sql.Date(currentTime);
        final String date = d.toString();

        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference("Users");

        final DatabaseReference locationRef = databaseReference.child(phoneNumber).child("location").child(time);
        locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LocationDB locationDB = new LocationDB(currentTime, date, lat, lon);
                locationRef.setValue(locationDB);

                final DatabaseReference lastLatitudeRef = databaseReference.child(phoneNumber).child("lastLatitude");
                lastLatitudeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        lastLatitudeRef.setValue(lat);
                        final DatabaseReference lastLongitudeRef = databaseReference.child(phoneNumber).child("lastLongitude");
                        lastLongitudeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                lastLongitudeRef.setValue(lon);

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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
});*/

//            new Thread(new AddLocationOnFirebase()).start();

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    public class AddLocationOnFirebase implements Runnable {

        @Override
        public void run() {

        }
////////////////////////////////////////////
    }
}
