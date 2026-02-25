package com.example.attendanceapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class StudentResultActivity extends AppCompatActivity {

    TextView tvName, tvPercentage, tvStats, tvClassName;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_result);

        dbHelper = new DatabaseHelper(this);
        tvName = findViewById(R.id.tvStudentName);
        tvClassName = findViewById(R.id.tvClassName); // New Text View
        tvPercentage = findViewById(R.id.tvPercentage);
        tvStats = findViewById(R.id.tvStats);
        Button btnBack = findViewById(R.id.btnBack);

        // 1. Get Data passed from Dashboard
        String sidStr = getIntent().getStringExtra("SID");
        String sName = getIntent().getStringExtra("SNAME");
        String cName = getIntent().getStringExtra("CNAME");

        // 2. Set the static texts
        tvName.setText(sName); // Shows "Shahid" instead of "Student ID: 1"
        tvClassName.setText(cName); // Shows "12th (Maths)"

        try {
            long sid = Long.parseLong(sidStr);
            loadStudentData(sid);
        } catch (Exception e) {
            tvName.setText("Error loading data");
        }

        // 3. THE FIX: Just finish() to go back to Dashboard
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadStudentData(long sid) {
        int present = dbHelper.countPresent(sid);
        int total = dbHelper.countTotal(sid);
        int percentage = 0;
        if (total > 0) {
            percentage = (present * 100) / total;
        }

        tvPercentage.setText(percentage + "%");
        tvStats.setText("Present: " + present + " / " + total + " Days");
    }
}
