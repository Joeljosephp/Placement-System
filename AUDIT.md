# Placement Management System - Codebase Audit Report

Date: October 2026
Target: KTU S3 OOP Mini Project ("Placement Management System")
Scope: All source files in src/com/placementsystem/ and test/

## 1. Bugs and Loopholes

### BUG-01: Object Identity Severed on Deserialization (Data Desynchronization)
- File: src/com/placementsystem/util/DataManager.java
- Method: loadAll(), readListFromFile(), saveAll()
- How to reproduce:
  1. Log in as Student Arun Kumar (TVE22CS001).
  2. Update profile name to "Arun K." and CGPA to 9.00. Click "Save Profile".
  3. Restart the application and log in as Placement Officer or go to Pipeline Tracker.
  4. Inspect Arun's existing application (APP301) for Google Cloud.
  5. The applicant name in Application.student still reads "Arun Kumar" with CGPA 8.75.
  6. Furthermore, when updateApplicationStatus() updates an application, it calls student.sendNotification() on the Application's copy of Student and saves students.dat. But that Student instance is not the one in DataManager.students, so notifications added to app.student are lost on next restart.
  7. Similarly, PlacementDrive.applicants holds separate Application copies from DataManager.applications.
- Severity: High
- Proposed fix:
  Store all system entities inside a single unified container class (for example, PlacementData or DatabaseContainer) serialized to a single data file (data/placement_data.dat). Because Java ObjectOutputStream maintains an active handle table across the entire object graph in a single stream, all references (Application.student -> Student, PlacementDrive.company -> Company, Interview.application -> Application) retain pointer identity (==) upon deserialization. Provide automatic fallback/migration from legacy separate .dat files so existing installations do not lose data.

### BUG-02: Missing Input Validation and Sanitization on Registration
- File: src/com/placementsystem/gui/LoginPanel.java, src/com/placementsystem/util/DataManager.java
- Method: showStudentRegistrationDialog(), showCompanyRegistrationDialog(), addStudent(), addCompany()
- How to reproduce:
  1. Open Student Registration dialog.
  2. Enter email as "  arun@ktu.edu  " with leading/trailing spaces, or "INVALID_EMAIL".
  3. Enter phone number as "abcde" or single digit "1".
  4. Enter empty department or special symbols.
  5. Click Register. The student is registered with malformed email and invalid phone.
  6. In addition, company registration accepts arbitrary strings for email with no domain validation.
- Severity: Medium
- Proposed fix:
  Create a centralized Validator utility class in com.placementsystem.util.
  Enforce trim() and lowercase normalization for all emails.
  Validate email format using standard regex: ^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$.
  Validate phone number as 10 numeric digits (with optional +91 country code prefix).
  Validate USN as 5 to 15 alphanumeric characters.
  Check uniqueness across normalized email addresses.

### BUG-03: Arbitrary Package Values and Past Dates on Drive Creation
- File: src/com/placementsystem/gui/CompanyDashboard.java, src/com/placementsystem/model/PlacementDrive.java
- Method: handlePostDrive(), PlacementDrive constructor
- How to reproduce:
  1. Log in as an approved company (for example, hr@infosys.com).
  2. Open "Post Placement Drive" tab.
  3. Set Offered Package to 5000000 (5 million LPA) or 0.001 LPA.
  4. Set Drive Date to "2020-01-01" or any past date, or invalid calendar text such as "2026-02-31" or "not-a-date".
  5. Click "Publish Placement Drive".
  6. The drive is created without error because dateField is handled purely as an unparsed String.
  7. In PlacementDrive.java, the constructor does not validate if packageOffered <= 0 or if eligibleCGPA is outside [0.0, 10.0].
- Severity: Medium
- Proposed fix:
  Parse drive date using java.time.LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE).
  Ensure drive date is strictly today or in the future: !date.isBefore(LocalDate.now()).
  Enforce realistic package limits (for example, 0.5 LPA to 150.0 LPA).
  Add defensive argument checks in the PlacementDrive constructor and setters.

### BUG-04: Unrestricted Application Status Transitions and Missing Recruiter Authorization
- File: src/com/placementsystem/gui/CompanyDashboard.java, src/com/placementsystem/util/DataManager.java
- Method: updateApplicantStatus()
- How to reproduce:
  1. Select an applicant whose status is "Rejected".
  2. Change the dropdown back to "Selected" or "Shortlisted" and click "Update Status". The candidate status flips from Rejected to Selected.
  3. Select an applicant whose status is "Selected". Change dropdown to "Applied". The candidate status reverts to Applied.
  4. In CompanyDashboard.updateApplicantStatus(), DataManager.getInstance().getApplications() is searched by ID without verifying if the drive belongs to the logged-in company (currentCompany).
- Severity: High
- Proposed fix:
  Implement a formal transition validation rule:
  - Allowed forward transitions: Applied -> Shortlisted, Shortlisted -> Selected, Applied -> Rejected, Shortlisted -> Rejected.
  - Terminal statuses: Rejected is a terminal state and cannot transition to Shortlisted or Selected.
  - Selected can only be cancelled by administrative officer override, not flipped back to Applied by a recruiter.
  - Verify that application.getDrive().getCompany().getCompanyId().equals(currentCompany.getCompanyId()) before allowing status modifications.

### BUG-05: Scheduling Interviews for Rejected Applicants and Duplicate Rounds
- File: src/com/placementsystem/gui/CompanyDashboard.java, src/com/placementsystem/util/DataManager.java
- Method: showScheduleInterviewDialog(), addInterview()
- How to reproduce:
  1. Reject an applicant so their status is "Rejected".
  2. Click "Schedule Interview" for that applicant. The schedule dialog opens and allows creating an interview round for a rejected candidate.
  3. Create an interview round with name "Technical Round 1" on "2026-11-26 10:30 AM".
  4. Open the dialog again and schedule another "Technical Round 1" for the exact same applicant. Both duplicate rounds appear in tracking.
  5. The interview date string is never parsed or checked against the current date/time.
- Severity: Medium
- Proposed fix:
  Disable or disallow "Schedule Interview" if application status is "Rejected".
  Check that the round name has not already been scheduled for this application.
  Validate interview date and time format (YYYY-MM-DD HH:MM) and ensure it is in the future.

### BUG-06: One-Way Company Verification (No Reject Button or Unapproved Company Flow)
- File: src/com/placementsystem/gui/AdminDashboard.java, src/com/placementsystem/model/PlacementOfficer.java, src/com/placementsystem/model/Company.java
- Method: approveSelectedCompany(), createPendingApprovalsTab()
- How to reproduce:
  1. A spam or fraudulent company registers.
  2. The Placement Officer logs in and views "Pending Company Approvals".
  3. The only available action button is "Approve Selected Company". There is no "Reject" button.
  4. The unverified company remains in the pending queue indefinitely.
  5. If the officer selects the placeholder row ("-", "No pending company registrations", ...), clicking "Approve Selected Company" silently fails with no message.
  6. The officer cannot view already approved companies or rejected companies in a dedicated list.
- Severity: Medium
- Proposed fix:
  Add an approvalStatus attribute to Company: PENDING, APPROVED, REJECTED (with rejectionReason).
  Add a "Reject Company" button in AdminDashboard with a confirmation and reason dialog.
  Add a tab or filter to view all companies (Approved, Pending, Rejected).
  Ignore clicks when placeholder rows are selected.

### BUG-07: Sequential Size-Based ID Generation Collisions
- File: src/com/placementsystem/util/DataManager.java, src/com/placementsystem/gui/LoginPanel.java, src/com/placementsystem/gui/CompanyDashboard.java
- Method: applyToDrive(), handlePostDrive(), showCompanyRegistrationDialog(), showScheduleInterviewDialog()
- How to reproduce:
  1. IDs are generated using "APP" + (applications.size() + 101), "DRV" + (drives.size() + 201), "INT" + (interviews.size() + 401), "CMP" + (companies.size() + 101).
  2. If applications or drives are ever removed, or if records are created concurrently, IDs collide.
  3. Furthermore, ID generation logic is scattered across different GUI classes instead of centralized in DataManager.
- Severity: Medium
- Proposed fix:
  Centralize all ID generation inside DataManager methods: nextApplicationId(), nextDriveId(), nextCompanyId(), nextInterviewId().
  Compute IDs by inspecting the maximum numeric suffix among existing items and incrementing by 1 (for example, finding max numeric part of APPxxx and adding 1).

### BUG-08: Incorrect Placed Students Count on Multiple Offers
- File: src/com/placementsystem/util/DataManager.java, src/com/placementsystem/gui/AdminDashboard.java
- Method: getPlacedStudentsCount(), loadAnalytics()
- How to reproduce:
  1. If Student Arun is marked "Selected" for Google Cloud and also marked "Selected" for Infosys.
  2. DataManager.getPlacedStudentsCount() iterates applications and counts every application with status "Selected".
  3. It returns count = 2, even though only 1 distinct student was placed.
  4. If total students = 2, placement rate is calculated as (2 / 2) * 100% = 100%, even if the second student is unplaced. If student has 3 offers, placement rate exceeds 100%.
- Severity: Medium
- Proposed fix:
  In DataManager.getPlacedStudentsCount(), count distinct student USNs among applications with status "Selected" using a Set<String>.

### BUG-09: Panel State and User Data Retained Across Logout
- File: src/com/placementsystem/MainFrame.java, src/com/placementsystem/gui/StudentProfilePanel.java, src/com/placementsystem/gui/CompanyDashboard.java, src/com/placementsystem/gui/AdminDashboard.java
- Method: logout()
- How to reproduce:
  1. Log in as Student Arun. View profile fields and applications.
  2. Click "Logout". Confirm logout dialog.
  3. Log in as Student Diya or view student panel.
  4. Prior to explicit refresh, or if panels are cached, previous student profile data and notification models remain in memory.
  5. In LoginPanel, previous password and email might remain in fields if not reset.
- Severity: Medium
- Proposed fix:
  Implement a clearSession() method on StudentProfilePanel, CompanyDashboard, AdminDashboard, and LoginPanel.
  Invoke clearSession() inside MainFrame.logout().

### BUG-10: Lack of Placed-Student Policy (Unrestricted Multi-Offer Hoarding)
- File: src/com/placementsystem/util/DataManager.java
- Method: applyToDrive()
- How to reproduce:
  1. Student gets selected for an 18 LPA drive.
  2. Student goes back to drives list and applies for a 4.5 LPA drive.
  3. System allows application because checkEligibility only checks CGPA and backlogs.
  4. This hoards interview slots and job offers from other eligible students who have not yet secured an offer.
- Severity: Low / Feature Logic
- Proposed fix:
  Implement a clear KTU placement policy:
  - If a student is already "Selected" for a drive, they cannot apply for drives with a lower or equal package.
  - They may only apply for an upgrade/dream drive if the new drive package is at least 1.5x higher than their current offer, or lock further applications once placed.
  - Document this rule clearly in code, README, and viva notes.

### BUG-11: Data File Corruption and Lack of Backup
- File: src/com/placementsystem/util/DataManager.java
- Method: readListFromFile(), writeListToFile()
- How to reproduce:
  1. If the machine crashes or process terminates abruptly during writeListToFile(), the destination .dat file can be truncated to 0 bytes or left partially written.
  2. On next launch, ObjectInputStream throws EOFException or StreamCorruptedException.
  3. DataManager returns an empty list and overwrites the corrupted file, permanently losing data.
- Severity: High
- Proposed fix:
  Write to a temporary file first (data/placement_data.dat.tmp) and atomically replace the destination file upon successful write.
  Maintain an automatic backup copy (data/placement_data.dat.bak).
  If primary file reading encounters corruption, attempt recovery from the backup copy before falling back to seeding.

---

## 2. Usability Problems

### USA-01: Missing Keyboard Shortcuts (Enter to Submit, Escape to Close)
- LoginPanel has no default button set on root pane. Pressing Enter after typing password does not submit.
- Modal dialogs (Student Registration, Company Registration, Schedule Interview) do not listen for the Escape key to close.
- Proposed fix: Set rootPane.setDefaultButton(loginButton) in LoginPanel. Register an Escape key action listener on dialog root panes to trigger dispose().

### USA-02: Missing Drives Search, Filter, and "Eligible Only" Toggle
- In StudentProfilePanel, students must read through every row in the drives table without search or filtering.
- Students must click "Apply" on an ineligible drive just to see a popup telling them they cannot apply.
- Proposed fix: Add a search box (filter by company, role) and a checkbox: "Show only drives I am eligible for". Mark ineligible drives in a muted grey text style with an eligibility reason tooltip.

### USA-03: No Application Withdrawal Option for Students
- Once a student applies, there is no way to withdraw an application even if the company has not yet reviewed it (status is still "Applied").
- Proposed fix: Add a "Withdraw Application" button on the "My Applications" tab, active only when status is "Applied", with a confirmation dialog.

### USA-04: Fake Placeholder Rows in Tables Confuse Users
- In Student Applications table, Admin Approvals table, and Pipeline Tracker, empty tables insert a dummy row containing "-" and "No applications yet".
- Users can click on this dummy row and click action buttons (like "Approve" or "Update Status"), leading to confusing behavior.
- Proposed fix: Use standard Swing table empty-state overlays or disable action buttons when no real row is selected.

### USA-05: Missing Password Change Dialog for Logged-In Users
- Students, recruiters, and placement officers cannot update their passwords once registered.
- Proposed fix: Add a "Change Password" button in each dashboard header or profile area with old password verification and new password confirmation.

### USA-06: Notifications Have No Unread Status, Counter, or Clear Action
- Notifications accumulate as an unbounded list of strings.
- Users cannot tell which notifications are new, cannot mark them as read, and cannot clear old notifications.
- Proposed fix: Introduce a Notification object with timestamp and isRead status. Add "Mark All as Read" and "Clear All" buttons, and show an unread count badge on the tab title or header.

### USA-07: Missing CSV Export for Placement Analytics and Pipeline View
- Placement officers have no way to export the list of placed students or recruitment drives for KTU accreditation or reporting.
- Proposed fix: Add an "Export Placed Students to CSV" and "Export Pipeline to CSV" button in AdminDashboard using plain Java FileWriter without external libraries.

### USA-08: Remember Last Chosen Role on Login Screen
- Every time the application starts or logs out, the role dropdown resets to "Student".
- Proposed fix: Persist the last selected role in user preferences or remember it across logouts within the session.

---

## 3. Evaluation of Candidate Simple Features

| Candidate Feature | Feasibility | Fit for Project | Recommendation |
|---|---|---|---|
| 1. Change password with SHA-256 + salt | Very High | Direct fit for all roles | **Include** |
| 2. Student can withdraw application (status: Applied) | Very High | Real-world placement flow | **Include** |
| 3. Search and filter box on drives table + "Eligible only" | High | Huge usability win | **Include** |
| 4. Muted ineligible drives with tooltip | High | Eliminates blind error clicks | **Include** |
| 5. Application deadline on drives | High | Realistic drive lifecycle | **Include** |
| 6. Notification read/unread state and clear button | High | Improves UX significantly | **Include** |
| 7. Officer reject company with reason + view approved list | High | Closes security loophole | **Include** |
| 8. Export placed students and pipeline to CSV | High | Plain Java, great for viva | **Include** |
| 9. Placed student policy (single offer lock / upgrade rule) | High | Realistic placement rule | **Include** |
| 10. Enter to submit, Escape to close dialogs, remember role | High | Polish and accessibility | **Include** |
| 11. Confirmation dialogs on reject, withdraw, logout | High | Prevents accidental data loss | **Include** |

All 11 candidate features are practical, require zero external libraries, respect the existing OOP architecture, and are directly explainable by a student in an academic viva.
