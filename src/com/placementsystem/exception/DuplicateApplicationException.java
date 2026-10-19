package com.placementsystem.exception;

/**
 * Custom checked exception thrown when a student attempts to apply for a placement
 * drive that they have already applied to.
 * Demonstrates Custom Exception Handling in Java OOP.
 */
public class DuplicateApplicationException extends Exception {
    private static final long serialVersionUID = 1L;

    public DuplicateApplicationException() {
        super("Student has already submitted an application for this placement drive.");
    }

    public DuplicateApplicationException(String message) {
        super(message);
    }
}
