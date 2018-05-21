package com.admin.friendsandfamilylocator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupInfoDialogActivity extends AppCompatActivity {

    TextView grpName, grpOwner;
    ListView grpMembersList;
    String code, name,ownerNumber, phoneNumber;
    ArrayList<String> membersList, membersNamesList;
    GroupMembersAdapter groupMembersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_group_info_dialog);

        SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("phoneNumber", null);

        grpName = (TextView) findViewById(R.id.grpName);
        grpOwner = (TextView) findViewById(R.id.grpOwner);
        grpMembersList = (ListView) findViewById(R.id.grpMembersList);
        membersList = new ArrayList<>();
        membersNamesList = new ArrayList<>();

        Intent incomingGroupCode = getIntent();
        code = incomingGroupCode.getStringExtra("code");
        name = incomingGroupCode.getStringExtra("name");

        grpName.setText(name);

        groupMembersAdapter = new GroupMembersAdapter();
        grpMembersList.setAdapter(groupMembersAdapter);

        new Thread(new MembersList()).start();

    }

    public class MembersList implements Runnable{

        @Override
        public void run() {
            final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference mainRef = firebaseDatabase.getReference("Groups");

            DatabaseReference codeRef = mainRef.child(code);

            codeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Groups grp = dataSnapshot.getValue(Groups.class);
                    ownerNumber = grp.getOwner();
                    membersList= grp.getMembers();

                    if(ownerNumber.equals(phoneNumber)){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                grpOwner.setText("(Created by: "+"Me)");
                            }
                        });
                    }
                    else{
                        FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
                        DatabaseReference userRef = firebaseDatabase1.getReference("Users").child(ownerNumber).child("userName");

                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String ownerName = (String) dataSnapshot.getValue();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        grpOwner.setText("(Created by: "+ownerName+")");
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    for(int i=0; i<membersList.size(); i++){
                        String num = membersList.get(i);

                        FirebaseDatabase fb = FirebaseDatabase.getInstance();
                        DatabaseReference userNameRef = fb.getReference("Users").child(num).child("userName");

                        userNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String uName = (String)dataSnapshot.getValue();
                                membersNamesList.add(uName);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        groupMembersAdapter.notifyDataSetChanged();
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

    public class GroupMembersAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return membersNamesList.size();
        }

        @Override
        public Object getItem(int i) {
            return membersNamesList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i*10;
        }

        @Override
        public View getView(int i, View customView, ViewGroup parent) {

            if(customView == null){
                LayoutInflater inflater = LayoutInflater.from(GroupInfoDialogActivity.this);
                customView = inflater.inflate(R.layout.memberslist, parent, false);
            }

            TextView grpMembers = (TextView) customView.findViewById(R.id.grpmembers);

            grpMembers.setText(membersNamesList.get(i));

            return customView;
        }
    }
}
