package at.fhj.hydromate.bl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import at.fhj.hydromate.R;
import at.fhj.hydromate.database.HydrationDatabase;
import at.fhj.hydromate.beans.HydrationEntry;

public class DrinkScreen extends AppCompatActivity {

    private double drinkProcent = 0.00;
    private String drinkType;

    private HydrationDatabase db;

    private SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = Room.databaseBuilder(getApplicationContext(),
                        HydrationDatabase.class, "hydration-db")
                .allowMainThreadQueries()
                .build();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_drink_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();

        sp = getApplicationContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        ImageView imageView = findViewById(R.id.ivIcon);

        drinkType = intent.getStringExtra("drinkType");

        switch (drinkType) {
            case "water":
                imageView.setImageResource(R.drawable.water_bottle256);
                drinkProcent = 1.00;
                break;
            case "coffee":
                imageView.setImageResource(R.drawable.coffee_cup256);
                drinkProcent = 0.95;
                break;
            case "juice":
                imageView.setImageResource(R.drawable.juice256);
                drinkProcent = 0.90;
                break;
            case "tea":
                imageView.setImageResource(R.drawable.tea256);
                drinkProcent = 1.00;
                break;
            case "milk":
                imageView.setImageResource(R.drawable.milk256);
                drinkProcent = 1.15;
                break;
            case "beer":
                imageView.setImageResource(R.drawable.beer256);
                drinkProcent = 0.80;
                break;
            case "liquor":
                imageView.setImageResource(R.drawable.liquor256);
                drinkProcent = 0.50;
                break;
        }

    }

    public void startMainScreen(View view) {
        Intent intent = new Intent(DrinkScreen.this, MainScreen.class);

        int volume = 0;

        if (view.getId() == R.id.btSave) {
            EditText etVolumeText = findViewById(R.id.etVolume);

            if (!etVolumeText.getText().toString().equals("")) {
                volume = Integer.parseInt(etVolumeText.getText().toString());
            }
        }
        if (view.getId() == R.id.btAdd150) {
            volume = 150;
        }
        if (view.getId() == R.id.btAdd250) {
            volume = 250;
        }
        if (view.getId() == R.id.btadd500) {
            volume = 500;
        }

        addDrink(volume);

        startActivity(intent);
    }

    public void addDrink(int volume) {
        HydrationEntry entry = new HydrationEntry();
        entry.volume = volume;
        entry.procent = drinkProcent;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        entry.date = sp.getString("date",sdf.format(calendar.getTime()));
        entry.drinkType = drinkType;
        db.hydrationDao().insert(entry);
    }
}