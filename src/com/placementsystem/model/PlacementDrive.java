package com.placementsystem.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a campus placement recruitment drive.
 * Demonstrates Method Overloading (scheduleInterview) and Business Logic Validation.
 */
public class PlacementDrive implements Serializable {
    private static final long serialVersionUID = 1L;

    private String driveId;
    private Company company;
    private String role;
    private double packageOffered;
    private double eligibleCGPA;
    private int maxBacklogsAllowed;
    private String driveDate;
    private String venue;
    private List<Application> applicants;

    public PlacementDrive() {
        this.applicants = new ArrayList<>();
    }

    public PlacementDrive(String driveId, Company company, String role, double packageOffered, double eligibleCGPA, int maxBacklogsAllowed, String driveDate) {
        this.driveId = driveId;
        this.company = company;
        this.role = role;
        this.packageOffered = packageOffered;
        this.eligibleCGPA = eligibleCGPA;
        this.maxBacklogsAllowed = maxBacklogsAllowed;
        this.driveDate = driveDate;
        this.venue = "Campus Auditorium / Online";
        this.applicants = new ArrayList<>();
    }

    // Getters and Setters
    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public double getPackageOffered() {
        return packageOffered;
    }

    public void setPackageOffered(double packageOffered) {
        this.packageOffered = packageOffered;
    }

    public double getEligibleCGPA() {
        return eligibleCGPA;
    }

    public void setEligibleCGPA(double eligibleCGPA) {
        this.eligibleCGPA = eligibleCGPA;
    }

    public int getMaxBacklogsAllowed() {
        return maxBacklogsAllowed;
    }

    public void setMaxBacklogsAllowed(int maxBacklogsAllowed) {
        this.maxBacklogsAllowed = maxBacklogsAllowed;
    }

    public String getDriveDate() {
        return driveDate;
    }

    public void setDriveDate(String driveDate) {
        this.driveDate = driveDate;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public List<Application> getApplicants() {
        if (applicants == null) {
            applicants = new ArrayList<>();
        }
        return applicants;
    }

    public void setApplicants(List<Application> applicants) {
        this.applicants = applicants;
    }

    /**
     * Checks if a student meets both CGPA and maximum backlog criteria.
     */
    public boolean checkEligibility(Student s) {
        if (s == null) return false;
        return s.getCgpa() >= this.eligibleCGPA && s.getBacklogs() <= this.maxBacklogsAllowed;
    }

    /**
     * Method Overloading: schedules an interview date.
     */
    public void scheduleInterview(String date) {
        this.driveDate = date;
    }

    /**
     * Method Overloading: schedules an interview date and specific venue.
     */
    public void scheduleInterview(String date, String venue) {
        this.driveDate = date;
        this.venue = venue;
    }

    @Override
    public String toString() {
        return (company != null ? company.getCompanyName() : "Unknown") + " - " + role + " (" + packageOffered + " LPA)";
    }
}