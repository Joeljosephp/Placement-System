package com.placementsystem.gui;

import com.placementsystem.MainFrame;
import com.placementsystem.model.Application;
import com.placementsystem.model.Company;
import com.placementsystem.model.PlacementDrive;
import com.placementsystem.model.PlacementOfficer;
import com.placementsystem.util.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

public class AdminDashboard extends JPanel {
    private static final long serialVersionUID = 1L;

    private MainFrame mainFrame;
    private PlacementOfficer currentOfficer;

    private JComboBox<String> companyFilterCombo;
    private JTable companiesTable;
    private DefaultTableModel companiesModel;

    private JTable allDrivesTable;
    private DefaultTableModel allDrivesModel;

    private JTable summaryStatsTable;
    private DefaultTableModel summaryStatsModel;
    private JTable placedCandidatesTable;
    private DefaultTableModel placedCandidatesModel;

    @SuppressWarnings("this-escape")
    public AdminDashboard(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        setBorder(new EmptyBorder(Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        setBackground(Theme.COLOR_BG);

        initComponents();
    }

    public void setCurrentOfficer(PlacementOfficer officer) {
        this.currentOfficer = officer;
        refreshData();
    }

    public void clearSession() {
        this.currentOfficer = null;
        if (companiesModel != null) companiesModel.setRowCount(0);
        if (allDrivesModel != null) allDrivesModel.setRowCount(0);
        if (summaryStatsModel != null) summaryStatsModel.setRowCount(0);
        if (placedCandidatesModel != null) placedCandidatesModel.setRowCount(0);
    }

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Theme.FONT_BODY_BOLD);

        tabbedPane.addTab("Company Verification", createCompanyVerificationTab());
        tabbedPane.addTab("All Placement Drives", createAllDrivesTab());
        tabbedPane.addTab("Placement Analytics", createAnalyticsTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createCompanyVerificationTab() {
        JPanel panel = new JPanel(new BorderLayout(Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        panel.setBackground(Theme.COLOR_SURFACE);
        panel.setBorder(new EmptyBorder(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel header = new JLabel("Company Approvals and Directory", SwingConstants.LEFT);
        header.setFont(Theme.FONT_TITLE);
        header.setForeground(Theme.COLOR_TEXT_PRIMARY);
        topBar.add(header, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SPACING_SMALL, 0));
        filterPanel.setOpaque(false);
        JLabel filterLbl = new JLabel("View:");
        filterLbl.setFont(Theme.FONT_BODY_BOLD);
        filterPanel.add(filterLbl);

        String[] filters = {"Pending Verification", "Approved Companies", "Rejected Companies", "All Companies"};
        companyFilterCombo = new JComboBox<>(filters);
        companyFilterCombo.setFont(Theme.FONT_BODY);
        companyFilterCombo.addActionListener(e -> loadCompanies());
        filterPanel.add(companyFilterCombo);

        topBar.add(filterPanel, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Company ID", "Company Name", "Industry Sector", "Official Email", "Status", "Notes / Rejection Reason"};
        companiesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        companiesTable = new JTable(companiesModel);
        Theme.styleTable(companiesTable);
        companiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(companiesTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SPACING_SMALL, 0));
        actionPanel.setOpaque(false);

        JButton refreshBtn = new JButton("Refresh");
        Theme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadCompanies());
        actionPanel.add(refreshBtn);

        JButton rejectBtn = new JButton("Reject Selected");
        Theme.styleSecondaryButton(rejectBtn);
        rejectBtn.addActionListener(e -> rejectSelectedCompany());
        actionPanel.add(rejectBtn);

        JButton approveBtn = new JButton("Approve Selected");
        Theme.stylePrimaryButton(approveBtn);
        approveBtn.addActionListener(e -> approveSelectedCompany());
        actionPanel.add(approveBtn);

        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAllDrivesTab() {
        JPanel panel = new JPanel(new BorderLayout(Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        panel.setBackground(Theme.COLOR_SURFACE);
        panel.setBorder(new EmptyBorder(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL));

        JLabel header = new JLabel("Active Campus Recruitment Drives", SwingConstants.LEFT);
        header.setFont(Theme.FONT_TITLE);
        header.setForeground(Theme.COLOR_TEXT_PRIMARY);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Drive ID", "Company", "Role", "Package (LPA)", "Min CGPA", "Max Backlogs", "Date", "Venue", "Applicants"};
        allDrivesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        allDrivesTable = new JTable(allDrivesModel);
        Theme.styleTable(allDrivesTable);

        // Right-align numeric columns
        allDrivesTable.getColumnModel().getColumn(3).setCellRenderer(Theme.getRightAlignRenderer());
        allDrivesTable.getColumnModel().getColumn(4).setCellRenderer(Theme.getRightAlignRenderer());
        allDrivesTable.getColumnModel().getColumn(5).setCellRenderer(Theme.getRightAlignRenderer());
        allDrivesTable.getColumnModel().getColumn(8).setCellRenderer(Theme.getRightAlignRenderer());

        panel.add(new JScrollPane(allDrivesTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SPACING_SMALL, 0));
        bottomBar.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh");
        Theme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadAllDrives());
        bottomBar.add(refreshBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAnalyticsTab() {
        JPanel panel = new JPanel(new BorderLayout(Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        panel.setBackground(Theme.COLOR_SURFACE);
        panel.setBorder(new EmptyBorder(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL));

        JPanel summaryPanel = new JPanel(new BorderLayout(Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        summaryPanel.setOpaque(false);

        JLabel summaryTitle = new JLabel("Placement Overview Statistics");
        summaryTitle.setFont(Theme.FONT_TITLE);
        summaryTitle.setForeground(Theme.COLOR_TEXT_PRIMARY);
        summaryPanel.add(summaryTitle, BorderLayout.NORTH);

        String[] statCols = {"Metric", "Value"};
        summaryStatsModel = new DefaultTableModel(statCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        summaryStatsTable = new JTable(summaryStatsModel);
        Theme.styleTable(summaryStatsTable);
        summaryStatsTable.getColumnModel().getColumn(1).setCellRenderer(Theme.getRightAlignRenderer());
        summaryStatsTable.setPreferredScrollableViewportSize(new Dimension(500, 150));

        summaryPanel.add(new JScrollPane(summaryStatsTable), BorderLayout.CENTER);
        panel.add(summaryPanel, BorderLayout.NORTH);

        JPanel tableCard = new JPanel(new BorderLayout(Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        tableCard.setOpaque(false);

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel placedTitle = new JLabel("Placed Candidates Directory");
        placedTitle.setFont(Theme.FONT_TITLE);
        placedTitle.setForeground(Theme.COLOR_TEXT_PRIMARY);
        tableHeader.add(placedTitle, BorderLayout.WEST);

        JButton exportCsvBtn = new JButton("Export Placed Students (CSV)");
        Theme.styleSecondaryButton(exportCsvBtn);
        exportCsvBtn.addActionListener(e -> exportPlacedStudentsToCsv());
        tableHeader.add(exportCsvBtn, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] cols = {"Student USN", "Student Name", "Department", "CGPA", "Company", "Job Role", "CTC (LPA)"};
        placedCandidatesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        placedCandidatesTable = new JTable(placedCandidatesModel);
        Theme.styleTable(placedCandidatesTable);

        // Right-align CGPA and CTC
        placedCandidatesTable.getColumnModel().getColumn(3).setCellRenderer(Theme.getRightAlignRenderer());
        placedCandidatesTable.getColumnModel().getColumn(6).setCellRenderer(Theme.getRightAlignRenderer());

        tableCard.add(new JScrollPane(placedCandidatesTable), BorderLayout.CENTER);
        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    private void approveSelectedCompany() {
        int selectedRow = companiesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a company to approve.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String compId = (String) companiesModel.getValueAt(selectedRow, 0);
        if ("-".equals(compId)) return;

        Company target = null;
        for (Company c : DataManager.getInstance().getCompanies()) {
            if (c.getCompanyId().equalsIgnoreCase(compId)) {
                target = c;
                break;
            }
        }

        if (target == null) {
            JOptionPane.showMessageDialog(this, "Company record not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (target.isApproved()) {
            JOptionPane.showMessageDialog(this, "Company '" + target.getCompanyName() + "' is already approved.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Approve company '" + target.getCompanyName() + "' for campus recruitment?",
                "Confirm Approval",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        if (currentOfficer != null) {
            currentOfficer.approveCompany(target);
            DataManager.getInstance().updateCompany(target);

            JOptionPane.showMessageDialog(this,
                    "Company '" + target.getCompanyName() + "' approved.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            loadCompanies();
            loadAnalytics();
        }
    }

    private void rejectSelectedCompany() {
        int selectedRow = companiesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a company to reject.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String compId = (String) companiesModel.getValueAt(selectedRow, 0);
        if ("-".equals(compId)) return;

        Company target = null;
        for (Company c : DataManager.getInstance().getCompanies()) {
            if (c.getCompanyId().equalsIgnoreCase(compId)) {
                target = c;
                break;
            }
        }

        if (target == null) {
            JOptionPane.showMessageDialog(this, "Company record not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (target.isRejected()) {
            JOptionPane.showMessageDialog(this, "Company is already rejected.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String reason = JOptionPane.showInputDialog(
                this,
                "Enter reason for rejecting '" + target.getCompanyName() + "':",
                "Reject Company Registration",
                JOptionPane.WARNING_MESSAGE
        );

        if (reason == null || reason.trim().isEmpty()) {
            return;
        }

        if (currentOfficer != null) {
            currentOfficer.rejectCompany(target, reason.trim());
            DataManager.getInstance().updateCompany(target);

            JOptionPane.showMessageDialog(this,
                    "Company '" + target.getCompanyName() + "' has been rejected.",
                    "Company Rejected",
                    JOptionPane.INFORMATION_MESSAGE);

            loadCompanies();
            loadAnalytics();
        }
    }

    private void exportPlacedStudentsToCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Placed Students CSV");
        fileChooser.setSelectedFile(new File("placed_students.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileToSave = fileChooser.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
            fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
            writer.println("Student USN,Student Name,Department,CGPA,Company,Job Role,CTC (LPA)");
            for (int r = 0; r < placedCandidatesModel.getRowCount(); r++) {
                StringBuilder line = new StringBuilder();
                for (int c = 0; c < placedCandidatesModel.getColumnCount(); c++) {
                    String val = String.valueOf(placedCandidatesModel.getValueAt(r, c));
                    if (val.contains(",") || val.contains("\"")) {
                        val = "\"" + val.replace("\"", "\"\"") + "\"";
                    }
                    line.append(val);
                    if (c < placedCandidatesModel.getColumnCount() - 1) {
                        line.append(",");
                    }
                }
                writer.println(line.toString());
            }
            JOptionPane.showMessageDialog(this, "CSV exported successfully to:\n" + fileToSave.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to export CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshData() {
        loadCompanies();
        loadAllDrives();
        loadAnalytics();
    }

    private void loadCompanies() {
        companiesModel.setRowCount(0);
        String filter = companyFilterCombo != null ? (String) companyFilterCombo.getSelectedItem() : "Pending Verification";

        List<Company> list;
        if ("Approved Companies".equals(filter)) {
            list = DataManager.getInstance().getApprovedCompanies();
        } else if ("Rejected Companies".equals(filter)) {
            list = DataManager.getInstance().getRejectedCompanies();
        } else if ("All Companies".equals(filter)) {
            list = DataManager.getInstance().getCompanies();
        } else {
            list = DataManager.getInstance().getPendingCompanies();
        }

        if (list.isEmpty()) {
            companiesModel.addRow(new Object[]{"-", "No companies in this category", "-", "-", "-", "-"});
            return;
        }

        for (Company c : list) {
            String status = c.isApproved() ? "Approved" : (c.isRejected() ? "Rejected" : "Pending Verification");
            String reason = c.getRejectionReason() != null ? c.getRejectionReason() : "-";
            companiesModel.addRow(new Object[]{
                    c.getCompanyId(),
                    c.getCompanyName(),
                    c.getIndustryType(),
                    c.getEmail(),
                    status,
                    reason
            });
        }
    }

    private void loadAllDrives() {
        allDrivesModel.setRowCount(0);
        List<PlacementDrive> drives = DataManager.getInstance().getDrives();
        for (PlacementDrive d : drives) {
            String compName = (d.getCompany() != null) ? d.getCompany().getCompanyName() : "N/A";
            allDrivesModel.addRow(new Object[]{
                    d.getDriveId(),
                    compName,
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

    private void loadAnalytics() {
        DataManager dm = DataManager.getInstance();

        int totalStudents = dm.getTotalStudentsCount();
        int totalPlaced = dm.getPlacedStudentsCount();
        int totalCompanies = dm.getTotalCompaniesCount();
        int totalDrives = dm.getTotalDrivesCount();
        double avgCgpa = dm.getAverageCGPA();
        double rate = (totalStudents > 0) ? ((double) totalPlaced / totalStudents) * 100.0 : 0.0;

        summaryStatsModel.setRowCount(0);
        summaryStatsModel.addRow(new Object[]{"Total Students Enrolled", String.valueOf(totalStudents)});
        summaryStatsModel.addRow(new Object[]{"Students Placed (Distinct)", String.valueOf(totalPlaced)});
        summaryStatsModel.addRow(new Object[]{"Placement Rate", String.format("%.1f%%", rate)});
        summaryStatsModel.addRow(new Object[]{"Registered Partner Companies", String.valueOf(totalCompanies)});
        summaryStatsModel.addRow(new Object[]{"Total Placement Drives", String.valueOf(totalDrives)});
        summaryStatsModel.addRow(new Object[]{"Average Student CGPA", String.format("%.2f", avgCgpa)});

        placedCandidatesModel.setRowCount(0);
        for (Application a : dm.getApplications()) {
            if ("Selected".equalsIgnoreCase(a.getStatus()) && a.getStudent() != null) {
                String comp = (a.getDrive() != null && a.getDrive().getCompany() != null)
                        ? a.getDrive().getCompany().getCompanyName() : "N/A";
                String role = (a.getDrive() != null) ? a.getDrive().getRole() : "N/A";
                String pkg = (a.getDrive() != null) ? String.format("%.1f", a.getDrive().getPackageOffered()) : "N/A";

                placedCandidatesModel.addRow(new Object[]{
                        a.getStudent().getUsn(),
                        a.getStudent().getName(),
                        a.getStudent().getDepartment(),
                        String.format("%.2f", a.getStudent().getCgpa()),
                        comp,
                        role,
                        pkg
                });
            }
        }
    }
}
