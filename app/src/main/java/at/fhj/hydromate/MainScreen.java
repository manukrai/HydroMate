package at.fhj.hydromate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;




public class MainScreen extends AppCompatActivity {

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
    }

    public void startDrinkScreen(View view) {
        Intent intent = new Intent(MainScreen.this, DrinkScreen.class);



        if(view.getId() == R.id.btWater)
        {
            intent.putExtra("drinkType","water");
        }

        if(view.getId() == R.id.btCoffee)
        {
            intent.putExtra("drinkType","coffee");
        }

        if(view.getId() == R.id.btJuice)
        {
            intent.putExtra("drinkType","juice");
        }

        if(view.getId() == R.id.btTea)
        {
            intent.putExtra("drinkType","tea");
        }

        if(view.getId() == R.id.btMilk)
        {
            intent.putExtra("drinkType","milk");
        }

        if(view.getId() == R.id.btBeer)
        {
            intent.putExtra("drinkType","beer");
        }

        if(view.getId() == R.id.btStrongAlcohol)
        {
            intent.putExtra("drinkType","strongAlcohol");
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