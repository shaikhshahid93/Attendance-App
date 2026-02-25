package com.example.attendanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    TextInputEditText etUsername, etPassword, etClass; // Added etClass
    TextInputLayout tilUsername, tilPassword, tilClass; // Added tilClass
    Button btnLogin;
    RadioGroup radioGroup;
    DatabaseHelper dbHelper;
    boolean isTeacher = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        // NEW: Find the Class inputs
        etClass = findViewById(R.id.etClass);
        tilClass = findViewById(R.id.tilClass);

        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);
        radioGroup = findViewById(R.id.radioGroup);
        TextView tvRegister = findViewById(R.id.tvRegister);

        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioTeacher) {
                isTeacher = true;
                tilUsername.setHint("Username");
                tilPassword.setHint("Password");
                etPassword.setInputType(129);
                tilClass.setVisibility(View.GONE); // Hide Class for Teacher
            } else {
                isTeacher = false;
                tilUsername.setHint("Student Name");
                tilPassword.setHint("Roll Number");
                etPassword.setInputType(2);
                tilClass.setVisibility(View.VISIBLE); // Show Class for Student
            }
        });

        btnLogin.setOnClickListener(v -> {
            String input1 = etUsername.getText().toString(); // Name
            String input2 = etPassword.getText().toString(); // Password/Roll
            String input3 = etClass.getText().toString();    // Class Name (New)

            if (isTeacher) {
                if (dbHelper.checkTeacher(input1, input2)) {
                    startActivity(new Intent(this, DashboardActivity.class));
                } else {
                    Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            } else {
                // STUDENT LOGIN
                if (input1.isEmpty() || input2.isEmpty() || input3.isEmpty()) {
                    Toast.makeText(this, "Enter Name, Roll No, and Class", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int roll = Integer.parseInt(input2);
                    Intent intent = new Intent(this, StudentDashboardActivity.class);
                    intent.putExtra("NAME", input1);
                    intent.putExtra("ROLL", roll);
                    intent.putExtra("CLASS", input3); // Pass Class Name too
                    startActivity(intent);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Roll No must be a number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
