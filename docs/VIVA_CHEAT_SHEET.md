# KTU S3 OOP Mini Project - Placement Management System: Viva Cheat Sheet

This guide prepares you to defend every Object-Oriented Programming (OOP) concept, design decision, and edge-case handling in the Placement Management System during academic examination and viva.

---

## 1. OOP Concepts Mapping

### 1. Abstraction
- **File**: `src/com/placementsystem/model/Person.java`
- **Class**: `public abstract class Person implements Serializable`
- **Line / Method**: `public abstract void displayDetails();`
- **Defense Explanation**: `Person` captures high-level identity attributes (`name`, `email`, `phone`) common to any human actor in the system without defining concrete role behavior. Declaring `displayDetails()` as abstract forces every subclass to provide its own role-specific display logic, ensuring high-level code can operate on abstract `Person` instances.

### 2. Inheritance
- **File**: `src/com/placementsystem/model/Student.java` & `src/com/placementsystem/model/PlacementOfficer.java`
- **Class**: `public class Student extends Person` / `public class PlacementOfficer extends Person`
- **Line / Method**: Constructor calls `super(name, email, phone);`
- **Defense Explanation**: Both `Student` and `PlacementOfficer` inherit core demographic state and getter/setter logic from `Person`, eliminating duplicate code. Each subclass extends the parent with role-specific attributes: academic fields (`usn`, `cgpa`, `backlogs`) for students, and administrative fields (`officerId`, `designation`) for officers.

### 3. Encapsulation
- **File**: `src/com/placementsystem/model/Student.java` & `src/com/placementsystem/model/PlacementDrive.java`
- **Class**: `Student` / `PlacementDrive`
- **Line / Method**: `setCgpa(double cgpa)`, `setPackageOffered(double packageOffered)`
- **Defense Explanation**: All instance variables are marked `private` to prevent arbitrary external state mutation. Defensive setter validations enforce business invariants (such as rejecting negative backlogs, bounding CGPA to the KTU `[0.0, 10.0]` scale, and restricting compensation to `[0.5, 150.0]` LPA) before accepting updates.

### 4. Polymorphism (Runtime / Dynamic Method Dispatch)
- **File**: `src/com/placementsystem/model/Person.java` & `Student.java` / `PlacementOfficer.java`
- **Class**: `Student` overrides `displayDetails()` and `PlacementOfficer` overrides `displayDetails()`
- **Line / Method**: `@Override public void displayDetails()`
- **Defense Explanation**: When a heterogeneous collection or reference of type `Person` is iterated, calling `person.displayDetails()` dynamically dispatches at runtime to the concrete implementation of `Student` or `PlacementOfficer`. This decouples caller logic from concrete class types.

### 5. Interfaces
- **File**: `src/com/placementsystem/model/Notifiable.java`
- **Class**: `public interface Notifiable`
- **Line / Method**: `void sendNotification(String message);`
- **Defense Explanation**: Represents an asynchronous event recipient contract implemented by both `Student` and `Company`, even though they do not share an inheritance hierarchy. The placement engine can notify any applicant or recruiting firm without knowing anything about their internal storage models.

### 6. Custom Checked Exceptions
- **File**: `src/com/placementsystem/exception/IneligibleStudentException.java` & `DuplicateApplicationException.java`
- **Class**: `public class IneligibleStudentException extends Exception`
- **Line / Method**: Thrown in `DataManager.applyToDrive(Student student, PlacementDrive drive)`
- **Defense Explanation**: Extending checked `Exception` (rather than unchecked `RuntimeException`) forces caller code in the Swing UI controller to anticipate and handle critical domain rejections at compile time. This prevents unhandled runtime crashes and guarantees clear user feedback when rules are violated.

### 7. Method Overloading (Compile-Time / Static Polymorphism)
- **File**: `src/com/placementsystem/util/DataManager.java`
- **Class**: `DataManager`
- **Line / Method**: 
  - `public void scheduleInterview(Application application, String roundName, String result, String interviewDate)`
  - `public void scheduleInterview(String applicationId, String roundName, String interviewDate)`
- **Defense Explanation**: Provides flexible entry points for scheduling interview rounds depending on whether the caller possesses an in-memory `Application` reference or only an identifier string. The Java compiler resolves the appropriate method signature at compile time based on the argument list.

### 8. Method Overriding
- **File**: `src/com/placementsystem/model/Application.java` & `Student.java`
- **Class**: `Application` overrides `toString()`, `Student` overrides `displayDetails()`
- **Line / Method**: `@Override public String toString()`
- **Defense Explanation**: Subclasses redefine the default behavior inherited from parent classes (like `java.lang.Object` or `Person`). In `Application`, overriding `toString()` returns a human-readable identifier and status string suitable for Swing list renderers and logging.

### 9. Singleton Design Pattern
- **File**: `src/com/placementsystem/util/DataManager.java`
- **Class**: `public class DataManager`
- **Line / Method**: `private static DataManager instance;` and `public static synchronized DataManager getInstance()`
- **Defense Explanation**: Guarantees that only one central database manager exists throughout the JVM lifecycle with a private constructor and lazy initialization. This prevents duplicate in-memory data representations and ensures synchronized file access across different UI panels.

### 10. CardLayout Controller Architecture
- **File**: `src/com/placementsystem/MainFrame.java`
- **Class**: `public class MainFrame extends JFrame`
- **Line / Method**: `private CardLayout cardLayout;` and `public void showCard(String cardName)`
- **Defense Explanation**: Implements a Single Document Interface (SDI) where screens are stacked on a single parent frame and flipped programmatically. This avoids spawning multiple untracked `JFrame` windows on the operating system desktop, preserves window dimensions, and prevents orphaned dialogs.

### 11. Object Serialization & Single-Stream Object Graph
- **File**: `src/com/placementsystem/model/PlacementData.java` & `src/com/placementsystem/util/DataManager.java`
- **Class**: `PlacementData` serialized via `ObjectOutputStream`
- **Line / Method**: `writeAtomicPlacementData()` & `syncObjectGraph()`
- **Defense Explanation**: Serializing all lists together inside a unified container object preserves the Java Serialization handle table. References between entities (`Application.student -> Student`, `PlacementDrive.company -> Company`) maintain pointer equality (`==`) upon deserialization, preventing stale data duplication.

### 12. Collections Framework
- **File**: `src/com/placementsystem/util/DataManager.java`
- **Class**: `DataManager`
- **Line / Method**: `List<Student>`, `Map<String, Student>`, `Set<String>`
- **Defense Explanation**: Employs `ArrayList` for ordered, indexed entity storage, `HashMap` in `syncObjectGraph()` for \(O(1)\) canonical entity resolution, and `HashSet` in `getPlacedStudentsCount()` to compute deduplicated placed students across multiple offers.

---

## 2. Common Viva Traps and How to Answer Them

### Trap 1: "Why did you use Java Serialization instead of SQLite / JDBC?"
**Examiner's Angle**: Checking if you know real-world database concepts vs. syllabus constraints.  
**Best Answer**:  
> "The KTU S3 Object-Oriented Programming curriculum specifically evaluates Java Standard Edition core competencies: abstract classes, inheritance, interfaces, custom checked exceptions, and Java File I/O (`java.io.Serializable`, `ObjectOutputStream`, `ObjectInputStream`) without third-party dependencies or external drivers.  
> Furthermore, Java Serialization natively preserves the entire in-memory object graph: bi-directional object pointers (such as `Application` referencing `Student`) are serialized with reference equality (`==`) preserved, eliminating the need for an external Object-Relational Mapping (ORM) layer."

### Trap 2: "What happens if two users log in at the same time?"
**Examiner's Angle**: Checking concurrency awareness and desktop application architecture.  
**Best Answer**:  
> "This is a single-user desktop Swing application operating within a single JVM process. User authentication switches the active session role inside `MainFrame`'s `CardLayout` controller.  
> However, to prevent file corruption from background asynchronous threads or concurrent writes, `DataManager` encapsulates all serialization operations behind a dedicated `fileLock` object with atomic `.tmp` temporary file staging and `.bak` backup copies."

### Trap 3: "Why is DataManager a singleton and could it be a problem?"
**Examiner's Angle**: Checking design pattern strengths and unit testability drawbacks.  
**Best Answer**:  
> "The Singleton pattern ensures there is only one source of truth in memory, preventing multiple UI panels from modifying desynchronized copies of students or drives.  
> A classic drawback of Singletons in enterprise software is tight coupling during automated testing. To solve this, our `DataManager` exposes controlled testing hooks (`resetForTesting()` and `reloadForTesting()`), allowing our test suite to reset state between test cases without compromising runtime singleton integrity."

### Trap 4: "How do you ensure data integrity without a database?"
**Examiner's Angle**: Checking durability, atomicity, and recovery.  
**Best Answer**:  
> "We implement a three-tier reliability strategy:  
> 1. **Atomic File Replacement**: New state is written to `data/placement_data.dat.tmp` first; only upon successful write and flush is it atomically renamed over `data/placement_data.dat`.  
> 2. **Automated Backup**: Before overwriting the primary file, a shadow backup `data/placement_data.dat.bak` is generated.  
> 3. **Self-Healing Startup**: If `readPlacementData()` detects a corrupted or truncated primary file (`StreamCorruptedException`), it automatically falls back to the `.bak` file, logs a recovery warning, and continues without crashing."

### Trap 5: "Why CardLayout instead of opening multiple JFrames?"
**Examiner's Angle**: Checking Swing UI lifecycle and memory management.  
**Best Answer**:  
> "Spawning separate `JFrame` windows consumes excessive operating system window handles, leads to disjoint taskbar icons, and causes memory leaks if windows are closed with `DISPOSE_ON_CLOSE` while holding listener references.  
> `CardLayout` manages all dashboards within a single top-level window. When a user logs out, `clearSession()` explicitly cleans panel fields and table models, preventing sensitive data from bleeding across user sessions."

### Trap 6: "How does password hashing with salt protect users?"
**Examiner's Angle**: Checking cybersecurity fundamentals.  
**Best Answer**:  
> "Passwords are never stored in plain text. When a user registers or updates their password, `PasswordUtil` generates a cryptographically random 16-byte salt via `SecureRandom` and computes a SHA-256 hash `(password + salt)`.  
> The salt defends against precomputed rainbow table attacks and dictionary attacks, ensuring that two users with the identical password will have completely distinct hash values in storage."

### Trap 7: "What is the KTU placed-student policy in your code?"
**Examiner's Angle**: Checking business domain logic and state machine enforcement.  
**Best Answer**:  
> "In college placements, a placed student should not hoard job offers from unplaced peers. Our policy specifies:  
> 1. Once a candidate is marked `Selected` for a drive, they are blocked from applying to any placement drive offering an equal or lower package.  
> 2. They are only eligible to apply for a 'Dream Option' drive if the offered package is strictly higher than their current offer.  
> This rule is enforced in `DataManager.applyToDrive()` by checking existing offers before creating an application."
