package com.example.attendanceapp;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;

public class DashboardActivity extends AppCompatActivity {

    FloatingActionButton fab;
    RecyclerView recyclerView;
    ClassAdapter classAdapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<ClassItem> classItems = new ArrayList<>();
    EditText edtClassName;
    EditText edtSubjectName;

    // NEW: Database Helper
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Database
        dbHelper = new DatabaseHelper(this);

        fab = findViewById(R.id.fab_add);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        classAdapter = new ClassAdapter(this, classItems);
        recyclerView.setAdapter(classAdapter);

        // 1. Find the logout button
        ImageButton logout = findViewById(R.id.logout);

        // 2. Set the click listener
        logout.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            // THIS IS IMPORTANT: Clears the history so "Back" button doesn't work
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        classAdapter.setOnItemClickListener(position -> gotoItemActivity(position));

        // NEW: Long Click to Delete/Edit Class
        classAdapter.setOnItemLongClickListener(position -> showUpdateDeleteDialog(position));

        fab.setOnClickListener(v -> showAddDialog());

        // NEW: Load Data immediately when app opens
        loadData();
    }

    // NEW: Method to read from Database and fill the list
    private void loadData() {
        classItems.clear();
        Cursor cursor = dbHelper.getClassTable();

        while (cursor.moveToNext()) {
            // FIXED: Get the ID (Index 0)
            long id = cursor.getLong(0);
            String className = cursor.getString(1);
            String subjectName = cursor.getString(2);

            // FIXED: Pass the ID to the ClassItem
            classItems.add(new ClassItem(id, className, subjectName));
        }
        classAdapter.notifyDataSetChanged();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_class, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        edtClassName = view.findViewById(R.id.edt_class_name);
        edtSubjectName = view.findViewById(R.id.edt_subject_name);

        Button btnAdd = view.findViewById(R.id.btn_add);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnAdd.setOnClickListener(v -> {
            String className = edtClassName.getText().toString();
            String subjectName = edtSubjectName.getText().toString();

            // NEW: Save to Database first
            long status = dbHelper.addClass(className, subjectName);

            if (status != -1) { // If save was successful
                classItems.add(new ClassItem(status, className, subjectName));
                classAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void gotoItemActivity(int position) {
        Intent intent = new Intent(this, StudentActivity.class);

        // FIXED: Send the actual Database ID
        intent.putExtra("cid", classItems.get(position).getCid());

        intent.putExtra("className", classItems.get(position).getClassName());
        intent.putExtra("subjectName", classItems.get(position).getSubjectName());
        intent.putExtra("position", position);
        startActivity(intent);
    }
    private void showUpdateDeleteDialog(int position) {
        ClassItem classItem = classItems.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Options");
        builder.setMessage("What do you want to do with " + classItem.getClassName() + "?");

        builder.setPositiveButton("Update", (dialog, which) -> showUpdateDialog(position));

        builder.setNegativeButton("Delete", (dialog, which) -> deleteClass(position));

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void deleteClass(int position) {
        dbHelper.deleteClass(classItems.get(position).getCid());
        classItems.remove(position);
        classAdapter.notifyItemRemoved(position);
    }

    private void showUpdateDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_class, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText edtClass = view.findViewById(R.id.edt_class_name);
        EditText edtSubject = view.findViewById(R.id.edt_subject_name);
        Button btnAdd = view.findViewById(R.id.btn_add);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        // Pre-fill data
        edtClass.setText(classItems.get(position).getClassName());
        edtSubject.setText(classItems.get(position).getSubjectName());
        btnAdd.setText("Update");

        btnAdd.setOnClickListener(v -> {
            String className = edtClass.getText().toString();
            String subjectName = edtSubject.getText().toString();

            long cid = classItems.get(position).getCid();
            dbHelper.updateClass(cid, className, subjectName);

            // Update List
            classItems.get(position).setClassName(className);
            classItems.get(position).setSubjectName(subjectName);
            classAdapter.notifyItemChanged(position);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }
}
