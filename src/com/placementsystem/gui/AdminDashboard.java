package com.placementsystem.gui;

import com.placementsystem.MainFrame;
import com.placementsystem.model.Application;
import com.placementsystem.model.Company;
import com.placementsystem.model.PlacementDrive;
import com.placementsystem.model.PlacementOfficer;
import com.placementsystem.util.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Administrative panel for the Placement Officer.
 * Features a JTabbedPane with 3 tabs:
 *  1. Pending Company Approvals
 *  2. All Active Placement Drives
 *  3. Placement Analytics & Summary Stats
 * Demonstrates Tabbed Panes, Data Filtering, Analytics Computation, and Officer Actions.
 */
public class AdminDashboard extends JPanel {
    private static final long serialVersionUID = 1L;

    private MainFrame mainFrame;
    private PlacementOfficer currentOfficer;

    // Tab 1: Pending Approvals
    private JTable pendingCompaniesTable;
    private DefaultTableModel pendingCompaniesModel;

    // Tab 2: All Active Drives
    private JTable allDrivesTable;
    private DefaultTableModel allDrivesModel;

    // Tab 3: Analytics components
    private JLabel totalStudentsVal;
    private JLabel totalPlacedVal;
    private JLabel placementRateVal;
    private JLabel totalCompaniesVal;
    private JLabel totalDrivesVal;
    private JLabel avgCgpaVal;
    private JTable placedCandidatesTable;
    private DefaultTableModel placedCandidatesModel;

    public AdminDashboard(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 20, 15, 20));
        setBackground(new Color(245, 247, 250));

        initComponents();
    }

    public void setCurrentOfficer(PlacementOfficer officer) {
        this.currentOfficer = officer;
        refreshData();
    }

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("1. Pending Company Approvals", createPendingApprovalsTab());
        tabbedPane.addTab("2. All Active Placement Drives", createAllDrivesTab());
        tabbedPane.addTab("3. Placement Analytics & Summary Stats", createAnalyticsTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createPendingApprovalsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel("Company Verification Queue", SwingConstants.LEFT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setForeground(new Color(24, 43, 73));
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Company ID", "Company Name", "Industry Sector", "Official Email", "Approval Status"};
        pendingCompaniesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        pendingCompaniesTable = new JTable(pendingCompaniesModel);
        pendingCompaniesTable.setRowHeight(26);
        pendingCompaniesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(pendingCompaniesTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setOpaque(false);

        JButton refreshBtn = new JButton("Refresh Queue");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshBtn.addActionListener(e -> loadPendingCompanies());
        actionPanel.add(refreshBtn);

        JButton approveBtn = new JButton("Approve Selected Company");
        approveBtn.setBackground(new Color(40, 167, 69));
        approveBtn.setForeground(Color.WHITE);
        approveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        approveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        approveBtn.addActionListener(e -> approveSelectedCompany());
        actionPanel.add(approveBtn);

        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAllDrivesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel("University Campus Recruitment Drives", SwingConstants.LEFT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setForeground(new Color(24, 43, 73));
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Drive ID", "Company", "Role", "Package (LPA)", "Min CGPA", "Max Backlogs", "Date", "Venue", "Total Applicants"};
        allDrivesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        allDrivesTable = new JTable(allDrivesModel);
        allDrivesTable.setRowHeight(26);
        panel.add(new JScrollPane(allDrivesTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh Drives");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshBtn.addActionListener(e -> loadAllDrives());
        bottomBar.add(refreshBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAnalyticsTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Metric Cards Grid
        JPanel metricsGrid = new JPanel(new GridLayout(2, 3, 15, 15));
        metricsGrid.setOpaque(false);

        totalStudentsVal = new JLabel("0", SwingConstants.CENTER);
        totalPlacedVal = new JLabel("0", SwingConstants.CENTER);
        placementRateVal = new JLabel("0.0%", SwingConstants.CENTER);
        totalCompaniesVal = new JLabel("0", SwingConstants.CENTER);
        totalDrivesVal = new JLabel("0", SwingConstants.CENTER);
        avgCgpaVal = new JLabel("0.0", SwingConstants.CENTER);

        metricsGrid.add(createMetricCard("Total Students Enrolled", totalStudentsVal, new Color(41, 128, 185)));
        metricsGrid.add(createMetricCard("Students Placed (Offers)", totalPlacedVal, new Color(39, 174, 96)));
        metricsGrid.add(createMetricCard("Placement Rate", placementRateVal, new Color(142, 68, 173)));
        metricsGrid.add(createMetricCard("Partner Companies", totalCompaniesVal, new Color(211, 84, 0)));
        metricsGrid.add(createMetricCard("Recruitment Drives", totalDrivesVal, new Color(22, 160, 133)));
        metricsGrid.add(createMetricCard("Average Student CGPA", avgCgpaVal, new Color(44, 62, 80)));

        panel.add(metricsGrid, BorderLayout.NORTH);

        // Placed candidates table
        JPanel tableCard = new JPanel(new BorderLayout(5, 5));
        tableCard.setOpaque(false);
        tableCard.setBorder(BorderFactory.createTitledBorder("Selected / Placed Candidates Directory"));

        String[] cols = {"Student USN", "Student Name", "Department", "CGPA", "Hiring Company", "Job Role", "Offered CTC"};
        placedCandidatesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        placedCandidatesTable = new JTable(placedCandidatesModel);
        placedCandidatesTable.setRowHeight(24);
        tableCard.add(new JScrollPane(placedCandidatesTable), BorderLayout.CENTER);

        panel.add(tableCard, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(new Color(250, 252, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 240), 1),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(Color.GRAY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(accentColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
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
            // Invokes PlacementOfficer method
            currentOfficer.approveCompany(target);
            DataManager.getInstance().updateCompany(target);

            JOptionPane.showMessageDialog(this, 
                    "Company '" + target.getCompanyName() + "' has been approved!\nNotification sent to company recruiter.", 
                    "Approval Completed", 
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
                    d.getPackageOffered() + " LPA",
                    d.getEligibleCGPA(),
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

        totalStudentsVal.setText(String.valueOf(totalStudents));
        totalPlacedVal.setText(String.valueOf(totalPlaced));
        totalCompaniesVal.setText(String.valueOf(totalCompanies));
        totalDrivesVal.setText(String.valueOf(totalDrives));
        avgCgpaVal.setText(String.valueOf(avgCgpa));

        double rate = (totalStudents > 0) ? ((double) totalPlaced / totalStudents) * 100.0 : 0.0;
        placementRateVal.setText(String.format("%.1f%%", rate));

        // Load placed candidates list
        placedCandidatesModel.setRowCount(0);
        for (Application a : dm.getApplications()) {
            if ("Selected".equalsIgnoreCase(a.getStatus()) && a.getStudent() != null) {
                String comp = (a.getDrive() != null && a.getDrive().getCompany() != null) 
                        ? a.getDrive().getCompany().getCompanyName() : "N/A";
                String role = (a.getDrive() != null) ? a.getDrive().getRole() : "N/A";
                String pkg = (a.getDrive() != null) ? a.getDrive().getPackageOffered() + " LPA" : "N/A";

                placedCandidatesModel.addRow(new Object[]{
                        a.getStudent().getUsn(),
                        a.getStudent().getName(),
                        a.getStudent().getDepartment(),
                        a.getStudent().getCgpa(),
                        comp,
                        role,
                        pkg
                });
            }
        }
    }
}
