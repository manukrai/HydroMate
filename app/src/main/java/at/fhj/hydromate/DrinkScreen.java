package at.fhj.hydromate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Date;
import java.util.Locale;

public class DrinkScreen extends AppCompatActivity {

    private double drinkProcent = 0.00;
    private String drinkType;

    private HydrationDatabase db;


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



        ImageView imageView = findViewById(R.id.ivIcon);

        switch(intent.getStringExtra("drinkType"))
        {
            case "water":imageView.setImageResource(R.drawable.water_bottle256);
                drinkProcent = 1.00;
                drinkType = "water";
                break;
            case "coffee":imageView.setImageResource(R.drawable.coffee_cup256);
                drinkProcent = 0.95;
                drinkType = "coffee";
                break;
            case "juice":imageView.setImageResource(R.drawable.juice256);
                drinkProcent = 0.90;
                drinkType = "juice";
                break;
            case "tea":imageView.setImageResource(R.drawable.tea256);
                drinkProcent = 1.00;
                drinkType = "tea";
                break;
            case "milk":imageView.setImageResource(R.drawable.milk256);
                drinkProcent = 1.15;
                drinkType = "milk";
                break;
            case "beer":imageView.setImageResource(R.drawable.beer256);
                drinkProcent = 0.80;;
                drinkType = "beer";
                break;
            case "strongAlcohol":imageView.setImageResource(R.drawable.liquor256);
                drinkProcent = 0.50;
                drinkType = "liquor";
                break;
        }

    }

    public void startMainScreen(View view) {
        Intent intent = new Intent(DrinkScreen.this, MainScreen.class);

        int volume = 0;


        if(view.getId() == R.id.btSave)
        {

            EditText etVolumeText = findViewById(R.id.etVolume);

            if(etVolumeText.getText() != null)
            {
                volume = Integer.parseInt(etVolumeText.getText().toString());
            }
            else
            {
                intent.putExtra("volume",0);
            }

        }
        if(view.getId() == R.id.btAdd150)
        {
            volume = 150;
        }
        if(view.getId() == R.id.btAdd250)
        {
            volume = 250;
        }
        if(view.getId() == R.id.btadd500)
        {
            volume = 500;
        }

        HydrationEntry entry = new HydrationEntry();
        entry.volume = volume;
        entry.procent = drinkProcent;
        entry.date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        entry.drinkType = drinkType;
        db.hydrationDao().insert(entry);

        startActivity(intent);
    }
}