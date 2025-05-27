package at.fhj.hydromate.bl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import at.fhj.hydromate.R;

/**
 * Die SettingScreen-Klasse stellt eine Benutzeroberfläche bereit, auf der Nutzer ihre persönlichen Informationen eingeben können
 * Diese Daten werden in den SharedPreferences gespeichert und beim Starten des MainScreens verwendet
 */
public class SettingScreen extends AppCompatActivity {

    /**
     * Referenz auf die SharedPreferences zur Speicherung der Benutzerdaten
     */
    private SharedPreferences sp;

    /**
     * EditText-Feld für das Gewicht des Benutzers
     */
    private EditText etWeight;
    /**
     * EditText-Feld für die Körpergröße des Benutzers
     */
    private EditText etHeight;
    /**
     * EditText-Feld für das Alter des Benutzers
     */
    private EditText etAge;
    /**
     * RadioGroup zur Auswahl des Geschlechts
     */
    private RadioGroup rgGender;


    /**
     * Wird beim Erstellen der Aktivität aufgerufen. Initialisiert die UI-Elemente
     * und lädt gespeicherte Werte aus den SharedPreferences, um sie im UI anzuzeigen
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
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

        sp = getApplicationContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        etAge = findViewById(R.id.etAge);
        rgGender = findViewById(R.id.rbGroupGender);

        etWeight.setText(sp.getInt("weight", 0) + "");
        etHeight.setText(sp.getInt("height", 0) + "");
        etAge.setText(sp.getInt("age", 0) + "");

        String gender = sp.getString("gender", "Male");

        for (int i = 0; i < rgGender.getChildCount(); i++) {
            View child = rgGender.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) child;
                if (radioButton.getText().toString().equals(gender)) {
                    radioButton.setChecked(true);
                    break;
                }
            }
        }
    }


    /**
     * Wird aufgerufen, wenn Nutzer auf den Bestätiogungs-Button klickt
     * Speichert alle eingegebenen Werte (Gewicht, Größe, Alter, Geschlecht) in den SharedPreferences
     * und startet anschließend die Hauptansicht(MainScreen
     * @param view das akutelle  View-Element (Button), das dieses Ereignis ausgelöst hat
     */
    public void startMainScreen(View view) {
        Intent intent = new Intent(SettingScreen.this, MainScreen.class);

        SharedPreferences.Editor editor = sp.edit();

        editor.putInt("weight", Integer.parseInt(etWeight.getText().toString()));
        editor.putInt("height", Integer.parseInt(etHeight.getText().toString()));
        editor.putInt("age", Integer.parseInt(etAge.getText().toString()));

        int selectedId = rgGender.getCheckedRadioButtonId();
        RadioButton selectedButton = (RadioButton) findViewById(selectedId);

        editor.putString("gender", selectedButton.getText().toString());

        editor.commit();

        startActivity(intent);
    }
}