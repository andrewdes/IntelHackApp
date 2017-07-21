package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Wrapper;
import java.util.Calendar;

public class SetAlarmActivity extends Activity {

    private EditText address;
    private String destination;
    private String origin;
    private int seconds;
    private int alarmHour;
    private int alarmMinute;
    private int prepTime;
    private int traffic;

    private int secondsToAdd;

    public boolean run = true;

    private ProgressDialog p;
    private int alarmCounter = 0;
    private int progressCounter = 0;
    private boolean selected = false;
    private String modeTransportation;

    //Sat = 0
    //Fri = 6
    private boolean[] days = {false,false,false,false,false,false,false};

    private String success = "Alarm successfully set";
    private String selectDays = "Please select at least one day!";
    int toastDuration = Toast.LENGTH_SHORT;
    private Toast toast;

    private int currentDay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);

        Calendar c = Calendar.getInstance();
        currentDay = c.get(Calendar.DAY_OF_WEEK);


    }

    public void sendAlarm(View v){

        TimePicker t = (TimePicker) findViewById(R.id.timePickerAlarm);
        EditText prep = (EditText) findViewById(R.id.preptimeEditText);



        //If preptime box is left blank, assume no preptime
        if(prep.getText().toString().trim().equals("")){
            prepTime = 0;
        }else {
            prepTime = toSeconds(0, Integer.parseInt((prep.getText().toString()).trim()));
        }

        //Get time
        alarmHour = t.getHour();
        alarmMinute = t.getMinute();

        //Convert to seconds (for traffic report at time of alarm)
        seconds = toSeconds(alarmHour, alarmMinute);

        //Get address
        address = (EditText) findViewById(R.id.addressEditText);
        destination = address.getText().toString();

        for(int i = 0; i < days.length; i++){
            if(days[i]){
                alarmCounter++;
                secondsToAdd = 86400 * daysToo(currentDay,i);
                new GetCoordinates().execute(destination.replace(" ", "+"), (Integer.toString(secondsToAdd)), Integer.toString(i)); //Start asynchronous task (call API)
                selected = true;
            }

        }

        if(!selected)
            toast.makeText(getApplicationContext(), selectDays, toastDuration).show();




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

    }

    /**
     * If the Monday toggle is switched
     * @param v
     */
    public void mondayClicked(View v){

        ToggleButton t = (ToggleButton) findViewById(R.id.mondayToggle);

        if(t.isChecked()){
            days[2] = true;
        }else{
            days[2] = false;
        }

    }

    /**
     * If the Tuesday toggle is switched
     * @param v
     */
    public void tuesdayClicked(View v){

        ToggleButton t = (ToggleButton) findViewById(R.id.tuesdayToggle);

        if(t.isChecked()){
            days[3] = true;
        }else{
            days[3] = false;
        }

    }

    /**
     * If the Wednesday toggle is switched
     * @param v
     */
    public void wednesdayClicked(View v){

        ToggleButton t = (ToggleButton) findViewById(R.id.wednesdayToggle);

        if(t.isChecked()){
            days[4] = true;
        }else{
            days[4] = false;
        }

    }

    /**
     * If the Thursday toggle is switched
     * @param v
     */
    public void thursdayClicked(View v){

        ToggleButton t = (ToggleButton) findViewById(R.id.thursdayToggle);

        if(t.isChecked()){
            days[5] = true;
        }else{
            days[5] = false;
        }

    }

    /**
     * If the Friday toggle is switched
     * @param v
     */
    public void fridayClicked(View v){

        ToggleButton t = (ToggleButton) findViewById(R.id.fridayToggle);

        if(t.isChecked()){
            days[6] = true;
        }else{
            days[6] = false;
        }

    }

    /**
     * If the Saturday toggle is switched
     * @param v
     */
    public void saturdayClicked(View v){

        ToggleButton t = (ToggleButton) findViewById(R.id.saturdayToggle);

        if(t.isChecked()){
            days[0] = true;
        }else{
            days[0] = false;
        }

    }

    /**
     * If the Sunday toggle is switched
     * @param v
     */
    public void sundayClicked(View v){

        ToggleButton t = (ToggleButton) findViewById(R.id.sundayToggle);

        if(t.isChecked()){
            days[1] = true;
        }else{
            days[1] = false;
        }

    }



    private int toSeconds(int h, int min){
        return((h*60*60) + (min*60));
    }

    private int toMinutes(int s){
        return ((s/60)%60);
    }

    private int toHours(int s){
        return (s/3600);
    }

    private void stopActivity(){
        this.finish();
    }

    private int daysToo(int current, int day){

        if(current == day){
            return 0;
        }

        int counter = 0;

        while(current != day){
            if(current < 6) {
                counter++;
                current++;
            }else{
                current = 0;
                counter++;
            }
        }

        return counter;





    }

    private class GetCoordinates extends AsyncTask<String, Void, Wrapper> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(progressCounter == 0){
                p = new ProgressDialog(SetAlarmActivity.this);
                p.setMessage("Gathering traffic data...");
                p.setCanceledOnTouchOutside(false);
                p.show();
                progressCounter++;
            }

        }

        @Override
        protected Wrapper doInBackground(String... strings) {

            String response;
            String dest = strings[0];

            origin = "1754+Woodview+Avenue+Pickering+ontario";
            long epoch = System.currentTimeMillis()/1000; //used for traffic prediction

            try {
                HttpDataHandler http = new HttpDataHandler();
                String url = String.format("https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins="
                        + origin +"&destinations=" + dest + "&departure_time=" + (epoch + seconds + Integer.parseInt(strings[1]))
                        + "&mode=" + modeTransportation + "&traffic_model:best_guess" +"&key=AIzaSyAe1KIAuVjYq6YCkeFbhZI5U9cy4IY6LF0");

                response = http.getHTTPData(url);

                Wrapper w = new Wrapper();
                w.googleResult = response;
                w.currentDayToSend = Integer.parseInt(strings[2]);

                return w;
            } catch (Exception e) {
                return null;
            }

        }


        protected void onPostExecute(Wrapper w) {

            try {

                String s = w.googleResult;
                int day = w.currentDayToSend;

                JSONObject jobj = new JSONObject(s);

                if(jobj.get("status").equals("OK")) {

                    if(modeTransportation.equals("driving")) {

                        JSONArray resultsArray = jobj.getJSONArray("rows");
                        traffic = resultsArray.getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration_in_traffic").getInt("value");

                    }else{
                        traffic = 0;
                    }

                    Log.d("HERE", "RETURNED: " + traffic);
                    Log.d("HERE", "onPostExecute: H: " + toHours(seconds - traffic - prepTime) + "M: " + toMinutes(seconds - traffic - prepTime));
                    Log.d("HERE", "RETURNED: " + day);




                }else{
                    traffic = 0;
                    int alarmTimeSec = seconds - traffic - prepTime;

                    //Send time via BluetoothLE
                    DeviceControlActivity.sendAlarm(toHours(alarmTimeSec), toMinutes(alarmTimeSec), day);
                }



                progressCounter += 1;

                if(progressCounter == (alarmCounter + 1)){
                    p.cancel();
                    stopActivity();
                    toast.makeText(getApplicationContext(), success, toastDuration).show();
                }

            } catch (JSONException e) {

            }


        }


    }


    public class Wrapper{
        public String googleResult;
        public int currentDayToSend;
    }
}
