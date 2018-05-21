package com.admin.friendsandfamilylocator;

import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MyInvitations extends AppCompatActivity {

    ArrayList<Groups> invites = new ArrayList<>();
    String phoneNumber;
    ListView invitationList;
    InvitationAdapter invitationAdapter;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_invitations);

        invitationList = (ListView) findViewById(R.id.invitationList);
        toolbar = (Toolbar) findViewById(R.id.invitationToolbar);


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Invitations");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        invitationAdapter = new InvitationAdapter();
        invitationList.setAdapter(invitationAdapter);

        SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("phoneNumber", null);

        Thread thread = new Thread(new Invitations());
        thread.start();
    }

    public class Invitations implements Runnable{
        @Override
        public void run() {

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference("Users");

            final DatabaseReference databaseinvites = databaseReference.child(phoneNumber).child("invitations");

            databaseinvites.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    ArrayList<String> myInvites;
                    myInvites = (ArrayList<String>) dataSnapshot.getValue();

                    if(myInvites == null){
                        myInvites = new ArrayList<String>();
                        Toast.makeText(MyInvitations.this, "No Invites!!", Toast.LENGTH_SHORT).show();
                    }

                    invites.clear();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            invitationAdapter.notifyDataSetChanged();
                            toolbar.setTitle("You have "+invites.size()+" Invitations");
                        }
                    });
                    for(int i=0; i<myInvites.size(); i++){

                        FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
                        DatabaseReference groupsRef = firebaseDatabase1.getReference("Groups");

                        DatabaseReference groupInviteRef = groupsRef.child(myInvites.get(i));

                        groupInviteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                Groups groups = dataSnapshot.getValue(Groups.class);
                                invites.add(groups);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        invitationAdapter.notifyDataSetChanged();
                                        toolbar.setTitle("You have "+invites.size()+" Invitations");
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

    class InvitationAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return invites.size();
        }

        @Override
        public Object getItem(int i) {
            return invites.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i*10;
        }

        @Override
        public View getView(final int i, View customView, ViewGroup parent) {

            if(customView == null){
                LayoutInflater inflater = LayoutInflater.from(MyInvitations.this);
                customView = inflater.inflate(R.layout.myinvitations, parent, false);
            }

            final TextView admin, name;
            ImageView yes, no;

            admin = (TextView) customView.findViewById(R.id.adminName);
            name = (TextView) customView.findViewById(R.id.group);

            no = (ImageView) customView.findViewById(R.id.reject);
            yes = (ImageView) customView.findViewById(R.id.accept);

            String adminNo = invites.get(i).getOwner();

            FirebaseDatabase fd = FirebaseDatabase.getInstance();
            DatabaseReference dr = fd.getReference("Users").child(adminNo).child("userName");

            dr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String adminName = (String)dataSnapshot.getValue();
                    admin.setText("Created By: "+ adminName);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            name.setText("Group: "+ invites.get(i).getGroupName());

            final String groupCode = invites.get(i).getGroupCode();
            final String groupName = invites.get(i).getGroupName();
            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference("Users");

                    final DatabaseReference myPhoneRef = databaseReference.child(phoneNumber).child("invitations");

                    myPhoneRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            ArrayList<String> recievedInvitations = new ArrayList<>();
                            recievedInvitations = (ArrayList<String>) dataSnapshot.getValue();
                            if (recievedInvitations == null) {

                            } else
                            {
                                int index = 0;
                            for (int i = 0; i < recievedInvitations.size(); i++) {
                                if (recievedInvitations.get(i).equals(groupCode)) {
                                    index = i;
                                    break;
                                }
                            }

                            recievedInvitations.remove(index);

                            myPhoneRef.setValue(recievedInvitations);

                            Toast.makeText(MyInvitations.this, "Invitation Removed from User Table!!", Toast.LENGTH_SHORT).show();

                            FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
                            DatabaseReference groupRef = firebaseDatabase1.getReference("Groups");
                            final DatabaseReference groupMembersRef = groupRef.child(groupCode).child("members");

                            groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    ArrayList<String> membersList = new ArrayList<>();
                                    membersList = (ArrayList<String>) dataSnapshot.getValue();
                                    if (membersList == null) {
                                        membersList = new ArrayList<>();
                                    } else {
                                        for (int j = 0; j < membersList.size(); j++) {
                                            Log.d("MYMSG: ", "" + membersList.get(j));
                                        }

                                        int memberIndex = 0;

                                        for (int i = 0; i < membersList.size(); i++) {

                                            if (membersList.get(i).equals(phoneNumber)) {
                                                memberIndex = i;
                                                break;
                                            }
                                        }
                                        membersList.remove(memberIndex);

                                        groupMembersRef.setValue(membersList);
                                        Toast.makeText(MyInvitations.this, "Number removed from groups !!", Toast.LENGTH_SHORT).show();

                                        new Thread(new Invitations()).start();
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
            });

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    final DatabaseReference userRef = firebaseDatabase.getReference("Users");
                    final DatabaseReference invitationRef = userRef.child(phoneNumber).child("invitations");

                    final DatabaseReference groupCodeRef = userRef.child(phoneNumber).child("groupCodes");
                    final DatabaseReference groupNameRef = userRef.child(phoneNumber).child("groupNames");


                    invitationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            ArrayList<String> myinvitationsList = (ArrayList<String>) dataSnapshot.getValue();
                            if (myinvitationsList == null) {
                                myinvitationsList = new ArrayList<String>();
                            } else {

                                int index = -1;
                                for (int i = 0; i < myinvitationsList.size(); i++) {

                                    if (myinvitationsList.get(i).equals(groupCode))
                                    index = i;
                                    break;

                                }
                                myinvitationsList.remove(index);

                                invitationRef.setValue(myinvitationsList);

                                groupCodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        ArrayList<String> groupCodeList = (ArrayList<String>) dataSnapshot.getValue();
                                        if (groupCodeList == null) {
                                            groupCodeList = new ArrayList<String>();
                                        }
                                            groupCodeList.add(groupCode);
                                            groupCodeRef.setValue(groupCodeList);

                                            groupNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    ArrayList<String> groupNameList = (ArrayList<String>) dataSnapshot.getValue();
                                                    if (groupNameList == null) {
                                                        groupNameList = new ArrayList<String>();
                                                    }
                                                        groupNameList.add(groupName);
                                                        groupNameRef.setValue(groupNameList);

                                                    FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
                                                    final DatabaseReference gRef = firebaseDatabase1.getReference("Groups").child(groupCode).child("members");
                                                        gRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                ArrayList<String> al =(ArrayList<String>) dataSnapshot.getValue();
                                                                al.add(phoneNumber);
                                                                gRef.setValue(al);
                                                                new Thread(new Invitations()).start();

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
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });
            return customView;
        }
    }
}

