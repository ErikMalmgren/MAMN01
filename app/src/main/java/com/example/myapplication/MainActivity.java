package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;


import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView textView;
    private boolean isSensorActive;
    private boolean showDegrees;
    private Button convertButton;
    private Switch toggleVoice;
    private Vibrator vibrator;
    private Boolean isVibrating;
    private Boolean isSpeaking;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);

        Button accelerometerButton = findViewById(R.id.button);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerometerButton.setOnClickListener(view -> toggleAccelerometer());

        convertButton = findViewById(R.id.convertButton);
        convertButton.setVisibility(View.GONE);
        convertButton.setEnabled(false);
        convertButton.setOnClickListener(view -> convertToDegrees());

        isVibrating = false;
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        isSpeaking = false;
        textToSpeech = new TextToSpeech(this, status -> {
            if(status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.getDefault());
            }
        });
        toggleVoice = findViewById(R.id.toggleVoice);
        toggleVoice.setVisibility(View.GONE);
        toggleVoice.setEnabled(false);
        toggleVoice.setOnCheckedChangeListener((buttonView, isChecked) -> isSpeaking = isChecked);
    }

    private void toggleAccelerometer() {
        if(isSensorActive) {
            sensorManager.unregisterListener(this);
            convertButton.setVisibility(View.GONE);
            convertButton.setEnabled(false);
            textView.setText("");

            toggleVoice.setVisibility(View.GONE);
            toggleVoice.setEnabled(false);
        } else {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            convertButton.setVisibility(View.VISIBLE);
            convertButton.setEnabled(true);

            toggleVoice.setVisibility(View.VISIBLE);
            toggleVoice.setEnabled(true);
        }
        isSensorActive = !isSensorActive;
    }

    private void convertToDegrees() {
        showDegrees = !showDegrees;
    }

    private void checkVibration(double pitch, double roll) {
        if (Math.abs(pitch) <= 0.5 && Math.abs(roll) <= 0.5) {
            if (!isVibrating) {
                if (isSpeaking) {
                    String message = "Telefonen ligger platt";
                    textToSpeech.speak(message, TextToSpeech.QUEUE_ADD, null, null);
                } else {
                    long[] pattern = {0, 1000};
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        VibrationEffect effect = VibrationEffect.createWaveform(pattern, 0);
                        vibrator.vibrate(effect);
                    } else {
                        vibrator.vibrate(pattern, 0);
                    }
                }
                isVibrating = true;
            }
        } else {
            if (isVibrating) {
                if (!isSpeaking) {
                    vibrator.cancel();
                }
                isVibrating = false;
            }
        }
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];

        if(showDegrees) {
            double pitch = Math.toDegrees(Math.atan2(y, Math.sqrt(x*x + z*z)));
            double roll = Math.toDegrees(Math.atan2(-x, Math.sqrt(y*y + z*z)));
            String degreeValues = String.format("Pitch: %.1f°\nRoll: %.1f°", pitch, roll);
            textView.setText(degreeValues);

            checkVibration(pitch, roll);
        } else {
            String accelerometerValues = "X: " + x + "\nY: " + y + "\nZ: " + z;
            textView.setText(accelerometerValues);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Gör inget
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isSensorActive) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSensorActive) {

            sensorManager.unregisterListener(this);
        }
    }
}