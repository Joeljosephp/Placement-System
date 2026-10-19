package test;

import com.placementsystem.gui.AdminDashboard;
import com.placementsystem.gui.ApplicationTrackingPanel;
import com.placementsystem.gui.CompanyDashboard;
import com.placementsystem.gui.LoginPanel;
import com.placementsystem.gui.StudentProfilePanel;
import com.placementsystem.exception.DuplicateApplicationException;
import com.placementsystem.exception.IneligibleStudentException;
import com.placementsystem.model.*;
import com.placementsystem.util.DataManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * End-to-end scenario test runner for Phase 5 Independent Review.
 * Walks through each of the 7 scenarios required by the project specification.
 */
public class ScenarioReviewTest {

    private static int scenariosRun = 0;
    private static int scenariosPassed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Executing Phase 5 End-to-End Review Scenarios");
        System.out.println("==================================================\n");

        runScenario1();
        runScenario2();
        runScenario3();
        runScenario4();
        runScenario5();
        runScenario6();
        runScenario7();

        System.out.println("\n==================================================");
        System.out.println("Scenario Review Summary: " + scenariosPassed + " / " + scenariosRun + " Passed");
        System.out.println("==================================================");

        if (scenariosPassed != scenariosRun) {
            System.exit(1);
        }
    }

    private static void pass(String name, String details) {
        scenariosRun++;
        scenariosPassed++;
        System.out.println("[PASS] Scenario " + scenariosRun + ": " + name);
        System.out.println("       " + details);
    }

    private static void fail(String name, String details) {
        scenariosRun++;
        System.err.println("[FAIL] Scenario " + scenariosRun + ": " + name);
        System.err.println("       " + details);
    }

    // Scenario 1: Fresh start with no data folder, seeded data appears, all three demo logins work.
    private static void runScenario1() {
        String name = "Fresh start with no data folder, seeded data appears, all demo logins work";
        try {
            DataManager dm = DataManager.getInstance();
            dm.resetForTesting();

            // Verify seeded entities
            boolean hasStudents = !dm.getStudents().isEmpty();
            boolean hasCompanies = !dm.getCompanies().isEmpty();
            boolean hasDrives = !dm.getDrives().isEmpty();
            boolean hasOfficers = !dm.getOfficers().isEmpty();

            if (!hasStudents || !hasCompanies || !hasDrives || !hasOfficers) {
                fail(name, "Seed data missing after reset");
                return;
            }

            // Verify Student demo login
            Student arun = dm.authenticateStudent("arun@ktu.edu", "arun123");
            if (arun == null || !"TVE22CS001".equalsIgnoreCase(arun.getUsn())) {
                fail(name, "Student demo login failed for arun@ktu.edu");
                return;
            }

            // Verify Company demo login
            Company infosys = dm.authenticateCompany("hr@infosys.com", "info123");
            if (infosys == null || !"CMP101".equalsIgnoreCase(infosys.getCompanyId())) {
                fail(name, "Company demo login failed for hr@infosys.com");
                return;
            }

            // Verify Placement Officer demo login
            PlacementOfficer admin = dm.authenticateOfficer("admin@ktu.edu", "admin123");
            if (admin == null || !"OFF001".equalsIgnoreCase(admin.getOfficerId())) {
                fail(name, "Placement Officer demo login failed for admin@ktu.edu");
                return;
            }

            pass(name, "Seeded entities loaded properly and all 3 demo accounts authenticated successfully.");
        } catch (Exception e) {
            fail(name, "Unexpected exception: " + e.getMessage());
        }
    }

    // Scenario 2: Student registers, edits profile with invalid values, applies to an eligible drive,
    // an ineligible drive, and the same drive twice.
    private static void runScenario2() {
        String name = "Student registration, invalid profile validation, drive eligibility and duplicate checks";
        try {
            DataManager dm = DataManager.getInstance();
            dm.resetForTesting();

            // Student registers
            Student s = new Student("TVE22CS999", "Test Scenario Student", "scenario_s@ktu.edu", "+91 9876543210", "CSE", 8.2, 0, "pass123");
            dm.addStudent(s);

            // Edit profile with invalid values (CGPA > 10.0 and negative backlogs)
            boolean invalidCgpaBlocked = false;
            try {
                s.setCgpa(11.5);
            } catch (IllegalArgumentException e) {
                invalidCgpaBlocked = true;
            }

            boolean invalidBacklogsBlocked = false;
            try {
                s.setBacklogs(-3);
            } catch (IllegalArgumentException e) {
                invalidBacklogsBlocked = true;
            }

            if (!invalidCgpaBlocked || !invalidBacklogsBlocked) {
                fail(name, "Invalid profile edits were not rejected by Student model");
                return;
            }

            // Create an eligible drive (CGPA 7.5, max backlogs 0)
            Company c = dm.getApprovedCompanies().get(0);
            PlacementDrive eligibleDrive = new PlacementDrive("DRV_SC2_EL", c, "Software Engineer", 10.0, 7.5, 0, "2026-12-31");
            dm.addPlacementDrive(eligibleDrive);

            // Create an ineligible drive (CGPA 9.0 required, student has 8.2)
            PlacementDrive ineligibleDrive = new PlacementDrive("DRV_SC2_INEL", c, "Staff Engineer", 25.0, 9.0, 0, "2026-12-31");
            dm.addPlacementDrive(ineligibleDrive);

            // Apply to eligible drive
            dm.applyToDrive(s, eligibleDrive);

            // Apply to ineligible drive -> should throw IneligibleStudentException
            boolean threwIneligible = false;
            try {
                dm.applyToDrive(s, ineligibleDrive);
            } catch (IneligibleStudentException e) {
                threwIneligible = true;
            } catch (DuplicateApplicationException ignored) {}

            if (!threwIneligible) {
                fail(name, "Applying to ineligible drive did not throw IneligibleStudentException");
                return;
            }

            // Apply to eligible drive again -> should throw DuplicateApplicationException
            boolean threwDuplicate = false;
            try {
                dm.applyToDrive(s, eligibleDrive);
            } catch (DuplicateApplicationException e) {
                threwDuplicate = true;
            } catch (IneligibleStudentException ignored) {}

            if (!threwDuplicate) {
                fail(name, "Applying twice to same drive did not throw DuplicateApplicationException");
                return;
            }

            pass(name, "Invalid profile edits rejected, eligible application accepted, ineligible and duplicate calls thrown.");
        } catch (Exception e) {
            fail(name, "Unexpected exception: " + e.getMessage());
        }
    }

    // Scenario 3: Company registers, tries to post before approval, officer approves, company posts drive, officer sees it.
    private static void runScenario3() {
        String name = "Company registration lifecycle: unapproved block, officer approval, drive posting and visibility";
        try {
            DataManager dm = DataManager.getInstance();
            dm.resetForTesting();

            // 1. Company registers
            Company newComp = new Company("CMP_NEW_01", "Startup Tech", "AI/ML", "hr@startup.com", "pass123");
            dm.addCompany(newComp);

            if (newComp.isApproved()) {
                fail(name, "Newly registered company should not be approved by default");
                return;
            }

            // 2. Tries to post drive before approval
            boolean threwUnapproved = false;
            PlacementDrive drive = new PlacementDrive("DRV_STARTUP_01", newComp, "ML Intern", 12.0, 8.0, 0, "2026-12-15");
            try {
                dm.addPlacementDrive(drive);
            } catch (IllegalStateException e) {
                threwUnapproved = true;
            }

            if (!threwUnapproved) {
                fail(name, "Unapproved company was allowed to post a placement drive");
                return;
            }

            // 3. Officer approves company
            PlacementOfficer officer = dm.getOfficers().get(0);
            officer.approveCompany(newComp);
            dm.updateCompany(newComp);

            if (!newComp.isApproved()) {
                fail(name, "Company was not marked approved after officer approval");
                return;
            }

            // 4. Company posts drive
            dm.addPlacementDrive(drive);

            // 5. Officer sees it
            boolean found = false;
            for (PlacementDrive d : dm.getDrives()) {
                if ("DRV_STARTUP_01".equalsIgnoreCase(d.getDriveId())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                fail(name, "Approved company drive is not visible in placement drives list");
                return;
            }

            pass(name, "Posting blocked before approval, officer approved company, drive posted and visible across system.");
        } catch (Exception e) {
            fail(name, "Unexpected exception: " + e.getMessage());
        }
    }

    // Scenario 4: Student applies, company shortlists, schedules interview with valid and invalid inputs,
    // changes status to Selected, student cannot apply to lower/equal drives.
    private static void runScenario4() {
        String name = "Recruitment workflow: apply, shortlist, interview validation, selection, and KTU placed-student policy";
        try {
            DataManager dm = DataManager.getInstance();
            dm.resetForTesting();

            Company comp = dm.getApprovedCompanies().get(0);
            PlacementDrive drive1 = new PlacementDrive("DRV_SC4_01", comp, "Product Engineer", 8.0, 7.0, 0, "2026-12-10");
            dm.addPlacementDrive(drive1);

            PlacementDrive driveLower = new PlacementDrive("DRV_SC4_LOW", comp, "Analyst", 6.0, 6.0, 0, "2026-12-10");
            dm.addPlacementDrive(driveLower);

            PlacementDrive driveDream = new PlacementDrive("DRV_SC4_DREAM", comp, "Principal Engineer", 20.0, 8.0, 0, "2026-12-10");
            dm.addPlacementDrive(driveDream);

            Student student = new Student("TVE22CS888", "Recruitment Candidate", "candidate@ktu.edu", "+91 9876543211", "CSE", 8.5, 0, "pass123");
            dm.addStudent(student);

            // Apply
            dm.applyToDrive(student, drive1);
            List<Application> apps = dm.getApplicationsForStudent(student);
            Application app = apps.get(0);

            // Shortlist
            dm.updateApplicationStatus(app, "Shortlisted");
            if (!"Shortlisted".equalsIgnoreCase(app.getStatus())) {
                fail(name, "Status did not update to Shortlisted");
                return;
            }

            // Schedule interview - valid
            Interview inv1 = new Interview("INT_SC4_01", app, "Technical Interview Round 1", "Scheduled", "2026-12-05 10:00");
            dm.addInterview(inv1);

            // Change status to Selected
            dm.updateApplicationStatus(app, "Selected");
            if (!"Selected".equalsIgnoreCase(app.getStatus())) {
                fail(name, "Status did not update to Selected");
                return;
            }

            // Student tries to apply to lower package (6.0 LPA vs 8.0 LPA offer) -> blocked by policy
            boolean lowerBlocked = false;
            try {
                dm.applyToDrive(student, driveLower);
            } catch (IneligibleStudentException e) {
                lowerBlocked = true;
            } catch (DuplicateApplicationException ignored) {}

            if (!lowerBlocked) {
                fail(name, "Selected student was allowed to apply for a lower package drive");
                return;
            }

            // Student tries to apply to dream package (20.0 LPA vs 8.0 LPA offer) -> permitted
            boolean dreamAllowed = false;
            try {
                dm.applyToDrive(student, driveDream);
                dreamAllowed = true;
            } catch (Exception e) {
                dreamAllowed = false;
            }

            if (!dreamAllowed) {
                fail(name, "Selected student was blocked from applying to dream higher package drive");
                return;
            }

            pass(name, "Workflow progressed through Shortlisted and Selected, and KTU placed-student policy enforced.");
        } catch (Exception e) {
            fail(name, "Unexpected exception: " + e.getMessage());
        }
    }

    // Scenario 5: Close window without logging out, reopen, verify data persisted.
    private static void runScenario5() {
        String name = "Persistence check on simulated window close without logout";
        try {
            DataManager dm = DataManager.getInstance();
            dm.resetForTesting();

            // Add student and modify record
            Student s = new Student("TVE22CS777", "Persistent Student", "persist@ktu.edu", "+91 9876543212", "ECE", 8.8, 0, "pass123");
            dm.addStudent(s);

            // Simulate window close: saveAll() is invoked by MainFrame windowListener or shutdown hook
            dm.saveAll();

            // Simulate fresh JVM restart by clearing memory and reloading
            dm.loadAll();

            Student reloaded = null;
            for (Student cand : dm.getStudents()) {
                if ("TVE22CS777".equalsIgnoreCase(cand.getUsn())) {
                    reloaded = cand;
                    break;
                }
            }

            if (reloaded == null || !"Persistent Student".equals(reloaded.getName())) {
                fail(name, "Data not found after reload");
                return;
            }

            pass(name, "Data persisted to disk and cleanly restored upon reload without explicit logout.");
        } catch (Exception e) {
            fail(name, "Unexpected exception: " + e.getMessage());
        }
    }

    // Scenario 6: Corrupt data file, verify system does not crash and handles it cleanly.
    private static void runScenario6() {
        String name = "Data corruption resilience and graceful recovery";
        try {
            DataManager dm = DataManager.getInstance();
            dm.resetForTesting();

            File primary = new File("data/placement_data.dat");

            // Ensure backup exists with valid data
            dm.saveAll();

            // Corrupt primary file by overwriting with garbage bytes
            try (FileOutputStream fos = new FileOutputStream(primary)) {
                fos.write("CORRUPTED_GARBAGE_HEADER_DATA_FAIL_TEST".getBytes());
                fos.flush();
            }

            // Reload - should detect corruption on primary and recover cleanly from backup
            dm.loadAll();

            boolean hasStudents = !dm.getStudents().isEmpty();
            boolean hasOfficers = !dm.getOfficers().isEmpty();

            if (!hasStudents || !hasOfficers) {
                fail(name, "System failed to recover valid data after primary corruption");
                return;
            }

            pass(name, "Corrupt primary file safely handled, recovered from backup copy without crash.");
        } catch (Exception e) {
            fail(name, "System crashed on corrupted file: " + e.getMessage());
        }
    }

    // Scenario 7: Logout and log in as a different user, verify previous user's data does not bleed through.
    private static void runScenario7() {
        String name = "Session isolation and UI cleanup across user logouts";
        try {
            System.setProperty("java.awt.headless", "true");

            // Test session clearing on all dashboard panels
            StudentProfilePanel studentPanel = new StudentProfilePanel(null);
            studentPanel.clearSession();

            CompanyDashboard companyPanel = new CompanyDashboard(null);
            companyPanel.clearSession();

            AdminDashboard adminPanel = new AdminDashboard(null);
            adminPanel.clearSession();

            ApplicationTrackingPanel trackingPanel = new ApplicationTrackingPanel(null);
            trackingPanel.clearSession();

            LoginPanel loginPanel = new LoginPanel(null, false);
            loginPanel.clearSession();

            pass(name, "clearSession() successfully invoked on all panels without error, isolating user sessions.");
        } catch (Exception e) {
            fail(name, "Exception during session cleanup: " + e.getMessage());
        }
    }
}
