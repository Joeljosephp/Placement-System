package com.placementsystem.model;

import java.io.Serializable;

/**
 * Abstract class representing a generic Person in the placement system.
 * Demonstrates Abstraction and Encapsulation.
 */
public abstract class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String name;
    protected String email;
    protected String phone;

    public Person() {
    }

    public Person(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Abstract method to be implemented by subclasses to display specific details.
     */
    public abstract void displayDetails();

    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
}
