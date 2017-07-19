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
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.ToggleButton;

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
    private int prepTime;

    private String modeTransportation;

    private boolean mon = false;
    private boolean tues = false;
    private boolean wed = false;
    private boolean thurs = false;
    private boolean fri = false;
    private boolean sat = false;
    private boolean sun = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);


    }

    public void sendAlarm(View v){

        TimePicker t = (TimePicker) findViewById(R.id.timePickerAlarm);
        EditText prep = (EditText) findViewById(R.id.preptimeEditText);

        //Time to add on
        prepTime = Integer.parseInt(prep.getText().toString());

        //Get time
        alarmHour = t.getHour();
        alarmMinute = t.getMinute();

        //Convert to seconds (for traffic report at time of alarm)
        seconds = toSeconds(alarmHour, alarmMinute);

        //Get address
        address = (EditText) findViewById(R.id.addressEditText);
        destination = address.getText().toString();

        new GetCoordinates().execute(destination.replace(" ", "+")); //Start asynchronous task (call API)

    }

    public void onRadioButtonClicked(View view) {

        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.driving:
                if (checked)
                    modeTransportation = "driving";
                break;
            case R.id.transit:
                if (checked)
                    modeTransportation = "transit";
                break;
            case R.id.walking:
                if (checked)
                    modeTransportation = "walking";
                break;
        }

        Log.d("HERE", "onRadioButtonClicked: " + modeTransportation);
    }

    /**
     * If the Monday toggle is switched
     * @param v
     */
    public void mondayClicked(View v){

        ToggleButton t = (ToggleButton) findViewById(R.id.mondayToggle);

        if(t.isChecked()){
            mon = true;
        }else{
            mon = false;
        }

    }

    /**
     * If the Tuesday toggle is switched
     * @param v
     */
    public void tuesdayClicked(View v){

        ToggleButton t = (ToggleButton) findViewById(R.id.tuesdayToggle);

        if(t.isChecked()){
            tues = true;
        }else{
            tues = false;
        }

    }

    /**
     * If the Wednesday toggle is switched
     * @param v
     */
    public void wednesdayClicked(View v){

        ToggleButton t = (ToggleButton) findViewById(R.id.wednesdayToggle);

        if(t.isChecked()){
            wed = true;
        }else{
            wed = false;
        }

    }

    /**
     * If the Thursday toggle is switched
     * @param v
     */
    public void thursdayClicked(View v){

        ToggleButton t = (ToggleButton) findViewById(R.id.thursdayToggle);

        if(t.isChecked()){
            thurs = true;
        }else{
            thurs = false;
        }

    }

    /**
     * If the Friday toggle is switched
     * @param v
     */
    public void fridayClicked(View v){

        ToggleButton t = (ToggleButton) findViewById(R.id.fridayToggle);

        if(t.isChecked()){
            fri = true;
        }else{
            fri = false;
        }

    }

    /**
     * If the Saturday toggle is switched
     * @param v
     */
    public void saturdayClicked(View v){

        ToggleButton t = (ToggleButton) findViewById(R.id.saturdayToggle);

        if(t.isChecked()){
            sat = true;
        }else{
            sat = false;
        }

    }

    /**
     * If the Sunday toggle is switched
     * @param v
     */
    public void sundayClicked(View v){

        ToggleButton t = (ToggleButton) findViewById(R.id.sundayToggle);

        if(t.isChecked()){
            sun = true;
        }else{
            sun = false;
        }

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
                        + "&mode=" + modeTransportation +"&key=AIzaSyAe1KIAuVjYq6YCkeFbhZI5U9cy4IY6LF0");

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

                while(alarmMinute - (minutes%60) - prepTime < 0){
                    alarmHour -= 1;
                    alarmMinute -= ((minutes%60) - prepTime) + 60;
                }

                //Send time via BluetoothLE
                //DeviceControlActivity.sendAlarm(alarmHour - hours, alarmMinute);

                stopActivity();

            } catch (JSONException e) {

            }


        }


    }
}
