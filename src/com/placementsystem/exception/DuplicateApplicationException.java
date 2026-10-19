package com.placementsystem.exception;

public class DuplicateApplicationException extends Exception {
    private static final long serialVersionUID = 1L;

    public DuplicateApplicationException() {
        super("Student has already submitted an application for this placement drive.");
    }

    public DuplicateApplicationException(String message) {
        super(message);
    }
}
