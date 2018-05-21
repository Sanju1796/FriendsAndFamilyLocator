package com.admin.friendsandfamilylocator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewMeetings extends AppCompatActivity {

    String owner;

    Toolbar viewMeetingsToolbar;

    ListView viewMeetingsList;

    ArrayList<Meetings> meetingsList;
    ArrayList<String> meetingCodesfromUsers;

    MyMeetingsAdapter myMeetingsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_meetings);

        SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
        owner = sharedPreferences.getString("phoneNumber", null);

        viewMeetingsToolbar = (Toolbar) findViewById(R.id.viewMeetingsToolbar);

        viewMeetingsList = (ListView) findViewById(R.id.viewMeetingsList);

        setSupportActionBar(viewMeetingsToolbar);

        getSupportActionBar();
        viewMeetingsToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        meetingsList = new ArrayList<>();

        myMeetingsAdapter = new MyMeetingsAdapter();
        viewMeetingsList.setAdapter(myMeetingsAdapter);

        new Thread(new MyMeetings()).start();

        viewMeetingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Intent meetingDetails = new Intent(ViewMeetings.this, MeetingDetails.class);
                String meetingCode = meetingCodesfromUsers.get(pos);
                meetingDetails.putExtra("code", meetingCode);
                startActivity(meetingDetails);
            }
        });
    }

    public class MyMeetings implements Runnable{

        @Override
        public void run() {

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference mainRef = firebaseDatabase.getReference("Users");

            DatabaseReference meetingsRef = mainRef.child(owner).child("meetingCodes");

            meetingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    meetingCodesfromUsers = (ArrayList<String>) dataSnapshot.getValue();
                    if(meetingCodesfromUsers == null){
                        meetingCodesfromUsers = new ArrayList<>();
                    }

                    for(int i=0; i<meetingCodesfromUsers.size(); i++) {
                        String meetingCode = meetingCodesfromUsers.get(i);

                        FirebaseDatabase fd = FirebaseDatabase.getInstance();
                        DatabaseReference meetingsRef = fd.getReference("Meetings");

                        DatabaseReference mRef = meetingsRef.child(meetingCode);

                        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Meetings meetings = dataSnapshot.getValue(Meetings.class);
                                meetingsList.add(meetings);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        myMeetingsAdapter.notifyDataSetChanged();
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
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }


    public class MyMeetingsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return meetingsList.size();
        }

        @Override
        public Object getItem(int i) {
            return meetingsList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i*10;
        }

        @Override
        public View getView(int i, View customView, ViewGroup parent) {

            if(customView == null){
                LayoutInflater inflater = LayoutInflater.from(ViewMeetings.this);
                customView = inflater.inflate(R.layout.viewmeetings, parent, false);
            }

            final TextView title, createdBy, date, time;

            title = (TextView) customView.findViewById(R.id.title);
            createdBy = (TextView) customView.findViewById(R.id.createdBy);
            date = (TextView) customView.findViewById(R.id.date);
            time = (TextView) customView.findViewById(R.id.time);

            title.setText(meetingsList.get(i).getTitle().toUpperCase());

            String num = meetingsList.get(i).getOwner();

            if(num.equals(owner)){
                createdBy.setText("Created By:-"+"Me");
            }
            else{
                FirebaseDatabase firebaseRef = FirebaseDatabase.getInstance();
                DatabaseReference ownerRef = firebaseRef.getReference("Users").child(num).child("userName");

                ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String ownerName = (String)dataSnapshot.getValue();
                        createdBy.setText("Created By:-"+ownerName);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            date.setText(meetingsList.get(i).getDate());
            time.setText(meetingsList.get(i).getTime());

            return customView;
        }
    }
}
