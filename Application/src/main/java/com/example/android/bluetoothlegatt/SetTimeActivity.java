package com.example.android.bluetoothlegatt;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TimePicker;

public class SetTimeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_time);
    }

    public void getTime (View v){

        TimePicker t = (TimePicker) findViewById(R.id.timePicker);

        //Send time via BluetoothLE
        DeviceControlActivity.sendTime(t.getHour(), t.getMinute());

        //Finish activity
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
