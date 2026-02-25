package com.example.attendanceapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Calendar;

public class StudentActivity extends AppCompatActivity {

    Toolbar toolbar;
    private String className;
    private String subjectName;
    private int position;
    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Student> studentItems = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private long cid;
    private FloatingActionButton fab;
    private String selectedDate;
    private Calendar calendar; // Declare Calendar here to reuse it

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        dbHelper = new DatabaseHelper(this);
        calendar = Calendar.getInstance(); // Initialize calendar

        Intent intent = getIntent();
        className = intent.getStringExtra("className");
        subjectName = intent.getStringExtra("subjectName");
        position = intent.getIntExtra("position", -1);
        cid = intent.getLongExtra("cid", -1);

        // Default to today's date
        // Note: Months are 0-indexed (0=Jan, 11=Dec), so we add 1 for display
        selectedDate = calendar.get(Calendar.DAY_OF_MONTH) + "-" + (calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.YEAR);

        setToolbar();

        recyclerView = findViewById(R.id.student_recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new StudentAdapter(this, studentItems);
        recyclerView.setAdapter(adapter);

        // Single Click to mark Present/Absent
        adapter.setOnItemClickListener(position -> changeStatus(position));

        // Long click to Edit/Delete
        adapter.setOnItemLongClickListener(position -> showUpdateDialog(position));

        loadStatus();

        fab = findViewById(R.id.fab_add_student);
        fab.setOnClickListener(v -> showAddStudentDialog());
    }

    private void loadStatus() {
        studentItems.clear();
        Cursor cursor = dbHelper.getClassStudent(cid);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(0);
            String name = cursor.getString(1);
            int roll = cursor.getInt(2);

            // 1. Get status for the specific date (Visual Color)
            String status = dbHelper.getStatus(id, selectedDate);

            // 2. Get Lifetime Stats (The Report Card)
            // Note: If these methods (countPresent/countTotal) don't exist in DBHelper yet,
            // the app will crash. Ensure they exist or remove these lines.
            // int presentDays = dbHelper.countPresent(id);
            // int totalDays = dbHelper.countTotal(id);

            // Simplified Roll Text for now
            String rollText = String.valueOf(roll);

            // 3. FIXED: Removed the 4th argument ("")
            Student student = new Student(String.valueOf(id), name, rollText);

            // 4. Set the status manually
            if (status != null) {
                student.setStatus(status);
            } else {
                student.setStatus("A");
            }

            studentItems.add(student);
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddStudentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_student, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText edtRoll = view.findViewById(R.id.edt_roll_no);
        EditText edtName = view.findViewById(R.id.edt_student_name);
        Button btnAdd = view.findViewById(R.id.btn_add);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnAdd.setOnClickListener(v -> {
            String name = edtName.getText().toString();
            String rollStr = edtRoll.getText().toString();

            if (name.isEmpty() || rollStr.isEmpty()) {
                Toast.makeText(this, "Please enter details", Toast.LENGTH_SHORT).show();
                return;
            }

            int roll = Integer.parseInt(rollStr);
            long sid = dbHelper.addStudent(cid, name, roll);

            if (sid != -1) {
                // FIXED: Removed the 4th argument ("")
                studentItems.add(new Student(String.valueOf(sid), name, String.valueOf(roll)));
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        TextView subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.save);

        title.setText(className);
        subtitle.setText(selectedDate); // Show date

        back.setOnClickListener(v -> onBackPressed());
        save.setOnClickListener(v -> saveAttendance());

        // When clicking the date, show the picker
        toolbar.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            month1 += 1; // Month is 0-indexed
            selectedDate = dayOfMonth + "-" + month1 + "-" + year1;

            // Update the toolbar text
            TextView subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
            subtitle.setText(selectedDate);

            // Reload database for this new date
            loadStatus();
        }, year, month, day);
        datePickerDialog.show();
    }

    private void changeStatus(int position) {
        String status = studentItems.get(position).getStatus();
        if (status.equals("P")) {
            status = "A";
        } else {
            status = "P";
        }
        studentItems.get(position).setStatus(status);
        adapter.notifyItemChanged(position);
    }

    private void saveAttendance() {
        for (Student student : studentItems) {
            String status = student.getStatus();
            long sid = Long.parseLong(student.getSid());
            // Save to history table
            dbHelper.addAttendance(sid, selectedDate, status);
        }
        Toast.makeText(this, "Attendance Saved", Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(int position) {
        Student student = studentItems.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Student");
        builder.setMessage("What do you want to do with " + student.getName() + "?");

        builder.setPositiveButton("Update", (dialog, which) -> showEditNameDialog(position));

        builder.setNegativeButton("Delete", (dialog, which) -> deleteStudent(position));

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void deleteStudent(int position) {
        long sid = Long.parseLong(studentItems.get(position).getSid());
        dbHelper.deleteStudent(sid);
        studentItems.remove(position);
        adapter.notifyItemRemoved(position);
    }

    private void showEditNameDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_student, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText edtName = view.findViewById(R.id.edt_student_name);
        EditText edtRoll = view.findViewById(R.id.edt_roll_no);
        Button btnAdd = view.findViewById(R.id.btn_add);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        // Pre-fill existing data
        edtName.setText(studentItems.get(position).getName());
        edtRoll.setText(studentItems.get(position).getRollNumber());
        edtRoll.setEnabled(false); // Let's disable roll no editing to keep it simple
        btnAdd.setText("Update");

        btnAdd.setOnClickListener(v -> {
            String name = edtName.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update Database
            long sid = Long.parseLong(studentItems.get(position).getSid());
            dbHelper.updateStudent(sid, name);

            // Update List
            studentItems.get(position).setName(name);
            adapter.notifyItemChanged(position);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }
}
