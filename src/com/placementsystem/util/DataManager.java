package com.placementsystem.util;

import com.placementsystem.exception.DuplicateApplicationException;
import com.placementsystem.exception.IneligibleStudentException;
import com.placementsystem.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DataManager {
    private static DataManager instance;

    private static final String DATA_DIR = "./data";
    private static final String STUDENTS_FILE = DATA_DIR + "/students.dat";
    private static final String COMPANIES_FILE = DATA_DIR + "/companies.dat";
    private static final String DRIVES_FILE = DATA_DIR + "/drives.dat";
    private static final String APPLICATIONS_FILE = DATA_DIR + "/applications.dat";
    private static final String OFFICERS_FILE = DATA_DIR + "/officers.dat";
    private static final String INTERVIEWS_FILE = DATA_DIR + "/interviews.dat";

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
            saveOfficers();
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
            saveStudents();

            Company c1 = new Company("CMP101", "Infosys Limited", "Information Technology", "hr@infosys.com", "info123");
            c1.setApproved(true);
            Company c2 = new Company("CMP102", "Google Cloud", "Software & Cloud Services", "careers@google.com", "google123");
            c2.setApproved(true);
            Company c3 = new Company("CMP103", "Innovate AI Labs", "Artificial Intelligence", "recruit@innovate.ai", "innovate123");
            c3.setApproved(false);
            companies.add(c1);
            companies.add(c2);
            companies.add(c3);
            saveCompanies();

            PlacementDrive d1 = new PlacementDrive("DRV201", c1, "Systems Engineer", 4.5, 6.5, 1, "2026-11-15");
            PlacementDrive d2 = new PlacementDrive("DRV202", c2, "Associate Software Engineer", 18.0, 8.5, 0, "2026-11-20");
            drives.add(d1);
            drives.add(d2);
            saveDrives();

            Application app1 = new Application("APP301", s1, d2);
            app1.updateStatus("Shortlisted");
            applications.add(app1);
            d2.getApplicants().add(app1);
            saveApplications();

            Interview int1 = new Interview("INT401", app1, "Round 1 - Technical Coding", "Cleared", "2026-11-22 10:00 AM");
            interviews.add(int1);
            saveInterviews();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> readListFromFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                return (List<T>) obj;
            }
        } catch (EOFException e) {
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error reading " + filePath + ": " + e.getMessage());
        }
        return new ArrayList<>();
    }

    private <T> void writeListToFile(String filePath, List<T> list) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(list);
            oos.flush();
        } catch (Exception e) {
            System.err.println("Error writing to " + filePath + ": " + e.getMessage());
        }
    }

    public synchronized void loadAll() {
        this.students = readListFromFile(STUDENTS_FILE);
        this.companies = readListFromFile(COMPANIES_FILE);
        this.drives = readListFromFile(DRIVES_FILE);
        this.applications = readListFromFile(APPLICATIONS_FILE);
        this.officers = readListFromFile(OFFICERS_FILE);
        this.interviews = readListFromFile(INTERVIEWS_FILE);

        seedDefaultOfficerIfEmpty();
        seedSampleDataIfEmpty();
    }

    // Synchronous save for shutdown hook or explicit flushes
    public synchronized void saveAll() {
        writeListToFile(STUDENTS_FILE, new ArrayList<>(students));
        writeListToFile(COMPANIES_FILE, new ArrayList<>(companies));
        writeListToFile(DRIVES_FILE, new ArrayList<>(drives));
        writeListToFile(APPLICATIONS_FILE, new ArrayList<>(applications));
        writeListToFile(OFFICERS_FILE, new ArrayList<>(officers));
        writeListToFile(INTERVIEWS_FILE, new ArrayList<>(interviews));
    }

    // Non-blocking save for UI thread operations to avoid freezing the EDT
    public void saveAllAsync() {
        final List<Student> studentsCopy;
        final List<Company> companiesCopy;
        final List<PlacementDrive> drivesCopy;
        final List<Application> applicationsCopy;
        final List<PlacementOfficer> officersCopy;
        final List<Interview> interviewsCopy;

        synchronized (this) {
            studentsCopy = new ArrayList<>(students);
            companiesCopy = new ArrayList<>(companies);
            drivesCopy = new ArrayList<>(drives);
            applicationsCopy = new ArrayList<>(applications);
            officersCopy = new ArrayList<>(officers);
            interviewsCopy = new ArrayList<>(interviews);
        }

        saveExecutor.submit(() -> {
            writeListToFile(STUDENTS_FILE, studentsCopy);
            writeListToFile(COMPANIES_FILE, companiesCopy);
            writeListToFile(DRIVES_FILE, drivesCopy);
            writeListToFile(APPLICATIONS_FILE, applicationsCopy);
            writeListToFile(OFFICERS_FILE, officersCopy);
            writeListToFile(INTERVIEWS_FILE, interviewsCopy);
        });
    }

    public synchronized void saveStudents() {
        writeListToFile(STUDENTS_FILE, new ArrayList<>(students));
    }

    public synchronized void saveCompanies() {
        writeListToFile(COMPANIES_FILE, new ArrayList<>(companies));
    }

    public synchronized void saveDrives() {
        writeListToFile(DRIVES_FILE, new ArrayList<>(drives));
    }

    public synchronized void saveApplications() {
        writeListToFile(APPLICATIONS_FILE, new ArrayList<>(applications));
    }

    public synchronized void saveOfficers() {
        writeListToFile(OFFICERS_FILE, new ArrayList<>(officers));
    }

    public synchronized void saveInterviews() {
        writeListToFile(INTERVIEWS_FILE, new ArrayList<>(interviews));
    }

    public synchronized Student authenticateStudent(String email, String password) {
        for (Student s : students) {
            if (s.getEmail().equalsIgnoreCase(email.trim()) && s.checkPassword(password)) {
                return s;
            }
        }
        return null;
    }

    public synchronized Company authenticateCompany(String email, String password) {
        for (Company c : companies) {
            if (c.getEmail().equalsIgnoreCase(email.trim()) && c.checkPassword(password)) {
                return c;
            }
        }
        return null;
    }

    public synchronized PlacementOfficer authenticateOfficer(String email, String password) {
        for (PlacementOfficer po : officers) {
            if (po.getEmail().equalsIgnoreCase(email.trim()) && po.checkPassword(password)) {
                return po;
            }
        }
        return null;
    }

    public synchronized List<Student> getStudents() {
        return new ArrayList<>(students);
    }

    public synchronized void addStudent(Student s) {
        for (Student existing : students) {
            if (existing.getEmail().equalsIgnoreCase(s.getEmail()) || existing.getUsn().equalsIgnoreCase(s.getUsn())) {
                throw new IllegalArgumentException("Student with this USN or Email already exists.");
            }
        }
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
        for (Company existing : companies) {
            if (existing.getEmail().equalsIgnoreCase(c.getEmail()) || existing.getCompanyId().equalsIgnoreCase(c.getCompanyId())) {
                throw new IllegalArgumentException("Company with this ID or Email already exists.");
            }
        }
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
            if (!c.isApproved()) {
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

    // Applies student to drive, verifying eligibility and checking for existing applications
    public synchronized void applyToDrive(Student student, PlacementDrive drive)
            throws IneligibleStudentException, DuplicateApplicationException {
        if (student == null || drive == null) {
            throw new IllegalArgumentException("Student and Drive must not be null.");
        }

        // 1. Eligibility Check
        if (!drive.checkEligibility(student)) {
            throw new IneligibleStudentException(
                    "Eligibility criteria not met. Drive requires minimum CGPA of " 
                    + drive.getEligibleCGPA() + " and max " + drive.getMaxBacklogsAllowed() 
                    + " backlogs. Student has CGPA: " + student.getCgpa() 
                    + " and " + student.getBacklogs() + " backlogs."
            );
        }

        // 2. Duplicate Application Check
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

        // 3. Create Application
        String appId = "APP" + (applications.size() + 101);
        Application newApp = new Application(appId, student, drive);
        applications.add(newApp);
        drive.getApplicants().add(newApp);

        if (drive.getCompany() != null) {
            drive.getCompany().sendNotification("New application received from " + student.getName() + " for role: " + drive.getRole());
        }

        saveApplications();
        saveDrives();
    }

    // Update status and record changes
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
        interviews.add(interview);
        saveInterviews();
        if (interview.getApplication() != null && interview.getApplication().getStudent() != null) {
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

    public synchronized int getPlacedStudentsCount() {
        int count = 0;
        for (Application a : applications) {
            if ("Selected".equalsIgnoreCase(a.getStatus())) {
                count++;
            }
        }
        return count;
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
