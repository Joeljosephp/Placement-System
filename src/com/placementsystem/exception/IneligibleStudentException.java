package com.placementsystem.exception;

/**
 * Custom checked exception thrown when a student does not meet the eligibility
 * criteria (such as minimum CGPA or maximum allowed backlogs) for a placement drive.
 * Demonstrates Custom Exception Handling in Java OOP.
 */
public class IneligibleStudentException extends Exception {
    private static final long serialVersionUID = 1L;

    public IneligibleStudentException() {
        super("Student does not meet the eligibility criteria for this placement drive.");
    }

    public IneligibleStudentException(String message) {
        super(message);
    }
}
