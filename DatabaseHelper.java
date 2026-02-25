package com.example.attendanceapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    // 1. Database Info
    private static final int DATABASE_VERSION = 2; // Incremented version to force update
    private static final String DATABASE_NAME = "Attendance.db";

    private static final String S_STATUS = "STATUS"; // New Column
    // 2. Table Names
    private static final String TABLE_CLASS = "CLASS_TABLE";
    private static final String TABLE_STUDENT = "STUDENT_TABLE";

    // 3. Column Names - Class Table
    private static final String C_ID = "_CID";
    private static final String C_NAME = "CLASS_NAME";
    private static final String C_SUBJECT = "SUBJECT_NAME";

    // 4. Column Names - Student Table
    private static final String S_ID = "_SID";
    private static final String S_NAME = "STUDENT_NAME";
    private static final String S_ROLL = "ROLL_NO";
    private static final String S_CLASS_ID = "CLASS_ID";

    private static final String TABLE_ATTENDANCE = "ATTENDANCE_TABLE";
    private static final String A_ID = "_AID";
    private static final String A_STUDENT_ID = "STUDENT_ID";
    private static final String A_DATE = "DATE";
    private static final String A_STATUS = "STATUS";

    // 5. Create Table Queries
    private static final String CREATE_TABLE_CLASS =
            "CREATE TABLE " + TABLE_CLASS + "(" +
                    C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    C_NAME + " TEXT NOT NULL," +
                    C_SUBJECT + " TEXT NOT NULL);";

    // USER TABLE (For Teachers)
    private static final String TABLE_USERS = "USER_TABLE";
    private static final String U_ID = "_UID";
    private static final String U_USERNAME = "USERNAME";
    private static final String U_PASSWORD = "PASSWORD";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + "(" +
                    U_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    U_USERNAME + " TEXT NOT NULL UNIQUE," +
                    U_PASSWORD + " TEXT NOT NULL);";

    private static final String CREATE_TABLE_STUDENT =
            "CREATE TABLE " + TABLE_STUDENT + "(" +
                    S_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    S_NAME + " TEXT NOT NULL," +
                    S_ROLL + " INTEGER," +
                    S_CLASS_ID + " INTEGER NOT NULL," +
                    S_STATUS + " TEXT," + // We added this line
                    "FOREIGN KEY (" + S_CLASS_ID + ") REFERENCES " + TABLE_CLASS + "(" + C_ID + ")" +
                    ");";

    private static final String CREATE_TABLE_ATTENDANCE =
            "CREATE TABLE " + TABLE_ATTENDANCE + "(" +
                    A_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    A_STUDENT_ID + " INTEGER NOT NULL," +
                    A_DATE + " TEXT NOT NULL," +
                    A_STATUS + " TEXT NOT NULL," +
                    "FOREIGN KEY (" + A_STUDENT_ID + ") REFERENCES " + TABLE_STUDENT + "(" + S_ID + ")" +
                    ");";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CLASS);
        db.execSQL(CREATE_TABLE_STUDENT);
        db.execSQL(CREATE_TABLE_ATTENDANCE);
        db.execSQL(CREATE_TABLE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTENDANCE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- CRUD OPERATIONS ---

    public long addClass(String className, String subjectName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(C_NAME, className);
        values.put(C_SUBJECT, subjectName);
        return db.insert(TABLE_CLASS, null, values);
    }

    public Cursor getClassTable() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CLASS, null);
    }

    public long addStudent(long classId, String name, int roll) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(S_CLASS_ID, classId);
        values.put(S_NAME, name);
        values.put(S_ROLL, roll);
        values.put(S_STATUS, "A"); // Default to Absent
        return db.insert(TABLE_STUDENT, null, values);
    }

    public Cursor getClassStudent(long classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_STUDENT, null, S_CLASS_ID + "=?", new String[]{String.valueOf(classId)}, null, null, S_ROLL);
    }

    // Method to update status (P or A)
    public long updateStatus(long sid, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(S_STATUS, status);

        return db.update(TABLE_STUDENT, values, S_ID + "=?", new String[]{String.valueOf(sid)});
    }

    public long addAttendance(long studentId, String date, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(A_STUDENT_ID, studentId);
        values.put(A_DATE, date);
        values.put(A_STATUS, status);

        // This command is magic: If a record exists for this student + date, UPDATE it.
        // If not, INSERT a new one.
        long id = db.update(TABLE_ATTENDANCE, values, A_STUDENT_ID + "=? AND " + A_DATE + "=?", new String[]{String.valueOf(studentId), date});
        if (id == 0) {
            id = db.insert(TABLE_ATTENDANCE, null, values);
        }
        return id;
    }

    public String getStatus(long studentId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String status = null;
        Cursor cursor = db.query(TABLE_ATTENDANCE, null, A_STUDENT_ID + "=? AND " + A_DATE + "=?", new String[]{String.valueOf(studentId), date}, null, null, null);
        if (cursor.moveToFirst()) {
            status = cursor.getString(cursor.getColumnIndexOrThrow(A_STATUS));
        }
        cursor.close();
        return status;
    }
    // 1. UPDATE STUDENT NAME
    public void updateStudent(long sid, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(S_NAME, name);
        db.update(TABLE_STUDENT, values, S_ID + "=?", new String[]{String.valueOf(sid)});
    }

    // 2. DELETE STUDENT
    public void deleteStudent(long sid) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete the student
        db.delete(TABLE_STUDENT, S_ID + "=?", new String[]{String.valueOf(sid)});
        // ALSO delete their attendance history (to keep database clean)
        db.delete(TABLE_ATTENDANCE, A_STUDENT_ID + "=?", new String[]{String.valueOf(sid)});
    }
    // DELETE CLASS (And all students inside it)
    public void deleteClass(long cid) {
        SQLiteDatabase db = this.getWritableDatabase();
        // 1. Delete Students assigned to this class
        db.delete(TABLE_STUDENT, S_CLASS_ID + "=?", new String[]{String.valueOf(cid)});
        // 2. Delete the Class itself
        db.delete(TABLE_CLASS, C_ID + "=?", new String[]{String.valueOf(cid)});
    }

    // UPDATE CLASS NAME/SUBJECT
    public void updateClass(long cid, String className, String subjectName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(C_NAME, className);
        values.put(C_SUBJECT, subjectName);
        db.update(TABLE_CLASS, values, C_ID + "=?", new String[]{String.valueOf(cid)});
    }
    // 1. COUNT PRESENT DAYS
    public int countPresent(long sid) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Count rows where Student ID matches AND Status is 'P'
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ATTENDANCE + " WHERE " + A_STUDENT_ID + "=? AND " + A_STATUS + "='P'", new String[]{String.valueOf(sid)});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // 2. COUNT TOTAL DAYS ATTENDANCE TAKEN
    public int countTotal(long sid) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Count rows where Student ID matches
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ATTENDANCE + " WHERE " + A_STUDENT_ID + "=?", new String[]{String.valueOf(sid)});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }
    // --- TEACHER METHODS ---
    public long addTeacher(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(U_USERNAME, username);
        values.put(U_PASSWORD, password);
        return db.insert(TABLE_USERS, null, values);
    }

    public boolean checkTeacher(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{U_ID},
                U_USERNAME + "=? AND " + U_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    // --- STUDENT METHOD (The "Best" Logic) ---
    // Finds ALL classes where this student exists
    public Cursor getStudentClasses(String name, int roll) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Complex Query: Join Class Table and Student Table
        // "Give me Class Name and Subject Name WHERE Student Name is X and Roll is Y"
        String query = "SELECT C." + C_ID + ", C." + C_NAME + ", C." + C_SUBJECT +
                " FROM " + TABLE_CLASS + " C " +
                " JOIN " + TABLE_STUDENT + " S ON C." + C_ID + " = S." + S_CLASS_ID +
                " WHERE S." + S_NAME + "=? AND S." + S_ROLL + "=?";

        return db.rawQuery(query, new String[]{name, String.valueOf(roll)});
    }

    // Get Student ID inside a specific class (helper for the result page)
    public long getStudentIdInClass(long classId, String name, int roll) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STUDENT, new String[]{S_ID},
                S_CLASS_ID + "=? AND " + S_NAME + "=? AND " + S_ROLL + "=?",
                new String[]{String.valueOf(classId), name, String.valueOf(roll)}, null, null, null);

        if (cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            cursor.close();
            return id;
        }
        return -1;
    }
    // NEW: Find a student in a SPECIFIC class
    public Cursor getStudentByNameRollClass(String name, int roll, String className) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Join Class and Student tables to check Class Name too
        String query = "SELECT C." + C_ID + ", C." + C_NAME + ", C." + C_SUBJECT +
                " FROM " + TABLE_CLASS + " C " +
                " JOIN " + TABLE_STUDENT + " S ON C." + C_ID + " = S." + S_CLASS_ID +
                " WHERE S." + S_NAME + "=? AND S." + S_ROLL + "=? AND C." + C_NAME + "=?";

        return db.rawQuery(query, new String[]{name, String.valueOf(roll), className});
    }
}
