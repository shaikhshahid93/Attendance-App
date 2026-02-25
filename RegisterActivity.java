package com.example.attendanceapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    // 1. Declare Variables
    EditText etUser, etPass;
    Button btnRegister;
    TextView tvLogin; // Variable for the "Login" text link
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 2. Initialize Helper and Views
        dbHelper = new DatabaseHelper(this);
        etUser = findViewById(R.id.etUsername);
        etPass = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin); // Find the text view

        // 3. LOGIC: Handle the "Back to Log in" click
        tvLogin.setOnClickListener(v -> {
            finish(); // Closes this screen, revealing Login screen behind it
        });

        // 4. LOGIC: Handle Registration
        btnRegister.setOnClickListener(v -> {
            String user = etUser.getText().toString().trim();
            String pass = etPass.getText().toString().trim();

            // Check if empty
            if(user.isEmpty() || pass.isEmpty()){
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if username is purely numbers
            if (user.matches("[0-9]+")) {
                Toast.makeText(this, "Username cannot be just numbers! Please use a name.", Toast.LENGTH_LONG).show();
                return;
            }

            // Check length
            if (user.length() < 3) {
                Toast.makeText(this, "Username must be at least 3 letters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to Database
            long id = dbHelper.addTeacher(user, pass);
            if(id > 0){
                Toast.makeText(this, "Registered! Now Login.", Toast.LENGTH_SHORT).show();
                finish(); // Close screen after success
            } else {
                Toast.makeText(this, "Registration Failed (User already exists)", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
