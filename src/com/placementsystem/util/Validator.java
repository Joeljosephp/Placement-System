package com.placementsystem.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Centralized validator for input sanitization and domain rule checks.
 * Used across GUI dialogs and model managers to keep validation uniform.
 */
public final class Validator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+91)?[6-9]\\d{9}$"
    );

    private static final Pattern USN_PATTERN = Pattern.compile(
            "^[A-Za-z0-9_-]{3,20}$"
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private Validator() {
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String trimmed = email.trim();
        return !trimmed.isEmpty() && EMAIL_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String trimmed = phone.trim().replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidUsn(String usn) {
        if (usn == null) return false;
        String trimmed = usn.trim();
        return USN_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidCgpa(double cgpa) {
        return cgpa >= 0.0 && cgpa <= 10.0;
    }

    public static boolean isValidBacklogs(int backlogs) {
        return backlogs >= 0 && backlogs <= 50;
    }

    public static boolean isValidPackage(double packageOffered) {
        return packageOffered >= 0.5 && packageOffered <= 150.0;
    }

    public static boolean isValidDateString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return false;
        try {
            LocalDate parsed = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
            return parsed != null;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isFutureOrToday(String dateStr) {
        if (!isValidDateString(dateStr)) return false;
        LocalDate parsed = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        return !parsed.isBefore(LocalDate.now());
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}
