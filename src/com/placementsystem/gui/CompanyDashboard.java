package com.placementsystem.gui;

import com.placementsystem.MainFrame;
import com.placementsystem.model.Application;
import com.placementsystem.model.Company;
import com.placementsystem.model.Interview;
import com.placementsystem.model.PlacementDrive;
import com.placementsystem.util.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CompanyDashboard extends JPanel {
    private static final long serialVersionUID = 1L;

    private MainFrame mainFrame;
    private Company currentCompany;

    private JLabel companyNameLabel;
    private JLabel statusBadgeLabel;
    private JPanel approvalBanner;

    private JTextField roleField;
    private JTextField packageField;
    private JTextField cgpaField;
    private JTextField backlogsField;
    private JTextField dateField;
    private JTextField venueField;

    private JLabel roleErrorLabel;
    private JLabel packageErrorLabel;
    private JLabel cgpaErrorLabel;
    private JLabel backlogsErrorLabel;
    private JLabel dateErrorLabel;

    private JButton postDriveButton;

    private JTable drivesTable;
    private DefaultTableModel drivesTableModel;
    private JTable applicantsTable;
    private DefaultTableModel applicantsTableModel;

    private PlacementDrive selectedDrive;

    public CompanyDashboard(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        setBorder(new EmptyBorder(Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        setBackground(Theme.COLOR_BG);

        initComponents();
    }

    public void setCurrentCompany(Company company) {
        this.currentCompany = company;
        refreshData();
    }

    private void initComponents() {
        JPanel northPanel = new JPanel(new BorderLayout(Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        northPanel.setOpaque(false);

        JPanel headerCard = new JPanel(new BorderLayout());
        headerCard.setBackground(Theme.COLOR_SURFACE);
        headerCard.setBorder(Theme.createCardBorder());

        companyNameLabel = new JLabel("Company Portal", SwingConstants.LEFT);
        companyNameLabel.setFont(Theme.FONT_TITLE);
        companyNameLabel.setForeground(Theme.COLOR_TEXT_PRIMARY);
        headerCard.add(companyNameLabel, BorderLayout.WEST);

        statusBadgeLabel = new JLabel("Status: Unknown", SwingConstants.RIGHT);
        statusBadgeLabel.setFont(Theme.FONT_BODY_BOLD);
        headerCard.add(statusBadgeLabel, BorderLayout.EAST);

        northPanel.add(headerCard, BorderLayout.NORTH);

        approvalBanner = new JPanel(new FlowLayout(FlowLayout.CENTER));
        approvalBanner.setBackground(Theme.COLOR_SUBTLE_BG);
        approvalBanner.setBorder(BorderFactory.createLineBorder(Theme.COLOR_BORDER, 1));
        JLabel bannerLabel = new JLabel("Account pending approval: Placement Officer verification required before you can post drives.");
        bannerLabel.setFont(Theme.FONT_BODY_BOLD);
        bannerLabel.setForeground(Theme.COLOR_ERROR);
        approvalBanner.add(bannerLabel);
        approvalBanner.setVisible(false);

        northPanel.add(approvalBanner, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Theme.FONT_BODY_BOLD);

        tabbedPane.addTab("Post Placement Drive", createPostDrivePanel());
        tabbedPane.addTab("Drives and Applicants", createManageApplicantsPanel());
        tabbedPane.addTab("Notifications", createNotificationsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createPostDrivePanel() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(Theme.COLOR_SURFACE);
        container.setBorder(new EmptyBorder(Theme.SPACING_MEDIUM, Theme.SPACING_LARGE, Theme.SPACING_MEDIUM, Theme.SPACING_LARGE));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Theme.COLOR_SURFACE);
        formCard.setBorder(Theme.createCardBorder());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, Theme.SPACING_SMALL, 4, Theme.SPACING_SMALL);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;

        JLabel formTitle = new JLabel("Create Placement Drive Announcement", SwingConstants.LEFT);
        formTitle.setFont(Theme.FONT_TITLE);
        formTitle.setForeground(Theme.COLOR_TEXT_PRIMARY);
        formCard.add(formTitle, g);

        g.gridwidth = 1;
        roleField = new JTextField("Graduate Engineer Trainee", 20);
        roleField.setFont(Theme.FONT_BODY);

        packageField = new JTextField("7.5", 20);
        packageField.setFont(Theme.FONT_BODY);

        cgpaField = new JTextField("7.0", 20);
        cgpaField.setFont(Theme.FONT_BODY);

        backlogsField = new JTextField("0", 20);
        backlogsField.setFont(Theme.FONT_BODY);

        dateField = new JTextField("2026-11-20", 20);
        dateField.setFont(Theme.FONT_BODY);

        venueField = new JTextField("Campus Auditorium / Online", 20);
        venueField.setFont(Theme.FONT_BODY);

        roleErrorLabel = Theme.createInlineValidationLabel();
        packageErrorLabel = Theme.createInlineValidationLabel();
        cgpaErrorLabel = Theme.createInlineValidationLabel();
        backlogsErrorLabel = Theme.createInlineValidationLabel();
        dateErrorLabel = Theme.createInlineValidationLabel();

        int row = 1;
        addFormRowWithValidation(formCard, g, row++, "Job Role:", roleField, roleErrorLabel);
        addFormRowWithValidation(formCard, g, row++, "Offered Package (LPA):", packageField, packageErrorLabel);
        addFormRowWithValidation(formCard, g, row++, "Minimum Eligible CGPA:", cgpaField, cgpaErrorLabel);
        addFormRowWithValidation(formCard, g, row++, "Max Backlogs Allowed:", backlogsField, backlogsErrorLabel);
        addFormRowWithValidation(formCard, g, row++, "Drive Date (YYYY-MM-DD):", dateField, dateErrorLabel);
        addFormRow(formCard, g, row++, "Venue / Mode:", venueField);

        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 2;
        postDriveButton = new JButton("Publish Placement Drive");
        Theme.stylePrimaryButton(postDriveButton);
        postDriveButton.addActionListener(e -> handlePostDrive());
        formCard.add(postDriveButton, g);

        container.add(formCard);
        return container;
    }

    private void addFormRow(JPanel panel, GridBagConstraints g, int row, String label, JTextField field) {
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_BODY_BOLD);
        lbl.setForeground(Theme.COLOR_TEXT_PRIMARY);
        panel.add(lbl, g);

        g.gridx = 1;
        panel.add(field, g);
    }

    private void addFormRowWithValidation(JPanel panel, GridBagConstraints g, int row, String label, JTextField field, JLabel errorLabel) {
        addFormRow(panel, g, row, label, field);
        g.gridx = 1;
        g.gridy = row + 1;
        panel.add(errorLabel, g);
    }

    private JPanel createManageApplicantsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        panel.setOpaque(false);

        JPanel drivesCard = new JPanel(new BorderLayout(Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        drivesCard.setBackground(Theme.COLOR_SURFACE);
        drivesCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.COLOR_BORDER, 1),
                new EmptyBorder(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL)
        ));

        JLabel drivesHeader = new JLabel("Posted Placement Drives");
        drivesHeader.setFont(Theme.FONT_BODY_BOLD);
        drivesCard.add(drivesHeader, BorderLayout.NORTH);

        String[] driveCols = {"Drive ID", "Role", "Package (LPA)", "Min CGPA", "Max Backlogs", "Drive Date", "Venue", "Applicants"};
        drivesTableModel = new DefaultTableModel(driveCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        drivesTable = new JTable(drivesTableModel);
        Theme.styleTable(drivesTable);
        drivesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Right-align numeric columns
        drivesTable.getColumnModel().getColumn(2).setCellRenderer(Theme.getRightAlignRenderer());
        drivesTable.getColumnModel().getColumn(3).setCellRenderer(Theme.getRightAlignRenderer());
        drivesTable.getColumnModel().getColumn(4).setCellRenderer(Theme.getRightAlignRenderer());
        drivesTable.getColumnModel().getColumn(7).setCellRenderer(Theme.getRightAlignRenderer());

        drivesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onDriveSelected();
            }
        });
        drivesCard.add(new JScrollPane(drivesTable), BorderLayout.CENTER);
        panel.add(drivesCard);

        JPanel applicantsCard = new JPanel(new BorderLayout(Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        applicantsCard.setBackground(Theme.COLOR_SURFACE);
        applicantsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.COLOR_BORDER, 1),
                new EmptyBorder(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL)
        ));

        JLabel applicantsHeader = new JLabel("Applicants for Selected Drive");
        applicantsHeader.setFont(Theme.FONT_BODY_BOLD);
        applicantsCard.add(applicantsHeader, BorderLayout.NORTH);

        String[] appCols = {"App ID", "Student Name", "USN", "Department", "CGPA", "Backlogs", "Phone", "Status"};
        applicantsTableModel = new DefaultTableModel(appCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        applicantsTable = new JTable(applicantsTableModel);
        Theme.styleTable(applicantsTable);
        applicantsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Right-align numeric columns
        applicantsTable.getColumnModel().getColumn(4).setCellRenderer(Theme.getRightAlignRenderer());
        applicantsTable.getColumnModel().getColumn(5).setCellRenderer(Theme.getRightAlignRenderer());

        applicantsCard.add(new JScrollPane(applicantsTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SPACING_SMALL, 0));
        actionPanel.setOpaque(false);

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(Theme.FONT_BODY_BOLD);
        actionPanel.add(statusLabel);

        String[] statuses = {"Shortlisted", "Selected", "Rejected"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        statusCombo.setFont(Theme.FONT_BODY);
        actionPanel.add(statusCombo);

        JButton updateStatusBtn = new JButton("Update Status");
        Theme.stylePrimaryButton(updateStatusBtn);
        updateStatusBtn.addActionListener(e -> {
            int selectedRow = applicantsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an applicant from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String appId = (String) applicantsTableModel.getValueAt(selectedRow, 0);
            String newStatus = (String) statusCombo.getSelectedItem();

            // Confirm destructive / irreversible action on reject
            if ("Rejected".equalsIgnoreCase(newStatus)) {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to reject this applicant? This action cannot be undone.",
                        "Confirm Rejection",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            updateApplicantStatus(appId, newStatus);
        });
        actionPanel.add(updateStatusBtn);

        JButton scheduleInterviewBtn = new JButton("Schedule Interview");
        Theme.styleSecondaryButton(scheduleInterviewBtn);
        scheduleInterviewBtn.addActionListener(e -> {
            int selectedRow = applicantsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an applicant from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String appId = (String) applicantsTableModel.getValueAt(selectedRow, 0);
            showScheduleInterviewDialog(appId);
        });
        actionPanel.add(scheduleInterviewBtn);

        applicantsCard.add(actionPanel, BorderLayout.SOUTH);
        panel.add(applicantsCard);

        return panel;
    }

    private JPanel createNotificationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        panel.setBackground(Theme.COLOR_SURFACE);
        panel.setBorder(new EmptyBorder(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL));

        DefaultListModel<String> notifListModel = new DefaultListModel<>();
        JList<String> notifList = new JList<>(notifListModel);
        notifList.setFont(Theme.FONT_BODY);
        notifList.setForeground(Theme.COLOR_TEXT_PRIMARY);

        panel.add(new JScrollPane(notifList), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        Theme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> {
            notifListModel.clear();
            if (currentCompany != null) {
                List<String> list = currentCompany.getNotifications();
                if (list.isEmpty()) {
                    notifListModel.addElement("No notifications received yet.");
                } else {
                    for (int i = list.size() - 1; i >= 0; i--) {
                        notifListModel.addElement(list.get(i));
                    }
                }
            }
        });
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SPACING_SMALL, 0));
        bot.setOpaque(false);
        bot.add(refreshBtn);
        panel.add(bot, BorderLayout.SOUTH);

        return panel;
    }

    private void handlePostDrive() {
        if (currentCompany == null) return;

        roleErrorLabel.setText(" ");
        packageErrorLabel.setText(" ");
        cgpaErrorLabel.setText(" ");
        backlogsErrorLabel.setText(" ");
        dateErrorLabel.setText(" ");

        if (!currentCompany.isApproved()) {
            JOptionPane.showMessageDialog(this,
                    "Your company account is pending approval by the Placement Officer.\nYou cannot publish placement drives until verified.",
                    "Approval Required",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String role = roleField.getText().trim();
        String date = dateField.getText().trim();
        String venue = venueField.getText().trim();

        boolean invalid = false;
        if (role.isEmpty()) {
            roleErrorLabel.setText("Job role is required.");
            invalid = true;
        }
        if (date.isEmpty()) {
            dateErrorLabel.setText("Drive date is required.");
            invalid = true;
        }

        double pkg = 0.0;
        try {
            pkg = Double.parseDouble(packageField.getText().trim());
            if (pkg <= 0.0) {
                packageErrorLabel.setText("Package must be greater than 0.");
                invalid = true;
            }
        } catch (NumberFormatException nfe) {
            packageErrorLabel.setText("Invalid numeric value for package.");
            invalid = true;
        }

        double cgpa = 0.0;
        try {
            cgpa = Double.parseDouble(cgpaField.getText().trim());
            if (cgpa < 0.0 || cgpa > 10.0) {
                cgpaErrorLabel.setText("CGPA must be between 0.0 and 10.0.");
                invalid = true;
            }
        } catch (NumberFormatException nfe) {
            cgpaErrorLabel.setText("Invalid numeric value for CGPA.");
            invalid = true;
        }

        int backlogs = 0;
        try {
            backlogs = Integer.parseInt(backlogsField.getText().trim());
            if (backlogs < 0) {
                backlogsErrorLabel.setText("Backlogs cannot be negative.");
                invalid = true;
            }
        } catch (NumberFormatException nfe) {
            backlogsErrorLabel.setText("Invalid numeric value for backlogs.");
            invalid = true;
        }

        if (invalid) {
            return;
        }

        try {
            String driveId = "DRV" + (DataManager.getInstance().getDrives().size() + 201);
            PlacementDrive drive = new PlacementDrive(driveId, currentCompany, role, pkg, cgpa, backlogs, date);

            if (venue.isEmpty()) {
                drive.scheduleInterview(date);
            } else {
                drive.scheduleInterview(date, venue);
            }

            DataManager.getInstance().addPlacementDrive(drive);
            JOptionPane.showMessageDialog(this, "Placement drive published successfully. (Drive ID: " + driveId + ")", "Success", JOptionPane.INFORMATION_MESSAGE);

            roleField.setText("");
            loadPostedDrives();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDriveSelected() {
        int selectedRow = drivesTable.getSelectedRow();
        if (selectedRow == -1) {
            selectedDrive = null;
            applicantsTableModel.setRowCount(0);
            return;
        }

        String driveId = (String) drivesTableModel.getValueAt(selectedRow, 0);
        for (PlacementDrive d : DataManager.getInstance().getDrives()) {
            if (d.getDriveId().equalsIgnoreCase(driveId)) {
                selectedDrive = d;
                break;
            }
        }

        loadApplicantsForSelectedDrive();
    }

    private void loadApplicantsForSelectedDrive() {
        applicantsTableModel.setRowCount(0);
        if (selectedDrive == null) return;

        List<Application> apps = DataManager.getInstance().getApplicationsForDrive(selectedDrive);
        for (Application a : apps) {
            if (a.getStudent() != null) {
                applicantsTableModel.addRow(new Object[]{
                        a.getApplicationId(),
                        a.getStudent().getName(),
                        a.getStudent().getUsn(),
                        a.getStudent().getDepartment(),
                        String.format("%.2f", a.getStudent().getCgpa()),
                        a.getStudent().getBacklogs(),
                        a.getStudent().getPhone(),
                        a.getStatus()
                });
            }
        }
    }

    private void updateApplicantStatus(String applicationId, String newStatus) {
        Application target = null;
        for (Application a : DataManager.getInstance().getApplications()) {
            if (a.getApplicationId().equalsIgnoreCase(applicationId)) {
                target = a;
                break;
            }
        }

        if (target != null) {
            DataManager.getInstance().updateApplicationStatus(target, newStatus);
            JOptionPane.showMessageDialog(this,
                    "Status updated to '" + newStatus + "'. Notification sent to candidate.",
                    "Candidate Notified",
                    JOptionPane.INFORMATION_MESSAGE);
            loadApplicantsForSelectedDrive();
        }
    }

    private void showScheduleInterviewDialog(String applicationId) {
        Application target = null;
        for (Application a : DataManager.getInstance().getApplications()) {
            if (a.getApplicationId().equalsIgnoreCase(applicationId)) {
                target = a;
                break;
            }
        }

        if (target == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Schedule Interview Round", true);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Theme.COLOR_SURFACE);
        dialog.setSize(420, 280);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField roundNameF = new JTextField("Technical Round 1", 15);
        roundNameF.setFont(Theme.FONT_BODY);
        JTextField dateTimeF = new JTextField("2026-11-26 10:30 AM", 15);
        dateTimeF.setFont(Theme.FONT_BODY);

        JLabel roundErr = Theme.createInlineValidationLabel();
        JLabel dateErr = Theme.createInlineValidationLabel();

        g.gridx = 0; g.gridy = 0;
        JLabel candLbl = new JLabel("Candidate:");
        candLbl.setFont(Theme.FONT_BODY_BOLD);
        dialog.add(candLbl, g);

        g.gridx = 1;
        JLabel nameLbl = new JLabel(target.getStudent().getName() + " (" + target.getStudent().getUsn() + ")");
        nameLbl.setFont(Theme.FONT_BODY);
        dialog.add(nameLbl, g);

        g.gridx = 0; g.gridy = 1;
        JLabel rLbl = new JLabel("Round Name:");
        rLbl.setFont(Theme.FONT_BODY_BOLD);
        dialog.add(rLbl, g);

        g.gridx = 1;
        dialog.add(roundNameF, g);

        g.gridx = 1; g.gridy = 2;
        dialog.add(roundErr, g);

        g.gridx = 0; g.gridy = 3;
        JLabel dtLbl = new JLabel("Date and Time:");
        dtLbl.setFont(Theme.FONT_BODY_BOLD);
        dialog.add(dtLbl, g);

        g.gridx = 1;
        dialog.add(dateTimeF, g);

        g.gridx = 1; g.gridy = 4;
        dialog.add(dateErr, g);

        JButton saveBtn = new JButton("Confirm Schedule");
        Theme.stylePrimaryButton(saveBtn);
        Application finalTarget = target;
        saveBtn.addActionListener(ev -> {
            roundErr.setText(" ");
            dateErr.setText(" ");

            String round = roundNameF.getText().trim();
            String dt = dateTimeF.getText().trim();

            boolean invalid = false;
            if (round.isEmpty()) {
                roundErr.setText("Round name is required.");
                invalid = true;
            }
            if (dt.isEmpty()) {
                dateErr.setText("Date and time are required.");
                invalid = true;
            }
            if (invalid) return;

            String intId = "INT" + (DataManager.getInstance().getInterviews().size() + 401);
            Interview interview = new Interview(intId, finalTarget, round, "Scheduled", dt);
            DataManager.getInstance().addInterview(interview);

            if ("Applied".equalsIgnoreCase(finalTarget.getStatus())) {
                DataManager.getInstance().updateApplicationStatus(finalTarget, "Shortlisted");
            }

            JOptionPane.showMessageDialog(dialog, "Interview scheduled. Candidate has been notified.", "Interview Scheduled", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            loadApplicantsForSelectedDrive();
        });

        g.gridx = 0; g.gridy = 5; g.gridwidth = 2;
        dialog.add(saveBtn, g);

        dialog.setVisible(true);
    }

    public void refreshData() {
        if (currentCompany == null) return;

        for (Company c : DataManager.getInstance().getCompanies()) {
            if (c.getCompanyId().equalsIgnoreCase(currentCompany.getCompanyId())) {
                currentCompany = c;
                break;
            }
        }

        companyNameLabel.setText(currentCompany.getCompanyName() + " (" + currentCompany.getIndustryType() + ")");
        if (currentCompany.isApproved()) {
            statusBadgeLabel.setText("Status: Approved");
            statusBadgeLabel.setForeground(Theme.COLOR_SUCCESS);
            approvalBanner.setVisible(false);
            postDriveButton.setEnabled(true);
        } else {
            statusBadgeLabel.setText("Status: Pending Approval");
            statusBadgeLabel.setForeground(Theme.COLOR_ERROR);
            approvalBanner.setVisible(true);
            postDriveButton.setEnabled(false);
        }

        loadPostedDrives();
    }

    private void loadPostedDrives() {
        drivesTableModel.setRowCount(0);
        if (currentCompany == null) return;

        List<PlacementDrive> list = DataManager.getInstance().getDrivesForCompany(currentCompany);
        for (PlacementDrive d : list) {
            drivesTableModel.addRow(new Object[]{
                    d.getDriveId(),
                    d.getRole(),
                    String.format("%.1f", d.getPackageOffered()),
                    String.format("%.2f", d.getEligibleCGPA()),
                    d.getMaxBacklogsAllowed(),
                    d.getDriveDate(),
                    d.getVenue(),
                    DataManager.getInstance().getApplicationsForDrive(d).size()
            });
        }
    }
}
