package com.placementsystem.exception;

public class IneligibleStudentException extends Exception {
    private static final long serialVersionUID = 1L;

    public IneligibleStudentException() {
        super("Student does not meet the eligibility criteria for this placement drive.");
    }

    public IneligibleStudentException(String message) {
        super(message);
    }
}
