package com.placementsystem.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a student candidate in the placement system.
 * Demonstrates Inheritance (extends Person), Interface implementation (implements Notifiable),
 * Encapsulation, and Validation.
 */
public class Student extends Person implements Notifiable {
    private static final long serialVersionUID = 1L;

    private String usn;
    private String department;
    private double cgpa;
    private int backlogs;
    private String password;
    private List<String> notifications;

    public Student() {
        super();
        this.notifications = new ArrayList<>();
    }

    public Student(String usn, String name, String email, String phone, String department, double cgpa, int backlogs, String password) {
        super(name, email, phone);
        this.usn = usn;
        this.department = department;
        setCgpa(cgpa); // Enforce validation
        setBacklogs(backlogs);
        this.password = password;
        this.notifications = new ArrayList<>();
    }

    // Getters and Setters with Validation
    public String getUsn() {
        return usn;
    }

    public void setUsn(String usn) {
        this.usn = usn;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getCgpa() {
        return cgpa;
    }

    /**
     * Validates that CGPA is strictly between 0.0 and 10.0.
     */
    public void setCgpa(double cgpa) {
        if (cgpa < 0.0 || cgpa > 10.0) {
            throw new IllegalArgumentException("Invalid CGPA: " + cgpa + ". CGPA must be between 0.0 and 10.0");
        }
        this.cgpa = cgpa;
    }

    public int getBacklogs() {
        return backlogs;
    }

    public void setBacklogs(int backlogs) {
        if (backlogs < 0) {
            throw new IllegalArgumentException("Backlogs cannot be negative");
        }
        this.backlogs = backlogs;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getNotifications() {
        if (notifications == null) {
            notifications = new ArrayList<>();
        }
        return notifications;
    }

    @Override
    public void displayDetails() {
        System.out.println("----- Student Details -----");
        System.out.println("USN: " + usn);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phone);
        System.out.println("Department: " + department);
        System.out.println("CGPA: " + cgpa);
        System.out.println("Backlogs: " + backlogs);
    }

    @Override
    public void sendNotification(String message) {
        getNotifications().add(message);
        System.out.println("[Notification -> Student " + name + " (" + usn + ")]: " + message);
    }

    @Override
    public String toString() {
        return name + " (" + usn + ") - " + department;
    }
}
