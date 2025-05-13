package at.fhj.hydromate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;
import android.Manifest;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainScreen extends AppCompatActivity implements StepCounterManager.StepUpdateListener {

    private StepCounterManager stepCounterManager;
    private GPSHelper gpsHelper;
    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 1001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    private TextView tvStepsView;
    private SharedPreferences sp;

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

        TextView textMili = findViewById(R.id.tvMl);
        TextView textProcent = findViewById(R.id.tvProcent);

        int volume = getIntent().getIntExtra("volume", 0);
        double dailyIntake = getDailyIntake(sp.getInt("weight", 0), sp.getInt("height", 0), sp.getInt("age", 0), sp.getString("gender", "Male"), 15, 5000);

        textMili.setText(volume + " ml / " + (int) dailyIntake + " ml");

        double procent = (volume / dailyIntake) * 100;
        double roundedProcent = Math.round(procent * 100.0) / 100.0;


        if (sp.getInt("weight", 0) == 0 ||
            sp.getInt("height", 0) == 0 ||
            sp.getInt("age", 0) == 0 ||
            sp.getString("gender", "").equals(""))
        {
            textMili.setText("0 ml / 0 ml");
            textProcent.setText("Please Type in Data first!");
        }
        else
        {
            textProcent.setText(roundedProcent + " % of your Goal");
        }



        this.tvStepsView = findViewById(R.id.tvSteps);
        TextView tvTemperatureView = findViewById(R.id.tvTemperature);

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
                Toast.makeText(this, "Aktivitätsberechtigung verweigert", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Standortberechtigung verweigert", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void startLocationRequest() {
        gpsHelper.requestLocation(new LocationCallback() {
            @Override
            public void onLocationReceived(String location) {
                Log.d("GPS", "Standort: " + location);
                TextView tvTemperatureView = findViewById(R.id.tvTemperature);
                tvTemperatureView.setText(location);
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

    public double getDailyIntake(int weight, int height,int age, String gender, int temperature, int steps)
    {
        double dailyIntake = (gender.toString().equals("Male") ? 35 : 31) * weight;

        // Alter berücksichtigen
        if (age >= 65)
        {
            dailyIntake *= 0.9;
        }

        // Temperaturzuschlag
        if (temperature >= 20 && temperature <= 25)
        {
            dailyIntake += 200;
        }
        else if (temperature > 25 && temperature <= 30)
        {
            dailyIntake += 500;
        }
        else if (temperature > 30)
        {
            dailyIntake += 700;
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

        return dailyIntake;
    }



}