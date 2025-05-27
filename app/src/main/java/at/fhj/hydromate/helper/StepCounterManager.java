package at.fhj.hydromate.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCounterManager implements SensorEventListener {

    private final SensorManager sensorManager;
    private final Sensor stepCounterSensor;
    private final Sensor stepDetectorSensor;
    private final SharedPreferences prefs;
    private final StepUpdateListener listener;

    private final Context context;

    private int stepsAtStart = 0;
    private int stepsFromDetector = 0;

    public interface StepUpdateListener {
        void onStepsUpdated(int stepsToday);
    }

    public StepCounterManager(Context context, StepUpdateListener listener) {
        this.context = context;
        this.listener = listener;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        prefs = context.getSharedPreferences("stepPrefs", Context.MODE_PRIVATE);

        checkNewDay();
        stepsAtStart = prefs.getInt("stepsAtStart", 0);
        stepsFromDetector = prefs.getInt("stepsFromDetector", 0);
    }

    public void start() {
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        } else if (stepDetectorSensor != null) {
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void checkNewDay() {
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String savedDate = prefs.getString("date", "");

        if (!today.equals(savedDate)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("date", today);
            editor.putInt("stepsAtStart", -1); // wird beim ersten Sensorwert gesetzt
            editor.putInt("stepsFromDetector", 0);
            editor.putInt("lastTotalSteps", -1);
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

            // Speichere den aktuellen Gesamtwert für spätere Abrufe
            prefs.edit().putInt("lastTotalSteps", totalSteps).apply();

            int stepsToday = totalSteps - stepsAtStart;
            listener.onStepsUpdated(stepsToday);

        } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            stepsFromDetector += (int) event.values[0];
            prefs.edit().putInt("stepsFromDetector", stepsFromDetector).apply();
            listener.onStepsUpdated(stepsFromDetector);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public int getDailySteps() {
        if (stepCounterSensor != null) {
            int currentTotalSteps = prefs.getInt("lastTotalSteps", -1);
            int startSteps = prefs.getInt("stepsAtStart", -1);
            if (currentTotalSteps != -1 && startSteps != -1) {
                return currentTotalSteps - startSteps;
            }
        }
        return stepsFromDetector;
    }
}
