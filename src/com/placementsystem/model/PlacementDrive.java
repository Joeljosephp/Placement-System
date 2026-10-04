package src.com.placementsystem.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlacementDrive implements Serializable {

    private String driveId;
    private Company company;
    private String role;
    private double packageOffered;
    private double eligibleCGPA;
    private String driveDate;
    private String venue;
    private List<Application> applicants;

    public PlacementDrive(String driveId, Company company, String role, double packageOffered, double eligibleCGPA, String driveDate) {
        this.driveId = driveId;
        this.company = company;
        this.role = role;
        this.packageOffered = packageOffered;
        this.eligibleCGPA = eligibleCGPA;
        this.driveDate = driveDate;
        this.applicants = new ArrayList<>();
    }

    public String getDriveId() { return driveId; }
    public Company getCompany() { return company; }
    public String getRole() { return role; }
    public double getPackageOffered() { return packageOffered; }
    public double getEligibleCGPA() { return eligibleCGPA; }
    public String getDriveDate() { return driveDate; }
    public String getVenue() { return venue; }
    public List<Application> getApplicants() { return applicants; }

    public void setDriveId(String driveId) { this.driveId = driveId; }
    public void setCompany(Company company) { this.company = company; }
    public void setRole(String role) { this.role = role; }
    public void setPackageOffered(double packageOffered) { this.packageOffered = packageOffered; }
    public void setEligibleCGPA(double eligibleCGPA) { this.eligibleCGPA = eligibleCGPA; }
    public void setDriveDate(String driveDate) { this.driveDate = driveDate; }
    public void setVenue(String venue) { this.venue = venue; }
    public void setApplicants(List<Application> applicants) { this.applicants = applicants; }

    // Correct method name according to PRD section 2.2
    public boolean checkEligibility(Student s) {
        return s.getCgpa() >= eligibleCGPA;
    }

    // Overload 1
    public void scheduleInterview(String date) {
        this.driveDate = date;
    }

    // Overload 2
    public void scheduleInterview(String date, String venue) {
        this.driveDate = date;
        this.venue = venue;
    }
}