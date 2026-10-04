package com.placementsystem.model;

import java.util.List;

public class Company implements Notifiable, java.io.Serializable {
    private String companyId;
    private String companyName;
    private String industryType;
    private String eligibilityCriteria;
    private boolean isApproved;

    // Required Constructor: public Company(String companyId, String companyName, String industryType)
    public Company(String companyId, String companyName, String industryType) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.industryType = industryType;
        this.isApproved = false; // Default approval state
    }

    // Getters
    public String getCompanyId() {
        return companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getIndustryType() {
        return industryType;
    }

    public String getEligibilityCriteria() {
        return eligibilityCriteria;
    }

    public boolean isApproved() {
        return isApproved;
    }

    // Setters
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    public void setEligibilityCriteria(String eligibilityCriteria) {
        this.eligibilityCriteria = eligibilityCriteria;
    }

    public void setApproved(boolean isApproved) {
        this.isApproved = isApproved;
    }

    // Adds a job drive to system registry if approved
    public void postJobOpening(PlacementDrive drive) {
        if (isApproved) {
            System.out.println("Job opening posted successfully for drive: " + drive.getDriveId());
        } else {
            System.out.println("Company is not approved by Admin.");
        }
    }

    // Returns applicant records for a drive
    public List<Application> viewApplicants(PlacementDrive drive) {
        return drive.getApplicants();
    }

    // Implements Notifiable interface
    @Override
    public void sendNotification(String msg) {
        System.out.println("Notification: " + msg);
    }
}