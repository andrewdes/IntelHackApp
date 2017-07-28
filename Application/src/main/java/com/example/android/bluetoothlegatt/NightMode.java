package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

public class NightMode extends Activity {

    private TimePicker start;
    private TimePicker end;

    private Toast toast;
    private String complete = "Night mode successfully set";
    private int duration = Toast.LENGTH_SHORT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_night_mode);

        start = (TimePicker) findViewById(R.id.startPicker);
        end = (TimePicker) findViewById(R.id.endPicker);

        start.setHour(22);
        start.setMinute(0);

        end.setHour(7);
        end.setMinute(0);

    }

    public void nightMode(View v){

        DeviceControlActivity.sendNightMode(start.getHour(), start.getMinute(), 10);
        DeviceControlActivity.sendNightMode(end.getHour(), end.getMinute(), 11);

        toast.makeText(getApplicationContext(), complete, duration).show();

        this.finish();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                this.finish(); //finish activity when back is clicked (preserves connection)
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
