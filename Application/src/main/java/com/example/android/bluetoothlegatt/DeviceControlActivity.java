/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity{

    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private static BluetoothLeService mBluetoothLeService;

    private boolean mConnected = false;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void clearUI() {
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.button_control);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mDataField = (TextView) findViewById(R.id.data_value);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText("Temperature: " + data + "°C");
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void onClickWrite(View v){
        if(mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(0x11,0);
        }
    }

    public void onShutOff(View v){
        if(mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(0x00,0);
        }
    }

    public void tempRead(View v){


    }

    public void setTimeActivity(View v){
        Intent i = new Intent(this, SetTimeActivity.class);
        startActivity(i);
    }
    public void setDateActivity(View v){
        Intent i = new Intent(this, SetDateActivity.class);
        startActivity(i);
    }
    public void setAlarmActivity(View v){
        Intent i = new Intent(this, SetAlarmActivity.class);
        startActivity(i);
    }
    public void setColorActivity(View v){
        Intent i = new Intent(this, ColorActivity.class);
        startActivity(i);
    }
    public void setNightModeActivity(View v){
        Intent i = new Intent(this, NightMode.class);
        startActivity(i);
    }

    public static void setEvent(int val) {

        if (mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(val, 6);
        }

        sleep(200);

    }

    public static void sendTime(int hour, int minute){

        setEvent(0);

        if (mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(hour, 1);
        }

        sleep(200);


        if (mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(minute, 2);
        }
    }

    public static void sendColor(int r, int g, int b){


        if (mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(r , 3);
        }

        sleep(200);


        if (mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(g, 4);
        }

        sleep(200);


        if (mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(b,8);
        }

    }

    public static void sendDate(int day, int month, int year){

        setEvent(0);

        if (mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(day, 3);
        }

        sleep(200);


        if (mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(month, 4);
        }

        sleep(200);


        if (mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(year, 5);
        }
    }

    public static void sendAlarm(int hour, int minute, int day){

        setEvent(day + 2);

        if (mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(hour, 1);
        }

        sleep(200);


        if (mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(minute, 2);
        }

        sleep(200);



    }

    public static void sendNightMode(int hour, int minute, int event){

        setEvent(event);

        if (mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(hour, 1);
        }

        sleep(200);


        if (mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(minute, 2);
        }

        sleep(200);

    }

    private static void sleep(int time){
        try {
            Thread.sleep(time);
        }catch(Exception e){

        }
    }


}


