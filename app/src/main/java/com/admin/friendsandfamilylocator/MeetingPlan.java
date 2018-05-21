package com.admin.friendsandfamilylocator;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MeetingPlan extends AppCompatActivity {

    Spinner spinner;
    EditText meetingTitle, meetingDate, meetingTime;
    TextView members;
    String phoneNumber, groupCode, groupName;
    String names = "";
    int p=0;
    SpinnerAdapter meetingSpinnerAdapter;
    Toolbar planMeetingToolbar;

    ArrayList<ViewGroups> groupsFromDatabaseForMeeting;
    ArrayList<String> grpCodesListFromUsers;
    ArrayList<String> grpNamesListFromUsers;
    ArrayList<String>listOfGroupMembersForMeeting;
    ArrayList<String> namesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_plan);

        planMeetingToolbar = (Toolbar) findViewById(R.id.planMeetingToolbar);

        setSupportActionBar(planMeetingToolbar);

        getSupportActionBar();

        planMeetingToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        spinner = (Spinner)findViewById(R.id.groupMeetingSpinner);
        members = (TextView)findViewById(R.id.selectedGroupMembers);
        meetingDate = (EditText) findViewById(R.id.meetingDate);
        meetingTime = (EditText) findViewById(R.id.meetingTime);
        meetingTitle = (EditText) findViewById(R.id.meetingTitle);

        groupsFromDatabaseForMeeting = new ArrayList<>();
        namesList = new ArrayList<>();

        SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("phoneNumber", null);

        meetingSpinnerAdapter = new SpinnerAdapter();
        spinner.setAdapter(meetingSpinnerAdapter);

        new Thread(new GroupsListForMeeting()).start();


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                groupCode = groupsFromDatabaseForMeeting.get(pos).getCode();
                groupName = groupsFromDatabaseForMeeting.get(pos).getName();

                final FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
                final DatabaseReference databaseReference = firebaseDatabase1.getReference("Groups");
                DatabaseReference membersRef = databaseReference.child(groupCode).child("members");

                membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        listOfGroupMembersForMeeting = (ArrayList<String>) dataSnapshot.getValue();
                        if(listOfGroupMembersForMeeting == null){
                            listOfGroupMembersForMeeting = new ArrayList<>();
                        }


                        for(int i=0; i<listOfGroupMembersForMeeting.size();i++){

                            String number = listOfGroupMembersForMeeting.get(i);

                            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                            DatabaseReference databaseReference1 = firebaseDatabase.getReference("Users");

                            DatabaseReference name = databaseReference1.child(number).child("userName");

                            names = "";
                            name.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String membNames = (String)dataSnapshot.getValue();
                                    names += membNames+", ";

                                    members.setText(names);
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
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        meetingDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(MeetingPlan.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        meetingTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(MeetingPlan.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        meetingTime.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
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

        meetingDate.setText(sdf.format(myCalendar.getTime()));
    }

    public class GroupsListForMeeting implements Runnable {

        @Override
        public void run() {

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference userRef = firebaseDatabase.getReference("Users");

            final DatabaseReference groupCodeRef = userRef.child(phoneNumber).child("groupCodes");
            final DatabaseReference groupNameRef = userRef.child(phoneNumber).child("groupNames");

            groupCodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    grpCodesListFromUsers = (ArrayList<String>) dataSnapshot.getValue();
                    if (grpCodesListFromUsers == null) {
                        grpCodesListFromUsers = new ArrayList<>();
                    }

                    groupNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            grpNamesListFromUsers = (ArrayList<String>) dataSnapshot.getValue();
                            if (grpNamesListFromUsers == null) {
                                grpNamesListFromUsers = new ArrayList<>();
                            }

                            groupsFromDatabaseForMeeting.add(new ViewGroups("--Select--","--Select--"));
                            for (int i = 0; i < grpCodesListFromUsers.size(); i++) {
                                String grpCode = grpCodesListFromUsers.get(i);
                                String grpName = grpNamesListFromUsers.get(i);
                                ViewGroups viewGroups = new ViewGroups(grpCode, grpName);
                                groupsFromDatabaseForMeeting.add(viewGroups);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    meetingSpinnerAdapter.notifyDataSetChanged();
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
            return groupsFromDatabaseForMeeting.size();
        }

        @Override
        public Object getItem(int i) {
            return groupsFromDatabaseForMeeting.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i * 10;
        }

        @Override
        public View getView(int i, View customView, ViewGroup parent) {

            if (customView == null) {
                LayoutInflater inflater = LayoutInflater.from(MeetingPlan.this);
                customView = inflater.inflate(R.layout.memberslist, parent, false);
            }

            TextView groupName;

            groupName = (TextView) customView.findViewById(R.id.grpmembers);

            groupName.setText(groupsFromDatabaseForMeeting.get(i).getName()+"");

            return customView;
        }

    }

    public void next(View view){

        boolean spinnerSelected = spinner.isSelected();
        if(spinnerSelected==false && members.getText().equals("")){
            Toast.makeText(this, "No Group Selected!", Toast.LENGTH_SHORT).show();
            spinnerSelected=true;
        }
        else if(meetingTitle.getText().toString().equals("")){
            Toast.makeText(this, "Give a Title!", Toast.LENGTH_SHORT).show();
        }
        else if(meetingDate.getText().toString().equals("")){
            Toast.makeText(this, "Select a Date for your Meeting!", Toast.LENGTH_SHORT).show();
        }
        else if(meetingTime.getText().toString().equals("")){
            Toast.makeText(this, "Select a Time for your Meeting!", Toast.LENGTH_SHORT).show();
        }
        else {

            Intent reverseGeocodingIntent = new Intent(MeetingPlan.this, ReverseGeocodingActivity.class);
            reverseGeocodingIntent.putExtra("grpCode", groupCode);
            reverseGeocodingIntent.putExtra("grpName", groupName);
            reverseGeocodingIntent.putExtra("title", meetingTitle.getText().toString());
            reverseGeocodingIntent.putExtra("date", meetingDate.getText().toString());
            reverseGeocodingIntent.putExtra("time", meetingTime.getText().toString());
            startActivity(reverseGeocodingIntent);
        }
    }
}
