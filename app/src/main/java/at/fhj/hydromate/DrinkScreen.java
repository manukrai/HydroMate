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

public class DrinkScreen extends AppCompatActivity {

    private double drinkProcent = 0.00;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                break;
            case "coffee":imageView.setImageResource(R.drawable.coffee_cup256);
                drinkProcent = 0.95;
                break;
            case "juice":imageView.setImageResource(R.drawable.juice256);
                drinkProcent = 0.90;
                break;
            case "tea":imageView.setImageResource(R.drawable.tea256);
                drinkProcent = 1.00;
                break;
            case "milk":imageView.setImageResource(R.drawable.milk256);
                drinkProcent = 1.15;
                break;
            case "beer":imageView.setImageResource(R.drawable.beer256);
                drinkProcent = 0.80;;
                break;
            case "strongAlcohol":imageView.setImageResource(R.drawable.liquor256);
                drinkProcent = 0.50;
                break;
        }

    }

    public void startMainScreen(View view) {
        Intent intent = new Intent(DrinkScreen.this, MainScreen.class);

        intent.putExtra("drinkProcent",drinkProcent);

        if(view.getId() == R.id.btSave)
        {
            EditText etVolume = findViewById(R.id.etVolume);
            if(etVolume != null)
            {
                intent.putExtra("volume",etVolume.getText());
            }
            else
            {
                intent.putExtra("volume",0);
            }

        }
        if(view.getId() == R.id.btAdd150)
        {
            intent.putExtra("volume",150);
        }
        if(view.getId() == R.id.btAdd250)
        {
            intent.putExtra("volume",250);
        }
        if(view.getId() == R.id.btadd500)
        {
            intent.putExtra("volume",500);
        }

        startActivity(intent);
    }
}