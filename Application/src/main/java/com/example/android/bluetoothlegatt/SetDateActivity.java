package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

public class SetDateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_date);
    }

    public void getDate (View v){

        DatePicker d = (DatePicker) findViewById(R.id.datePicker);

        //Send date via BluetoothLE
        DeviceControlActivity.sendDate(d.getDayOfMonth(), d.getMonth() + 1,  d.getYear() - 2000);

        //Finish activity
        this.finish();

    }
}
