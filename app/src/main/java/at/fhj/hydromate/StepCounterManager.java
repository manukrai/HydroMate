package at.fhj.hydromate;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCounterManager implements SensorEventListener {


    private final SensorManager sensorManager;
    private final Sensor stepCounterSensor;
    private final SharedPreferences prefs;
    private final StepUpdateListener listener;

    private final Context context;

    private int stepsAtStart = 0;

    private final Sensor stepDetectorSensor;
    private int stepsFromDetector = 0;

    public interface StepUpdateListener {
        void onStepsUpdated(int stepsToday);
    }

    public StepCounterManager(Context context, StepUpdateListener listener) {
        this.context = context;
        this.listener = listener;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // Initialisiere den Step Detector Sensor (Fallback)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        prefs = context.getSharedPreferences("stepPrefs", Context.MODE_PRIVATE);
        checkNewDay();
        stepsAtStart = prefs.getInt("stepsAtStart", 0);
    }
    public void start() {
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    private void checkNewDay() {
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String savedDate = prefs.getString("date", "");

        if (!today.equals(savedDate)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("date", today);
            editor.putInt("stepsAtStart", 0); // wird beim ersten Sensorwert gesetzt
            editor.apply();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalSteps = (int) event.values[0];

            // Setze stepsAtStart nur, wenn es der erste Wert des Tages ist.
            if (prefs.getInt("stepsAtStart", -1) == -1 && totalSteps > 0) {
                prefs.edit().putInt("stepsAtStart", totalSteps).apply();
                stepsAtStart = totalSteps;
            }

            int stepsToday = totalSteps - stepsAtStart;
            listener.onStepsUpdated(stepsToday);

        } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // TYPE_STEP_DETECTOR gibt bei jedem Schritt typischerweise 1.0 aus
            stepsFromDetector += (int) event.values[0];
            // Alternativ: Aktualisiere den Listener mit den neuen Werten
            listener.onStepsUpdated(stepsFromDetector);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nicht benötigt
    }

}

