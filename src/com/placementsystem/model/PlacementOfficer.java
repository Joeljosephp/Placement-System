package com.placementsystem.model;

import com.placementsystem.util.PasswordUtil;

public class PlacementOfficer extends Person {
    private static final long serialVersionUID = 1L;

    private String officerId;
    private String designation;
    private String passwordHash;
    private String salt;

    public PlacementOfficer() {
        super();
    }

    public PlacementOfficer(String officerId, String name, String email, String phone, String designation, String password) {
        super(name, email, phone);
        this.officerId = officerId;
        this.designation = designation;
        setPassword(password);
    }

    public String getOfficerId() {
        return officerId;
    }

    public void setOfficerId(String officerId) {
        this.officerId = officerId;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
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

    public void approveCompany(Company c) {
        if (c != null) {
            c.setApproved(true);
            c.sendNotification("Your company profile has been verified and approved by " + getName() + " (" + designation + "). You can now post placement drives.");
        }
    }

    public void rejectCompany(Company c, String reason) {
        if (c != null) {
            c.setApproved(false);
            c.setRejectionReason(reason);
            c.sendNotification("Your company registration was rejected by " + getName() + " (" + designation + "). Reason: " + reason);
        }
    }

    @Override
    public void displayDetails() {
        System.out.println("Officer: " + name + " (" + officerId + "), " + designation);
    }

    @Override
    public String toString() {
        return name + " (" + designation + ")";
    }
}
