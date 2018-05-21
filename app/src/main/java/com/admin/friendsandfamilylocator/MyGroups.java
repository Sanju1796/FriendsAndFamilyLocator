package com.admin.friendsandfamilylocator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToLongBiFunction;

public class MyGroups extends AppCompatActivity {

    ListView myGroupsList;
    ArrayList<ViewGroups> myGroupsListFromDatabase;
    ArrayList<String> groupCodesfromUsers;
    ArrayList<String> groupNamesfromUsers;
    String phoneNumber;
    MyGroupAdapter myGroupAdapter;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups);

        toolbar = (Toolbar) findViewById(R.id.myGroupToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Groups");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        myGroupsList = (ListView) findViewById(R.id.myGroupsList);
        myGroupsListFromDatabase = new ArrayList<>();
        groupCodesfromUsers = new ArrayList<>();
        groupNamesfromUsers = new ArrayList<>();

        SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("phoneNumber", null);

        myGroupAdapter = new MyGroupAdapter();
        myGroupsList.setAdapter(myGroupAdapter);

        new Thread(new MyGroupsList()).start();
    }

    public class MyGroupsList implements Runnable{

        @Override
        public void run() {

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference userRef = firebaseDatabase.getReference("Users");

            final DatabaseReference groupCodeRef = userRef.child(phoneNumber).child("groupCodes");
            final DatabaseReference groupNameRef = userRef.child(phoneNumber).child("groupNames");

            groupCodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    groupCodesfromUsers = (ArrayList<String>) dataSnapshot.getValue();
                    if(groupCodesfromUsers == null){
                        groupCodesfromUsers = new ArrayList<>();
                    }

                    groupNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            groupNamesfromUsers = (ArrayList<String>) dataSnapshot.getValue();
                            if(groupNamesfromUsers == null){
                                groupNamesfromUsers = new ArrayList<>();
                            }

                            for(int i=0; i<groupCodesfromUsers.size(); i++){
                                String grpCode = groupCodesfromUsers.get(i);
                                String grpName = groupNamesfromUsers.get(i);
                                ViewGroups viewGroups = new ViewGroups(grpCode, grpName);
                                myGroupsListFromDatabase.add(viewGroups);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    myGroupAdapter.notifyDataSetChanged();
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

    public class MyGroupAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return myGroupsListFromDatabase.size();
        }

        @Override
        public Object getItem(int i) {
            return myGroupsListFromDatabase.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i*10;
        }

        @Override
        public View getView(int i, View customView, ViewGroup parent) {

            if(customView == null){
                LayoutInflater inflater = LayoutInflater.from(MyGroups.this);
                customView = inflater.inflate(R.layout.mygroups, parent, false);
            }

            TextView groupName;
            ImageView groupInfo;
            final String grpCode = myGroupsListFromDatabase.get(i).getCode();
            final String grpName = myGroupsListFromDatabase.get(i).getName();


            groupName = (TextView) customView.findViewById(R.id.gName);
            groupInfo = (ImageView) customView.findViewById(R.id.groupInfo);

            groupName.setText(myGroupsListFromDatabase.get(i).getName());

            groupInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent groupInfoDialog = new Intent(MyGroups.this, GroupInfoDialogActivity.class);
                    groupInfoDialog.putExtra("code", grpCode);
                    groupInfoDialog.putExtra("name", grpName);
                    startActivity(groupInfoDialog);
                }
            });

            return customView;
        }
    }
}
