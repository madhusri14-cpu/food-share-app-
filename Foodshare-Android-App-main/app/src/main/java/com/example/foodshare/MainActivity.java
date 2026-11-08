
package com.example.foodshare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    Button btnDonate, btnFindFood;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        // Check login session
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(
                    MainActivity.this,
                    LoginActivity.class
            );
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        btnDonate = findViewById(R.id.btnDonate);
        btnFindFood = findViewById(R.id.btnFindFood);

        // Donate Food Button
        btnDonate.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    DonateActivity.class
            );
            startActivity(intent);
        });

        Button btnProfile = findViewById(R.id.btnProfile);

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    ProfileActivity.class
            );
            startActivity(intent);
        });


        // Find Food Button
        btnFindFood.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    FindFoodActivity.class
            );
            startActivity(intent);
        });
    }
}
