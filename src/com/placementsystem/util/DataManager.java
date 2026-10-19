package com.placementsystem.util;

import com.placementsystem.exception.DuplicateApplicationException;
import com.placementsystem.exception.IneligibleStudentException;
import com.placementsystem.model.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DataManager {
    private static DataManager instance;

    private static final String DATA_DIR = "./data";
    private static final String UNIFIED_DATA_FILE = DATA_DIR + "/placement_data.dat";
    private static final String BACKUP_DATA_FILE = DATA_DIR + "/placement_data.dat.bak";
    private static final String TEMP_DATA_FILE = DATA_DIR + "/placement_data.dat.tmp";

    // Legacy individual file paths for automatic migration fallback
    private static final String LEGACY_STUDENTS_FILE = DATA_DIR + "/students.dat";
    private static final String LEGACY_COMPANIES_FILE = DATA_DIR + "/companies.dat";
    private static final String LEGACY_DRIVES_FILE = DATA_DIR + "/drives.dat";
    private static final String LEGACY_APPLICATIONS_FILE = DATA_DIR + "/applications.dat";
    private static final String LEGACY_OFFICERS_FILE = DATA_DIR + "/officers.dat";
    private static final String LEGACY_INTERVIEWS_FILE = DATA_DIR + "/interviews.dat";

    private final Object fileLock = new Object();

    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "DataManager-Save-Worker");
        t.setDaemon(true);
        return t;
    });

    private List<Student> students;
    private List<Company> companies;
    private List<PlacementDrive> drives;
    private List<Application> applications;
    private List<PlacementOfficer> officers;
    private List<Interview> interviews;

    private DataManager() {
        this.students = new ArrayList<>();
        this.companies = new ArrayList<>();
        this.drives = new ArrayList<>();
        this.applications = new ArrayList<>();
        this.officers = new ArrayList<>();
        this.interviews = new ArrayList<>();

        initializeStorage();
        loadAll();
    }

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    private void initializeStorage() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public synchronized void loadAll() {
        PlacementData data = readPlacementData();
        if (data != null) {
            this.students = data.getStudents();
            this.companies = data.getCompanies();
            this.drives = data.getDrives();
            this.applications = data.getApplications();
            this.officers = data.getOfficers();
            this.interviews = data.getInterviews();
            syncObjectGraph();
        } else {
            loadFromLegacyFiles();
        }

        seedDefaultOfficerIfEmpty();
        seedSampleDataIfEmpty();

        // Write initial unified file
        saveAll();
    }

    private PlacementData readPlacementData() {
        synchronized (fileLock) {
            File primary = new File(UNIFIED_DATA_FILE);
            if (primary.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(primary))) {
                    Object obj = ois.readObject();
                    if (obj instanceof PlacementData) {
                        return (PlacementData) obj;
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Primary data file corrupted or unreadable: " + e.getMessage());
                }
            }

            // Try backup file if primary is missing or corrupted
            File backup = new File(BACKUP_DATA_FILE);
            if (backup.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(backup))) {
                    Object obj = ois.readObject();
                    if (obj instanceof PlacementData) {
                        System.out.println("Recovered database from backup: " + BACKUP_DATA_FILE);
                        return (PlacementData) obj;
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Backup data file also unreadable: " + e.getMessage());
                }
            }

            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> readLegacyList(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                return (List<T>) obj;
            }
        } catch (Exception e) {
            System.err.println("Error reading legacy file " + filePath + ": " + e.getMessage());
        }
        return new ArrayList<>();
    }

    private void loadFromLegacyFiles() {
        this.students = readLegacyList(LEGACY_STUDENTS_FILE);
        this.companies = readLegacyList(LEGACY_COMPANIES_FILE);
        this.drives = readLegacyList(LEGACY_DRIVES_FILE);
        this.applications = readLegacyList(LEGACY_APPLICATIONS_FILE);
        this.officers = readLegacyList(LEGACY_OFFICERS_FILE);
        this.interviews = readLegacyList(LEGACY_INTERVIEWS_FILE);

        syncObjectGraph();
    }

    /**
     * Resolves object identity by connecting Application, PlacementDrive, Student, Company,
     * and Interview references so that all entities share identical memory references.
     */
    private void syncObjectGraph() {
        Map<String, Student> studentMap = new HashMap<>();
        for (Student s : students) {
            if (s != null && s.getUsn() != null) {
                studentMap.put(s.getUsn().toUpperCase().trim(), s);
            }
        }

        Map<String, Company> companyMap = new HashMap<>();
        for (Company c : companies) {
            if (c != null && c.getCompanyId() != null) {
                companyMap.put(c.getCompanyId().toUpperCase().trim(), c);
            }
        }

        Map<String, PlacementDrive> driveMap = new HashMap<>();
        for (PlacementDrive d : drives) {
            if (d != null && d.getDriveId() != null) {
                driveMap.put(d.getDriveId().toUpperCase().trim(), d);
                if (d.getCompany() != null && d.getCompany().getCompanyId() != null) {
                    Company canonical = companyMap.get(d.getCompany().getCompanyId().toUpperCase().trim());
                    if (canonical != null) {
                        d.setCompany(canonical);
                    }
                }
            }
        }

        Map<String, Application> appMap = new HashMap<>();
        for (Application a : applications) {
            if (a != null && a.getApplicationId() != null) {
                appMap.put(a.getApplicationId().toUpperCase().trim(), a);
                if (a.getStudent() != null && a.getStudent().getUsn() != null) {
                    Student canonical = studentMap.get(a.getStudent().getUsn().toUpperCase().trim());
                    if (canonical != null) {
                        a.setStudent(canonical);
                    }
                }
                if (a.getDrive() != null && a.getDrive().getDriveId() != null) {
                    PlacementDrive canonical = driveMap.get(a.getDrive().getDriveId().toUpperCase().trim());
                    if (canonical != null) {
                        a.setDrive(canonical);
                    }
                }
            }
        }

        for (PlacementDrive d : drives) {
            List<Application> canonicalApplicants = new ArrayList<>();
            for (Application a : d.getApplicants()) {
                if (a != null && a.getApplicationId() != null) {
                    Application canonical = appMap.get(a.getApplicationId().toUpperCase().trim());
                    if (canonical != null) {
                        canonicalApplicants.add(canonical);
                    }
                }
            }
            d.setApplicants(canonicalApplicants);
        }

        for (Interview inv : interviews) {
            if (inv != null && inv.getApplication() != null && inv.getApplication().getApplicationId() != null) {
                Application canonical = appMap.get(inv.getApplication().getApplicationId().toUpperCase().trim());
                if (canonical != null) {
                    inv.setApplication(canonical);
                }
            }
        }
    }

    private void seedDefaultOfficerIfEmpty() {
        if (officers.isEmpty()) {
            PlacementOfficer defaultAdmin = new PlacementOfficer(
                    "OFF001",
                    "Prof. Placement Officer",
                    "admin@ktu.edu",
                    "+91 9447001122",
                    "Chief Placement Coordinator",
                    "admin123"
            );
            officers.add(defaultAdmin);
            saveAll();
        }
    }

    private void seedSampleDataIfEmpty() {
        if (students.isEmpty() && companies.isEmpty() && drives.isEmpty()) {
            Student s1 = new Student("TVE22CS001", "Arun Kumar", "arun@ktu.edu", "9876543210", "Computer Science", 8.75, 0, "arun123");
            Student s2 = new Student("TVE22CS045", "Diya Menon", "diya@ktu.edu", "9876543211", "Computer Science", 9.10, 0, "diya123");
            Student s3 = new Student("TVE22EC012", "Rahul Varma", "rahul@ktu.edu", "9876543212", "Electronics & Comm.", 6.80, 1, "rahul123");
            students.add(s1);
            students.add(s2);
            students.add(s3);

            Company c1 = new Company("CMP101", "Infosys Limited", "Information Technology", "hr@infosys.com", "info123");
            c1.setApproved(true);
            Company c2 = new Company("CMP102", "Google Cloud", "Software & Cloud Services", "careers@google.com", "google123");
            c2.setApproved(true);
            Company c3 = new Company("CMP103", "Innovate AI Labs", "Artificial Intelligence", "recruit@innovate.ai", "innovate123");
            c3.setApproved(false);
            companies.add(c1);
            companies.add(c2);
            companies.add(c3);

            PlacementDrive d1 = new PlacementDrive("DRV201", c1, "Systems Engineer", 4.5, 6.5, 1, "2026-11-15");
            PlacementDrive d2 = new PlacementDrive("DRV202", c2, "Associate Software Engineer", 18.0, 8.5, 0, "2026-11-20");
            drives.add(d1);
            drives.add(d2);

            Application app1 = new Application("APP301", s1, d2);
            app1.updateStatus("Shortlisted");
            applications.add(app1);
            d2.getApplicants().add(app1);

            Interview int1 = new Interview("INT401", app1, "Round 1 - Technical Coding", "Cleared", "2026-11-22 10:00 AM");
            interviews.add(int1);

            saveAll();
        }
    }

    private void writeAtomicPlacementData(PlacementData data) {
        synchronized (fileLock) {
            File tempFile = new File(TEMP_DATA_FILE);
            File destFile = new File(UNIFIED_DATA_FILE);
            File backupFile = new File(BACKUP_DATA_FILE);

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
                oos.writeObject(data);
                oos.flush();
            } catch (Exception e) {
                System.err.println("Error writing temp data file: " + e.getMessage());
                return;
            }

            if (destFile.exists()) {
                copyFile(destFile, backupFile);
            }

            if (!tempFile.renameTo(destFile)) {
                copyFile(tempFile, destFile);
                tempFile.delete();
            }
        }
    }

    private void copyFile(File src, File dest) {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException ignored) {
        }
    }

    public synchronized void saveAll() {
        synchronized (fileLock) {
            PlacementData data = new PlacementData(
                    new ArrayList<>(students),
                    new ArrayList<>(companies),
                    new ArrayList<>(drives),
                    new ArrayList<>(applications),
                    new ArrayList<>(officers),
                    new ArrayList<>(interviews)
            );
            writeAtomicPlacementData(data);
        }
    }

    public void saveAllAsync() {
        saveExecutor.submit(this::saveAll);
    }

    public synchronized void saveStudents() {
        saveAll();
    }

    public synchronized void saveCompanies() {
        saveAll();
    }

    public synchronized void saveDrives() {
        saveAll();
    }

    public synchronized void saveApplications() {
        saveAll();
    }

    public synchronized void saveOfficers() {
        saveAll();
    }

    public synchronized void saveInterviews() {
        saveAll();
    }

    // Monotonic ID Generators
    public synchronized String nextCompanyId() {
        int maxId = 100;
        for (Company c : companies) {
            if (c != null && c.getCompanyId() != null && c.getCompanyId().toUpperCase().startsWith("CMP")) {
                try {
                    int val = Integer.parseInt(c.getCompanyId().substring(3).trim());
                    if (val > maxId) maxId = val;
                } catch (NumberFormatException ignored) {}
            }
        }
        return "CMP" + (maxId + 1);
    }

    public synchronized String nextDriveId() {
        int maxId = 200;
        for (PlacementDrive d : drives) {
            if (d != null && d.getDriveId() != null && d.getDriveId().toUpperCase().startsWith("DRV")) {
                try {
                    int val = Integer.parseInt(d.getDriveId().substring(3).trim());
                    if (val > maxId) maxId = val;
                } catch (NumberFormatException ignored) {}
            }
        }
        return "DRV" + (maxId + 1);
    }

    public synchronized String nextApplicationId() {
        int maxId = 300;
        for (Application a : applications) {
            if (a != null && a.getApplicationId() != null && a.getApplicationId().toUpperCase().startsWith("APP")) {
                try {
                    int val = Integer.parseInt(a.getApplicationId().substring(3).trim());
                    if (val > maxId) maxId = val;
                } catch (NumberFormatException ignored) {}
            }
        }
        return "APP" + (maxId + 1);
    }

    public synchronized String nextInterviewId() {
        int maxId = 400;
        for (Interview inv : interviews) {
            if (inv != null && inv.getInterviewId() != null && inv.getInterviewId().toUpperCase().startsWith("INT")) {
                try {
                    int val = Integer.parseInt(inv.getInterviewId().substring(3).trim());
                    if (val > maxId) maxId = val;
                } catch (NumberFormatException ignored) {}
            }
        }
        return "INT" + (maxId + 1);
    }

    public synchronized Student authenticateStudent(String email, String password) {
        String normalized = Validator.normalizeEmail(email);
        for (Student s : students) {
            if (Validator.normalizeEmail(s.getEmail()).equalsIgnoreCase(normalized) && s.checkPassword(password)) {
                return s;
            }
        }
        return null;
    }

    public synchronized Company authenticateCompany(String email, String password) {
        String normalized = Validator.normalizeEmail(email);
        for (Company c : companies) {
            if (Validator.normalizeEmail(c.getEmail()).equalsIgnoreCase(normalized) && c.checkPassword(password)) {
                return c;
            }
        }
        return null;
    }

    public synchronized PlacementOfficer authenticateOfficer(String email, String password) {
        String normalized = Validator.normalizeEmail(email);
        for (PlacementOfficer po : officers) {
            if (Validator.normalizeEmail(po.getEmail()).equalsIgnoreCase(normalized) && po.checkPassword(password)) {
                return po;
            }
        }
        return null;
    }

    public synchronized List<Student> getStudents() {
        return new ArrayList<>(students);
    }

    public synchronized void addStudent(Student s) {
        if (s == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }
        String normalizedEmail = Validator.normalizeEmail(s.getEmail());
        String normalizedUsn = s.getUsn() != null ? s.getUsn().trim() : "";

        if (!Validator.isValidEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (!Validator.isValidUsn(normalizedUsn)) {
            throw new IllegalArgumentException("Invalid USN format.");
        }

        for (Student existing : students) {
            if (Validator.normalizeEmail(existing.getEmail()).equalsIgnoreCase(normalizedEmail)) {
                throw new IllegalArgumentException("Student with this email address already exists.");
            }
            if (existing.getUsn() != null && existing.getUsn().equalsIgnoreCase(normalizedUsn)) {
                throw new IllegalArgumentException("Student with this USN already exists.");
            }
        }

        s.setEmail(normalizedEmail);
        s.setUsn(normalizedUsn);
        students.add(s);
        saveStudents();
    }

    public synchronized void updateStudent(Student s) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getUsn().equalsIgnoreCase(s.getUsn())) {
                students.set(i, s);
                saveStudents();
                return;
            }
        }
    }

    public synchronized List<Company> getCompanies() {
        return new ArrayList<>(companies);
    }

    public synchronized void addCompany(Company c) {
        if (c == null) {
            throw new IllegalArgumentException("Company cannot be null.");
        }
        String normalizedEmail = Validator.normalizeEmail(c.getEmail());
        String normalizedId = c.getCompanyId() != null ? c.getCompanyId().trim() : "";

        if (!Validator.isValidEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Invalid company email format.");
        }

        for (Company existing : companies) {
            if (Validator.normalizeEmail(existing.getEmail()).equalsIgnoreCase(normalizedEmail)) {
                throw new IllegalArgumentException("Company with this email address already exists.");
            }
            if (existing.getCompanyId() != null && existing.getCompanyId().equalsIgnoreCase(normalizedId)) {
                throw new IllegalArgumentException("Company with this ID already exists.");
            }
        }

        c.setEmail(normalizedEmail);
        companies.add(c);
        saveCompanies();
    }

    public synchronized void updateCompany(Company c) {
        for (int i = 0; i < companies.size(); i++) {
            if (companies.get(i).getCompanyId().equalsIgnoreCase(c.getCompanyId())) {
                companies.set(i, c);
                saveCompanies();
                return;
            }
        }
    }

    public synchronized List<Company> getPendingCompanies() {
        List<Company> pending = new ArrayList<>();
        for (Company c : companies) {
            if (!c.isApproved() && !c.isRejected()) {
                pending.add(c);
            }
        }
        return pending;
    }

    public synchronized List<Company> getApprovedCompanies() {
        List<Company> approved = new ArrayList<>();
        for (Company c : companies) {
            if (c.isApproved()) {
                approved.add(c);
            }
        }
        return approved;
    }

    public synchronized List<Company> getRejectedCompanies() {
        List<Company> rejected = new ArrayList<>();
        for (Company c : companies) {
            if (c.isRejected()) {
                rejected.add(c);
            }
        }
        return rejected;
    }

    public synchronized List<PlacementDrive> getDrives() {
        return new ArrayList<>(drives);
    }

    public synchronized List<PlacementDrive> getDrivesForCompany(Company c) {
        List<PlacementDrive> list = new ArrayList<>();
        if (c == null) return list;
        for (PlacementDrive d : drives) {
            if (d.getCompany() != null && d.getCompany().getCompanyId().equalsIgnoreCase(c.getCompanyId())) {
                list.add(d);
            }
        }
        return list;
    }

    public synchronized void addPlacementDrive(PlacementDrive drive) {
        if (drive.getCompany() == null || !drive.getCompany().isApproved()) {
            throw new IllegalStateException("Company is not approved. Cannot post placement drive.");
        }
        drives.add(drive);
        saveDrives();
    }

    public synchronized void updatePlacementDrive(PlacementDrive drive) {
        for (int i = 0; i < drives.size(); i++) {
            if (drives.get(i).getDriveId().equalsIgnoreCase(drive.getDriveId())) {
                drives.set(i, drive);
                saveDrives();
                return;
            }
        }
    }

    public synchronized List<Application> getApplications() {
        return new ArrayList<>(applications);
    }

    public synchronized List<Application> getApplicationsForStudent(Student s) {
        List<Application> list = new ArrayList<>();
        if (s == null) return list;
        for (Application a : applications) {
            if (a.getStudent() != null && a.getStudent().getUsn().equalsIgnoreCase(s.getUsn())) {
                list.add(a);
            }
        }
        return list;
    }

    public synchronized List<Application> getApplicationsForDrive(PlacementDrive d) {
        List<Application> list = new ArrayList<>();
        if (d == null) return list;
        for (Application a : applications) {
            if (a.getDrive() != null && a.getDrive().getDriveId().equalsIgnoreCase(d.getDriveId())) {
                list.add(a);
            }
        }
        return list;
    }

    /**
     * Finds any active job offer for the student.
     */
    public synchronized Application getActiveOfferForStudent(Student student) {
        if (student == null) return null;
        for (Application a : applications) {
            if (a.getStudent() != null && a.getStudent().getUsn().equalsIgnoreCase(student.getUsn())
                    && "Selected".equalsIgnoreCase(a.getStatus())) {
                return a;
            }
        }
        return null;
    }

    /**
     * Applies student to a drive. Enforces:
     * 1. Drive deadline check.
     * 2. Eligibility criteria (CGPA and backlogs).
     * 3. Single offer / dream drive policy.
     * 4. Duplicate application check.
     */
    public synchronized void applyToDrive(Student student, PlacementDrive drive)
            throws IneligibleStudentException, DuplicateApplicationException {
        if (student == null || drive == null) {
            throw new IllegalArgumentException("Student and Drive must not be null.");
        }

        // 1. Deadline Check
        if (drive.isDeadlinePassed()) {
            throw new IneligibleStudentException("Application deadline for this drive (" + drive.getDriveDate() + ") has already passed.");
        }

        // 2. Eligibility Check
        if (!drive.checkEligibility(student)) {
            throw new IneligibleStudentException(
                    "Eligibility criteria not met. Drive requires minimum CGPA of "
                            + drive.getEligibleCGPA() + " and max " + drive.getMaxBacklogsAllowed()
                            + " backlogs. Student has CGPA: " + student.getCgpa()
                            + " and " + student.getBacklogs() + " backlogs."
            );
        }

        // 3. KTU Placement Policy: Single offer lock / Dream drive rule
        Application activeOffer = getActiveOfferForStudent(student);
        if (activeOffer != null && activeOffer.getDrive() != null) {
            double currentPackage = activeOffer.getDrive().getPackageOffered();
            if (drive.getPackageOffered() <= currentPackage) {
                throw new IneligibleStudentException(
                        "Placement Policy: You have already been selected for an offer ("
                                + activeOffer.getDrive().getRole() + " at "
                                + activeOffer.getDrive().getCompany().getCompanyName()
                                + " with " + currentPackage + " LPA). You can only apply to dream drives with a higher package."
                );
            }
        }

        // 4. Duplicate Application Check
        for (Application existing : applications) {
            if (existing.getStudent() != null && existing.getDrive() != null
                    && existing.getStudent().getUsn().equalsIgnoreCase(student.getUsn())
                    && existing.getDrive().getDriveId().equalsIgnoreCase(drive.getDriveId())) {
                throw new DuplicateApplicationException(
                        "You have already applied for " + drive.getRole() + " at "
                                + drive.getCompany().getCompanyName() + "."
                );
            }
        }

        // 5. Create Application
        String appId = nextApplicationId();
        Application newApp = new Application(appId, student, drive);
        applications.add(newApp);
        drive.getApplicants().add(newApp);

        if (drive.getCompany() != null) {
            drive.getCompany().sendNotification("New application received from " + student.getName() + " for role: " + drive.getRole());
        }

        saveApplications();
        saveDrives();
    }

    public synchronized void withdrawApplication(Student student, Application app) {
        if (student == null || app == null) {
            throw new IllegalArgumentException("Student and Application cannot be null.");
        }
        if (!"Applied".equalsIgnoreCase(app.getStatus())) {
            throw new IllegalStateException("Only applications in 'Applied' status can be withdrawn.");
        }

        applications.removeIf(a -> a.getApplicationId().equalsIgnoreCase(app.getApplicationId()));
        if (app.getDrive() != null) {
            app.getDrive().getApplicants().removeIf(a -> a.getApplicationId().equalsIgnoreCase(app.getApplicationId()));
            if (app.getDrive().getCompany() != null) {
                app.getDrive().getCompany().sendNotification(
                        "Candidate " + student.getName() + " has withdrawn their application for " + app.getDrive().getRole()
                );
            }
        }

        saveApplications();
        saveDrives();
    }

    public synchronized void updateApplicationStatus(Application app, String newStatus) {
        app.updateStatus(newStatus);
        for (int i = 0; i < applications.size(); i++) {
            if (applications.get(i).getApplicationId().equalsIgnoreCase(app.getApplicationId())) {
                applications.set(i, app);
                break;
            }
        }
        saveApplications();
        saveStudents();
    }

    public synchronized List<Interview> getInterviews() {
        return new ArrayList<>(interviews);
    }

    public synchronized void addInterview(Interview interview) {
        if (interview == null || interview.getApplication() == null) {
            throw new IllegalArgumentException("Interview and Application cannot be null.");
        }

        if ("Rejected".equalsIgnoreCase(interview.getApplication().getStatus())) {
            throw new IllegalStateException("Cannot schedule an interview for a rejected candidate.");
        }

        // Check for duplicate round name for the same application
        for (Interview existing : interviews) {
            if (existing.getApplication() != null
                    && existing.getApplication().getApplicationId().equalsIgnoreCase(interview.getApplication().getApplicationId())
                    && existing.getRoundName().equalsIgnoreCase(interview.getRoundName().trim())) {
                throw new IllegalArgumentException("An interview round with name '" + interview.getRoundName() + "' is already scheduled for this application.");
            }
        }

        interviews.add(interview);
        saveInterviews();

        if (interview.getApplication().getStudent() != null) {
            interview.getApplication().getStudent().sendNotification(
                    "Interview Scheduled: " + interview.getRoundName() + " on " + interview.getInterviewDate()
            );
            saveStudents();
        }
    }

    public synchronized void updateInterview(Interview interview) {
        for (int i = 0; i < interviews.size(); i++) {
            if (interviews.get(i).getInterviewId().equalsIgnoreCase(interview.getInterviewId())) {
                interviews.set(i, interview);
                saveInterviews();
                return;
            }
        }
    }

    public synchronized List<PlacementOfficer> getOfficers() {
        return new ArrayList<>(officers);
    }

    public synchronized int getTotalStudentsCount() {
        return students.size();
    }

    /**
     * Returns the distinct count of placed students.
     */
    public synchronized int getPlacedStudentsCount() {
        Set<String> placedUsns = new HashSet<>();
        for (Application a : applications) {
            if ("Selected".equalsIgnoreCase(a.getStatus()) && a.getStudent() != null && a.getStudent().getUsn() != null) {
                placedUsns.add(a.getStudent().getUsn().toUpperCase().trim());
            }
        }
        return placedUsns.size();
    }

    public synchronized int getTotalCompaniesCount() {
        return companies.size();
    }

    public synchronized int getApprovedCompaniesCount() {
        return getApprovedCompanies().size();
    }

    public synchronized int getTotalDrivesCount() {
        return drives.size();
    }

    public synchronized int getTotalApplicationsCount() {
        return applications.size();
    }

    public synchronized double getAverageCGPA() {
        if (students.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Student s : students) {
            sum += s.getCgpa();
        }
        return Math.round((sum / students.size()) * 100.0) / 100.0;
    }

    public synchronized void resetForTesting() {
        this.students.clear();
        this.companies.clear();
        this.drives.clear();
        this.applications.clear();
        this.officers.clear();
        this.interviews.clear();
        seedDefaultOfficerIfEmpty();
        seedSampleDataIfEmpty();
        saveAll();
    }

    public void shutdown() {
        try {
            saveAll();
            saveExecutor.shutdown();
            saveExecutor.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
