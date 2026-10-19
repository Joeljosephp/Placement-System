# Placement Management System

A desktop application for managing college campus placement drives, built for the KTU S3 Object-Oriented Programming (OOP) course. It connects students, recruiting companies, and the college placement cell into a single system.

## How to Build and Run

### Requirements
- Java Development Kit (JDK) 8 or newer

### macOS / Linux
```bash
# Make run script executable (first time only)
chmod +x run.sh

# Run the application
./run.sh

# Or run with demo hints on the login screen
./run.sh --demo
```

### Windows
```cmd
# Compile
build.bat

# Run
run.bat
```

### Manual Compilation
```bash
javac -encoding UTF-8 -d bin src/com/placementsystem/*.java src/com/placementsystem/model/*.java src/com/placementsystem/exception/*.java src/com/placementsystem/util/*.java src/com/placementsystem/gui/*.java
java -cp bin com.placementsystem.Main
```

### Running Tests
Plain Java test cases without external dependencies:
```bash
# macOS / Linux
chmod +x test.sh
./test.sh

# Windows
test.bat
```

## Demo Logins

Pre-seeded accounts available on first launch:

- Placement Officer: `admin@ktu.edu` / `admin123`
- Student: `arun@ktu.edu` / `arun123`
- Company: `hr@infosys.com` / `info123`

Passing `--demo` as a command line argument enables demo hints directly in the login window.

## Packages

- `com.placementsystem`: Application launcher (Main) and primary CardLayout window (MainFrame).
- `com.placementsystem.model`: Core entities including Person (abstract base), Student, PlacementOfficer, Company, PlacementDrive, Application, Interview, and the Notifiable interface.
- `com.placementsystem.gui`: Swing interface panels (LoginPanel, StudentProfilePanel, CompanyDashboard, AdminDashboard, ApplicationTrackingPanel) and shared Theme constants.
- `com.placementsystem.util`: DataManager for file-based serialization and PasswordUtil for salted SHA-256 password hashing.
- `com.placementsystem.exception`: Custom checked exceptions (IneligibleStudentException, DuplicateApplicationException).

## Documentation

Detailed design, audit, and examination resources are organized in the [`docs/`](docs/) directory:

- [`docs/VIVA_CHEAT_SHEET.md`](docs/VIVA_CHEAT_SHEET.md): Comprehensive viva defense guide mapping every OOP concept (abstraction, polymorphism, encapsulation, inheritance, exceptions, patterns) to line numbers and examiner questions.
- [`docs/REVIEW.md`](docs/REVIEW.md): Independent code review and end-to-end scenario verification report.
- [`docs/AUDIT.md`](docs/AUDIT.md): Technical audit report detailing bugs, security gaps, and usability findings.
- [`docs/PLAN.md`](docs/PLAN.md): Prioritized implementation plan for refactoring phases.
- [`docs/CHANGELOG.md`](docs/CHANGELOG.md): History of implementation increments and smoke tests.

