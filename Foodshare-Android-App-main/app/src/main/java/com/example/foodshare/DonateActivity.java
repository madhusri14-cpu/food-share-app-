
package com.example.foodshare;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class DonateActivity extends AppCompatActivity {

    EditText etFoodName, etQuantity, etPickupLocation;
    EditText etPreparedTime, etSafeUntil, etDonorPhone;
    Spinner spinnerFoodType;
    Button btnSubmitDonation;

    DatabaseReference databaseReference;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        auth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance(
                "https://foodshare-98c9c-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("Donations");

        etFoodName = findViewById(R.id.etFoodName);
        etQuantity = findViewById(R.id.etQuantity);
        etPickupLocation = findViewById(R.id.etPickupLocation);
        etPreparedTime = findViewById(R.id.etPreparedTime);
        etSafeUntil = findViewById(R.id.etSafeUntil);
        etDonorPhone = findViewById(R.id.etDonorPhone);
        spinnerFoodType = findViewById(R.id.spinnerFoodType);
        btnSubmitDonation = findViewById(R.id.btnSubmitDonation);

        String[] foodTypes = {"Veg", "Non-Veg"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                foodTypes
        );

        spinnerFoodType.setAdapter(adapter);

        btnSubmitDonation.setOnClickListener(v -> saveDonation());
    }

    private void saveDonation() {

        String foodName = etFoodName.getText().toString().trim();
        String quantity = etQuantity.getText().toString().trim();
        String foodType = spinnerFoodType.getSelectedItem().toString();
        String location = etPickupLocation.getText().toString().trim();
        String preparedTime = etPreparedTime.getText().toString().trim();
        String safeUntil = etSafeUntil.getText().toString().trim();
        String phone = etDonorPhone.getText().toString().trim();

        if (foodName.isEmpty() || quantity.isEmpty()
                || location.isEmpty() || preparedTime.isEmpty()
                || safeUntil.isEmpty() || phone.isEmpty()) {

            Toast.makeText(this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this,
                    "Please login first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String donationId = databaseReference.push().getKey();

        if (donationId == null) {
            Toast.makeText(this,
                    "Could not create donation ID",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> donation = new HashMap<>();

        donation.put("donationId", donationId);
        donation.put("donorId", auth.getCurrentUser().getUid());
        donation.put("foodName", foodName);
        donation.put("quantity", quantity);
        donation.put("foodType", foodType);
        donation.put("pickupLocation", location);
        donation.put("preparedTime", preparedTime);
        donation.put("safeUntil", safeUntil);
        donation.put("contactNumber", phone);
        donation.put("status", "Available");
        donation.put("createdAt", System.currentTimeMillis());

        btnSubmitDonation.setEnabled(false);

        databaseReference.child(donationId)
                .setValue(donation)
                .addOnCompleteListener(task -> {

                    btnSubmitDonation.setEnabled(true);

                    if (task.isSuccessful()) {

                        Toast.makeText(this,
                                "Food Donation Saved Successfully!",
                                Toast.LENGTH_LONG).show();

                        finish();

                    } else {

                        Toast.makeText(this,
                                "Failed: " + (task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
