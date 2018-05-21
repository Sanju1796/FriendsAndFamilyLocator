package com.admin.friendsandfamilylocator;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class InboxActivity extends AppCompatActivity {

    EditText message;
    String code, name, phoneNumber;
    ArrayList<Messages> messageList;
    ArrayList<String> meetingMembers;

    Toolbar inboxToolbar;

    ListView listViewFromDb;

    MessagesListAdapter messagesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        message = (EditText) findViewById(R.id.textMessage);

        inboxToolbar = (Toolbar) findViewById(R.id.inboxToolbar);

        listViewFromDb = (ListView) findViewById(R.id.messagesList);

        messageList = new ArrayList<>();
        meetingMembers = new ArrayList<>();

        setSupportActionBar(inboxToolbar);

        getSupportActionBar();

        inboxToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("data.txt", MODE_PRIVATE);
        phoneNumber = sharedPreferences.getString("phoneNumber", null);

        Intent incomingIntent = getIntent();
        code = incomingIntent.getStringExtra("meetingCode");

        FirebaseDatabase firebase = FirebaseDatabase.getInstance();
        DatabaseReference userNameRef = firebase.getReference("Users").child(phoneNumber).child("userName");
        DatabaseReference membersOfMeetingRef = firebase.getReference("Meetings").child(code).child("meetingMembers");

        userNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name = (String)dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        membersOfMeetingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleNo: dataSnapshot.getChildren()){
                    meetingMembers.add((String)singleNo.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        messagesListAdapter = new MessagesListAdapter();
        listViewFromDb.setAdapter(messagesListAdapter);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference messagesRef = firebaseDatabase.getReference("Meetings").child(code).child("Messages");

        messageList.clear();
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                messageList.clear();
                for(DataSnapshot singleMessage: dataSnapshot.getChildren()){
                    Messages messages = singleMessage.getValue(Messages.class);
                    messageList.add(messages);
                }
                Collections.reverse(messageList);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messagesListAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void sendMessage(View view){

        if(message.getText().toString().equals("")){
            Toast.makeText(this, "Enter a message first!!", Toast.LENGTH_SHORT).show();
        }
        else {

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference messagesRef = firebaseDatabase.getReference("Meetings").child(code).child("Messages");

            final String key = messagesRef.push().getKey();

            DatabaseReference messageRef = messagesRef.child(key);

            long currentTime = System.currentTimeMillis();
            java.sql.Time t = new java.sql.Time(currentTime);
            String time = t.toString();
            java.sql.Date d = new java.sql.Date(currentTime);
            String date = d.toString();

            Messages meetings = new Messages(key, message.getText().toString(), phoneNumber, name, date, time);

            messageRef.setValue(meetings);

            Toast.makeText(this, "Message Sent!!", Toast.LENGTH_SHORT).show();

            message.setText("");

            new Thread(new Runnable() {
                @Override
                public void run() {

                    String tokens = "";

                    for (int i = 0; i < meetingMembers.size(); i++) {

                        String checkedUserNumber = meetingMembers.get(i);

                        try {
                            String packagenameofapp = getPackageName();

                            String cloudserverip = "server1.vmm.education";

                            URL url = new URL("http://" + cloudserverip + "/VMMCloudMessaging/GetTokenOfMobileno?packagename=" + packagenameofapp + "&mobileno=" + checkedUserNumber);
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                            int status = urlConnection.getResponseCode();
                            Log.d("MYMESSAGE", "Response status " + status);

                            if (status == 200) {
                                InputStream inputStream = urlConnection.getInputStream();

                                int conlength = urlConnection.getContentLength();

                                byte b[] = new byte[conlength];

                                inputStream.read(b, 0, conlength);

                                String ans = new String(b);
                                if (!ans.contains("NOT FOUND")) {
                                    ans = ans.replace("\n", "");
                                    ans = ans.replace("\r", "");
                                    tokens += ans + ",";

                                }

                                Log.d("MYMESSAGE", "ans from server " + ans);
                                Log.d("MYMESSAGE", "tokens from server " + tokens);

                            } else {
                                Log.d("MYMESSAGE", "ERROR -> " + status + " " + urlConnection.getResponseMessage());
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                    tokens = tokens.substring(0, tokens.length() - 1);
                    try {
                        String cloudserverip = "server1.vmm.education";

                        URL url = new URL("http://" + cloudserverip + "/VMMCloudMessaging/SendSimpleNotificationUsingTokens?serverkey=AAAAoaezwUw:APA91bF3ZqC3qNBlu0HrmYR2TgxijvqzB7B7qSwpMdk02cC55pIrTzS2PcRfwgzQNPtBsRZJD4cPSEEOmHQv8ztDRlGvd90gDfgLwmpgeEeqB-xkJ565BfRZ2obU2ndcupGPXOkYJzDz&tokens=" + tokens + "&title=You%20recieved%20an%20inbox%20message&message=message");
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("POST");

                        int status = urlConnection.getResponseCode();
                        Log.d("MYMESSAGE", "Response status " + status);

                        if (status == 200) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(InboxActivity.this, "Message Recieved!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.d("MYMESSAGE", "ERROR -> " + status + " " + urlConnection.getResponseMessage());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }).start();
        }

    }

    public class MessagesListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return messageList.size();
        }

        @Override
        public Object getItem(int i) {
            return messageList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i*10;
        }

        @Override
        public View getView(final int pos, View customView, ViewGroup parent) {

            if(customView == null){
                LayoutInflater inflater = LayoutInflater.from(InboxActivity.this);
                customView = inflater.inflate(R.layout.message, parent, false);
            }

            final TextView name, message, date, time;
            ImageView delete;

            name = (TextView) customView.findViewById(R.id.senderName);
            message = (TextView) customView.findViewById(R.id.senderMessage);
            date = (TextView) customView.findViewById(R.id.senderDate);
            time = (TextView) customView.findViewById(R.id.senderTime);
            delete = (ImageView) customView.findViewById(R.id.deleteMessage);

            name.setText(messageList.get(pos).getSenderName().toUpperCase());
            message.setText(messageList.get(pos).getMessage());
            date.setText(messageList.get(pos).getDate());
            time.setText(messageList.get(pos).getTime());


            if(messageList.get(pos).getSenderNumber().equals(phoneNumber)){
                delete.setVisibility(View.VISIBLE);
            }
            else{
                delete.setVisibility(View.INVISIBLE);
            }

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(InboxActivity.this);
                    builder.setTitle("Delete Message");
                    builder.setIcon(R.drawable.ic_delete_black_24dp);
                    builder.setMessage("Are you sure you want to delete this message?");
                    builder.setCancelable(false);

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Log.d("MYMSG",code+" "+messageList.get(pos).getMessageCode());
                            FirebaseDatabase firebase = FirebaseDatabase.getInstance();
                            DatabaseReference database = firebase.getReference("Meetings").child(code).child("Messages").child(messageList.get(pos).getMessageCode());
                            database.removeValue();
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    AlertDialog ad = builder.create();
                    ad.show();

                }
            });

            return customView;
        }
    }
}
