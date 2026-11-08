
package com.example.foodshare;

import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class FindFoodActivity extends AppCompatActivity {

    LinearLayout donationsList;
    TextView tvEmptyFood;
    DatabaseReference donationsRef;
    ValueEventListener donationsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_food);

        donationsList = findViewById(R.id.donationsList);
        tvEmptyFood = findViewById(R.id.tvEmptyFood);

        donationsRef = FirebaseDatabase.getInstance(
                "https://foodshare-98c9c-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("Donations");

        loadDonations();
    }

    private void loadDonations() {

        donationsListener = donationsRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        donationsList.removeAllViews();

                        int count = 0;

                        for (DataSnapshot item : snapshot.getChildren()) {

                            String status = item.child("status")
                                    .getValue(String.class);

                            if (!"Available".equals(status)) {
                                continue;
                            }

                            count++;

                            String foodName = getText(item, "foodName");
                            String quantity = getText(item, "quantity");
                            String foodType = getText(item, "foodType");
                            String location = getText(item, "pickupLocation");
                            String safeUntil = getText(item, "safeUntil");
                            String phone = getText(item, "contactNumber");

                            String details =
                                    "Food: " + foodName + "\n" +
                                            "Quantity: " + quantity + "\n" +
                                            "Type: " + foodType + "\n" +
                                            "Location: " + location + "\n" +
                                            "Safe Until: " + safeUntil + "\n" +
                                            "Contact: " + phone;

                            LinearLayout card = new LinearLayout(
                                    FindFoodActivity.this
                            );
                            card.setOrientation(LinearLayout.VERTICAL);
                            card.setPadding(32, 28, 32, 28);
                            card.setBackgroundColor(Color.WHITE);

                            TextView foodText = new TextView(
                                    FindFoodActivity.this
                            );
                            foodText.setText(details);
                            foodText.setTextSize(16);
                            foodText.setTextColor(Color.DKGRAY);

                            Button claimButton = new Button(
                                    FindFoodActivity.this
                            );
                            claimButton.setText("Claim Food");

                            String donationId = item.getKey();

                            claimButton.setOnClickListener(v ->
                                    claimFood(donationId)
                            );

                            card.addView(foodText);
                            card.addView(claimButton);

                            LinearLayout.LayoutParams params =
                                    new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                    );

                            params.bottomMargin = 20;
                            donationsList.addView(card, params);
                        }

                        tvEmptyFood.setVisibility(
                                count == 0 ? View.VISIBLE : View.GONE
                        );

                        if (count == 0) {
                            tvEmptyFood.setText("No food available right now.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvEmptyFood.setVisibility(View.VISIBLE);
                        tvEmptyFood.setText(
                                "Failed to load food: " + error.getMessage()
                        );
                    }
                }
        );
    }

    private String getText(DataSnapshot item, String key) {
        String value = item.child(key).getValue(String.class);
        return value == null ? "Not provided" : value;
    }

    private void claimFood(String donationId) {

        if (donationId == null) return;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this,
                    "Please login first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance()
                .getCurrentUser().getUid();

        DatabaseReference itemRef = donationsRef.child(donationId);

        // Transaction prevents two users claiming the same donation
        itemRef.runTransaction(
                new com.google.firebase.database.Transaction.Handler() {

                    @NonNull
                    @Override
                    public com.google.firebase.database.Transaction.Result
                    doTransaction(
                            @NonNull com.google.firebase.database.MutableData data
                    ) {

                        if (data.getValue() == null) {
                            return com.google.firebase.database.Transaction.abort();
                        }

                        Object status = data.child("status").getValue();

                        if (!"Available".equals(status)) {
                            return com.google.firebase.database.Transaction.abort();
                        }

                        data.child("status").setValue("Claimed");
                        data.child("claimedBy").setValue(userId);
                        data.child("claimedAt")
                                .setValue(System.currentTimeMillis());

                        return com.google.firebase.database.Transaction.success(data);
                    }

                    @Override
                    public void onComplete(
                            DatabaseError error,
                            boolean committed,
                            DataSnapshot snapshot
                    ) {

                        if (error != null) {
                            Toast.makeText(FindFoodActivity.this,
                                    "Claim failed: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();

                        } else if (committed) {
                            Toast.makeText(FindFoodActivity.this,
                                    "Food Claimed Successfully!",
                                    Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(FindFoodActivity.this,
                                    "Food is no longer available",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (donationsListener != null) {
            donationsRef.removeEventListener(donationsListener);
        }
    }
}
