
package com.example.foodshare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    TextView tvName, tvEmail, tvRole, tvPhone, tvLocation;
    Button btnLogout;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();

        tvName = findViewById(R.id.tvProfileName);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvRole = findViewById(R.id.tvProfileRole);
        tvPhone = findViewById(R.id.tvProfilePhone);
        tvLocation = findViewById(R.id.tvProfileLocation);
        btnLogout = findViewById(R.id.btnLogout);

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            openLogin();
            return;
        }

        tvEmail.setText("Email: " + user.getEmail());

        DatabaseReference userRef = FirebaseDatabase.getInstance(
                "https://foodshare-98c9c-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("Users").child(user.getUid());

        userRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        tvName.setText("Name: " + value(snapshot, "name"));
                        tvRole.setText("Role: " + value(snapshot, "role"));
                        tvPhone.setText("Phone: " + value(snapshot, "phone"));
                        tvLocation.setText("Location: " + value(snapshot, "location"));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(
                                ProfileActivity.this,
                                "Failed to load profile: " + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            openLogin();
        });
    }

    private String value(DataSnapshot snapshot, String key) {
        String result = snapshot.child(key).getValue(String.class);
        return result == null ? "Not provided" : result;
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
