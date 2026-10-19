package com.placementsystem.gui;

import com.placementsystem.MainFrame;
import com.placementsystem.exception.DuplicateApplicationException;
import com.placementsystem.exception.IneligibleStudentException;
import com.placementsystem.model.Application;
import com.placementsystem.model.PlacementDrive;
import com.placementsystem.model.Student;
import com.placementsystem.util.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Student portal for viewing/updating profile, browsing drives, applying with
 * custom exception handling, and tracking application status.
 * Demonstrates Custom Exception Handling, Event Driven Swing, and MVC patterns.
 */
public class StudentProfilePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private MainFrame mainFrame;
    private Student currentStudent;

    // Profile form fields
    private JTextField usnField;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField deptField;
    private JTextField cgpaField;
    private JTextField backlogsField;
    private JButton updateProfileButton;

    // Tables
    private JTable drivesTable;
    private DefaultTableModel drivesTableModel;
    private JTable applicationsTable;
    private DefaultTableModel applicationsTableModel;
    private DefaultListModel<String> notificationListModel;

    public StudentProfilePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 20, 15, 20));
        setBackground(new Color(245, 247, 250));

        initComponents();
    }

    public void setCurrentStudent(Student student) {
        this.currentStudent = student;
        refreshData();
    }

    private void initComponents() {
        // Left Panel: Student Profile
        JPanel profileCard = createProfileCard();
        add(profileCard, BorderLayout.WEST);

        // Center Panel: Tabs for Drives, Applications, and Notifications
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("Available Placement Drives", createDrivesPanel());
        tabbedPane.addTab("My Applications & Status", createApplicationsPanel());
        tabbedPane.addTab("Notifications Inbox", createNotificationsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createProfileCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(340, 600));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 225), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel title = new JLabel("Student Profile", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(24, 43, 73));
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        usnField = new JTextField(12);
        usnField.setEditable(false);
        nameField = new JTextField(12);
        emailField = new JTextField(12);
        emailField.setEditable(false);
        phoneField = new JTextField(12);
        deptField = new JTextField(12);
        cgpaField = new JTextField(12);
        backlogsField = new JTextField(12);

        addField(panel, gbc, "USN:", usnField);
        addField(panel, gbc, "Full Name:", nameField);
        addField(panel, gbc, "Email:", emailField);
        addField(panel, gbc, "Phone:", phoneField);
        addField(panel, gbc, "Department:", deptField);
        addField(panel, gbc, "CGPA (0.0-10.0):", cgpaField);
        addField(panel, gbc, "Backlogs:", backlogsField);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        updateProfileButton = new JButton("Save Profile Changes");
        updateProfileButton.setBackground(new Color(30, 100, 200));
        updateProfileButton.setForeground(Color.WHITE);
        updateProfileButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        updateProfileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateProfileButton.addActionListener(e -> updateProfile());
        panel.add(updateProfileButton, gbc);

        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(field, gbc);
    }

    private JPanel createDrivesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Drive ID", "Company", "Role", "Package (LPA)", "Min CGPA", "Max Backlogs", "Date", "Venue"};
        drivesTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        drivesTable = new JTable(drivesTableModel);
        drivesTable.setRowHeight(26);
        drivesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        drivesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        panel.add(new JScrollPane(drivesTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        JButton applyBtn = new JButton("Apply for Selected Drive");
        applyBtn.setBackground(new Color(40, 167, 69));
        applyBtn.setForeground(Color.WHITE);
        applyBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        applyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        applyBtn.addActionListener(e -> applySelectedDrive());

        JButton refreshBtn = new JButton("Refresh Drives");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshBtn.addActionListener(e -> loadDrives());

        bottomBar.add(refreshBtn);
        bottomBar.add(applyBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createApplicationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Application ID", "Company", "Role", "Offered Package", "Status", "Applied On"};
        applicationsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        applicationsTable = new JTable(applicationsTableModel);
        applicationsTable.setRowHeight(26);
        applicationsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        panel.add(new JScrollPane(applicationsTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh Applications");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshBtn.addActionListener(e -> loadApplications());
        bottomBar.add(refreshBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createNotificationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        notificationListModel = new DefaultListModel<>();
        JList<String> notifList = new JList<>(notificationListModel);
        notifList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notifList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(notifList), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh Notifications");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshBtn.addActionListener(e -> loadNotifications());
        bottomBar.add(refreshBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private void updateProfile() {
        if (currentStudent == null) return;
        try {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String dept = deptField.getText().trim();
            double cgpa = Double.parseDouble(cgpaField.getText().trim());
            int backlogs = Integer.parseInt(backlogsField.getText().trim());

            if (name.isEmpty() || dept.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Department cannot be blank.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            currentStudent.setName(name);
            currentStudent.setPhone(phone);
            currentStudent.setDepartment(dept);
            currentStudent.setCgpa(cgpa); // Triggers 0.0-10.0 validation
            currentStudent.setBacklogs(backlogs);

            DataManager.getInstance().updateStudent(currentStudent);
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            mainFrame.updateActiveUserDisplay("Student: " + currentStudent.getName());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for CGPA and Backlogs.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applySelectedDrive() {
        int selectedRow = drivesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a placement drive from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String driveId = (String) drivesTableModel.getValueAt(selectedRow, 0);
        PlacementDrive selectedDrive = null;
        for (PlacementDrive d : DataManager.getInstance().getDrives()) {
            if (d.getDriveId().equalsIgnoreCase(driveId)) {
                selectedDrive = d;
                break;
            }
        }

        if (selectedDrive == null) {
            JOptionPane.showMessageDialog(this, "Selected drive could not be found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Invokes business logic with custom exceptions
            DataManager.getInstance().applyToDrive(currentStudent, selectedDrive);
            JOptionPane.showMessageDialog(this, 
                    "Application submitted successfully for " + selectedDrive.getRole() + " at " + selectedDrive.getCompany().getCompanyName() + "!", 
                    "Application Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            loadApplications();
            loadNotifications();
        } catch (IneligibleStudentException ise) {
            // Custom Checked Exception 1
            JOptionPane.showMessageDialog(this, 
                    ise.getMessage(), 
                    "Eligibility Criteria Not Met", 
                    JOptionPane.WARNING_MESSAGE);
        } catch (DuplicateApplicationException dae) {
            // Custom Checked Exception 2
            JOptionPane.showMessageDialog(this, 
                    dae.getMessage(), 
                    "Duplicate Application", 
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Application Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshData() {
        if (currentStudent == null) return;
        usnField.setText(currentStudent.getUsn());
        nameField.setText(currentStudent.getName());
        emailField.setText(currentStudent.getEmail());
        phoneField.setText(currentStudent.getPhone());
        deptField.setText(currentStudent.getDepartment());
        cgpaField.setText(String.valueOf(currentStudent.getCgpa()));
        backlogsField.setText(String.valueOf(currentStudent.getBacklogs()));

        loadDrives();
        loadApplications();
        loadNotifications();
    }

    private void loadDrives() {
        drivesTableModel.setRowCount(0);
        List<PlacementDrive> drives = DataManager.getInstance().getDrives();
        for (PlacementDrive d : drives) {
            String compName = (d.getCompany() != null) ? d.getCompany().getCompanyName() : "N/A";
            drivesTableModel.addRow(new Object[]{
                    d.getDriveId(),
                    compName,
                    d.getRole(),
                    d.getPackageOffered() + " LPA",
                    d.getEligibleCGPA(),
                    d.getMaxBacklogsAllowed(),
                    d.getDriveDate(),
                    d.getVenue()
            });
        }
    }

    private void loadApplications() {
        applicationsTableModel.setRowCount(0);
        if (currentStudent == null) return;
        List<Application> apps = DataManager.getInstance().getApplicationsForStudent(currentStudent);
        for (Application a : apps) {
            String compName = (a.getDrive() != null && a.getDrive().getCompany() != null) 
                    ? a.getDrive().getCompany().getCompanyName() : "N/A";
            String role = (a.getDrive() != null) ? a.getDrive().getRole() : "N/A";
            String pkg = (a.getDrive() != null) ? a.getDrive().getPackageOffered() + " LPA" : "N/A";

            applicationsTableModel.addRow(new Object[]{
                    a.getApplicationId(),
                    compName,
                    role,
                    pkg,
                    a.getStatus(),
                    a.getAppliedDate()
            });
        }
    }

    private void loadNotifications() {
        notificationListModel.clear();
        if (currentStudent == null) return;
        List<String> notifs = currentStudent.getNotifications();
        if (notifs.isEmpty()) {
            notificationListModel.addElement("No notifications yet.");
        } else {
            for (int i = notifs.size() - 1; i >= 0; i--) {
                notificationListModel.addElement("• " + notifs.get(i));
            }
        }
    }
}
