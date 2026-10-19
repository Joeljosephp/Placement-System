package com.placementsystem.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    public static boolean isValidTransition(String currentStatus, String targetStatus) {
        if (currentStatus == null || targetStatus == null) return false;
        if (currentStatus.equalsIgnoreCase(targetStatus)) return true;

        if ("Applied".equalsIgnoreCase(currentStatus)) {
            return "Shortlisted".equalsIgnoreCase(targetStatus) || "Rejected".equalsIgnoreCase(targetStatus);
        } else if ("Shortlisted".equalsIgnoreCase(currentStatus)) {
            return "Selected".equalsIgnoreCase(targetStatus) || "Rejected".equalsIgnoreCase(targetStatus);
        }
        // "Rejected" is a terminal state
        // "Selected" is a terminal state
        return false;
    }

    // Updates application status and notifies the applicant
    public void updateStatus(String newStatus) {
        if (!isValidTransition(this.status, newStatus)) {
            throw new IllegalStateException("Invalid status transition from '" + this.status + "' to '" + newStatus + "'");
        }
        this.status = newStatus;
        if (student != null) {
            String companyName = (drive != null && drive.getCompany() != null) 
                    ? drive.getCompany().getCompanyName() 
                    : "Company";
            String role = (drive != null) ? drive.getRole() : "Job Role";
            student.sendNotification("Application update: Your application for '" 
                    + role + "' at " + companyName + " is now " + newStatus);
        }
    }

    @Override
    public String toString() {
        return "App#" + applicationId + " - " + 
               (student != null ? student.getName() : "Student") + " -> " + 
               (drive != null ? drive.getRole() : "Drive") + " [" + status + "]";
    }
}