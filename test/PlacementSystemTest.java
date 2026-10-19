package test;

import com.placementsystem.exception.DuplicateApplicationException;
import com.placementsystem.exception.IneligibleStudentException;
import com.placementsystem.model.*;
import com.placementsystem.util.DataManager;
import com.placementsystem.util.PasswordUtil;
import com.placementsystem.util.Validator;

import java.time.LocalDate;
import java.util.List;

/**
 * Plain Java test suite without external dependencies.
 * Exercises business logic, boundaries, state transitions, validation, and object identity.
 */
public class PlacementSystemTest {
    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running Placement Management System Test Suite...\n");

        testStudentCgpaValidation();
        testPlacementDriveEligibilityBoundaries();
        testUnapprovedCompanyPosting();
        testApplicationStatusTransitions();
        testRegistrationValidations();
        testApplyToDriveExceptions();
        testPlacedStudentSingleOfferPolicy();
        testApplicationWithdrawal();
        testPasswordHashing();
        testSaveAndLoadRoundTripObjectIdentity();

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
        System.out.println("1. Testing Student.setCgpa validation:");

        Student student = new Student("TEST001", "Test User", "test@ktu.edu", "9999999999", "CSE", 8.0, 0, "pass123");

        student.setCgpa(0.0);
        assertTrue(student.getCgpa() == 0.0, "Student CGPA boundary 0.0 allowed");

        student.setCgpa(10.0);
        assertTrue(student.getCgpa() == 10.0, "Student CGPA boundary 10.0 allowed");

        student.setCgpa(7.5);
        assertTrue(student.getCgpa() == 7.5, "Student CGPA mid-range 7.5 allowed");

        boolean threwNegative = false;
        try {
            student.setCgpa(-0.1);
        } catch (IllegalArgumentException e) {
            threwNegative = true;
        }
        assertTrue(threwNegative, "Student setCgpa(-0.1) throws IllegalArgumentException");

        boolean threwExcess = false;
        try {
            student.setCgpa(10.01);
        } catch (IllegalArgumentException e) {
            threwExcess = true;
        }
        assertTrue(threwExcess, "Student setCgpa(10.01) throws IllegalArgumentException");
    }

    private static void testPlacementDriveEligibilityBoundaries() {
        System.out.println("\n2. Testing PlacementDrive.checkEligibility boundaries:");

        Company company = new Company("CMP_TEST_ELIG", "Boundary Tech", "IT", "hr@boundary.com", "pass123");
        company.setApproved(true);

        // Cutoff: min CGPA 7.50, max backlogs 1
        PlacementDrive drive = new PlacementDrive("DRV_BOUND", company, "Software Engineer", 10.0, 7.50, 1, "2026-12-01");

        // Exact boundary match (CGPA == 7.50, backlogs == 1) -> must be eligible
        Student exactBoundary = new Student("TEST_EXACT", "Exact Match", "exact@ktu.edu", "9876543210", "CSE", 7.50, 1, "pass123");
        assertTrue(drive.checkEligibility(exactBoundary), "Student with exact cutoff CGPA (7.50) and max backlogs (1) is eligible");

        // Better than cutoff
        Student betterStudent = new Student("TEST_BETTER", "Better Match", "better@ktu.edu", "9876543211", "CSE", 8.50, 0, "pass123");
        assertTrue(drive.checkEligibility(betterStudent), "Student with higher CGPA and 0 backlogs is eligible");

        // CGPA just below cutoff (7.49)
        Student justBelowCgpa = new Student("TEST_LOW_CGPA", "Low CGPA", "lowc@ktu.edu", "9876543212", "CSE", 7.49, 1, "pass123");
        assertFalse(drive.checkEligibility(justBelowCgpa), "Student with CGPA 7.49 when 7.50 required is ineligible");

        // Backlogs just above cutoff (2 backlogs when max is 1)
        Student excessBacklogs = new Student("TEST_BACKLOGS", "Excess Backlogs", "backlog@ktu.edu", "9876543213", "CSE", 9.00, 2, "pass123");
        assertFalse(drive.checkEligibility(excessBacklogs), "Student with 2 backlogs when max 1 allowed is ineligible");

        assertFalse(drive.checkEligibility(null), "Null student is ineligible");
    }

    private static void testUnapprovedCompanyPosting() {
        System.out.println("\n3. Testing Unapproved Company Posting restriction:");

        DataManager dm = DataManager.getInstance();
        dm.resetForTesting();

        Company pendingCompany = new Company("CMP_PENDING_TEST", "Pending Corp", "IT", "hr@pendingcorp.com", "pass123");
        pendingCompany.setApproved(false);
        dm.addCompany(pendingCompany);

        PlacementDrive drive = new PlacementDrive("DRV_PENDING", pendingCompany, "Dev", 6.0, 7.0, 0, "2026-12-01");

        boolean threwIllegalState = false;
        try {
            dm.addPlacementDrive(drive);
        } catch (IllegalStateException e) {
            threwIllegalState = true;
        }
        assertTrue(threwIllegalState, "Unapproved company posting drive throws IllegalStateException");

        // After approval, posting must succeed
        PlacementOfficer officer = dm.getOfficers().get(0);
        officer.approveCompany(pendingCompany);
        dm.updateCompany(pendingCompany);

        boolean postSucceeded = false;
        try {
            dm.addPlacementDrive(drive);
            postSucceeded = true;
        } catch (Exception e) {
            postSucceeded = false;
        }
        assertTrue(postSucceeded, "Approved company can successfully post placement drive");
    }

    private static void testApplicationStatusTransitions() {
        System.out.println("\n4. Testing Application Status Transition rules:");

        Student s = new Student("TEST_TRANS", "Transition Tester", "trans@ktu.edu", "9876543220", "CSE", 8.0, 0, "pass123");
        Company c = new Company("CMP_TRANS", "Trans Tech", "IT", "hr@transtech.com", "pass123");
        c.setApproved(true);
        PlacementDrive d = new PlacementDrive("DRV_TRANS", c, "Analyst", 7.0, 7.0, 0, "2026-12-01");

        Application app = new Application("APP_TRANS", s, d);
        assertTrue("Applied".equals(app.getStatus()), "Initial status is 'Applied'");

        // Valid transition: Applied -> Shortlisted
        app.updateStatus("Shortlisted");
        assertTrue("Shortlisted".equals(app.getStatus()), "Transition 'Applied -> Shortlisted' allowed");

        // Valid transition: Shortlisted -> Selected
        app.updateStatus("Selected");
        assertTrue("Selected".equals(app.getStatus()), "Transition 'Shortlisted -> Selected' allowed");

        // Invalid transition: Selected -> Applied (must throw IllegalStateException)
        boolean threwRevert = false;
        try {
            app.updateStatus("Applied");
        } catch (IllegalStateException e) {
            threwRevert = true;
        }
        assertTrue(threwRevert, "Transition 'Selected -> Applied' throws IllegalStateException");

        // Test Terminal status: Rejected
        Application app2 = new Application("APP_TRANS2", s, d);
        app2.updateStatus("Rejected");
        assertTrue("Rejected".equals(app2.getStatus()), "Transition 'Applied -> Rejected' allowed");

        boolean threwFromRejected = false;
        try {
            app2.updateStatus("Selected");
        } catch (IllegalStateException e) {
            threwFromRejected = true;
        }
        assertTrue(threwFromRejected, "Transition 'Rejected -> Selected' throws IllegalStateException");
    }

    private static void testRegistrationValidations() {
        System.out.println("\n5. Testing Registration Validation logic:");

        // Email validation
        assertTrue(Validator.isValidEmail("student@ktu.edu"), "Valid email recognized");
        assertTrue(Validator.isValidEmail("john.doe+work@domain.co.in"), "Valid email with subdomain recognized");
        assertFalse(Validator.isValidEmail("plainaddress"), "Plain address without @ rejected");
        assertFalse(Validator.isValidEmail("@missingusername.com"), "Email missing username rejected");
        assertFalse(Validator.isValidEmail("missingdomain@.com"), "Email missing domain rejected");
        assertFalse(Validator.isValidEmail(""), "Empty email rejected");

        // Phone validation
        assertTrue(Validator.isValidPhone("9876543210"), "Valid 10-digit phone recognized");
        assertTrue(Validator.isValidPhone("+919876543210"), "Valid phone with +91 prefix recognized");
        assertFalse(Validator.isValidPhone("12345"), "Short phone rejected");
        assertFalse(Validator.isValidPhone("abcdefghij"), "Non-numeric phone rejected");

        // USN validation
        assertTrue(Validator.isValidUsn("TVE22CS001"), "Standard alphanumeric USN valid");
        assertTrue(Validator.isValidUsn("TVE22CS-001"), "USN with hyphen valid");
        assertFalse(Validator.isValidUsn("AB"), "Too short USN (<3 chars) rejected");

        // DataManager registration enforcement
        DataManager dm = DataManager.getInstance();
        dm.resetForTesting();

        Student validStudent = new Student("TVE_VAL_01", "Valid User", "valid1@ktu.edu", "9876543230", "CSE", 8.0, 0, "pass123");
        dm.addStudent(validStudent);

        // Duplicate email
        Student dupEmailStudent = new Student("TVE_VAL_02", "Dup User", "VALID1@KTU.EDU", "9876543231", "CSE", 8.0, 0, "pass123");
        boolean threwDupEmail = false;
        try {
            dm.addStudent(dupEmailStudent);
        } catch (IllegalArgumentException e) {
            threwDupEmail = true;
        }
        assertTrue(threwDupEmail, "Registering student with duplicate normalized email throws IllegalArgumentException");
    }

    private static void testApplyToDriveExceptions() {
        System.out.println("\n6. Testing DataManager.applyToDrive exceptions and deadlines:");

        DataManager dm = DataManager.getInstance();
        dm.resetForTesting();

        Company company = new Company("TEST_CMP_EX", "Exception Tech", "IT", "hr@exception.com", "pass123");
        company.setApproved(true);
        dm.addCompany(company);

        // Drive with future date
        PlacementDrive drive = new PlacementDrive("DRV_EX_01", company, "Developer", 8.0, 8.0, 0, "2026-12-01");
        dm.addPlacementDrive(drive);

        // Ineligible CGPA
        Student ineligibleStudent = new Student("TEST_INEL", "Ineligible Student", "ineligible@ktu.edu", "9876543240", "CSE", 7.0, 0, "pass123");
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

        // First application succeeds
        Student eligibleStudent = new Student("TEST_DUP_S", "Duplicate Tester", "dup@ktu.edu", "9876543241", "CSE", 9.0, 0, "pass123");
        dm.addStudent(eligibleStudent);

        boolean firstApplyPassed = false;
        try {
            dm.applyToDrive(eligibleStudent, drive);
            firstApplyPassed = true;
        } catch (Exception e) {
            firstApplyPassed = false;
        }
        assertTrue(firstApplyPassed, "First application succeeds for eligible student");

        // Second application throws DuplicateApplicationException
        boolean threwDuplicate = false;
        try {
            dm.applyToDrive(eligibleStudent, drive);
        } catch (DuplicateApplicationException e) {
            threwDuplicate = true;
        } catch (IneligibleStudentException e) {
            threwDuplicate = false;
        }
        assertTrue(threwDuplicate, "Second application throws DuplicateApplicationException");

        // Deadline passed drive
        PlacementDrive expiredDrive = new PlacementDrive("DRV_EX_OLD", company, "Old Role", 8.0, 7.0, 0, "2020-01-01");
        company.setApproved(true);
        dm.addPlacementDrive(expiredDrive);

        boolean threwExpired = false;
        try {
            dm.applyToDrive(eligibleStudent, expiredDrive);
        } catch (IneligibleStudentException e) {
            threwExpired = true;
        } catch (DuplicateApplicationException ignored) {}
        assertTrue(threwExpired, "Applying to drive with past deadline throws IneligibleStudentException");
    }

    private static void testPlacedStudentSingleOfferPolicy() {
        System.out.println("\n7. Testing KTU Placed-Student Policy:");

        DataManager dm = DataManager.getInstance();
        dm.resetForTesting();

        Company c1 = new Company("CMP_POL1", "Base Tech", "IT", "hr@base.com", "pass123");
        c1.setApproved(true);
        dm.addCompany(c1);

        Company c2 = new Company("CMP_POL2", "Dream Tech", "IT", "hr@dream.com", "pass123");
        c2.setApproved(true);
        dm.addCompany(c2);

        // Drive 1: 5.0 LPA
        PlacementDrive d1 = new PlacementDrive("DRV_POL1", c1, "Junior Dev", 5.0, 7.0, 0, "2026-12-10");
        dm.addPlacementDrive(d1);

        // Drive 2: 4.5 LPA (lower)
        PlacementDrive d2Lower = new PlacementDrive("DRV_POL2", c1, "Support Eng", 4.5, 7.0, 0, "2026-12-15");
        dm.addPlacementDrive(d2Lower);

        // Drive 3: 12.0 LPA (dream offer)
        PlacementDrive d3Dream = new PlacementDrive("DRV_POL3", c2, "Cloud Architect", 12.0, 8.0, 0, "2026-12-20");
        dm.addPlacementDrive(d3Dream);

        Student student = new Student("TEST_PLACED_S", "Placed Arun", "placed@ktu.edu", "9876543250", "CSE", 8.5, 0, "pass123");
        dm.addStudent(student);

        // Student applies to d1 and gets Selected
        try {
            dm.applyToDrive(student, d1);
            List<Application> apps = dm.getApplicationsForStudent(student);
            dm.updateApplicationStatus(apps.get(0), "Shortlisted");
            dm.updateApplicationStatus(apps.get(0), "Selected");
        } catch (Exception e) {
            System.err.println("Setup failed: " + e.getMessage());
        }

        // Student tries to apply to a lower or equal package drive (4.5 LPA) -> blocked
        boolean threwLowerOffer = false;
        try {
            dm.applyToDrive(student, d2Lower);
        } catch (IneligibleStudentException e) {
            threwLowerOffer = true;
        } catch (DuplicateApplicationException ignored) {}
        assertTrue(threwLowerOffer, "Selected student applying for lower package is blocked by policy");

        // Student tries to apply to a higher dream package drive (12.0 LPA) -> allowed
        boolean dreamApplyPassed = false;
        try {
            dm.applyToDrive(student, d3Dream);
            dreamApplyPassed = true;
        } catch (Exception e) {
            dreamApplyPassed = false;
        }
        assertTrue(dreamApplyPassed, "Selected student applying for dream drive with higher package is permitted");
    }

    private static void testApplicationWithdrawal() {
        System.out.println("\n8. Testing Student Application Withdrawal:");

        DataManager dm = DataManager.getInstance();
        dm.resetForTesting();

        Company c = new Company("CMP_WITHDRAW", "Withdraw Co", "IT", "hr@with.com", "pass123");
        c.setApproved(true);
        dm.addCompany(c);

        PlacementDrive d = new PlacementDrive("DRV_WITHDRAW", c, "Intern", 6.0, 7.0, 0, "2026-12-10");
        dm.addPlacementDrive(d);

        Student s = new Student("TEST_WITH_S", "Withdraw Student", "with@ktu.edu", "9876543260", "CSE", 8.0, 0, "pass123");
        dm.addStudent(s);

        try {
            dm.applyToDrive(s, d);
        } catch (Exception ignored) {}

        List<Application> apps = dm.getApplicationsForStudent(s);
        assertTrue(apps.size() == 1, "Application initially present");
        Application target = apps.get(0);

        // Withdraw application while in "Applied" status
        dm.withdrawApplication(s, target);
        assertTrue(dm.getApplicationsForStudent(s).isEmpty(), "Application successfully removed upon withdrawal");

        // Re-apply and advance to Shortlisted
        try {
            dm.applyToDrive(s, d);
            List<Application> reApps = dm.getApplicationsForStudent(s);
            Application advanced = reApps.get(0);
            dm.updateApplicationStatus(advanced, "Shortlisted");

            boolean threwWithdrawOnShortlisted = false;
            try {
                dm.withdrawApplication(s, advanced);
            } catch (IllegalStateException e) {
                threwWithdrawOnShortlisted = true;
            }
            assertTrue(threwWithdrawOnShortlisted, "Cannot withdraw application after it has been Shortlisted");
        } catch (Exception e) {
            assertTrue(false, "Re-apply unexpected exception: " + e.getMessage());
        }
    }

    private static void testPasswordHashing() {
        System.out.println("\n9. Testing PasswordUtil hashing and verification:");

        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();
        assertFalse(salt1.equals(salt2), "Generated salts are distinct");

        String hash1 = PasswordUtil.hashPassword("secret123", salt1);
        String hash2 = PasswordUtil.hashPassword("secret123", salt2);
        assertFalse(hash1.equals(hash2), "Same password with different salts yields different hashes");

        assertTrue(PasswordUtil.verifyPassword("secret123", salt1, hash1), "verifyPassword succeeds with correct password");
        assertFalse(PasswordUtil.verifyPassword("wrongpass", salt1, hash1), "verifyPassword fails with incorrect password");
    }

    private static void testSaveAndLoadRoundTripObjectIdentity() {
        System.out.println("\n10. Testing Unified Storage and Object Identity (Reference Equality):");

        DataManager dm = DataManager.getInstance();
        dm.resetForTesting();

        // Check seeded student Arun Kumar and his application APP301
        Student arun = null;
        for (Student s : dm.getStudents()) {
            if ("TVE22CS001".equalsIgnoreCase(s.getUsn())) {
                arun = s;
                break;
            }
        }
        assertTrue(arun != null, "Student Arun Kumar exists in seeded database");

        Application app = null;
        for (Application a : dm.getApplications()) {
            if ("APP301".equalsIgnoreCase(a.getApplicationId())) {
                app = a;
                break;
            }
        }
        assertTrue(app != null, "Application APP301 exists in seeded database");

        // Test in-memory pointer equality
        assertTrue(app.getStudent() == arun, "Application.student and DataManager.students share identical memory reference (==)");

        // Modify Arun's profile and save
        arun.setName("Arun Updated");
        arun.setCgpa(9.45);
        dm.updateStudent(arun);
        dm.saveAll();

        // Reload data from disk
        dm.loadAll();

        Student reloadedArun = null;
        for (Student s : dm.getStudents()) {
            if ("TVE22CS001".equalsIgnoreCase(s.getUsn())) {
                reloadedArun = s;
                break;
            }
        }
        Application reloadedApp = null;
        for (Application a : dm.getApplications()) {
            if ("APP301".equalsIgnoreCase(a.getApplicationId())) {
                reloadedApp = a;
                break;
            }
        }

        assertTrue("Arun Updated".equals(reloadedArun.getName()), "Reloaded student reflects updated name");
        assertTrue(reloadedApp.getStudent() == reloadedArun, "Reloaded Application.student matches canonical Student instance (==)");
        assertTrue("Arun Updated".equals(reloadedApp.getStudent().getName()), "Application reflects updated student name without desynchronization");
    }
}
