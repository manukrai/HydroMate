package at.fhj.hydromate;

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
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.Manifest;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import org.json.JSONObject;


public class MainScreen extends AppCompatActivity implements StepCounterManager.StepUpdateListener {

    private StepCounterManager stepCounterManager;
    private GPSHelper gpsHelper;
    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 1001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    private TextView tvStepsView;
    private TextView tvTemperatureView;
    private TextView textMili;
    private TextView textProcent;

    private double dailyIntake;
    private SharedPreferences sp;
    private HydrationDatabase db;

    private final String apiKey = "38127a56fbc2778ac0038588c589242a";


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

        db = Room.databaseBuilder(getApplicationContext(),
                                HydrationDatabase.class, "hydration-db")
                                .build();


        this.textMili = findViewById(R.id.tvMl);
        this.textProcent = findViewById(R.id.tvProcent);
        this.tvTemperatureView = findViewById(R.id.tvTemperature);
        this.tvStepsView = findViewById(R.id.tvSteps);


        if (sp.getInt("weight", 0) == 0 ||
            sp.getInt("height", 0) == 0 ||
            sp.getInt("age", 0) == 0 ||
            sp.getString("gender", "").equals(""))
        {
            textMili.setText("Please change settings!");
            textProcent.setText("");
        }

        stepCounterManager = new StepCounterManager(this, stepsToday -> {tvStepsView.setText(stepsToday + " Steps");});
        gpsHelper = new GPSHelper(this);

        if (!checkActivityPermission()) {
            requestActivityPermission();
        } else if (!checkLocationPermission()) {
            requestLocationPermission();
        } else {
            startStepCounter();
            startLocationRequest();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();

        dailyIntake = getDailyIntake(sp.getInt("weight", 0), sp.getInt("height", 0), sp.getInt("age", 0), sp.getString("gender", "Male"), 0, 5000);

        executor.execute(() -> {
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            double volume = db.hydrationDao().getEffectiveHydrationForToday(todayDate);

            // Optional: zurück an UI-Thread
            new Handler(Looper.getMainLooper()).post(() -> {
                textMili.setText(volume + " ml / " + (int) dailyIntake + " ml");
                double procent = (volume / dailyIntake) * 100;
                double roundedProcent = Math.round(procent * 100.0) / 100.0;
                textProcent.setText(roundedProcent + " % of your Goal");
            });
        });

    }


    public void fetchTemperature(double lat , double lon){
        new Thread(() -> {
            try {
                String urlString = String.format(
                        Locale.US,
                        "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric",
                        lat, lon, this.apiKey
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

                // Zurück auf den UI-Thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    tvTemperatureView.setText(temp + "°C");
                    if (temp >= 20 && temp <= 25)
                    {
                        dailyIntake += 200;
                    }
                    else if (temp > 25 && temp <= 30)
                    {
                        dailyIntake += 500;
                    }
                    else if (temp > 30)
                    {
                        dailyIntake += 700;
                    }

                    String text = textMili.getText().toString();  // "750 ml / 2000 ml"
                    String[] parts = text.split(" ");             // ["750", "ml", "/", "2000", "ml"]
                    double volume = Double.parseDouble(parts[0]);

                    textMili.setText(volume + " ml / " + (int) dailyIntake + " ml");
                    double procent = (volume / dailyIntake) * 100;
                    double roundedProcent = Math.round(procent * 100.0) / 100.0;
                    textProcent.setText(roundedProcent + " % of your Goal");
                });

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    tvTemperatureView.setText("Fehler beim Laden");
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
        tvStepsView.setText(stepsToday+"");
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
                fetchTemperature(lat,lon);
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
        drinkMap.put(R.id.btStrongAlcohol, "strongAlcohol");

        String drinkType = drinkMap.get(view.getId());
        if (drinkType != null) {
            intent.putExtra("drinkType", drinkType);
        }

        startActivity(intent);

    }

    public void startSettingsScreen(View view) {
        Intent intent = new Intent(MainScreen.this, SettingScreen.class);
        startActivity(intent);
    }

    public double getDailyIntake(int weight, int height,int age, String gender, double temperature, int steps)
    {
        double dailyIntake = (gender.toString().equals("Male") ? 35 : 31) * weight;

        // Alter berücksichtigen
        if (age >= 65)
        {
            dailyIntake *= 0.9;
        }

        // Aktivitätszuschlag durch Schritte
        if (steps >= 5000 && steps <= 10000)
        {
            dailyIntake += 300;
        }
        else if (steps > 10000)
        {
            dailyIntake += 500;
        }


        if(dailyIntake >= 6000)
        {
            return 6000;
        }
        return dailyIntake;
    }



}