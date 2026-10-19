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
