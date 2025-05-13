package at.fhj.hydromate;

import android.content.Intent;
import android.content.pm.PackageManager;
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
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

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

        TextView textMili = findViewById(R.id.tvMl);
        TextView textProcent = findViewById(R.id.tvProcent);

        int volume = getIntent().getIntExtra("volume",0);
        double dailyIntake = getDailyIntake(86,185,22,"Male",15,5000);

        textMili.setText(volume + " ml / " +(int)dailyIntake + " ml");

        double procent = (volume/dailyIntake) * 100;
        double roundedProcent = Math.round(procent * 100.0) / 100.0;


        textProcent.setText(roundedProcent + " % of your Goal");

        TextView tvStepsView = findViewById(R.id.tvSteps);

        stepCounterManager = new StepCounterManager(this, stepsToday -> {tvStepsView.setText(stepsToday + " Steps");});

        gpsHelper = new GPSHelper(this);

        gpsHelper.requestLocation(new LocationCallback() {
            @Override
            public void onLocationReceived(String location) {
                // Hier hast du den Standort
                Log.d("GPS", "Standort: " + location);
                Toast.makeText(MainScreen.this, "Standort: " + location, Toast.LENGTH_LONG).show();
            }
        });

        stepCounterManager = new StepCounterManager(this, this);
        stepCounterManager.start(); // Startet den Sensor

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stepCounterManager.stop(); // Wichtig zum Ressourcen freigeben
    }

    @Override
    public void onStepsUpdated(int stepsToday) {
        // Hier bekommst du die aktuellen Schritte des Tages
        Log.d("Steps", "Schritte heute: " + stepsToday);
        // z.B. UI aktualisieren:
        TextView stepTextView = findViewById(R.id.tvSteps);
        stepTextView.setText("Schritte heute: " + stepsToday);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gpsHelper.requestLocation(new LocationCallback() {
                    @Override
                    public void onLocationReceived(String location) {
                        Log.d("GPS", "Standort nach Berechtigung: " + location);
                        Toast.makeText(MainScreen.this, "Standort: " + location, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(this, "Standortberechtigung wurde verweigert", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stepCounterManager.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stepCounterManager.stop();
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
        double dailyIntake = (gender == "Male" ? 35 : 31) * weight;

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