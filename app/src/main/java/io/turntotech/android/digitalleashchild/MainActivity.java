package io.turntotech.android.digitalleashchild;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    //declare variables
    public FusedLocationProviderClient mFusedLocationClient;
     EditText editTxtParentUsername;
     EditText editTxtChildUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         editTxtParentUsername = (EditText) findViewById(R.id.editTxtParentUserName);
         editTxtChildUsername = (EditText) findViewById(R.id.editTxtChildUserName);

        }

    //Get current location
    public void updateLastLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Need Location Permission", Toast.LENGTH_LONG)
                    .show();
            return;

        }

        mFusedLocationClient.getLastLocation()

                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Get last known location. In some rare situations this can be null.

                        if (location != null) {
                            // Logic to handle location object

                            postLocation(location);


                        } else {
                            Toast.makeText(MainActivity.this, "Did not get location", Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                });
    }

    public void postLocation(Location location) {
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("latitude", location.getLatitude());
            jsonObject.put("longitude", location.getLongitude());
            jsonObject.put("timestamp", new Date().getTime());
            String message = jsonObject.toString();

            //postData(message);
            PostDataToServer postToServer = new PostDataToServer();
            postToServer.execute(message);


        } catch (JSONException e) {
            e.printStackTrace();

        }

    }

    public void postData(String message) throws IOException {
        try {

            //constants
            String parent_username = "" + editTxtParentUsername.getText();
            String child_username = "" +  editTxtChildUsername.getText();


            String urlString = "https://turntotech.firebaseio.com/digitalleash/users/" + parent_username + "/" + child_username + ".json";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /*milliseconds*/);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("PUT");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(message.getBytes().length);

            //make some HTTP header nicety
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");

            //open
            conn.connect();

            //setup send
            OutputStream outStream = new BufferedOutputStream(conn.getOutputStream());
            outStream.write(message.getBytes());

//            Toast.makeText(this, "Reported Successful", Toast.LENGTH_LONG)
//                    .show();

            //clean up
            outStream.flush();
            outStream.close();

            int responseCode = conn.getResponseCode();

            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader inStream = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = inStream.readLine()) != null) {
                response.append(inputLine);
            }
            inStream.close();

            conn.disconnect();

            //print result
            System.out.println(response.toString());

        } catch (IOException e) {
            e.printStackTrace();


        }

    }

    public void onReport(View view) {

        updateLastLocation();
    }


public class PostDataToServer extends AsyncTask<String, String, String> {


        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... message) {

            try {
                postData(message[0]);


            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            // do something UI
            Intent reportSuccess = new Intent(MainActivity.this, ActivitySuccess.class);
            startActivity(reportSuccess);



        }



    }
}