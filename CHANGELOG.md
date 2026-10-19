# Changelog

All notable changes to the Placement Management System are documented here.

## [Unreleased] - 2026-10-20

### Step 1: Unified Object Serialization and Persistence Integrity
- Created `PlacementData.java` container holding all system entity lists.
- Refactored `DataManager.java` to serialize the entire object graph to `data/placement_data.dat` in a single stream, preserving reference equality (`==`) across restarts.
- Implemented atomic file saving with temporary file write (`.tmp`) and automatic backup (`.bak`) before overwrite.
- Added automatic recovery from backup on file corruption, with fallback migration from legacy separate `.dat` files.
- Added `syncObjectGraph()` to ensure bidirectional references (`Application.student`, `PlacementDrive.company`, `Interview.application`) are identical in memory.
- Smoke Test: Verified save and reload round-trip, existing test suite passes 16/16.

### Step 2: Centralized Input Validation and Monotonic ID Generation
- Created `Validator.java` with centralized domain validation methods for email regex, phone number formats, USN patterns, CGPA bounds (0.0 to 10.0), backlog ranges, and future date checks.
- Centralized ID generation inside `DataManager` using monotonic numeric increments (`nextCompanyId()`, `nextDriveId()`, `nextApplicationId()`, `nextInterviewId()`) rather than list size offsets.
- Added input normalization (`normalizeEmail`) and defensive validations across registration methods.
- Smoke Test: Verified validation blocks malformed email and invalid USNs.

### Step 3: Drive Date, Package Bounds, and Status Transitions
- Enforced package bounds (0.5 to 150.0 LPA), CGPA bounds, and valid date formatting on `PlacementDrive`. Added `isDeadlinePassed()` check.
- Added `isValidTransition()` state machine to `Application.java` preventing illegal reversals (such as `Rejected -> Selected`).
- Added company rejection tracking (`rejectionReason`) to `Company.java` and `rejectCompany()` to `PlacementOfficer.java`.
- Smoke Test: Verified PlacementDrive and Application state machines enforce constraints.

### Step 4: Company Management and Recruiter Authorization
- Added recruiter authorization check in `CompanyDashboard.java` so recruiters cannot view or modify drives owned by other companies.
- Added drive creation input validation using `Validator`.
- Added company rejection flow in `AdminDashboard.java` allowing officers to reject pending companies with an explanatory reason.
- Added company directory status filter (All, Pending, Approved, Rejected) in `AdminDashboard.java`.
- Added dialog Escape key dismissal handlers.
- Smoke Test: Verified company approval/rejection updates table and status persists.

### Step 5: Placement Pipeline and CSV Reporting
- Added placed students CSV export in `AdminDashboard.java` (USN, name, email, department, company, package, date).
- Added pipeline tracking CSV export in `ApplicationTrackingPanel.java` (Application ID, student USN, student name, company, role, package, status).
- Smoke Test: Verified CSV files export with valid headers and formatted data rows.

### Step 6: Authentication, Security, and Session Lifecycle
- Created `ChangePasswordDialog.java` enabling all three roles (Student, Company, Placement Officer) to update passwords.
- Verified passwords hashed with SHA-256 and unique 16-byte cryptographic salt via `PasswordUtil.java`.
- Enhanced `LoginPanel.java` with Enter key submission, Escape key dismissal on dialogs, and remembered last-used role.
- Added window closing listener and unsaved profile edits confirmation in `MainFrame.java`.
- Implemented explicit session clearing (`clearSession()`) across dashboard panels upon user logout.
- Smoke Test: Verified password update persists, authentication succeeds with new password, and session cleans up cleanly.

### Step 7: Student Drive Discovery, Withdrawal, and Structured Notifications
- Added search and filtering in `StudentProfilePanel.java` (by company name, role, package, and "Show only eligible drives" checkbox).
- Created `DriveEligibilityRenderer` with muted visual style and custom tooltip detailing ineligibility reasons (CGPA cutoff, active backlogs, passed deadline, or existing higher offer).
- Implemented KTU placed student policy: selected students cannot apply to lower/equal package drives, but may apply to dream drives with higher compensation.
- Implemented application withdrawal while status is `Applied`.
- Created structured `Notification.java` with read/unread tracking, header badge, and clear-all capabilities.
- Smoke Test: Verified eligibility filter, tooltips, withdrawal flow, and notification badge updates.

### Step 8: Plain Java Test Harness and Verification
- Expanded `test/PlacementSystemTest.java` to 10 comprehensive test suites with 51 assertions.
- Covers CGPA boundaries, drive eligibility, unapproved company posting, state transitions, regex validations, deadline checks, placed student policy, application withdrawal, password hashing, and unified object serialization with pointer equality (`==`).
- Added `test.sh` and `test.bat` runners.
- Results: 51 / 51 tests pass.
