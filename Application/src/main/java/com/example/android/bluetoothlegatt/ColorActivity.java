package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

public class ColorActivity extends Activity{

    private SeekBar redBar;
    private SeekBar greenBar;
    private SeekBar blueBar;
    private View rect;

    private int r = 0;
    private int g = 0;
    private int b = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);

        redBar = (SeekBar) findViewById(R.id.redBar);
        greenBar = (SeekBar) findViewById(R.id.greenBar);
        blueBar = (SeekBar) findViewById(R.id.blueBar);
        rect = findViewById(R.id.rectangle);

        redBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                r = seekBar.getProgress();
                rect.setBackgroundColor(Color.rgb(r,g,b));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        greenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                g = seekBar.getProgress();
                rect.setBackgroundColor(Color.rgb(r,g,b));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        blueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                b = seekBar.getProgress();
                rect.setBackgroundColor(Color.rgb(r,g,b));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    public void sendColor(View v){
        DeviceControlActivity.sendColor(r,g,b);
        this.finish();
    }







}
