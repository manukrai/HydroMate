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

/**
 * Activity zur Eingabe und Speicherung eines neuen Trinkeintrags
 * Benutzer können die Getränkemenge wählen uns speichern
 */
public class DrinkScreen extends AppCompatActivity {

    /**
     * Prozentuale Flüssigkeitsverwertbarkeit des gewählten Getränks
     */
    private double drinkProcent = 0.00;
    /**
     * Der ausgewählte Getränketyp
     */
    private String drinkType;

    /**
     * Instanz der Room-Datenbank für Trinkeinträge
     */
    private HydrationDatabase db;

    /**
     * SharedPreferences zur Speicherung des aktuellen Datums
     */
    private SharedPreferences sp;


    /**
     * Lifecycle-Methode beim Start der Activity
     * Initialisiert Layout, Datenbank und UI
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
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
                drinkProcent = 0.98;
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
                drinkProcent = 0.60;
                break;
        }

    }

    /**
     * Wird aufgerufen, wenn ein Button geklickt wird
     * Erstellt einen neuen Trinkeintrag und startet die Hauptansicht
     * @param view die gedrückte View
     */
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

    /**
     * Fügt einen neuen {@link HydrationEntry} mit dem angegebenen Volumen in die Datenbank ein
     * @param volume Volumen des Getränks in ml
     */
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