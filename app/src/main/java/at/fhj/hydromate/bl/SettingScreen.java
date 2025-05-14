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

public class SettingScreen extends AppCompatActivity {

    private SharedPreferences sp;

    private EditText etWeight;
    private EditText etHeight;
    private EditText etAge;
    private RadioGroup rgGender;


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