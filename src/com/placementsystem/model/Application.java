package com.placementsystem.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an application made by a student for a placement drive.
 * Demonstrates Object Associations and Notification triggering.
 */
public class Application implements Serializable {
    private static final long serialVersionUID = 1L;

    private String applicationId;
    private Student student;
    private PlacementDrive drive;
    private String status; // "Applied", "Shortlisted", "Selected", "Rejected"
    private String appliedDate;

    public Application() {
        this.status = "Applied";
        this.appliedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public Application(String applicationId, Student student, PlacementDrive drive) {
        this.applicationId = applicationId;
        this.student = student;
        this.drive = drive;
        this.status = "Applied";
        this.appliedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public Application(String applicationId, Student student, PlacementDrive drive, String status, String appliedDate) {
        this.applicationId = applicationId;
        this.student = student;
        this.drive = drive;
        this.status = status;
        this.appliedDate = appliedDate;
    }

    // Getters and Setters
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public PlacementDrive getDrive() {
        return drive;
    }

    public void setDrive(PlacementDrive drive) {
        this.drive = drive;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }

    /**
     * Updates the application status and dispatches a notification to the student.
     */
    public void updateStatus(String newStatus) {
        this.status = newStatus;
        if (student != null) {
            String companyName = (drive != null && drive.getCompany() != null) 
                    ? drive.getCompany().getCompanyName() 
                    : "Company";
            String role = (drive != null) ? drive.getRole() : "Job Role";
            student.sendNotification("Application Status Update: Your application for '" 
                    + role + "' at " + companyName + " has been changed to: " + newStatus);
        }
    }

    @Override
    public String toString() {
        return "App#" + applicationId + " - " + 
               (student != null ? student.getName() : "Student") + " -> " + 
               (drive != null ? drive.getRole() : "Drive") + " [" + status + "]";
    }
}