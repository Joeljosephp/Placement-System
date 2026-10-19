# Placement Management System - Independent Review Report

Date: October 2026  
Role: Senior Independent Reviewer  
Target: KTU S3 OOP Mini Project ("Placement Management System")  
Scope: Entire codebase (`src/`, `test/`, build scripts, and documentation)

---

## 1. Executive Summary

A comprehensive, multi-pass audit and refactor of the Placement Management System was conducted. All 11 bugs and loopholes identified in `AUDIT.md` have been resolved. All 8 usability deficiencies and 11 planned features from `PLAN.md` have been implemented.

The application adheres strictly to standard Java SE without external libraries, compiles with zero warnings under `javac -Xlint:all`, contains zero emojis or em dashes, and preserves every graded Object-Oriented Programming construct required by the KTU syllabus.

All 51 automated unit/integration assertions and all 7 end-to-end user scenarios pass cleanly (100% test success rate).

---

## 2. Verification of Audit Findings (AUDIT.md)

| Issue ID | Description | Resolution Status | Verification Mechanism |
|---|---|---|---|
| **BUG-01** | Object identity severed on deserialization | **Fixed** | Entities unified into `PlacementData.java` serialized in a single stream to `data/placement_data.dat`. Bi-directional references verified identical in memory (`==`) in Test Suite 10. |
| **BUG-02** | Missing input validation on registration | **Fixed** | Centralized in `Validator.java`. Enforces standard email regex, phone formatting, USN pattern, and trimmed/lowercase email uniqueness. Verified in Test Suite 5. |
| **BUG-03** | Arbitrary packages and past drive dates | **Fixed** | Enforced 0.5 to 150.0 LPA package boundaries and ISO LocalDate validation. Ineligible/expired drives rejected. Verified in Test Suite 6. |
| **BUG-04** | Unrestricted status transitions & recruiter bypass | **Fixed** | Formal state machine in `Application.isValidTransition()` (`Applied -> Shortlisted -> Selected`, `Rejected` terminal). Drive ownership verified in `CompanyDashboard`. Verified in Test Suite 4. |
| **BUG-05** | Interviews for rejected applicants & duplicates | **Fixed** | Scheduling blocked for rejected applicants; duplicate rounds blocked; date/time format validated. Verified in Scenario 4. |
| **BUG-06** | One-way company approvals (no reject flow) | **Fixed** | Added `rejectionReason` in `Company.java`, `rejectCompany()` in `PlacementOfficer.java`, dialog prompt and status filter in `AdminDashboard`. Verified in Scenario 3. |
| **BUG-07** | Sequential ID collisions | **Fixed** | Centralized monotonic ID generators in `DataManager` (`nextApplicationId()`, `nextDriveId()`, `nextCompanyId()`, `nextInterviewId()`) inspecting max existing numeric suffixes. |
| **BUG-08** | Inaccurate placed students count on multiple offers | **Fixed** | `DataManager.getPlacedStudentsCount()` uses `Set<String>` of unique student USNs with status "Selected", preventing inflated placement rates. |
| **BUG-09** | Panel state and user data retained on logout | **Fixed** | Implemented `clearSession()` across all panels (`StudentProfilePanel`, `CompanyDashboard`, `AdminDashboard`, `ApplicationTrackingPanel`, `LoginPanel`). Verified in Scenario 7. |
| **BUG-10** | Unrestricted multi-offer hoarding | **Fixed** | Implemented KTU placed-student policy: selected candidates blocked from lower/equal packages; upgrade to higher dream package permitted. Verified in Test Suite 7. |
| **BUG-11** | Data file corruption and lack of backup | **Fixed** | Atomic file writes via `.tmp` temporary file, automatic `.bak` backup copy, and automatic recovery in `readPlacementData()` on stream corruption. Verified in Scenario 6. |

---

## 3. Verification of Usability Simplifications

- **USA-01 (Keyboard Navigation)**: Set default button on `LoginPanel` root pane so Enter key submits login. Attached `KeyEvent.VK_ESCAPE` listeners across dialogs (`ChangePasswordDialog`, registration dialogs, interview dialogs) to dismiss cleanly.
- **USA-02 (Drives Search & Filter)**: Added live search field (filtering company name, role, package) and "Show only eligible drives" checkbox in `StudentProfilePanel`.
- **USA-03 (Application Withdrawal)**: Added "Withdraw Application" button on student dashboard, allowed strictly while status is `Applied`.
- **USA-04 (Table Empty States & Overlays)**: Removed dummy "-" placeholder rows. Tables display clean empty states and safely ignore clicks when no valid row is selected.
- **USA-05 (Change Password)**: Created `ChangePasswordDialog` supporting Student, Company, and Placement Officer password updates using salted SHA-256.
- **USA-06 (Structured Notifications)**: Replaced unbounded string lists with `Notification` objects containing timestamp and `isRead` flag. Added "Mark All as Read", "Clear All", and an unread counter badge.
- **USA-07 (CSV Reporting)**: Added "Export Placed Students to CSV" in `AdminDashboard` and "Export Pipeline to CSV" in `ApplicationTrackingPanel` using plain Java `FileWriter`.
- **USA-08 (Remember Role)**: Role dropdown in `LoginPanel` remembers the last-selected role across logouts within the session.

---

## 4. End-to-End Walkthrough Scenarios

All 7 scenarios specified for Phase 5 were executed programmatically and manually:

### Scenario 1: Fresh Start with Seeded Data & Demo Logins
- **Action**: Started system without pre-existing data directory.
- **Result**: `DataManager` initialized seed datasets (students, approved companies, drives, default officer). Authenticated `arun@ktu.edu` (`arun123`), `hr@infosys.com` (`info123`), and `admin@ktu.edu` (`admin123`).
- **Status**: **PASS**

### Scenario 2: Student Registration, Validation & Drive Eligibility
- **Action**: Registered new student, attempted setting CGPA to 11.5 and backlogs to -3, applied to eligible drive, ineligible drive, and duplicate drive.
- **Result**: Invalid profile values rejected with `IllegalArgumentException`. Eligible application accepted. Ineligible drive threw `IneligibleStudentException`. Duplicate submission threw `DuplicateApplicationException`.
- **Status**: **PASS**

### Scenario 3: Company Registration, Approval & Drive Visibility
- **Action**: Registered new company, attempted drive posting prior to approval, approved company as Placement Officer, posted drive, verified officer visibility.
- **Result**: Drive creation blocked while unapproved (`IllegalStateException`). Officer approval updated company status. Drive posted successfully and rendered in officer drive catalog.
- **Status**: **PASS**

### Scenario 4: Recruitment Lifecycle & KTU Placed-Student Policy
- **Action**: Student applied, recruiter shortlisted candidate, scheduled interview, updated status to "Selected", student attempted applying to lower (6 LPA) and dream (20 LPA) drives.
- **Result**: Status transitions validated. Interview scheduled. Selected student blocked from applying to lower package (`IneligibleStudentException`). Application to dream higher package succeeded.
- **Status**: **PASS**

### Scenario 5: Persistence Across Window Close
- **Action**: Modified student record, invoked save (simulating window close / shutdown hook), simulated application reload.
- **Result**: Updated student record persisted to disk and restored intact without requiring explicit logout.
- **Status**: **PASS**

### Scenario 6: Data Corruption Recovery
- **Action**: Overwrote primary `placement_data.dat` with corrupted garbage bytes and reloaded system.
- **Result**: Primary corruption detected gracefully (`invalid stream header`), system logged warning, recovered database from `placement_data.dat.bak`, and resumed normal operation without crash.
- **Status**: **PASS**

### Scenario 7: Session Isolation Across Logouts
- **Action**: Logged in as one user, logged out, checked UI state across all panels.
- **Result**: `clearSession()` reset input fields, cached entity references, table selections, and models. Previous user data does not bleed into subsequent sessions.
- **Status**: **PASS**

---

## 5. Code Quality and Standards Compliance

1. **Compiler Diagnostics (`javac -Xlint:all`)**:
   - Zero compilation errors.
   - Zero compilation warnings across all packages (`src/com/placementsystem/...` and `test/...`).
2. **Typography and Tone**:
   - Zero em dashes (`—`) in code, UI strings, comments, or documentation.
   - Zero emojis in any file.
   - Human, restrained tone explainable by a second-year engineering student.
3. **Core OOP Architecture (KTU Syllabus)**:
   - `Person` (abstract base class with common fields `name`, `email`, `phone`, abstract method `displayDetails()`).
   - `Student` and `PlacementOfficer` subclasses demonstrating inheritance and polymorphism.
   - `Notifiable` interface implemented by `Student` and `Company`.
   - Custom checked exceptions `IneligibleStudentException` and `DuplicateApplicationException`.
   - Overloaded `scheduleInterview` methods (`(Application, String, String, String)` and `(String, String, String)`).
   - `DataManager` singleton pattern managing centralized state and serialization.
   - `MainFrame` CardLayout controller managing screen navigation.
4. **Testing Harness**:
   - 10 plain Java test suites in `test/PlacementSystemTest.java` (51 assertions).
   - 7 end-to-end scenario tests in `test/ScenarioReviewTest.java` (7 assertions).
   - Convenient runners `test.sh` (macOS/Linux) and `test.bat` (Windows).

---

## 6. Conclusion

The codebase is clean, robust, and fully verified. It functions as an authentic, high-quality college mini-project that satisfies all KTU academic evaluation criteria while maintaining professional software engineering standards.
