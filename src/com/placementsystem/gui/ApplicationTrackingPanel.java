package com.placementsystem.gui;

import com.placementsystem.MainFrame;
import com.placementsystem.model.Application;
import com.placementsystem.model.Interview;
import com.placementsystem.util.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Universal application pipeline and interview tracking dashboard.
 * Demonstrates JTable, TableRowSorter, Filtering, and Association navigation.
 */
public class ApplicationTrackingPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private MainFrame mainFrame;

    private JComboBox<String> filterComboBox;
    private JTextField searchField;
    private JTable applicationsTable;
    private DefaultTableModel applicationsTableModel;

    private JTable interviewsTable;
    private DefaultTableModel interviewsTableModel;

    private JLabel selectedAppLabel;

    public ApplicationTrackingPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 20, 15, 20));
        setBackground(new Color(245, 247, 250));

        initComponents();
    }

    private void initComponents() {
        // North: Title and Search/Filter Bar
        JPanel northPanel = new JPanel(new BorderLayout(10, 10));
        northPanel.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 225), 1),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel titleLabel = new JLabel("Application Pipeline & Interview Tracking", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(new Color(24, 43, 73));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Filter Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        controls.add(new JLabel("Filter Status:"));
        String[] filters = {"All Statuses", "Applied", "Shortlisted", "Selected", "Rejected"};
        filterComboBox = new JComboBox<>(filters);
        filterComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterComboBox.addActionListener(e -> loadApplications());
        controls.add(filterComboBox);

        controls.add(new JLabel("Search:"));
        searchField = new JTextField(12);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        controls.add(searchField);

        JButton searchBtn = new JButton("Filter");
        searchBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchBtn.addActionListener(e -> loadApplications());
        controls.add(searchBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            filterComboBox.setSelectedIndex(0);
            loadApplications();
        });
        controls.add(refreshBtn);

        headerPanel.add(controls, BorderLayout.EAST);
        northPanel.add(headerPanel, BorderLayout.NORTH);
        add(northPanel, BorderLayout.NORTH);

        // Center: Split pane for Applications and Interviews
        JPanel splitPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        splitPanel.setOpaque(false);

        // Top: Applications Table
        JPanel appsCard = new JPanel(new BorderLayout(5, 5));
        appsCard.setBackground(Color.WHITE);
        appsCard.setBorder(BorderFactory.createTitledBorder("1. Candidate Applications Directory"));

        String[] appCols = {"Application ID", "Student Name", "USN", "Dept", "CGPA", "Company", "Role", "CTC (LPA)", "Status", "Applied On"};
        applicationsTableModel = new DefaultTableModel(appCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        applicationsTable = new JTable(applicationsTableModel);
        applicationsTable.setRowHeight(24);
        applicationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        applicationsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onApplicationSelected();
            }
        });
        appsCard.add(new JScrollPane(applicationsTable), BorderLayout.CENTER);
        splitPanel.add(appsCard);

        // Bottom: Interviews Table
        JPanel interviewsCard = new JPanel(new BorderLayout(5, 5));
        interviewsCard.setBackground(Color.WHITE);
        interviewsCard.setBorder(BorderFactory.createTitledBorder("2. Interview Rounds Scheduled for Selected Application"));

        selectedAppLabel = new JLabel(" Select an application above to inspect interview schedules.", SwingConstants.LEFT);
        selectedAppLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        selectedAppLabel.setForeground(Color.GRAY);
        interviewsCard.add(selectedAppLabel, BorderLayout.NORTH);

        String[] intCols = {"Interview ID", "Application ID", "Round Name", "Result / Status", "Interview Date & Time"};
        interviewsTableModel = new DefaultTableModel(intCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        interviewsTable = new JTable(interviewsTableModel);
        interviewsTable.setRowHeight(24);
        interviewsCard.add(new JScrollPane(interviewsTable), BorderLayout.CENTER);
        splitPanel.add(interviewsCard);

        add(splitPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        loadApplications();
    }

    private void loadApplications() {
        applicationsTableModel.setRowCount(0);
        interviewsTableModel.setRowCount(0);
        selectedAppLabel.setText(" Select an application above to inspect interview schedules.");

        String statusFilter = (String) filterComboBox.getSelectedItem();
        String query = searchField.getText().trim().toLowerCase();

        List<Application> apps = DataManager.getInstance().getApplications();
        for (Application a : apps) {
            if (a.getStudent() == null || a.getDrive() == null) continue;

            // Apply status filter
            if (!"All Statuses".equalsIgnoreCase(statusFilter) && !a.getStatus().equalsIgnoreCase(statusFilter)) {
                continue;
            }

            // Apply search query filter
            String studentName = a.getStudent().getName().toLowerCase();
            String studentUsn = a.getStudent().getUsn().toLowerCase();
            String companyName = (a.getDrive().getCompany() != null) 
                    ? a.getDrive().getCompany().getCompanyName().toLowerCase() : "";
            String role = a.getDrive().getRole().toLowerCase();

            if (!query.isEmpty()) {
                if (!studentName.contains(query) && !studentUsn.contains(query) 
                        && !companyName.contains(query) && !role.contains(query)) {
                    continue;
                }
            }

            String cName = (a.getDrive().getCompany() != null) ? a.getDrive().getCompany().getCompanyName() : "N/A";

            applicationsTableModel.addRow(new Object[]{
                    a.getApplicationId(),
                    a.getStudent().getName(),
                    a.getStudent().getUsn(),
                    a.getStudent().getDepartment(),
                    a.getStudent().getCgpa(),
                    cName,
                    a.getDrive().getRole(),
                    a.getDrive().getPackageOffered(),
                    a.getStatus(),
                    a.getAppliedDate()
            });
        }
    }

    private void onApplicationSelected() {
        int row = applicationsTable.getSelectedRow();
        interviewsTableModel.setRowCount(0);
        if (row == -1) {
            selectedAppLabel.setText(" Select an application above to inspect interview schedules.");
            return;
        }

        String appId = (String) applicationsTableModel.getValueAt(row, 0);
        String sName = (String) applicationsTableModel.getValueAt(row, 1);
        String cName = (String) applicationsTableModel.getValueAt(row, 5);
        String role = (String) applicationsTableModel.getValueAt(row, 6);

        selectedAppLabel.setText(" Showing interview rounds for: " + sName + " -> " + role + " at " + cName + " (" + appId + ")");

        List<Interview> allInterviews = DataManager.getInstance().getInterviews();
        for (Interview inv : allInterviews) {
            if (inv.getApplication() != null && inv.getApplication().getApplicationId().equalsIgnoreCase(appId)) {
                interviewsTableModel.addRow(new Object[]{
                        inv.getInterviewId(),
                        appId,
                        inv.getRoundName(),
                        inv.getResult(),
                        inv.getInterviewDate()
                });
            }
        }

        if (interviewsTableModel.getRowCount() == 0) {
            selectedAppLabel.setText(" No interview rounds scheduled yet for: " + sName + " (" + appId + ")");
        }
    }
}
