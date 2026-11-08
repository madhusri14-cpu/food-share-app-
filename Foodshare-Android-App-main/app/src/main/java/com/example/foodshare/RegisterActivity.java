
package com.example.foodshare;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword;
    EditText etPhone, etLocation, etCapacity;
    Spinner spinnerRole;
    Button btnRegister;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etLocation = findViewById(R.id.etLocation);
        etCapacity = findViewById(R.id.etCapacity);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnRegister = findViewById(R.id.btnRegister);

        String[] roles = {"Donor", "NGO"};

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        roles);

        spinnerRole.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String phone = etPhone.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String capacity = etCapacity.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(location)) {

            Toast.makeText(this,
                    "Please fill all required fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Minimum 6 characters");
            return;
        }

        if (role.equals("NGO") && capacity.isEmpty()) {
            etCapacity.setError("Enter NGO capacity");
            return;
        }

        btnRegister.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        String uid = auth.getCurrentUser().getUid();

                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);
                        user.put("phone", phone);
                        user.put("location", location);
                        user.put("role", role);
                        user.put("capacity",
                                role.equals("NGO") ? capacity : "");

                        FirebaseDatabase.getInstance(
                                        "https://foodshare-98c9c-default-rtdb.asia-southeast1.firebasedatabase.app"
                                ).getReference("Users")
                                .child(uid)
                                .setValue(user)
                                .addOnCompleteListener(saveTask -> {

                                    btnRegister.setEnabled(true);

                                    if (saveTask.isSuccessful()) {
                                        Toast.makeText(this,
                                                "Registration Successful!",
                                                Toast.LENGTH_LONG).show();
                                        finish();
                                    } else {
                                        Toast.makeText(this,
                                                "Database Error: " +
                                                        saveTask.getException().getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });

                    } else {
                        btnRegister.setEnabled(true);

                        Toast.makeText(this,
                                "Registration Failed: " +
                                        task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
