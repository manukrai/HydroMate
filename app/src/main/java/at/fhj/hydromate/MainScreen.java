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

        textMili.setText(getIntent().getIntExtra("volume",0) + " ml");

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




}