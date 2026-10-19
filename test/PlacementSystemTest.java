package test;

import com.placementsystem.exception.DuplicateApplicationException;
import com.placementsystem.exception.IneligibleStudentException;
import com.placementsystem.model.Company;
import com.placementsystem.model.PlacementDrive;
import com.placementsystem.model.Student;
import com.placementsystem.util.DataManager;
import com.placementsystem.util.PasswordUtil;

public class PlacementSystemTest {
    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running Placement Management System Tests...\n");

        testStudentCgpaValidation();
        testPlacementDriveEligibility();
        testApplyToDriveExceptions();
        testPasswordHashing();

        System.out.println("\n-------------------------------------------");
        System.out.println("Test Results: " + passedTests + " / " + totalTests + " passed.");
        if (passedTests != totalTests) {
            System.err.println("Some tests failed.");
            System.exit(1);
        } else {
            System.out.println("All tests passed successfully.");
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("[PASS] " + testName);
        } else {
            System.err.println("[FAIL] " + testName + " (expected true, got false)");
        }
    }

    private static void assertFalse(boolean condition, String testName) {
        assertTrue(!condition, testName);
    }

    private static void testStudentCgpaValidation() {
        System.out.println("Testing Student.setCgpa validation:");

        Student student = new Student("TEST001", "Test User", "test@ktu.edu", "9999999999", "CSE", 8.0, 0, "pass123");

        // Boundary valid values
        student.setCgpa(0.0);
        assertTrue(student.getCgpa() == 0.0, "Student CGPA boundary 0.0 allowed");

        student.setCgpa(10.0);
        assertTrue(student.getCgpa() == 10.0, "Student CGPA boundary 10.0 allowed");

        student.setCgpa(7.5);
        assertTrue(student.getCgpa() == 7.5, "Student CGPA mid-range 7.5 allowed");

        // Negative CGPA
        boolean threwNegative = false;
        try {
            student.setCgpa(-0.1);
        } catch (IllegalArgumentException e) {
            threwNegative = true;
        }
        assertTrue(threwNegative, "Student setCgpa(-0.1) throws IllegalArgumentException");

        // Above 10.0 CGPA
        boolean threwExcess = false;
        try {
            student.setCgpa(10.01);
        } catch (IllegalArgumentException e) {
            threwExcess = true;
        }
        assertTrue(threwExcess, "Student setCgpa(10.01) throws IllegalArgumentException");
    }

    private static void testPlacementDriveEligibility() {
        System.out.println("\nTesting PlacementDrive.checkEligibility:");

        Company company = new Company("TEST_CMP", "Test Tech", "IT", "hr@testtech.com", "pass123");
        company.setApproved(true);

        PlacementDrive drive = new PlacementDrive("TEST_DRV", company, "Software Engineer", 10.0, 7.5, 0, "2026-12-01");

        Student eligibleStudent = new Student("TEST002", "Eligible Student", "el@ktu.edu", "9999999998", "CSE", 8.5, 0, "pass123");
        assertTrue(drive.checkEligibility(eligibleStudent), "Student with higher CGPA and 0 backlogs is eligible");

        Student lowCgpaStudent = new Student("TEST003", "Low CGPA", "low@ktu.edu", "9999999997", "CSE", 7.49, 0, "pass123");
        assertFalse(drive.checkEligibility(lowCgpaStudent), "Student with CGPA below 7.5 is not eligible");

        Student backlogsStudent = new Student("TEST004", "Has Backlogs", "backlog@ktu.edu", "9999999996", "CSE", 9.0, 1, "pass123");
        assertFalse(drive.checkEligibility(backlogsStudent), "Student with 1 backlog when max allowed is 0 is not eligible");

        assertFalse(drive.checkEligibility(null), "Null student is not eligible");
    }

    private static void testApplyToDriveExceptions() {
        System.out.println("\nTesting DataManager.applyToDrive exceptions:");

        DataManager dm = DataManager.getInstance();

        Company company = new Company("TEST_CMP_EX", "Exception Tech", "IT", "hr@exception.com", "pass123");
        company.setApproved(true);
        dm.addCompany(company);

        PlacementDrive drive = new PlacementDrive("DRV_EX_01", company, "Developer", 8.0, 8.0, 0, "2026-12-01");
        dm.addPlacementDrive(drive);

        // 1. IneligibleStudentException
        Student ineligibleStudent = new Student("TEST_INELIGIBLE", "Ineligible Student", "ineligible@ktu.edu", "9999999995", "CSE", 7.0, 0, "pass123");
        dm.addStudent(ineligibleStudent);

        boolean threwIneligible = false;
        try {
            dm.applyToDrive(ineligibleStudent, drive);
        } catch (IneligibleStudentException e) {
            threwIneligible = true;
        } catch (DuplicateApplicationException e) {
            threwIneligible = false;
        }
        assertTrue(threwIneligible, "applyToDrive throws IneligibleStudentException for low CGPA");

        // 2. DuplicateApplicationException
        Student eligibleStudent = new Student("TEST_DUP", "Duplicate Tester", "dup@ktu.edu", "9999999994", "CSE", 9.0, 0, "pass123");
        dm.addStudent(eligibleStudent);

        boolean firstApplyPassed = false;
        try {
            dm.applyToDrive(eligibleStudent, drive);
            firstApplyPassed = true;
        } catch (Exception e) {
            firstApplyPassed = false;
        }
        assertTrue(firstApplyPassed, "First application succeeds for eligible student");

        boolean threwDuplicate = false;
        try {
            dm.applyToDrive(eligibleStudent, drive);
        } catch (DuplicateApplicationException e) {
            threwDuplicate = true;
        } catch (IneligibleStudentException e) {
            threwDuplicate = false;
        }
        assertTrue(threwDuplicate, "Second application throws DuplicateApplicationException");
    }

    private static void testPasswordHashing() {
        System.out.println("\nTesting PasswordUtil hashing and verification:");

        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();
        assertFalse(salt1.equals(salt2), "Generated salts are distinct");

        String hash1 = PasswordUtil.hashPassword("secret123", salt1);
        String hash2 = PasswordUtil.hashPassword("secret123", salt2);
        assertFalse(hash1.equals(hash2), "Same password with different salts yields different hashes");

        assertTrue(PasswordUtil.verifyPassword("secret123", salt1, hash1), "verifyPassword succeeds with correct password");
        assertFalse(PasswordUtil.verifyPassword("wrongpass", salt1, hash1), "verifyPassword fails with incorrect password");
    }
}
