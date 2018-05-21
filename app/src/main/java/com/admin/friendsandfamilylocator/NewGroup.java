package com.admin.friendsandfamilylocator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.provider.ContactsContract;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NewGroup extends AppCompatActivity {

    ArrayList<Users> phoneContactsList = new ArrayList<Users>();
    ArrayList<Users> databaseContactsList = new ArrayList<Users>();
    ArrayList<Users> commonContactsList = new ArrayList<Users>();
    ArrayList<Users> checkedContactsList = new ArrayList<Users>();
    String name;
    String phoneNumber;
    ListView contactsList;
    ContactsListAdapter contactsListAdapter;
    TextView numberOfSelectedContacts, nameOfSelectedContacts, myName;
    EditText groupName;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        //replace action bar with toolbar
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("New Group");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        contactsList = (ListView) findViewById(R.id.contactsList);
        numberOfSelectedContacts = (TextView) findViewById(R.id.numberOfSelectedContacts);
        nameOfSelectedContacts = (TextView) findViewById(R.id.nameOfSelectedContacts);
        groupName = (EditText) findViewById(R.id.groupName);

        contactsListAdapter = new ContactsListAdapter();

        contactsList.setAdapter(contactsListAdapter);

        Thread thread = new Thread(new FilterContacts());
        thread.start();
    }

    public class FilterContacts implements Runnable{

        @Override
        public void run() {

            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            while (phones.moveToNext()) {
                name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int flag = 0;
                for (int i = 0; i < phoneContactsList.size(); i++) {

                    if (phoneNumber.equals(phoneContactsList.get(i).getPhoneNumber())) {
                        flag = 1;
                        break;
                    }
                }
                if (flag == 0) {
                    phoneContactsList.add(new Users(phoneNumber, null, name, null, null, null, null, null, 0.0, 0.0));
                }
            }
            phones.close();
            Log.d("MYMSG:", phoneContactsList.size()+" phone contacts");
            for (int i = 0; i < phoneContactsList.size(); i++) {
                Log.d("MYMSG:", phoneContactsList.get(i).getUserName() + " " + phoneContactsList.get(i).getPhoneNumber());
            }

            Log.d("Break:", "-------------------------------------------------------------");

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference mainRef = firebaseDatabase.getReference("Users");

            mainRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot singleContact : dataSnapshot.getChildren()) {
                        Users u = singleContact.getValue(Users.class);
                        databaseContactsList.add(u);
                    }

                    Log.d("MYMSG:", databaseContactsList.size() + " database contacts");
                    for (int i = 0; i < databaseContactsList.size(); i++) {

                        Log.d("MYMSG", databaseContactsList.get(i).getUserName() + " " + databaseContactsList.get(i).getPhoneNumber());
                    }

                    commonContactsList = databaseContactsList;



                    commonContactsList.retainAll(phoneContactsList);
                    Log.d("MYMSG:","-----------------------------------------------------");
                    Log.d("MYMSG:", commonContactsList.size()+" common contacts!!");
                    for (int i = 0; i < commonContactsList.size(); i++) {

                        Log.d("MYMSG:", commonContactsList.get(i).getUserName()+" "+commonContactsList.get(i).getPhoneNumber());
                    }
                    contactsListAdapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public class ContactsListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return commonContactsList.size();
        }

        @Override
        public Object getItem(int i) {
            return commonContactsList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i*10;
        }

        @Override
        public View getView(int i, View customView, ViewGroup parent) {

            if(customView == null){
                LayoutInflater inflater = LayoutInflater.from(NewGroup.this);
                customView = inflater.inflate(R.layout.contacts, parent, false);
            }
            TextView contactName, contactNumber;
            ImageView contactPic;
            CheckBox selectContact;
            RelativeLayout Layout;

             final Users user = commonContactsList.get(i);

            contactPic = (ImageView) customView.findViewById(R.id.contactPic);
            contactName = (TextView) customView.findViewById(R.id.contactName);
            contactNumber = (TextView) customView.findViewById(R.id.contactNumber);
            selectContact = (CheckBox) customView.findViewById(R.id.selectContant);
            Layout = (RelativeLayout) customView.findViewById(R.id.newGroupLayout);

            if(i%2==0){
                Layout.setBackgroundColor(Color.rgb(224,243,248));
            }
            else{
                Layout.setBackgroundColor(Color.WHITE);
            }

            Picasso.with(NewGroup.this).load(user.getPhoto()).into(contactPic);
            contactName.setText(user.getUserName());
            contactNumber.setText("("+user.getPhoneNumber()+")");

            selectContact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                    if (isChecked) {


                        checkedContactsList.add(user);
                        Toast.makeText(NewGroup.this, user.getUserName() + " " + user.getPhoneNumber(), Toast.LENGTH_LONG).show();
                    } else {
                        checkedContactsList.remove(user);

                    }
                    numberOfSelectedContacts.setText("Contacts Selected: " + checkedContactsList.size());
                    String names="";
                    for (int i = 0; i < checkedContactsList.size(); i++) {
                        names += checkedContactsList.get(i).getUserName()+" ";

                    }
                    nameOfSelectedContacts.setText(names);

                }
            });
            return customView;
        }
    }

    public void createGroup(View view){

        if(groupName.getText().toString().equals("")){
            Toast.makeText(NewGroup.this, "Invalid Group Name!!", Toast.LENGTH_LONG).show();
        }
        else if(checkedContactsList.size() <= 1){
            Toast.makeText(NewGroup.this, "Select Minimum 2 Contacts for Group!!", Toast.LENGTH_LONG).show();
        }
        else {

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference mainRef = firebaseDatabase.getReference("Groups");

            final String key = mainRef.push().getKey();
            final DatabaseReference groupRef = mainRef.child(key);

            SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
            String groupOwner = sharedPreferences.getString("phoneNumber", null);

            final int size = checkedContactsList.size();

            final Groups groups = new Groups(key, groupName.getText().toString(), groupOwner, null);

            //To Firebase Database

            groupRef.setValue(groups);
            DatabaseReference memberRef = groupRef.child("members");
//            DatabaseReference member = memberRef.child("0");
//            member.setValue(groupOwner);
//            int ownerIndex = 0;
            for (int i = 0; i < size; i++) {

//                ownerIndex +=1;



                FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
                final DatabaseReference databaseReference = firebaseDatabase1.getReference("Users");

                final DatabaseReference userRef = databaseReference.child(checkedContactsList.get(i).getPhoneNumber()).child("invitations");

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                            ArrayList<String> prevInvitaions = (ArrayList<String>) dataSnapshot.getValue();
                            if(prevInvitaions==null)
                            {
                                prevInvitaions = new ArrayList<>();
                            }
                            prevInvitaions.add(key);
                            userRef.setValue(prevInvitaions);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            DatabaseReference ownerMember = memberRef.child("0");
            ownerMember.setValue(groupOwner);

            FirebaseDatabase firebase = FirebaseDatabase.getInstance();
            final DatabaseReference database = firebase.getReference("Users");

            final DatabaseReference usergroupCodeRef = database.child(groupOwner).child("groupCodes");
            final DatabaseReference usergroupNameRef = database.child(groupOwner).child("groupNames");

            usergroupCodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    ArrayList<String> groupCodes = (ArrayList<String>) dataSnapshot.getValue();
                    if(groupCodes == null){
                        groupCodes = new ArrayList<>();
                    }

                    groupCodes.add(key);
                    usergroupCodeRef.setValue(groupCodes);

                    usergroupNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            ArrayList<String> groupNames = (ArrayList<String>) dataSnapshot.getValue();
                            if(groupNames == null){
                                groupNames = new ArrayList<>();
                            }

                            groupNames.add(groupName.getText().toString());
                            usergroupNameRef.setValue(groupNames);

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

            Toast.makeText(NewGroup.this, "Group Created!!", Toast.LENGTH_SHORT).show();

        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                String tokens = "";

                for(int i=0; i<checkedContactsList.size(); i++){

                    String checkedUserNumber = checkedContactsList.get(i).getPhoneNumber();

                    try
                    {
                        String packagenameofapp = getPackageName();

                        String cloudserverip = "server1.vmm.education";

                        URL url = new URL("http://"+ cloudserverip +"/VMMCloudMessaging/GetTokenOfMobileno?packagename="+packagenameofapp+"&mobileno="+checkedUserNumber);
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                        int status = urlConnection.getResponseCode();
                        Log.d("MYMESSAGE","Response status "+ status);

                        if(status==200)
                        {
                            InputStream inputStream = urlConnection.getInputStream();

                            int conlength = urlConnection.getContentLength();

                            byte b[]=new byte[conlength];

                            inputStream.read(b,0,conlength);

                            String ans=new String(b);
                            if(!ans.contains("NOT FOUND"))
                            {
                                ans = ans.replace("\n","");
                                ans = ans.replace("\r","");
                                tokens += ans+",";

                            }

                            Log.d("MYMESSAGE","ans from server "+ans);
                            Log.d("MYMESSAGE","tokens from server "+tokens);

                        }
                        else
                        {
                            Log.d("MYMESSAGE","ERROR -> "+status+" "+urlConnection.getResponseMessage());
                        }
                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                    }

                }
                if(tokens.length()!=0)
                {
                    tokens = tokens.substring(0,tokens.length()-1);
                    try
                    {
                        String cloudserverip = "server1.vmm.education";

                        URL url = new URL("http://"+ cloudserverip +"/VMMCloudMessaging/SendSimpleNotificationUsingTokens?serverkey=AAAAoaezwUw:APA91bF3ZqC3qNBlu0HrmYR2TgxijvqzB7B7qSwpMdk02cC55pIrTzS2PcRfwgzQNPtBsRZJD4cPSEEOmHQv8ztDRlGvd90gDfgLwmpgeEeqB-xkJ565BfRZ2obU2ndcupGPXOkYJzDz&tokens="+tokens+"&title=This%20is%20an%20invite&message=invite");
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("POST");

                        int status = urlConnection.getResponseCode();
                        Log.d("MYMESSAGE","Response status "+ status);

                        if(status==200)
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(NewGroup.this, "Invites Sent", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                        {
                            Log.d("MYMESSAGE","ERROR -> "+status+" "+urlConnection.getResponseMessage());
                        }
                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                    }

                }

            }
        }).start();

        finish();
    }
}
