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
import java.util.List;

public class AdminDashboard extends JPanel {
    private static final long serialVersionUID = 1L;

    private MainFrame mainFrame;
    private PlacementOfficer currentOfficer;

    private JTable pendingCompaniesTable;
    private DefaultTableModel pendingCompaniesModel;

    private JTable allDrivesTable;
    private DefaultTableModel allDrivesModel;

    private JTable summaryStatsTable;
    private DefaultTableModel summaryStatsModel;
    private JTable placedCandidatesTable;
    private DefaultTableModel placedCandidatesModel;

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

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Theme.FONT_BODY_BOLD);

        tabbedPane.addTab("Pending Company Approvals", createPendingApprovalsTab());
        tabbedPane.addTab("All Placement Drives", createAllDrivesTab());
        tabbedPane.addTab("Placement Analytics", createAnalyticsTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createPendingApprovalsTab() {
        JPanel panel = new JPanel(new BorderLayout(Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        panel.setBackground(Theme.COLOR_SURFACE);
        panel.setBorder(new EmptyBorder(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL));

        JLabel header = new JLabel("Company Verification Queue", SwingConstants.LEFT);
        header.setFont(Theme.FONT_TITLE);
        header.setForeground(Theme.COLOR_TEXT_PRIMARY);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Company ID", "Company Name", "Industry Sector", "Official Email", "Status"};
        pendingCompaniesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        pendingCompaniesTable = new JTable(pendingCompaniesModel);
        Theme.styleTable(pendingCompaniesTable);
        pendingCompaniesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(pendingCompaniesTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SPACING_SMALL, 0));
        actionPanel.setOpaque(false);

        JButton refreshBtn = new JButton("Refresh");
        Theme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadPendingCompanies());
        actionPanel.add(refreshBtn);

        JButton approveBtn = new JButton("Approve Selected Company");
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

        // Two-column summary metrics table replacing the 3x2 card grid
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

        // Placed candidates table
        JPanel tableCard = new JPanel(new BorderLayout(Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        tableCard.setOpaque(false);

        JLabel placedTitle = new JLabel("Placed Candidates Directory");
        placedTitle.setFont(Theme.FONT_TITLE);
        placedTitle.setForeground(Theme.COLOR_TEXT_PRIMARY);
        tableCard.add(placedTitle, BorderLayout.NORTH);

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
        int selectedRow = pendingCompaniesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a company to approve.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String compId = (String) pendingCompaniesModel.getValueAt(selectedRow, 0);
        Company target = null;
        for (Company c : DataManager.getInstance().getCompanies()) {
            if (c.getCompanyId().equalsIgnoreCase(compId)) {
                target = c;
                break;
            }
        }

        if (target != null && currentOfficer != null) {
            currentOfficer.approveCompany(target);
            DataManager.getInstance().updateCompany(target);

            JOptionPane.showMessageDialog(this,
                    "Company '" + target.getCompanyName() + "' approved.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            loadPendingCompanies();
            loadAnalytics();
        }
    }

    public void refreshData() {
        loadPendingCompanies();
        loadAllDrives();
        loadAnalytics();
    }

    private void loadPendingCompanies() {
        pendingCompaniesModel.setRowCount(0);
        List<Company> pending = DataManager.getInstance().getPendingCompanies();
        if (pending.isEmpty()) {
            pendingCompaniesModel.addRow(new Object[]{"-", "No pending company registrations", "-", "-", "-"});
            return;
        }
        for (Company c : pending) {
            pendingCompaniesModel.addRow(new Object[]{
                    c.getCompanyId(),
                    c.getCompanyName(),
                    c.getIndustryType(),
                    c.getEmail(),
                    "Pending Verification"
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
        summaryStatsModel.addRow(new Object[]{"Students Placed", String.valueOf(totalPlaced)});
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
