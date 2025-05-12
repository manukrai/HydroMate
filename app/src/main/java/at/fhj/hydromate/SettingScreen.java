package at.fhj.hydromate;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingScreen extends AppCompatActivity {

    private StepCounterManager stepCounterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvStepsView = findViewById(R.id.tvSteps);

        stepCounterManager = new StepCounterManager(this, stepsToday -> {tvStepsView.setText(stepsToday + "Steps");});
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



    public void startMainScreen(View view) {
        Intent intent = new Intent(SettingScreen.this, MainScreen.class);

        EditText etWeight = findViewById(R.id.etWeight);
        EditText etHeight = findViewById(R.id.etHeight);
        EditText etAge = findViewById(R.id.etAge);
        RadioGroup rbGender = findViewById(R.id.rbGroupGender);

        intent.putExtra("weight",etWeight.getText());
        intent.putExtra("height",etHeight.getText());
        intent.putExtra("age",etAge.getText());
        intent.putExtra("gender","");

        startActivity(intent);
    }
}