package com.example.attendanceapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast; // Import Toast
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton; // Import FAB
import java.util.ArrayList;

public class StudentDashboardActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ClassAdapter adapter;
    ArrayList<ClassItem> classItems = new ArrayList<>();
    DatabaseHelper dbHelper;
    String studentName;
    int studentRoll;
    String classNameInput; // New variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. HIDE THE PLUS BUTTON (Crucial Step!)
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setVisibility(View.GONE);

        dbHelper = new DatabaseHelper(this);
        studentName = getIntent().getStringExtra("NAME");
        studentRoll = getIntent().getIntExtra("ROLL", 0);
        classNameInput = getIntent().getStringExtra("CLASS"); // Get Class Name

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassAdapter(this, classItems);
        recyclerView.setAdapter(adapter);

        // --- LOGOUT LOGIC ---
        // 1. Find the button (It acts like an ImageButton)
        View logout = findViewById(R.id.logout);

        // 2. Make it work
        logout.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            // Clear all history so they can't press "Back" to get in
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        adapter.setOnItemClickListener(position -> {
            long classId = classItems.get(position).getCid();
            String className = classItems.get(position).getClassName(); // Get Class Name
            String subjectName = classItems.get(position).getSubjectName(); // Get Subject Name

            long sid = dbHelper.getStudentIdInClass(classId, studentName, studentRoll);

            Intent intent = new Intent(this, StudentResultActivity.class);
            intent.putExtra("SID", String.valueOf(sid));

            // NEW: Send Name and Class info to the next page
            intent.putExtra("SNAME", studentName);
            intent.putExtra("CNAME", className + " (" + subjectName + ")");

            startActivity(intent);
        });

        loadStudentClasses();
    }

    private void loadStudentClasses() {
        classItems.clear();
        // Use the new SPECIFIC search (Name + Roll + Class)
        Cursor cursor = dbHelper.getStudentByNameRollClass(studentName, studentRoll, classNameInput);

        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No record found! Check Class Name.", Toast.LENGTH_LONG).show();
        }

        while (cursor.moveToNext()) {
            long cid = cursor.getLong(0);
            String className = cursor.getString(1);
            String subjectName = cursor.getString(2);
            classItems.add(new ClassItem(cid, className, subjectName));
        }
        adapter.notifyDataSetChanged();
    }
}
