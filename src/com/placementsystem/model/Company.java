package com.placementsystem.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a recruiting company in the placement system.
 * Demonstrates Interface implementation (Notifiable) and Object Serialization.
 */
public class Company implements Notifiable, Serializable {
    private static final long serialVersionUID = 1L;

    private String companyId;
    private String companyName;
    private String industryType;
    private String email;
    private String password;
    private boolean isApproved;
    private List<String> notifications;

    public Company() {
        this.isApproved = false;
        this.notifications = new ArrayList<>();
    }

    public Company(String companyId, String companyName, String industryType) {
        this(companyId, companyName, industryType, "", "");
    }

    public Company(String companyId, String companyName, String industryType, String email, String password) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.industryType = industryType;
        this.email = email;
        this.password = password;
        this.isApproved = false;
        this.notifications = new ArrayList<>();
    }

    // Getters and Setters
    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIndustryType() {
        return industryType;
    }

    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean isApproved) {
        this.isApproved = isApproved;
    }

    public List<String> getNotifications() {
        if (notifications == null) {
            notifications = new ArrayList<>();
        }
        return notifications;
    }

    @Override
    public void sendNotification(String message) {
        getNotifications().add(message);
        System.out.println("[Notification -> Company " + companyName + "]: " + message);
    }

    @Override
    public String toString() {
        return companyName + " (" + industryType + ") " + (isApproved ? "[Approved]" : "[Pending]");
    }
}