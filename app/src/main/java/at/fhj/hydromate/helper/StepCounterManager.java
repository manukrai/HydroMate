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

/**
 * Die Klasse {@code StepCounterManager} verwaltet die Schrittzählung mithilfe
 * von Android-Sensoren wie {@link Sensor#TYPE_STEP_COUNTER} und
 * {@link Sensor#TYPE_STEP_DETECTOR}.
 *
 * <p>
 * Sie verfolgt die täglichen Schritte, speichert die relevanten Werte in
 * {@link SharedPreferences} und benachrichtigt einen {@link StepUpdateListener},
 * sobald sich die Schrittzahl ändert.
 * </p>
 *
 * <p>
 * Die Klasse behandelt automatisch Tageswechsel, indem sie beim Start überprüft,
 * ob ein neuer Tag begonnen hat, und gegebenenfalls die gespeicherten Werte zurücksetzt.
 * </p>
 */

public class StepCounterManager implements SensorEventListener {

    private final SensorManager sensorManager;
    private final Sensor stepCounterSensor;
    private final Sensor stepDetectorSensor;
    private final SharedPreferences prefs;
    private final StepUpdateListener listener;

    private final Context context;

    private int stepsAtStart = 0;
    private int stepsFromDetector = 0;

    /**
     * Interface zur Benachrichtigung über Schrittaktualisierungen.
     */
    public interface StepUpdateListener {
        /**
         * Wird aufgerufen, wenn sich die tägliche Schrittzahl ändert.
         *
         * @param stepsToday aktuelle Anzahl der heute zurückgelegten Schritte.
         */
        void onStepsUpdated(int stepsToday);
    }

    /**
     * Konstruktor zum Erstellen eines neuen {@code StepCounterManager}.
     *
     * @param context  der Anwendungskontext.
     * @param listener ein Callback zur Aktualisierung der Schritte.
     */
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

    /**
     * Startet die Sensorüberwachung für Schrittzähler oder Schritt-Detektor.
     */
    public void start() {
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        } else if (stepDetectorSensor != null) {
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     * Überprüft, ob ein neuer Tag begonnen hat, und setzt bei Bedarf die Zählerwerte zurück.
     */
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

    /**
     * Wird aufgerufen, wenn sich die Sensorwerte ändern.
     *
     * @param event das {@link SensorEvent}, das die Sensordaten enthält.
     */
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

    /**
     * Wird aufgerufen, wenn sich die Genauigkeit des Sensors ändert.
     * Diese Implementierung ignoriert Genauigkeitsänderungen.
     *
     * @param sensor der betroffene Sensor
     * @param i      neuer Genauigkeitswert
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    /**
     * Gibt die Anzahl der heute gezählten Schritte zurück.
     *
     * @return die heutige Schrittanzahl basierend auf dem StepCounter oder StepDetector.
     */
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
