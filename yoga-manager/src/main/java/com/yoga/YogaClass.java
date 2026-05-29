package com.yoga;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class YogaClass implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title;
    private LocalDateTime dateTime;
    private String location;
    private int capacity;
    private List<String> enrolledStudents;
    private String notes;

    public YogaClass(String title, LocalDateTime dateTime, String location, int capacity, String notes) {
        this.title = title;
        this.dateTime = dateTime;
        this.location = location;
        this.capacity = capacity;
        this.notes = notes;
        this.enrolledStudents = new ArrayList<>();
    }

    public String getTitle() { return title; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getLocation() { return location; }
    public int getCapacity() { return capacity; }
    public List<String> getEnrolledStudents() { return enrolledStudents; }
    public String getNotes() { return notes; }

    public boolean enroll(String studentName) {
        if (enrolledStudents.size() >= capacity) return false;
        if (enrolledStudents.contains(studentName)) return false;
        enrolledStudents.add(studentName);
        return true;
    }

    public boolean unenroll(String studentName) {
        return enrolledStudents.remove(studentName);
    }

    public boolean isFuture() {
        return dateTime.isAfter(LocalDateTime.now());
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, MMM d yyyy 'at' HH:mm");
        return String.format("%s | %s | %s | %d/%d enrolled%s",
                title, dateTime.format(fmt), location,
                enrolledStudents.size(), capacity,
                notes.isEmpty() ? "" : " | " + notes);
    }
}
