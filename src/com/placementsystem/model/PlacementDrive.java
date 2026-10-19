package com.placementsystem.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
        this.role = role != null ? role.trim() : "";
        setPackageOffered(packageOffered);
        setEligibleCGPA(eligibleCGPA);
        setMaxBacklogsAllowed(maxBacklogsAllowed);
        setDriveDate(driveDate);
        this.venue = "Campus Auditorium / Online";
        this.applicants = new ArrayList<>();
    }

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
        if (packageOffered < 0.5 || packageOffered > 150.0) {
            throw new IllegalArgumentException("Package must be between 0.5 and 150.0 LPA");
        }
        this.packageOffered = packageOffered;
    }

    public double getEligibleCGPA() {
        return eligibleCGPA;
    }

    public void setEligibleCGPA(double eligibleCGPA) {
        if (eligibleCGPA < 0.0 || eligibleCGPA > 10.0) {
            throw new IllegalArgumentException("Eligible CGPA must be between 0.0 and 10.0");
        }
        this.eligibleCGPA = eligibleCGPA;
    }

    public int getMaxBacklogsAllowed() {
        return maxBacklogsAllowed;
    }

    public void setMaxBacklogsAllowed(int maxBacklogsAllowed) {
        if (maxBacklogsAllowed < 0 || maxBacklogsAllowed > 50) {
            throw new IllegalArgumentException("Max backlogs must be between 0 and 50");
        }
        this.maxBacklogsAllowed = maxBacklogsAllowed;
    }

    public String getDriveDate() {
        return driveDate;
    }

    public void setDriveDate(String driveDate) {
        if (driveDate != null && !driveDate.trim().isEmpty()) {
            try {
                java.time.LocalDate.parse(driveDate.trim(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format. Expected YYYY-MM-DD");
            }
        }
        this.driveDate = driveDate != null ? driveDate.trim() : "";
    }

    public boolean isDeadlinePassed() {
        if (driveDate == null || driveDate.trim().isEmpty()) return false;
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(driveDate.trim(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            return date.isBefore(java.time.LocalDate.now());
        } catch (Exception e) {
            return false;
        }
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

    // Checks if a student meets both CGPA and maximum backlog criteria
    public boolean checkEligibility(Student s) {
        if (s == null) return false;
        return s.getCgpa() >= this.eligibleCGPA && s.getBacklogs() <= this.maxBacklogsAllowed;
    }

    public void scheduleInterview(String date) {
        this.driveDate = date;
    }

    public void scheduleInterview(String date, String venue) {
        this.driveDate = date;
        this.venue = venue;
    }

    @Override
    public String toString() {
        return (company != null ? company.getCompanyName() : "Unknown") + " - " + role + " (" + packageOffered + " LPA)";
    }
}