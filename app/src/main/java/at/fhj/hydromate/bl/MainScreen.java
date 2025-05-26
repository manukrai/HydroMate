package at.fhj.hydromate.bl;

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


public class MainScreen extends AppCompatActivity implements StepCounterManager.StepUpdateListener {

    private StepCounterManager stepCounterManager;
    private GPSHelper gpsHelper;
    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 1001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    private TextView tvStepsView;
    private TextView tvTemperatureView;
    private TextView textMili;
    private ProgressBar progressBar;
    private TextView tvProgressInfo;

    private EditText etDate;


    private double dailyIntake;
    private SharedPreferences sp;
    private HydrationDatabase db;

    private final String apiKey = "38127a56fbc2778ac0038588c589242a";

    private String date;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Calendar calendar = Calendar.getInstance();


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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            }
        }

        setTrinkErinnerungAlarm(this, 9, 0);
        setTrinkErinnerungAlarm(this, 12, 0);
        setTrinkErinnerungAlarm(this, 15, 0);
        setTrinkErinnerungAlarm(this, 19, 0);

        findViewById(R.id.btNotification).setOnClickListener(v -> {
            new AlarmReceiver().onReceive(this, new Intent());
        });

    }


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
                            5000
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
                            5000
                    );
                });
            }
        }).start();
    }

    private boolean checkActivityPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void requestActivityPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    ACTIVITY_RECOGNITION_REQUEST_CODE);
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }


    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }


    @Override
    public void onStepsUpdated(int stepsToday) {
        tvStepsView = findViewById(R.id.tvSteps);
        tvStepsView.setText(stepsToday + " Steps");
    }

    private void startStepCounter() {
        // Hier initialisierst du deinen StepCounterManager
        stepCounterManager = new StepCounterManager(this, this); // `this` ist StepUpdateListener
        stepCounterManager.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStepCounter();
            } else {
                tvStepsView.setText("Allow Actvity!");

            }

            // Standortberechtigung trotzdem prüfen!
            if (!checkLocationPermission()) {
                requestLocationPermission();
            } else {
                startLocationRequest();
            }

        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean permissionGranted = false;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                    break;
                }
            }

            if (permissionGranted) {
                startLocationRequest();
            } else {
                tvTemperatureView.setText("Allow Location!");
            }
        }
    }


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

    public void startSettingsScreen(View view) {
        Intent intent = new Intent(MainScreen.this, SettingScreen.class);

        startActivity(intent);
    }

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


    public void setTrinkErinnerungAlarm(Context context, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Wenn die Zeit heute schon vorbei ist, auf morgen verschieben
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        // Wiederholender Alarm (täglich)
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

}