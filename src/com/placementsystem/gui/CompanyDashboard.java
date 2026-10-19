package com.placementsystem.gui;

import com.placementsystem.MainFrame;
import com.placementsystem.model.Application;
import com.placementsystem.model.Company;
import com.placementsystem.model.Interview;
import com.placementsystem.model.PlacementDrive;
import com.placementsystem.util.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dashboard for corporate recruiters to post placement drives, inspect applicants,
 * update candidate statuses, and schedule interview rounds.
 * Demonstrates Business Rule Enforcement (approval check), Overloaded Methods, and Event Handling.
 */
public class CompanyDashboard extends JPanel {
    private static final long serialVersionUID = 1L;

    private MainFrame mainFrame;
    private Company currentCompany;

    // Header labels
    private JLabel companyNameLabel;
    private JLabel statusBadgeLabel;
    private JPanel approvalBanner;

    // Post Drive Fields
    private JTextField roleField;
    private JTextField packageField;
    private JTextField cgpaField;
    private JTextField backlogsField;
    private JTextField dateField;
    private JTextField venueField;
    private JButton postDriveButton;

    // Tables
    private JTable drivesTable;
    private DefaultTableModel drivesTableModel;
    private JTable applicantsTable;
    private DefaultTableModel applicantsTableModel;

    // Selected Drive
    private PlacementDrive selectedDrive;

    public CompanyDashboard(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 20, 15, 20));
        setBackground(new Color(245, 247, 250));

        initComponents();
    }

    public void setCurrentCompany(Company company) {
        this.currentCompany = company;
        refreshData();
    }

    private void initComponents() {
        // North Panel: Company Header & Approval Alert Banner
        JPanel northPanel = new JPanel(new BorderLayout(5, 5));
        northPanel.setOpaque(false);

        JPanel headerCard = new JPanel(new BorderLayout());
        headerCard.setBackground(Color.WHITE);
        headerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 225), 1),
                new EmptyBorder(12, 20, 12, 20)
        ));

        companyNameLabel = new JLabel("Company Portal", SwingConstants.LEFT);
        companyNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        companyNameLabel.setForeground(new Color(24, 43, 73));
        headerCard.add(companyNameLabel, BorderLayout.WEST);

        statusBadgeLabel = new JLabel("Status: Unknown", SwingConstants.RIGHT);
        statusBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        headerCard.add(statusBadgeLabel, BorderLayout.EAST);

        northPanel.add(headerCard, BorderLayout.NORTH);

        approvalBanner = new JPanel(new FlowLayout(FlowLayout.CENTER));
        approvalBanner.setBackground(new Color(255, 235, 238));
        approvalBanner.setBorder(BorderFactory.createLineBorder(new Color(239, 83, 80), 1));
        JLabel bannerLabel = new JLabel("⚠️ ACCOUNT PENDING APPROVAL: Placement Officer verification required before you can post drives.");
        bannerLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bannerLabel.setForeground(new Color(198, 40, 40));
        approvalBanner.add(bannerLabel);
        approvalBanner.setVisible(false);

        northPanel.add(approvalBanner, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // Center: Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("Post New Placement Drive", createPostDrivePanel());
        tabbedPane.addTab("Manage Drives & Review Applicants", createManageApplicantsPanel());
        tabbedPane.addTab("Company Notifications", createNotificationsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createPostDrivePanel() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235), 1),
                new EmptyBorder(20, 30, 20, 30)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;

        JLabel formTitle = new JLabel("Create Placement Drive Announcement", SwingConstants.CENTER);
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        formTitle.setForeground(new Color(24, 43, 73));
        formCard.add(formTitle, g);

        g.gridwidth = 1;
        roleField = new JTextField("Graduate Engineer Trainee", 20);
        packageField = new JTextField("7.5", 20);
        cgpaField = new JTextField("7.0", 20);
        backlogsField = new JTextField("0", 20);
        dateField = new JTextField("2026-11-20", 20);
        venueField = new JTextField("Main Auditorium / Online", 20);

        int row = 1;
        addFormRow(formCard, g, row++, "Job Role / Title:", roleField);
        addFormRow(formCard, g, row++, "Offered Package (LPA):", packageField);
        addFormRow(formCard, g, row++, "Minimum Eligible CGPA:", cgpaField);
        addFormRow(formCard, g, row++, "Max Backlogs Allowed:", backlogsField);
        addFormRow(formCard, g, row++, "Drive Date (YYYY-MM-DD):", dateField);
        addFormRow(formCard, g, row++, "Venue / Mode:", venueField);

        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 2;
        postDriveButton = new JButton("Publish Placement Drive");
        postDriveButton.setBackground(new Color(30, 100, 200));
        postDriveButton.setForeground(Color.WHITE);
        postDriveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        postDriveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lbl, g);

        g.gridx = 1;
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(field, g);
    }

    private JPanel createManageApplicantsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setOpaque(false);

        // Top: Drives Table
        JPanel drivesCard = new JPanel(new BorderLayout(5, 5));
        drivesCard.setBackground(Color.WHITE);
        drivesCard.setBorder(BorderFactory.createTitledBorder("1. Select One of Your Posted Drives"));

        String[] driveCols = {"Drive ID", "Role", "Package (LPA)", "Min CGPA", "Max Backlogs", "Drive Date", "Venue", "Applicant Count"};
        drivesTableModel = new DefaultTableModel(driveCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        drivesTable = new JTable(drivesTableModel);
        drivesTable.setRowHeight(24);
        drivesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        drivesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onDriveSelected();
            }
        });
        drivesCard.add(new JScrollPane(drivesTable), BorderLayout.CENTER);
        panel.add(drivesCard);

        // Bottom: Applicants Table & Action Controls
        JPanel applicantsCard = new JPanel(new BorderLayout(5, 5));
        applicantsCard.setBackground(Color.WHITE);
        applicantsCard.setBorder(BorderFactory.createTitledBorder("2. Applicants for Selected Drive"));

        String[] appCols = {"App ID", "Student Name", "USN", "Department", "CGPA", "Backlogs", "Phone", "Status"};
        applicantsTableModel = new DefaultTableModel(appCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        applicantsTable = new JTable(applicantsTableModel);
        applicantsTable.setRowHeight(24);
        applicantsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        applicantsCard.add(new JScrollPane(applicantsTable), BorderLayout.CENTER);

        // Controls bar
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setOpaque(false);

        JLabel statusLabel = new JLabel("Update Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        actionPanel.add(statusLabel);

        String[] statuses = {"Shortlisted", "Selected", "Rejected"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        actionPanel.add(statusCombo);

        JButton updateStatusBtn = new JButton("Apply Status Change");
        updateStatusBtn.setBackground(new Color(30, 100, 200));
        updateStatusBtn.setForeground(Color.WHITE);
        updateStatusBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        updateStatusBtn.addActionListener(e -> {
            int selectedRow = applicantsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an applicant from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String appId = (String) applicantsTableModel.getValueAt(selectedRow, 0);
            String newStatus = (String) statusCombo.getSelectedItem();
            updateApplicantStatus(appId, newStatus);
        });
        actionPanel.add(updateStatusBtn);

        JButton scheduleInterviewBtn = new JButton("Schedule Interview Round");
        scheduleInterviewBtn.setBackground(new Color(40, 167, 69));
        scheduleInterviewBtn.setForeground(Color.WHITE);
        scheduleInterviewBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
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
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        DefaultListModel<String> notifListModel = new DefaultListModel<>();
        JList<String> notifList = new JList<>(notifListModel);
        notifList.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panel.add(new JScrollPane(notifList), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh Notifications");
        refreshBtn.addActionListener(e -> {
            notifListModel.clear();
            if (currentCompany != null) {
                List<String> list = currentCompany.getNotifications();
                if (list.isEmpty()) {
                    notifListModel.addElement("No notifications received yet.");
                } else {
                    for (int i = list.size() - 1; i >= 0; i--) {
                        notifListModel.addElement("• " + list.get(i));
                    }
                }
            }
        });
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bot.setOpaque(false);
        bot.add(refreshBtn);
        panel.add(bot, BorderLayout.SOUTH);

        return panel;
    }

    private void handlePostDrive() {
        if (currentCompany == null) return;

        // GUARD: Blocked if isApproved == false
        if (!currentCompany.isApproved()) {
            JOptionPane.showMessageDialog(this, 
                    "Posting Blocked: Your company account is PENDING APPROVAL by the Placement Officer.\n" 
                    + "You cannot publish placement drives until your company is verified.", 
                    "Approval Required", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String role = roleField.getText().trim();
            double pkg = Double.parseDouble(packageField.getText().trim());
            double cgpa = Double.parseDouble(cgpaField.getText().trim());
            int backlogs = Integer.parseInt(backlogsField.getText().trim());
            String date = dateField.getText().trim();
            String venue = venueField.getText().trim();

            if (role.isEmpty() || date.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Role and Drive Date cannot be blank.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String driveId = "DRV" + (DataManager.getInstance().getDrives().size() + 201);
            PlacementDrive drive = new PlacementDrive(driveId, currentCompany, role, pkg, cgpa, backlogs, date);
            
            // Method overloading demonstration
            if (venue.isEmpty()) {
                drive.scheduleInterview(date);
            } else {
                drive.scheduleInterview(date, venue);
            }

            DataManager.getInstance().addPlacementDrive(drive);
            JOptionPane.showMessageDialog(this, "Placement drive published successfully! (Drive ID: " + driveId + ")", "Success", JOptionPane.INFORMATION_MESSAGE);

            roleField.setText("");
            loadPostedDrives();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Package, CGPA, and Backlogs must be valid numeric values.", "Input Error", JOptionPane.ERROR_MESSAGE);
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
                        a.getStudent().getCgpa(),
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
            // Updates status and automatically notifies student via polymorphism
            DataManager.getInstance().updateApplicationStatus(target, newStatus);
            JOptionPane.showMessageDialog(this, 
                    "Status updated to '" + newStatus + "'! Notification dispatched to candidate.", 
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
        dialog.setSize(400, 260);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField roundNameF = new JTextField("Technical Round 1", 15);
        JTextField dateTimeF = new JTextField("2026-11-26 10:30 AM", 15);

        g.gridx = 0; g.gridy = 0;
        dialog.add(new JLabel("Candidate:"), g);
        g.gridx = 1;
        dialog.add(new JLabel(target.getStudent().getName() + " (" + target.getStudent().getUsn() + ")"), g);

        g.gridx = 0; g.gridy = 1;
        dialog.add(new JLabel("Round Name:"), g);
        g.gridx = 1;
        dialog.add(roundNameF, g);

        g.gridx = 0; g.gridy = 2;
        dialog.add(new JLabel("Interview Date/Time:"), g);
        g.gridx = 1;
        dialog.add(dateTimeF, g);

        JButton saveBtn = new JButton("Confirm & Send Schedule");
        saveBtn.setBackground(new Color(40, 167, 69));
        saveBtn.setForeground(Color.WHITE);
        Application finalTarget = target;
        saveBtn.addActionListener(ev -> {
            String round = roundNameF.getText().trim();
            String dt = dateTimeF.getText().trim();
            if (round.isEmpty() || dt.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Round name and date are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String intId = "INT" + (DataManager.getInstance().getInterviews().size() + 401);
            Interview interview = new Interview(intId, finalTarget, round, "Scheduled", dt);
            DataManager.getInstance().addInterview(interview);
            
            // Also update candidate status to Shortlisted if currently just Applied
            if ("Applied".equalsIgnoreCase(finalTarget.getStatus())) {
                DataManager.getInstance().updateApplicationStatus(finalTarget, "Shortlisted");
            }

            JOptionPane.showMessageDialog(dialog, "Interview scheduled! Candidate has been notified.", "Interview Set", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            loadApplicantsForSelectedDrive();
        });

        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        dialog.add(saveBtn, g);

        dialog.setVisible(true);
    }

    public void refreshData() {
        if (currentCompany == null) return;

        // Check if officer updated approval status in background
        for (Company c : DataManager.getInstance().getCompanies()) {
            if (c.getCompanyId().equalsIgnoreCase(currentCompany.getCompanyId())) {
                currentCompany = c;
                break;
            }
        }

        companyNameLabel.setText(currentCompany.getCompanyName() + " (" + currentCompany.getIndustryType() + ")");
        if (currentCompany.isApproved()) {
            statusBadgeLabel.setText("Status: APPROVED ✅");
            statusBadgeLabel.setForeground(new Color(40, 167, 69));
            approvalBanner.setVisible(false);
            postDriveButton.setEnabled(true);
        } else {
            statusBadgeLabel.setText("Status: PENDING APPROVAL ⏳");
            statusBadgeLabel.setForeground(new Color(211, 47, 47));
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
                    d.getPackageOffered(),
                    d.getEligibleCGPA(),
                    d.getMaxBacklogsAllowed(),
                    d.getDriveDate(),
                    d.getVenue(),
                    DataManager.getInstance().getApplicationsForDrive(d).size()
            });
        }
    }
}
