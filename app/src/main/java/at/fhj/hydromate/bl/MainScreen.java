package at.fhj.hydromate.bl;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.Manifest;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import org.json.JSONObject;

import at.fhj.hydromate.Adapter.DrinkAdapter;
import at.fhj.hydromate.beans.HydrationEntry;
import at.fhj.hydromate.helper.AlarmReceiver;
import at.fhj.hydromate.helper.GPSHelper;
import at.fhj.hydromate.helper.LocationCallback;
import at.fhj.hydromate.R;
import at.fhj.hydromate.helper.StepCounterManager;
import at.fhj.hydromate.database.HydrationDatabase;


/**
 * Klasse MainScreen ist die Hauptaktivität der Hydromate-App
 * Sie zeigt die akutelle Hydrationsübersicht, Schritte, Temperatur und erlaubt Interaktionen
 * mit Trink-und Einstellungsbildschirmen
 *
 * Sie verwaltet auch Benachrichtigungen, Standort- und Schrittzählerdaten sowie die Berechnung des empfohlenen Tagesbedarfs an Flüssigkeit
 */
public class MainScreen extends AppCompatActivity implements StepCounterManager.StepUpdateListener {

    /**
     * Schrittzähler-Manager zur Erfassung der täglichen Schritte
     */
    private StepCounterManager stepCounterManager;
    /**
     * Helferklasse zur Standortermittlung
     */
    private GPSHelper gpsHelper;

    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 1000;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1002;

    /**
     * TextView zur Anzeige der Schritte
     */
    private TextView tvStepsView;
    /**
     * TextView zur Anzeige der aktuellen Temperatur
     */
    private TextView tvTemperatureView;
    /**
     * TextView zur Anzeige der getrunkenen Flüssigkeitsmenge
     */
    private TextView textMili;
    /**
     * Fortschittsbalken für den Flüssigkeitsverbrauch
     */
    private ProgressBar progressBar;
    /**
     * TextView für prozentualen Fortschritt
     */
    private TextView tvProgressInfo;

    /**
     * Eingabefeld für das Datum
     */
    private EditText etDate;

    /**
     * Empfohlene täglihce Flüssigkeitsaufnahme in ml
     */
    private double dailyIntake;
    /**
     * Zugriff auf gespeichterte Benutzereinstellungen
     */
    private SharedPreferences sp;
    /**
     * Zugriff auf die lokale Hydrationsdatenbank
     */
    private HydrationDatabase db;

    /**
     * OpenWeatherMap API-Schlüssel
     */
    private final String apiKey = "38127a56fbc2778ac0038588c589242a";

    /**
     * Aktuell ausgewähltes Datum
     */
    private String date;

    /**
     * Datumsformatierer
     */
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    /**
     * Kalenderinstanz für Datumsauswahl
     */
    private Calendar calendar = Calendar.getInstance();

    /**
     * Heutige Schrittanzahl
     */
    private int stepsToday;


    /**
     * Wird beim Starten der Aktivität aufgerufen
     * Initialisiert UI-Komponenten, Datenbank, Schrittzähler und Standortabfrage
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_screen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        db = Room.databaseBuilder(getApplicationContext(),
                        HydrationDatabase.class, "hydration-db")
                .build();

        this.textMili = findViewById(R.id.tvMl);
        this.tvTemperatureView = findViewById(R.id.tvTemperature);
        this.tvStepsView = findViewById(R.id.tvSteps);
        this.progressBar = findViewById(R.id.progressBar);
        this.tvProgressInfo = findViewById(R.id.tvProgressInfo);
        this.etDate = findViewById(R.id.etDate);

        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        calendar = Calendar.getInstance();

        date = sp.getString("date", sdf.format(calendar.getTime()));


        etDate.setText(date);


        etDate.setOnClickListener(v -> {
            String[] parts = sp.getString("date", sdf.format(calendar.getTime())).split("-");

            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int day = Integer.parseInt(parts[2]);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MainScreen.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(Calendar.YEAR, selectedYear);
                        calendar.set(Calendar.MONTH, selectedMonth);
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                        String selectedDate = sdf.format(calendar.getTime());
                        editor.putString("date", selectedDate);
                        etDate.setText(selectedDate);
                        updateHydrationDataForDate(selectedDate);
                        editor.commit();
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });

        stepCounterManager = new StepCounterManager(this, stepsToday -> {
            tvStepsView.setText(stepsToday + " Steps");
        });

        gpsHelper = new GPSHelper(this);

        if (!checkActivityPermission()) {
            requestActivityPermission();
        } else if (!checkLocationPermission()) {
            requestLocationPermission();
        } else {
            startStepCounter();
            startLocationRequest();
        }

        textMili.setText("Loading...");
        progressBar.setProgress(0);
        tvProgressInfo.setText("Loading...");
        editor.putString("date", date);
        editor.commit();

        stepCounterManager = new StepCounterManager(this, this);

        // Sensoren starten
        stepCounterManager.start();

        stepsToday = stepCounterManager.getDailySteps();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1002);
            }
        }



        findViewById(R.id.btNotification).setOnClickListener(v -> {
            new AlarmReceiver().onReceive(this, new Intent());
        });



    }

    /**
     * Aktualisiert die Hydrationsdaten für ein gegebenes Datum aus der Datenbank
     * @param date Das zu aktualisierende Datum im Format yyyy-mm-dd
     */
    private void updateHydrationDataForDate(String date) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            double volume = db.hydrationDao().getEffectiveHydrationForToday(date);

            new Handler(Looper.getMainLooper()).post(() -> {

                textMili.setText(volume + " ml / " + (int) dailyIntake + " ml");
                double procent = (volume / dailyIntake) * 100;
                int roundedProcent = (int) Math.round(procent);
                progressBar.setProgress(roundedProcent);
                tvProgressInfo.setText(roundedProcent + "%");
                updateList(date);
            });
        });
    }

    /**
     * Aktualisiert die Getränkeliste für den aktuellen Taf im RecyclerView
     * @param date Das anzuzeigende Datum
     */
    private void updateList(String date) {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            List<HydrationEntry> entries = db.hydrationDao().getEntriesForDate(date);

            runOnUiThread(() -> {
                DrinkAdapter adapter = new DrinkAdapter(entries);
                recyclerView.setAdapter(adapter);
            });
        });
    }


    /**
     * Ruft die Temperaturdaten von OpenWeatherMap basierend auf dem Standort ab
     * @param lat Breitengrad
     * @param lon Längengrad
     */
    public void fetchTemperature(double lat, double lon) {
        new Thread(() -> {
            try {
                String urlString = String.format(
                        Locale.US,
                        "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric",
                        lat, lon, this.apiKey // Stelle sicher, dass apiKey korrekt gesetzt ist
                );
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject json = new JSONObject(result.toString());
                double temp = json.getJSONObject("main").getDouble("temp");



                new Handler(Looper.getMainLooper()).post(() -> {
                    tvTemperatureView.setText(temp + "°C");

                    dailyIntake = getDailyIntake(
                            sp.getInt("weight", 0),
                            sp.getInt("height", 0),
                            sp.getInt("age", 0),
                            sp.getString("gender", "Male"),
                            temp,
                            stepsToday
                    );

                    updateHydrationDataForDate(sp.getString("date", sdf.format(calendar.getTime())));
                    updateList(sp.getString("date", sdf.format(calendar.getTime())));
                });

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    tvTemperatureView.setText("Error");
                    dailyIntake = getDailyIntake(
                            sp.getInt("weight", 0),
                            sp.getInt("height", 0),
                            sp.getInt("age", 0),
                            sp.getString("gender", "Male"),
                            0,
                            stepsToday
                    );
                });
            }
        }).start();
    }

    /**
     * Überprüft, ob die Berechtigung zur Aktivitötserkennung gewährt wurde
     * @return true, wenn die Berechtigung vorhanden ist
     */
    private boolean checkActivityPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Fordert die Berechtigung zur Aktivitätserkennung an
     */
    private void requestActivityPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    ACTIVITY_RECOGNITION_REQUEST_CODE);
        }
    }

    /**
     * Überprüft, ob Standortberechtigungen gewährt wurden
     * @return true, wenn Zugriff auf Standort erlaubt ist
     */
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * Fordert Standortberechtigungen vom Benutzer an
     */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }


    /**
     * Wird aufgerufen, wenn die Schrittanzahl aktualisiert wurde
     * @param stepsToday
     */
    @Override
    public void onStepsUpdated(int stepsToday) {
        tvStepsView = findViewById(R.id.tvSteps);
        tvStepsView.setText(stepsToday + " Steps");
    }

    /**
     * Startet die Schrittzähler-Logik
     */
    private void startStepCounter() {
        // Hier initialisierst du deinen StepCounterManager
        stepCounterManager = new StepCounterManager(this, this); // `this` ist StepUpdateListener
        stepCounterManager.start();
    }

    /**
     * Behandelt die Ergebnisse von Berechtigungsanfragen
     * @param requestCode The request code
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case ACTIVITY_RECOGNITION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startStepCounter();
                } else {
                    Toast.makeText(this, "Aktivitäten blockiert", Toast.LENGTH_SHORT).show();
                }

                // Danach: Standortberechtigung prüfen
                if (!checkLocationPermission()) {
                    requestLocationPermission();  // Löst LOCATION_PERMISSION_REQUEST_CODE aus
                } else {
                    startLocationRequest();

                    // Danach: Benachrichtigungsberechtigung prüfen
                    if (!checkNotificationPermission()) {
                        requestNotificationPermission();  // Löst 1002 aus
                    }
                }
                break;

            case LOCATION_PERMISSION_REQUEST_CODE:
                boolean locationGranted = false;
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        locationGranted = true;
                        break;
                    }
                }

                if (locationGranted) {
                    startLocationRequest();
                } else {
                    Toast.makeText(this, "Standort blockiert", Toast.LENGTH_SHORT).show();
                }


                if (!checkNotificationPermission()) {
                    requestNotificationPermission();
                }
                break;

            case 1002:  // POST_NOTIFICATIONS
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setTrinkErinnerungAlarm(this, 9, 0);
                    setTrinkErinnerungAlarm(this, 12, 0);
                    setTrinkErinnerungAlarm(this, 15, 0);
                    setTrinkErinnerungAlarm(this, 19, 0);
                } else {
                    Toast.makeText(this, "Benachrichtigungen blockiert", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Prüft, ob Benachrichtigungsrechte erteilt wurden
     * @return
     */
    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Vor Android 13 ist sie automatisch erlaubt
    }

    /**
     * Fordert die Benachrichtigungsberechtigung an
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1002);
        }
    }


    /**
     * Startet eine Standortanfrage mithilfe von GPSHelper
     */
    private void startLocationRequest() {
        gpsHelper.requestLocation(new LocationCallback() {
            @Override
            public void onLocationReceived(String location) {
                Log.d("GPS", "Standort: " + location);
                String[] parts = location.split(",");
                double lat = Double.parseDouble(parts[0].trim());
                double lon = Double.parseDouble(parts[1].trim());
                fetchTemperature(lat, lon);
            }
        });
    }


    /**
     * Startet die DrinkScreen-Aktivität mit übergebenem Getränketyp
     * @param view Die geklickte Schaltfläche
     */
    public void startDrinkScreen(View view) {
        Intent intent = new Intent(MainScreen.this, DrinkScreen.class);

        Map<Integer, String> drinkMap = new HashMap<>();
        drinkMap.put(R.id.btWater, "water");
        drinkMap.put(R.id.btCoffee, "coffee");
        drinkMap.put(R.id.btJuice, "juice");
        drinkMap.put(R.id.btTea, "tea");
        drinkMap.put(R.id.btMilk, "milk");
        drinkMap.put(R.id.btBeer, "beer");
        drinkMap.put(R.id.btStrongAlcohol, "liquor");

        String drinkType = drinkMap.get(view.getId());
        if (drinkType != null) {
            intent.putExtra("drinkType", drinkType);
        }

        intent.putExtra("date", date);

        startActivity(intent);

    }

    /**
     * Löscht einen Eintrag anhand seiner ID
     * @param view Das View-Element, dessen ID dem löschenden Eintrag entspricht
     */
    public void deleteItem(View view) {

        Executors.newSingleThreadExecutor().execute(() -> {
            db.hydrationDao().deleteById(view.getId());

            runOnUiThread(() -> {
                String date = sp.getString("date", sdf.format(calendar.getTime()));
                updateHydrationDataForDate(date);
                updateList(date);
            });
        });
    }

    /**
     * Öffnet die Einstellungsansicht
     * @param view
     */
    public void startSettingsScreen(View view) {
        Intent intent = new Intent(MainScreen.this, SettingScreen.class);

        startActivity(intent);
    }

    /**
     * Berechnet den empfohlenen täglichen Flüssigkeitsbedarf
     * @param weight Gewicht in kg
     * @param height Größe in cm
     * @param age Alter in Jahren
     * @param gender Geschlecht ("Male" oder "Female")
     * @param temp Außentemperatur in °C
     * @param steps Schritte pro Tag
     * @return empfohlene Flüssigkeitsmenge in ml
     */
    public double getDailyIntake(int weight, int height, int age, String gender, double temp, int steps) {
        double dailyIntake = (gender.toString().equals("Male") ? 35 : 31) * weight;

        // Alter berücksichtigen
        if (age >= 65) {
            dailyIntake *= 0.9;
        }

        // Aktivitätszuschlag durch Schritte
        if (steps >= 5000 && steps <= 10000) {
            dailyIntake += 300;
        } else if (steps > 10000) {
            dailyIntake += 500;
        }


        if (dailyIntake >= 6000) {
            return 6000;
        }


        if (temp >= 20 && temp <= 25) {
            dailyIntake += 200;
        } else if (temp > 25 && temp <= 30) {
            dailyIntake += 500;
        } else if (temp > 30) {
            dailyIntake += 700;
        }

        if (height > 180) {
            dailyIntake += 100;
        }

        return dailyIntake;
    }


    /**
     * Setzt einen täglichen Trink-Erinnerungsalarm zur angegebenen Uhrzeit
     * @param context Kontext der App
     * @param hour Stunde (24-Stunden Format)
     * @param minute Minute
     */
    public void setTrinkErinnerungAlarm(Context context, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Wiederholender Alarm (täglich)
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

}