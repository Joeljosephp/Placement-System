# Placement Management System - Refactoring and Implementation Plan

This plan organizes fixes and enhancements into prioritized groups. Each item includes the files touched, risk level, estimated effort, and step-by-step manual verification.

---

## Group 1: Must-Fix Bugs

### 1. Fix Object Identity Deserialization via Unified Storage
- **Description**: Replace separate file serialization with a unified `PlacementData` container serialized into `data/placement_data.dat`. In Java Serialization, writing the object graph in one stream preserves reference equality (`==`). When an application or student is updated, all related views reference the exact same object in memory. Add automatic fallback to load legacy `.dat` files if the unified file is absent.
- **Files touched**:
  - `src/com/placementsystem/util/DataManager.java`
  - New internal or model class `src/com/placementsystem/model/PlacementData.java`
- **Risk level**: Medium (affects persistence layer, mitigated by legacy data import fallback)
- **Estimated effort**: Medium (1 hour)
- **How to verify manually**:
  1. Log in as Student Arun Kumar (`arun@ktu.edu` / `arun123`).
  2. Change profile name to "Arun K." and click "Save Profile".
  3. Close the application completely.
  4. Run `./run.sh`. Log in as Placement Officer (`admin@ktu.edu` / `admin123`).
  5. Open "Pipeline Tracker". Verify that Arun's application APP301 reflects "Arun K." instead of the old name.

### 2. Centralized Input Validation and Sanitization (Validator Utility)
- **Description**: Create a single `Validator.java` utility in `com.placementsystem.util`. Validate emails with standard regex, trim inputs, enforce 10-digit phone numbers, 5-15 character alphanumeric USNs, CGPA between 0.0 and 10.0, non-negative backlogs, and non-empty text fields. Apply these validations consistently in GUI forms and defensively in `DataManager`.
- **Files touched**:
  - New file `src/com/placementsystem/util/Validator.java`
  - `src/com/placementsystem/gui/LoginPanel.java`
  - `src/com/placementsystem/gui/StudentProfilePanel.java`
  - `src/com/placementsystem/util/DataManager.java`
- **Risk level**: Low
- **Estimated effort**: Small (45 minutes)
- **How to verify manually**:
  1. On the Login screen, click "Register Student".
  2. Enter invalid email `bademail`, phone `123`, CGPA `11.5`.
  3. Verify inline error labels appear next to the fields before submission.
  4. Fix values and submit. Verify student registers cleanly and email is saved trimmed and lowercase.

### 3. Drive Date Validation, Deadline Enforcement, and Package Bounds
- **Description**: Parse drive dates using `java.time.LocalDate`. Validate date format (`YYYY-MM-DD`), ensure drive date is not in the past, and bound packages to realistic numbers (0.5 to 150.0 LPA). Disallow applications to drives whose date has already passed.
- **Files touched**:
  - `src/com/placementsystem/model/PlacementDrive.java`
  - `src/com/placementsystem/gui/CompanyDashboard.java`
  - `src/com/placementsystem/util/DataManager.java`
- **Risk level**: Low
- **Estimated effort**: Small (30 minutes)
- **How to verify manually**:
  1. Log in as Company (`hr@infosys.com` / `info123`).
  2. Try creating a drive with date `2020-01-01` or package `0` or `500000`.
  3. Verify inline validation rejects past dates and unreasonable packages.
  4. Create a valid drive with a future date (for example, `2026-11-25`). Verify successful creation.

### 4. Application Status Transition State Machine and Recruiter Authorization
- **Description**: Restrict application status transitions to valid flows:
  - `Applied -> Shortlisted`
  - `Shortlisted -> Selected`
  - `Applied -> Rejected`
  - `Shortlisted -> Rejected`
  `Rejected` is terminal and cannot transition back to Shortlisted or Selected. `Selected` cannot be reverted to Applied by a recruiter. Also ensure recruiters can only modify applications belonging to their own company drives.
- **Files touched**:
  - `src/com/placementsystem/model/Application.java`
  - `src/com/placementsystem/gui/CompanyDashboard.java`
  - `src/com/placementsystem/util/DataManager.java`
- **Risk level**: Low
- **Estimated effort**: Small (45 minutes)
- **How to verify manually**:
  1. Log in as Company.
  2. Select an applicant and set status to "Rejected". Confirm the prompt.
  3. Attempt to change that applicant to "Selected". Verify the system blocks the transition with a clear error message.

### 5. Robust Non-Colliding ID Generation
- **Description**: Centralize ID generation in `DataManager`. Instead of relying on list size (`size() + 101`), find the maximum numeric suffix among existing items and increment by 1.
- **Files touched**:
  - `src/com/placementsystem/util/DataManager.java`
  - `src/com/placementsystem/gui/CompanyDashboard.java`
  - `src/com/placementsystem/gui/LoginPanel.java`
- **Risk level**: Low
- **Estimated effort**: Small (30 minutes)
- **How to verify manually**:
  1. Add multiple companies, drives, and applications.
  2. Verify all generated IDs follow consistent patterns (CMP101, DRV201, APP301, INT401) without collision even if items exist out of order.

---

## Group 2: Security and Data Integrity

### 6. Atomic File Saves and Automatic Backup Recovery
- **Description**: When saving data, write to a temporary file (`placement_data.dat.tmp`) first, then rename or copy to `placement_data.dat`. Maintain a `placement_data.dat.bak` copy. If the primary file is ever missing or corrupted, automatically recover from the `.bak` file before falling back to initial sample seeding.
- **Files touched**:
  - `src/com/placementsystem/util/DataManager.java`
- **Risk level**: Low
- **Estimated effort**: Small (30 minutes)
- **How to verify manually**:
  1. Corrupt `data/placement_data.dat` by truncating it.
  2. Launch the application. Verify it successfully restores data from `placement_data.dat.bak` without crashing or showing an empty screen.

### 7. Change Password Feature for All Roles
- **Description**: Add a "Change Password" dialog accessible from the header or user profile. Require entry of current password, new password (minimum 6 characters), and confirmation. Re-hash with a fresh salt using `PasswordUtil`.
- **Files touched**:
  - `src/com/placementsystem/MainFrame.java`
  - `src/com/placementsystem/util/DataManager.java`
  - New dialog or helper in `src/com/placementsystem/gui/ChangePasswordDialog.java`
- **Risk level**: Low
- **Estimated effort**: Medium (45 minutes)
- **How to verify manually**:
  1. Log in as Student Arun (`arun123`).
  2. Click "Change Password" in the header.
  3. Enter current password `arun123` and new password `newarun456`.
  4. Log out and verify login with `arun123` fails, while `newarun456` succeeds.

### 8. Officer Company Rejection Flow with Reason
- **Description**: Allow the Placement Officer to reject a pending company registration with a mandatory reason. The rejected status and reason are recorded on the `Company` object. Add an "All Companies" tab or filter in `AdminDashboard` so the officer can inspect Approved, Pending, and Rejected companies.
- **Files touched**:
  - `src/com/placementsystem/model/Company.java`
  - `src/com/placementsystem/model/PlacementOfficer.java`
  - `src/com/placementsystem/gui/AdminDashboard.java`
  - `src/com/placementsystem/util/DataManager.java`
- **Risk level**: Low
- **Estimated effort**: Medium (45 minutes)
- **How to verify manually**:
  1. Register a new company "Test Spam Corp".
  2. Log in as Placement Officer. Select the company in Pending Approvals.
  3. Click "Reject Selected Company". Enter reason "Invalid corporate registration".
  4. Verify company moves to Rejected status and cannot post placement drives.

### 9. Distinct Placed-Students Count and Single Offer Policy
- **Description**: Fix `DataManager.getPlacedStudentsCount()` to count distinct student USNs using a Set. Implement a clear KTU placement policy: once a student is "Selected" for a drive, they are locked from applying to other drives (or only allowed to apply to dream drives with higher package).
- **Files touched**:
  - `src/com/placementsystem/util/DataManager.java`
  - `src/com/placementsystem/gui/StudentProfilePanel.java`
  - `src/com/placementsystem/gui/AdminDashboard.java`
- **Risk level**: Low
- **Estimated effort**: Small (30 minutes)
- **How to verify manually**:
  1. Have an applicant selected for a drive.
  2. Check Placement Analytics: verify distinct placed count is accurate.
  3. Try applying for another drive as the placed student: verify the system displays an informative message explaining the placement policy.

---

## Group 3: Usability Simplifications

### 10. Keyboard Accessibility and Login Improvements
- **Description**:
  - Pressing `Enter` in login fields submits the login form.
  - Pressing `Escape` in any dialog closes the dialog.
  - Remember the last selected role in the login dropdown across logouts during the application run.
- **Files touched**:
  - `src/com/placementsystem/gui/LoginPanel.java`
  - Dialogs in `CompanyDashboard`, `StudentProfilePanel`, `AdminDashboard`
- **Risk level**: Very Low
- **Estimated effort**: Small (20 minutes)
- **How to verify manually**:
  1. Type username and password on login screen. Press Enter without clicking the mouse. Verify login triggers.
  2. Open any registration or interview dialog. Press Escape. Verify dialog dismisses immediately.

### 11. Drives Search, Filter, and Visual Eligibility Indication
- **Description**:
  - Add a search box (filter by company, role) and a "Show only eligible drives" checkbox in the student drives tab.
  - Ineligible drives are styled with muted text and an eligibility tooltip (explaining whether CGPA or backlogs is the cause).
- **Files touched**:
  - `src/com/placementsystem/gui/StudentProfilePanel.java`
- **Risk level**: Low
- **Estimated effort**: Medium (45 minutes)
- **How to verify manually**:
  1. Log in as Student Rahul Varma (CGPA 6.80, 1 backlog).
  2. Open Available Placement Drives tab.
  3. Check "Show only eligible drives": Google Cloud (requires 8.5 CGPA, 0 backlogs) disappears; Infosys remains.
  4. Uncheck: Google Cloud row appears with muted text and a tooltip showing "Requires minimum CGPA 8.5 (Current: 6.80)".

### 12. Student Application Withdrawal
- **Description**: Allow students to withdraw an application while its status is still "Applied". Show a confirmation dialog. Once an application is "Shortlisted" or "Selected", withdrawal is disabled.
- **Files touched**:
  - `src/com/placementsystem/gui/StudentProfilePanel.java`
  - `src/com/placementsystem/util/DataManager.java`
- **Risk level**: Low
- **Estimated effort**: Small (30 minutes)
- **How to verify manually**:
  1. Log in as Student Arun. Apply for an available drive.
  2. In "My Applications", select the newly applied drive and click "Withdraw Application".
  3. Confirm the dialog. Verify the application is removed and the company is notified.

### 13. Session Clearing on Logout and Clean Window Close
- **Description**: Clear cached table models, text fields, and student/company references in panels when `logout()` is called, preventing lingering data. Add a window closing listener on `MainFrame` that checks for unsaved changes before exiting and saves data cleanly.
- **Files touched**:
  - `src/com/placementsystem/MainFrame.java`
  - `src/com/placementsystem/gui/StudentProfilePanel.java`
  - `src/com/placementsystem/gui/CompanyDashboard.java`
  - `src/com/placementsystem/gui/AdminDashboard.java`
- **Risk level**: Low
- **Estimated effort**: Small (30 minutes)
- **How to verify manually**:
  1. Log in as Student Arun, edit profile without saving.
  2. Click the window close button (X). Verify prompt warns about unsaved changes.
  3. Click Logout. Log in as Company. Verify no student profile data leaks into the session.

---

## Group 4: Small New Features

### 14. Structured Notifications with Unread State and Clear All
- **Description**: Replace raw string notifications with a `Notification` object containing message, timestamp, and `isRead` flag. Add an unread count badge on the tab title (for example, "Notifications (2)"), a "Mark All as Read" button, and a "Clear All" button.
- **Files touched**:
  - New class `src/com/placementsystem/model/Notification.java`
  - `src/com/placementsystem/model/Student.java`
  - `src/com/placementsystem/model/Company.java`
  - `src/com/placementsystem/gui/StudentProfilePanel.java`
  - `src/com/placementsystem/gui/CompanyDashboard.java`
- **Risk level**: Low
- **Estimated effort**: Medium (45 minutes)
- **How to verify manually**:
  1. When a new notification arrives, tab shows "Notifications (1)".
  2. View the notifications tab and click "Mark as Read". Unread badge clears.
  3. Click "Clear All". Notifications list empties.

### 15. Export Placed Students and Pipeline Tracker to CSV
- **Description**: Add an "Export to CSV" button in `AdminDashboard` (Placed Candidates) and `ApplicationTrackingPanel`. Use standard Java `FileWriter` to produce well-formed CSV files compatible with Excel or LibreOffice Calc.
- **Files touched**:
  - `src/com/placementsystem/gui/AdminDashboard.java`
  - `src/com/placementsystem/gui/ApplicationTrackingPanel.java`
- **Risk level**: Very Low
- **Estimated effort**: Small (30 minutes)
- **How to verify manually**:
  1. In Admin Dashboard Analytics, click "Export Placed Students (CSV)".
  2. Choose a save location (or default `exports/placed_students.csv`).
  3. Open the file in any text editor or spreadsheet to verify header and comma-separated rows.

### 16. Comprehensive Test Suite Expansion
- **Description**: Expand `test/PlacementSystemTest.java` and add tests for:
  - Exact boundary checks (CGPA == minimum, backlogs == maximum).
  - Registration input validations.
  - Status transition rules and invalid state rejection.
  - Serialization round-trip preserving object identity (`==`).
  - Salted SHA-256 password hashing.
  - Placed student single-offer policy.
  Create `test.sh` and `test.bat` scripts to run all tests with zero dependencies.
- **Files touched**:
  - `test/PlacementSystemTest.java`
  - `test.sh`
  - `test.bat`
- **Risk level**: Very Low
- **Estimated effort**: Medium (45 minutes)
- **How to verify manually**:
  1. Run `./test.sh` on macOS/Linux or `test.bat` on Windows.
  2. Verify all test cases pass with green output and zero errors.

---

## Summary of Implementation Order

1. **Step 1**: Group 1 - Items 1 to 5 (Core bug fixes, unified serialization, Validator, status transitions, ID generation).
2. **Step 2**: Group 2 - Items 6 to 9 (Atomic saves & backup, Change Password, Company rejection, Placed student policy).
3. **Step 3**: Group 3 - Items 10 to 13 (Keyboard shortcuts, drives search/filter, application withdrawal, session clearing).
4. **Step 4**: Group 4 - Items 14 to 16 (Structured notifications, CSV export, expanded test suite & test scripts).

Each step will be committed incrementally, compiled, and smoke tested.

---
**Status**: Waiting for user approval of this plan before writing or modifying any source code.
