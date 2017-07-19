package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SetAlarmActivity extends Activity {

    private EditText address;
    private String destination;
    private String origin;
    private int seconds;
    private int alarmHour;
    private int alarmMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);

    }

    public void sendAlarm(View v){

        TimePicker t = (TimePicker) findViewById(R.id.timePickerAlarm);

        alarmHour = t.getHour();
        alarmMinute = t.getMinute();

        seconds = toSeconds(alarmHour, alarmMinute);

        //Get address
        //address = (EditText) findViewById(R.id.addressEditText);
        destination = address.getText().toString();

        new GetCoordinates().execute(destination.replace(" ", "+")); //Start asynchronous task (call API)

    }



    private int toSeconds(int h, int min){
        return((h*60*60) + (min*60));
    }

    private void stopActivity(){
        this.finish();
    }

    private class GetCoordinates extends AsyncTask<String, Void, String> {

        ProgressDialog pDialog = new ProgressDialog(SetAlarmActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Please wait...");
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            String response;
            String dest = strings[0];

            origin = "test address";
            long epoch = System.currentTimeMillis()/1000; //used for traffic prediction

            try {
                HttpDataHandler http = new HttpDataHandler();
                String url = String.format("https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins="
                        + origin +"&destinations=" + dest + "&departure_time=" + epoch + seconds
                        + "&key=AIzaSyAe1KIAuVjYq6YCkeFbhZI5U9cy4IY6LF0");

                response = http.getHTTPData(url);
                return response;
            } catch (Exception e) {
                return null;
            }

        }


        protected void onPostExecute(String s) {

            try {

                pDialog.cancel(); //cancel the progress dialog

                JSONObject jobj = new JSONObject(s);
                JSONArray resultsArray = jobj.getJSONArray("rows");
                int traffic = resultsArray.getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration").getInt("value");

                int minutes = traffic / 60;
                int hours = minutes / 60;

                if(alarmMinute + (minutes%60) > 59){
                    alarmHour += 1;
                    alarmMinute = (alarmMinute + minutes%60) - 60;
                }else{
                    alarmMinute += minutes % 60;
                }

                if(alarmHour + hours > 24){
                    alarmHour = (alarmHour + hours) - 24;
                }

                //Send time via BluetoothLE
                //DeviceControlActivity.sendAlarm(alarmHour + hours, alarmMinute);

                stopActivity();

            } catch (JSONException e) {

            }


        }


    }
}
