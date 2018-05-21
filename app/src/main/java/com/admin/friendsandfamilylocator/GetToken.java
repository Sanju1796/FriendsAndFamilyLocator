package com.admin.friendsandfamilylocator;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetToken extends FirebaseInstanceIdService
{
    @Override
    public void onTokenRefresh()
    {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("MYMESSAGE", "Refreshed token: " + refreshedToken);

        // THIS TOKEN IN UNIQUE FOR EACH DEVICE AND IS ONLY AUTO GENERATED WHEN UR APP
        // FIRST CONNECT TO INTERNET TO REGISTER IT ON CLOUD

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the

        // Instance ID token to your app server.
         sendRegistrationToVMMCloudServer(refreshedToken);
    }

    void sendRegistrationToVMMCloudServer(String refreshedToken)
    {

        //// Record a local copy of refreshed token in shared preference ////
        SharedPreferences sharedPreferences = getSharedPreferences("mypref1",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("devicetoken",refreshedToken);

        editor.commit();
        Log.d("MYMESSAGE","TOKEN SAVED locally");


        //// Also Record Token, packagenameofapp and mobileno (Optional IF REQUIRED/AVAILABLE in your APP)
        /// on VMM Cloud Server  /////
        try
        {
            String packagenameofapp = getPackageName();


            String cloudserverip = "server1.vmm.education";

            URL url = new URL("http://"+ cloudserverip +"/VMMCloudMessaging/RecordDeviceInfo?devicetoken="+refreshedToken+"&packagenameofapp="+packagenameofapp);
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
                Log.d("MYMESSAGE","ans from server "+ans);

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




