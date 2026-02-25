package com.example.attendanceapp;

import java.io.Serializable;

public class Student implements Serializable {

    // 1. DATA FIELDS
    private String sid;          // Unique Database ID (e.g., "S101")
    private String name;         // Student Name
    private String rollNumber;   // Roll No (Display purpose)
    private String status;       // "P" (Present), "A" (Absent)
    private boolean isExpanded;  // UI Feature: To show/hide extra details (like Edit/Delete buttons)

    // 2. CONSTRUCTOR
    public Student(String sid, String name, String rollNumber) {
        this.sid = sid;
        this.name = name;
        this.rollNumber = rollNumber;
        this.status = "A";      // Default to Absent until marked otherwise
        this.isExpanded = false;
    }

    // 3. GETTERS AND SETTERS

    public String getSid() { return sid; }
    public void setSid(String sid) { this.sid = sid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Helper for UI Checkbox
    public boolean isPresent() {
        return "P".equals(this.status);
    }

    // UI Helper: Used to expand the row
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}
