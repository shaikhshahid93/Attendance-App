package com.example.attendanceapp;

public class ClassItem {
    private long cid; // NEW: Database ID
    private String className;
    private String subjectName;

    // Update Constructor to accept ID
    public ClassItem(long cid, String className, String subjectName) {
        this.cid = cid;
        this.className = className;
        this.subjectName = subjectName;
    }

    public long getCid() { return cid; } // NEW Getter
    public void setCid(long cid) { this.cid = cid; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
}
