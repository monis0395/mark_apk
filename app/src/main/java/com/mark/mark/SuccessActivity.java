package com.mark.mark;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SuccessActivity extends AppCompatActivity {

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    private String HOSTNAME;
    public static final String CLASS_ID = "1";
    private RecyclerView dailyPeriod;
    private AdapterDailyPeriod mAdapter;
    public SharedPreferences sharedPreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);
        HOSTNAME = getString(R.string.hostname);

        sharedPreferences = getSharedPreferences(MyPREFERENCES, MainActivity.MODE_PRIVATE);

        //Make call to AsyncTask
        new AsyncFetch().execute();
    }

    // Triggers when Scan NFC Button clicked
    public void scanNFC(View arg0) {

        String tagUID = "C09705f7";
        String location = "61";
        long time= System.currentTimeMillis();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("NFC_Location", location);
        editor.putString("NFC_TimeMills", time+"");
        editor.putString("NFC_UID", tagUID);

        Intent intent =  new Intent(SuccessActivity.this, ScanFPActivity.class);
        intent.putExtra("subject","Enterprise Resource Planning");
        startActivity(intent);
    }

    private class AsyncFetch extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(SuccessActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your json file resides
                // Even you can make call to php file which returns json data
                url = new URL(HOSTNAME+"dailyPeriod.inc.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return e.toString();
            }
            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("classid", CLASS_ID);
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return e1.toString();
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    Log.d("djson", result.toString());

                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {

                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            pdLoading.dismiss();
            List<DailyPeriod> data=new ArrayList<>();

            pdLoading.dismiss();
            try {

                JSONArray jArray = new JSONArray(result);

                TextView textPeriod;
                textPeriod= (TextView) findViewById(R.id.textPeriod);
                textPeriod.setText("/ "+jArray.length()+"");


                // Extract data from json and store into ArrayList as class objects
                for(int i=0;i<jArray.length();i++){
                    JSONObject json_data = jArray.getJSONObject(i);
                    DailyPeriod periodData = new DailyPeriod();
                    periodData.did = json_data.getString("did");
                    periodData.subjectName= json_data.getString("Subject");
                    periodData.teacherName= json_data.getString("Teacher");
                    String tstart = json_data.getString("Time Start");
                    String tend = json_data.getString("Time End");
                    periodData.startTime= tstart.substring(0,tstart.length()-7);
                    periodData.endTime = tend.substring(0,tend.length()-7);

                    periodData.location = json_data.getString("location");
                    data.add(periodData);
                }

                // Setup and Handover data to recyclerview
                dailyPeriod = (RecyclerView)findViewById(R.id.dailyPeriod);
                mAdapter = new AdapterDailyPeriod(SuccessActivity.this, data);
                dailyPeriod.setAdapter(mAdapter);
                dailyPeriod.setLayoutManager(new LinearLayoutManager(SuccessActivity.this));



            } catch (JSONException e) {
                Toast.makeText(SuccessActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }

        }

    }
}
