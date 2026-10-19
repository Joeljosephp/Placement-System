package com.placementsystem.model;

/**
 * Represents a Placement Officer (Admin) in the placement system.
 * Demonstrates Inheritance (extends Person) and Business Authorization.
 */
public class PlacementOfficer extends Person {
    private static final long serialVersionUID = 1L;

    private String officerId;
    private String designation;
    private String password;

    public PlacementOfficer() {
        super();
    }

    public PlacementOfficer(String officerId, String name, String email, String phone, String designation, String password) {
        super(name, email, phone);
        this.officerId = officerId;
        this.designation = designation;
        this.password = password;
    }

    // Getters and Setters
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
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Approves a recruiting company, enabling them to post placement drives.
     */
    public void approveCompany(Company c) {
        if (c != null) {
            c.setApproved(true);
            c.sendNotification("Your company profile has been verified and approved by " + getName() + " (" + designation + "). You can now post placement drives.");
        }
    }

    @Override
    public void displayDetails() {
        System.out.println("----- Placement Officer Details -----");
        System.out.println("Officer ID: " + officerId);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phone);
        System.out.println("Designation: " + designation);
    }

    @Override
    public String toString() {
        return name + " (" + designation + ")";
    }
}
