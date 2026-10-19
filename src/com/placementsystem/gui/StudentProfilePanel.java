package com.placementsystem.gui;

import com.placementsystem.MainFrame;
import com.placementsystem.exception.DuplicateApplicationException;
import com.placementsystem.exception.IneligibleStudentException;
import com.placementsystem.model.Application;
import com.placementsystem.model.Notification;
import com.placementsystem.model.PlacementDrive;
import com.placementsystem.model.Student;
import com.placementsystem.util.DataManager;
import com.placementsystem.util.Validator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StudentProfilePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private MainFrame mainFrame;
    private Student currentStudent;

    private JTextField usnField;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField deptField;
    private JTextField cgpaField;
    private JTextField backlogsField;

    private JLabel nameErrorLabel;
    private JLabel phoneErrorLabel;
    private JLabel deptErrorLabel;
    private JLabel cgpaErrorLabel;
    private JLabel backlogsErrorLabel;

    private JButton updateProfileButton;

    private JTabbedPane tabbedPane;

    private JTextField driveSearchField;
    private JCheckBox eligibleOnlyCheckBox;
    private JTable drivesTable;
    private DefaultTableModel drivesTableModel;

    private JTable applicationsTable;
    private DefaultTableModel applicationsTableModel;
    private DefaultListModel<String> notificationListModel;

    private transient List<PlacementDrive> currentRenderedDrives = new ArrayList<>();

    @SuppressWarnings("this-escape")
    public StudentProfilePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        setBorder(new EmptyBorder(Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        setBackground(Theme.COLOR_BG);

        initComponents();
    }

    public void setCurrentStudent(Student student) {
        this.currentStudent = student;
        refreshData();
    }

    public void clearSession() {
        this.currentStudent = null;
        if (usnField != null) usnField.setText("");
        if (nameField != null) nameField.setText("");
        if (emailField != null) emailField.setText("");
        if (phoneField != null) phoneField.setText("");
        if (deptField != null) deptField.setText("");
        if (cgpaField != null) cgpaField.setText("");
        if (backlogsField != null) backlogsField.setText("");
        if (drivesTableModel != null) drivesTableModel.setRowCount(0);
        if (applicationsTableModel != null) applicationsTableModel.setRowCount(0);
        if (notificationListModel != null) notificationListModel.clear();
        if (tabbedPane != null) tabbedPane.setTitleAt(2, "Notifications");
    }

    public boolean hasUnsavedChanges() {
        if (currentStudent == null) return false;
        String curName = nameField.getText().trim();
        String curPhone = phoneField.getText().trim();
        String curDept = deptField.getText().trim();
        String curCgpa = cgpaField.getText().trim();
        String curBacklogs = backlogsField.getText().trim();

        if (!curName.equals(currentStudent.getName() != null ? currentStudent.getName() : "")) return true;
        if (!curPhone.equals(currentStudent.getPhone() != null ? currentStudent.getPhone() : "")) return true;
        if (!curDept.equals(currentStudent.getDepartment() != null ? currentStudent.getDepartment() : "")) return true;

        try {
            double parsedCgpa = Double.parseDouble(curCgpa);
            if (Math.abs(parsedCgpa - currentStudent.getCgpa()) > 0.001) return true;
        } catch (NumberFormatException ignored) {
            return true;
        }

        try {
            int parsedBacklogs = Integer.parseInt(curBacklogs);
            if (parsedBacklogs != currentStudent.getBacklogs()) return true;
        } catch (NumberFormatException ignored) {
            return true;
        }

        return false;
    }

    private void initComponents() {
        JPanel profileCard = createProfileCard();
        add(profileCard, BorderLayout.WEST);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Theme.FONT_BODY_BOLD);

        tabbedPane.addTab("Available Placement Drives", createDrivesPanel());
        tabbedPane.addTab("My Applications", createApplicationsPanel());
        tabbedPane.addTab("Notifications", createNotificationsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createProfileCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(340, 620));
        panel.setBackground(Theme.COLOR_SURFACE);
        panel.setBorder(Theme.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, Theme.SPACING_SMALL, 3, Theme.SPACING_SMALL);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel title = new JLabel("Student Profile", SwingConstants.LEFT);
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.COLOR_TEXT_PRIMARY);
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        usnField = new JTextField(12);
        usnField.setFont(Theme.FONT_BODY);
        usnField.setEditable(false);

        nameField = new JTextField(12);
        nameField.setFont(Theme.FONT_BODY);

        emailField = new JTextField(12);
        emailField.setFont(Theme.FONT_BODY);
        emailField.setEditable(false);

        phoneField = new JTextField(12);
        phoneField.setFont(Theme.FONT_BODY);

        deptField = new JTextField(12);
        deptField.setFont(Theme.FONT_BODY);

        cgpaField = new JTextField(12);
        cgpaField.setFont(Theme.FONT_BODY);

        backlogsField = new JTextField(12);
        backlogsField.setFont(Theme.FONT_BODY);

        nameErrorLabel = Theme.createInlineValidationLabel();
        phoneErrorLabel = Theme.createInlineValidationLabel();
        deptErrorLabel = Theme.createInlineValidationLabel();
        cgpaErrorLabel = Theme.createInlineValidationLabel();
        backlogsErrorLabel = Theme.createInlineValidationLabel();

        addField(panel, gbc, "USN:", usnField);
        addFieldWithValidation(panel, gbc, "Full Name:", nameField, nameErrorLabel);
        addField(panel, gbc, "Email:", emailField);
        addFieldWithValidation(panel, gbc, "Phone:", phoneField, phoneErrorLabel);
        addFieldWithValidation(panel, gbc, "Department:", deptField, deptErrorLabel);
        addFieldWithValidation(panel, gbc, "CGPA (0.0 to 10.0):", cgpaField, cgpaErrorLabel);
        addFieldWithValidation(panel, gbc, "Backlogs:", backlogsField, backlogsErrorLabel);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        updateProfileButton = new JButton("Save Profile");
        Theme.stylePrimaryButton(updateProfileButton);
        updateProfileButton.addActionListener(e -> updateProfile());
        panel.add(updateProfileButton, gbc);

        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_BODY_BOLD);
        lbl.setForeground(Theme.COLOR_TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void addFieldWithValidation(JPanel panel, GridBagConstraints gbc, String label, JTextField field, JLabel errorLabel) {
        addField(panel, gbc, label, Theme.createFieldWithValidation(field, errorLabel));
    }

    private JPanel createDrivesPanel() {
        JPanel panel = new JPanel(new BorderLayout(Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        panel.setBackground(Theme.COLOR_SURFACE);
        panel.setBorder(new EmptyBorder(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.SPACING_SMALL, 0));
        filterBar.setOpaque(false);

        JLabel searchLbl = new JLabel("Search Drives:");
        searchLbl.setFont(Theme.FONT_BODY_BOLD);
        filterBar.add(searchLbl);

        driveSearchField = new JTextField(14);
        driveSearchField.setFont(Theme.FONT_BODY);
        driveSearchField.addActionListener(e -> loadDrives());
        filterBar.add(driveSearchField);

        JButton filterBtn = new JButton("Filter");
        Theme.styleSecondaryButton(filterBtn);
        filterBtn.addActionListener(e -> loadDrives());
        filterBar.add(filterBtn);

        eligibleOnlyCheckBox = new JCheckBox("Show only drives I am eligible for");
        eligibleOnlyCheckBox.setFont(Theme.FONT_BODY);
        eligibleOnlyCheckBox.setOpaque(false);
        eligibleOnlyCheckBox.addActionListener(e -> loadDrives());
        filterBar.add(eligibleOnlyCheckBox);

        panel.add(filterBar, BorderLayout.NORTH);

        String[] cols = {"Drive ID", "Company", "Role", "Package (LPA)", "Min CGPA", "Max Backlogs", "Date", "Venue"};
        drivesTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        drivesTable = new JTable(drivesTableModel);
        Theme.styleTable(drivesTable);
        drivesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DriveEligibilityRenderer customRenderer = new DriveEligibilityRenderer();
        for (int i = 0; i < drivesTable.getColumnCount(); i++) {
            drivesTable.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }

        drivesTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        drivesTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        drivesTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        drivesTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        drivesTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        drivesTable.getColumnModel().getColumn(5).setPreferredWidth(85);

        panel.add(new JScrollPane(drivesTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SPACING_SMALL, 0));
        bottomBar.setOpaque(false);

        JButton refreshBtn = new JButton("Refresh Drives");
        Theme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> {
            driveSearchField.setText("");
            eligibleOnlyCheckBox.setSelected(false);
            loadDrives();
        });

        JButton applyBtn = new JButton("Apply for Selected Drive");
        Theme.stylePrimaryButton(applyBtn);
        applyBtn.addActionListener(e -> applySelectedDrive());

        bottomBar.add(refreshBtn);
        bottomBar.add(applyBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private class DriveEligibilityRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            if (col == 3 || col == 4 || col == 5) {
                setHorizontalAlignment(SwingConstants.RIGHT);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }

            if (row >= 0 && row < currentRenderedDrives.size()) {
                PlacementDrive drive = currentRenderedDrives.get(row);
                boolean deadlinePassed = drive.isDeadlinePassed();
                boolean eligible = (currentStudent != null) && drive.checkEligibility(currentStudent);
                boolean alreadyApplied = false;

                if (currentStudent != null) {
                    for (Application a : DataManager.getInstance().getApplicationsForStudent(currentStudent)) {
                        if (a.getDrive() != null && a.getDrive().getDriveId().equalsIgnoreCase(drive.getDriveId())) {
                            alreadyApplied = true;
                            break;
                        }
                    }
                }

                if (!isSelected) {
                    if (alreadyApplied) {
                        c.setForeground(new Color(0x2B, 0x6C, 0xB0)); // soft blue
                        setToolTipText("Already submitted an application for this drive.");
                    } else if (deadlinePassed) {
                        c.setForeground(Theme.COLOR_TEXT_MUTED);
                        setToolTipText("Application deadline has passed (" + drive.getDriveDate() + ").");
                    } else if (!eligible) {
                        c.setForeground(Theme.COLOR_TEXT_MUTED);
                        setToolTipText("Ineligible: Requires CGPA >= " + drive.getEligibleCGPA()
                                + " and backlogs <= " + drive.getMaxBacklogsAllowed()
                                + " (Your CGPA: " + (currentStudent != null ? currentStudent.getCgpa() : 0.0)
                                + ", backlogs: " + (currentStudent != null ? currentStudent.getBacklogs() : 0) + ")");
                    } else {
                        c.setForeground(Theme.COLOR_TEXT_PRIMARY);
                        setToolTipText("You meet all eligibility criteria for this placement drive.");
                    }
                }
            }

            return c;
        }
    }

    private JPanel createApplicationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        panel.setBackground(Theme.COLOR_SURFACE);
        panel.setBorder(new EmptyBorder(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL));

        String[] cols = {"Application ID", "Company", "Role", "Package (LPA)", "Status", "Applied On"};
        applicationsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        applicationsTable = new JTable(applicationsTableModel);
        Theme.styleTable(applicationsTable);
        applicationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        applicationsTable.getColumnModel().getColumn(3).setCellRenderer(Theme.getRightAlignRenderer());

        panel.add(new JScrollPane(applicationsTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SPACING_SMALL, 0));
        bottomBar.setOpaque(false);

        JButton withdrawBtn = new JButton("Withdraw Application");
        Theme.styleSecondaryButton(withdrawBtn);
        withdrawBtn.addActionListener(e -> withdrawSelectedApplication());

        JButton refreshBtn = new JButton("Refresh");
        Theme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadApplications());

        bottomBar.add(withdrawBtn);
        bottomBar.add(refreshBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createNotificationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        panel.setBackground(Theme.COLOR_SURFACE);
        panel.setBorder(new EmptyBorder(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL));

        notificationListModel = new DefaultListModel<>();
        JList<String> notifList = new JList<>(notificationListModel);
        notifList.setFont(Theme.FONT_BODY);
        notifList.setForeground(Theme.COLOR_TEXT_PRIMARY);
        notifList.setBackground(Theme.COLOR_SURFACE);
        notifList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(notifList), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SPACING_SMALL, 0));
        bottomBar.setOpaque(false);

        JButton refreshBtn = new JButton("Refresh");
        Theme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadNotifications());
        bottomBar.add(refreshBtn);

        JButton markReadBtn = new JButton("Mark All as Read");
        Theme.styleSecondaryButton(markReadBtn);
        markReadBtn.addActionListener(e -> {
            if (currentStudent != null) {
                currentStudent.markAllNotificationsAsRead();
                DataManager.getInstance().saveStudents();
                loadNotifications();
            }
        });
        bottomBar.add(markReadBtn);

        JButton clearBtn = new JButton("Clear All");
        Theme.styleSecondaryButton(clearBtn);
        clearBtn.addActionListener(e -> {
            if (currentStudent != null && !currentStudent.getNotifications().isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Clear all notifications?",
                        "Confirm Clear",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    currentStudent.clearNotifications();
                    DataManager.getInstance().saveStudents();
                    loadNotifications();
                }
            }
        });
        bottomBar.add(clearBtn);

        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private void updateProfile() {
        if (currentStudent == null) return;
        nameErrorLabel.setText(" ");
        phoneErrorLabel.setText(" ");
        deptErrorLabel.setText(" ");
        cgpaErrorLabel.setText(" ");
        backlogsErrorLabel.setText(" ");

        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String dept = deptField.getText().trim();

        boolean invalid = false;
        if (name.isEmpty()) {
            nameErrorLabel.setText("Name cannot be blank.");
            invalid = true;
        }

        if (phone.isEmpty()) {
            phoneErrorLabel.setText("Phone number is required.");
            invalid = true;
        } else if (!Validator.isValidPhone(phone)) {
            phoneErrorLabel.setText("Enter valid 10-digit mobile number.");
            invalid = true;
        }

        if (dept.isEmpty()) {
            deptErrorLabel.setText("Department cannot be blank.");
            invalid = true;
        }

        double cgpa = 0.0;
        try {
            cgpa = Double.parseDouble(cgpaField.getText().trim());
            if (!Validator.isValidCgpa(cgpa)) {
                cgpaErrorLabel.setText("CGPA must be between 0.0 and 10.0.");
                invalid = true;
            }
        } catch (NumberFormatException ex) {
            cgpaErrorLabel.setText("Invalid numeric format for CGPA.");
            invalid = true;
        }

        int backlogs = 0;
        try {
            backlogs = Integer.parseInt(backlogsField.getText().trim());
            if (!Validator.isValidBacklogs(backlogs)) {
                backlogsErrorLabel.setText("Backlogs must be between 0 and 50.");
                invalid = true;
            }
        } catch (NumberFormatException ex) {
            backlogsErrorLabel.setText("Invalid numeric format for backlogs.");
            invalid = true;
        }

        if (invalid) {
            revalidate();
            repaint();
            return;
        }

        try {
            currentStudent.setName(name);
            currentStudent.setPhone(phone);
            currentStudent.setDepartment(dept);
            currentStudent.setCgpa(cgpa);
            currentStudent.setBacklogs(backlogs);

            DataManager.getInstance().updateStudent(currentStudent);
            JOptionPane.showMessageDialog(this, "Profile updated successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
            mainFrame.updateActiveUserDisplay(currentStudent.getName() + " (Student)");
            revalidate();
            repaint();
            loadDrives();
        } catch (IllegalArgumentException ex) {
            cgpaErrorLabel.setText(ex.getMessage());
            revalidate();
            repaint();
        }
    }

    private void applySelectedDrive() {
        int selectedRow = drivesTable.getSelectedRow();
        if (selectedRow == -1 || selectedRow >= currentRenderedDrives.size()) {
            JOptionPane.showMessageDialog(this, "Please select a placement drive from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PlacementDrive selectedDrive = currentRenderedDrives.get(selectedRow);

        try {
            DataManager.getInstance().applyToDrive(currentStudent, selectedDrive);
            JOptionPane.showMessageDialog(this,
                    "Application submitted successfully for " + selectedDrive.getRole() + " at " + selectedDrive.getCompany().getCompanyName() + ".",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            loadDrives();
            loadApplications();
            loadNotifications();
        } catch (IneligibleStudentException ise) {
            JOptionPane.showMessageDialog(this, ise.getMessage(), "Eligibility Criteria Not Met", JOptionPane.WARNING_MESSAGE);
        } catch (DuplicateApplicationException dae) {
            JOptionPane.showMessageDialog(this, dae.getMessage(), "Duplicate Application", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Application Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void withdrawSelectedApplication() {
        int selectedRow = applicationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an application to withdraw.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String appId = (String) applicationsTableModel.getValueAt(selectedRow, 0);
        if ("No applications yet".equals(appId)) return;

        Application target = null;
        for (Application a : DataManager.getInstance().getApplicationsForStudent(currentStudent)) {
            if (a.getApplicationId().equalsIgnoreCase(appId)) {
                target = a;
                break;
            }
        }

        if (target == null) {
            JOptionPane.showMessageDialog(this, "Selected application could not be found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!"Applied".equalsIgnoreCase(target.getStatus())) {
            JOptionPane.showMessageDialog(this,
                    "Applications with status '" + target.getStatus() + "' cannot be withdrawn.\nOnly applications pending initial recruiter review ('Applied') may be withdrawn.",
                    "Withdrawal Restricted",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to withdraw your application for '"
                        + (target.getDrive() != null ? target.getDrive().getRole() : "Job Role") + "'?\nThis action cannot be undone.",
                "Confirm Application Withdrawal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            DataManager.getInstance().withdrawApplication(currentStudent, target);
            JOptionPane.showMessageDialog(this, "Application successfully withdrawn.", "Withdrawn", JOptionPane.INFORMATION_MESSAGE);
            loadApplications();
            loadDrives();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error withdrawing application: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        currentRenderedDrives.clear();
        if (currentStudent == null) return;

        String query = driveSearchField != null ? driveSearchField.getText().trim().toLowerCase() : "";
        boolean eligibleOnly = eligibleOnlyCheckBox != null && eligibleOnlyCheckBox.isSelected();

        List<PlacementDrive> allDrives = DataManager.getInstance().getDrives();
        for (PlacementDrive d : allDrives) {
            String compName = (d.getCompany() != null) ? d.getCompany().getCompanyName() : "N/A";
            String role = d.getRole() != null ? d.getRole() : "";

            if (!query.isEmpty()) {
                if (!compName.toLowerCase().contains(query) && !role.toLowerCase().contains(query)) {
                    continue;
                }
            }

            if (eligibleOnly) {
                if (!d.checkEligibility(currentStudent) || d.isDeadlinePassed()) {
                    continue;
                }
            }

            currentRenderedDrives.add(d);
            drivesTableModel.addRow(new Object[]{
                    d.getDriveId(),
                    compName,
                    d.getRole(),
                    String.format("%.1f", d.getPackageOffered()),
                    String.format("%.2f", d.getEligibleCGPA()),
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
        if (apps.isEmpty()) {
            applicationsTableModel.addRow(new Object[]{"No applications yet", "-", "-", "-", "-", "-"});
            return;
        }
        for (Application a : apps) {
            String compName = (a.getDrive() != null && a.getDrive().getCompany() != null)
                    ? a.getDrive().getCompany().getCompanyName() : "N/A";
            String role = (a.getDrive() != null) ? a.getDrive().getRole() : "N/A";
            String pkg = (a.getDrive() != null) ? String.format("%.1f", a.getDrive().getPackageOffered()) : "N/A";

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

        List<Notification> notifs = currentStudent.getNotifications();
        if (notifs.isEmpty()) {
            notificationListModel.addElement("No notifications yet.");
        } else {
            for (int i = notifs.size() - 1; i >= 0; i--) {
                notificationListModel.addElement(notifs.get(i).toString());
            }
        }

        int unread = currentStudent.getUnreadNotificationCount();
        if (tabbedPane != null && tabbedPane.getTabCount() > 2) {
            tabbedPane.setTitleAt(2, unread > 0 ? "Notifications (" + unread + ")" : "Notifications");
        }
    }
}
