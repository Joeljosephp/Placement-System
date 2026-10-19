package com.placementsystem.model;

import com.placementsystem.util.PasswordUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Company implements Notifiable, Serializable {
    private static final long serialVersionUID = 1L;

    private String companyId;
    private String companyName;
    private String industryType;
    private String email;
    private String passwordHash;
    private String salt;
    private boolean isApproved;
    private String rejectionReason;
    private List<String> notifications;

    public Company() {
        this.isApproved = false;
        this.rejectionReason = null;
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
        setPassword(password);
        this.isApproved = false;
        this.notifications = new ArrayList<>();
    }

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

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean isApproved) {
        this.isApproved = isApproved;
        if (isApproved) {
            this.rejectionReason = null;
        }
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public boolean isRejected() {
        return !isApproved && rejectionReason != null && !rejectionReason.trim().isEmpty();
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
    }

    @Override
    public String toString() {
        return companyName + " (" + industryType + ") " + (isApproved ? "[Approved]" : "[Pending]");
    }
}