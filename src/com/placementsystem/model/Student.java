package com.placementsystem.model;

import com.placementsystem.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;

public class Student extends Person implements Notifiable {
    private static final long serialVersionUID = 1L;

    private String usn;
    private String department;
    private double cgpa;
    private int backlogs;
    private String passwordHash;
    private String salt;
    private List<String> notifications;

    public Student() {
        super();
        this.notifications = new ArrayList<>();
    }

    public Student(String usn, String name, String email, String phone, String department, double cgpa, int backlogs, String password) {
        super(name, email, phone);
        this.usn = usn;
        this.department = department;
        setCgpa(cgpa);
        setBacklogs(backlogs);
        setPassword(password);
        this.notifications = new ArrayList<>();
    }

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

    // CGPA must be within KTU 10-point scale
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
        return passwordHash;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setPassword(String password) {
        if (password != null && !password.isEmpty()) {
            this.salt = PasswordUtil.generateSalt();
            this.passwordHash = PasswordUtil.hashPassword(password, this.salt);
        }
    }

    public boolean checkPassword(String plainPassword) {
        if (plainPassword == null) {
            return false;
        }
        if (salt != null && passwordHash != null) {
            return PasswordUtil.verifyPassword(plainPassword, salt, passwordHash);
        }
        return false;
    }

    public List<String> getNotifications() {
        if (notifications == null) {
            notifications = new ArrayList<>();
        }
        return notifications;
    }

    @Override
    public void displayDetails() {
        System.out.println("Student: " + name + " (" + usn + "), Department: " + department + ", CGPA: " + cgpa);
    }

    @Override
    public void sendNotification(String message) {
        getNotifications().add(message);
    }

    @Override
    public String toString() {
        return name + " (" + usn + ") - " + department;
    }
}
