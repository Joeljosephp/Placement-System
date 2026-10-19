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

    @SuppressWarnings("this-escape")
    public ApplicationTrackingPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        setBorder(new EmptyBorder(Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        setBackground(Theme.COLOR_BG);

        initComponents();
    }

    private void initComponents() {
        JPanel northPanel = new JPanel(new BorderLayout(Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        northPanel.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.COLOR_SURFACE);
        headerPanel.setBorder(Theme.createCardBorder());

        JLabel titleLabel = new JLabel("Application Pipeline and Interview Tracking", SwingConstants.LEFT);
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.COLOR_TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SPACING_SMALL, 0));
        controls.setOpaque(false);

        JLabel filterLbl = new JLabel("Status:");
        filterLbl.setFont(Theme.FONT_BODY_BOLD);
        controls.add(filterLbl);

        String[] filters = {"All Statuses", "Applied", "Shortlisted", "Selected", "Rejected"};
        filterComboBox = new JComboBox<>(filters);
        filterComboBox.setFont(Theme.FONT_BODY);
        filterComboBox.addActionListener(e -> loadApplications());
        controls.add(filterComboBox);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(Theme.FONT_BODY_BOLD);
        controls.add(searchLbl);

        searchField = new JTextField(12);
        searchField.setFont(Theme.FONT_BODY);
        controls.add(searchField);

        JButton searchBtn = new JButton("Filter");
        Theme.stylePrimaryButton(searchBtn);
        searchBtn.addActionListener(e -> loadApplications());
        controls.add(searchBtn);

        JButton refreshBtn = new JButton("Reset");
        Theme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            filterComboBox.setSelectedIndex(0);
            loadApplications();
        });
        JButton exportBtn = new JButton("Export CSV");
        Theme.styleSecondaryButton(exportBtn);
        exportBtn.addActionListener(e -> exportPipelineToCsv());
        controls.add(exportBtn);

        headerPanel.add(controls, BorderLayout.EAST);
        northPanel.add(headerPanel, BorderLayout.NORTH);
        add(northPanel, BorderLayout.NORTH);

        JPanel splitPanel = new JPanel(new GridLayout(2, 1, Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        splitPanel.setOpaque(false);

        JPanel appsCard = new JPanel(new BorderLayout(Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        appsCard.setBackground(Theme.COLOR_SURFACE);
        appsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.COLOR_BORDER, 1),
                new EmptyBorder(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL)
        ));

        JLabel appsHeader = new JLabel("Candidate Applications");
        appsHeader.setFont(Theme.FONT_BODY_BOLD);
        appsCard.add(appsHeader, BorderLayout.NORTH);

        String[] appCols = {"App ID", "Student Name", "USN", "Department", "CGPA", "Company", "Role", "CTC (LPA)", "Status", "Applied On"};
        applicationsTableModel = new DefaultTableModel(appCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        applicationsTable = new JTable(applicationsTableModel);
        Theme.styleTable(applicationsTable);
        applicationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Right-align numeric columns
        applicationsTable.getColumnModel().getColumn(4).setCellRenderer(Theme.getRightAlignRenderer());
        applicationsTable.getColumnModel().getColumn(7).setCellRenderer(Theme.getRightAlignRenderer());

        applicationsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onApplicationSelected();
            }
        });
        appsCard.add(new JScrollPane(applicationsTable), BorderLayout.CENTER);
        splitPanel.add(appsCard);

        JPanel interviewsCard = new JPanel(new BorderLayout(Theme.SPACING_SMALL, Theme.SPACING_SMALL));
        interviewsCard.setBackground(Theme.COLOR_SURFACE);
        interviewsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.COLOR_BORDER, 1),
                new EmptyBorder(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL)
        ));

        selectedAppLabel = new JLabel("Select an application above to inspect interview schedules.", SwingConstants.LEFT);
        selectedAppLabel.setFont(Theme.FONT_SMALL);
        selectedAppLabel.setForeground(Theme.COLOR_TEXT_MUTED);
        interviewsCard.add(selectedAppLabel, BorderLayout.NORTH);

        String[] intCols = {"Interview ID", "Application ID", "Round Name", "Result", "Date and Time"};
        interviewsTableModel = new DefaultTableModel(intCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        interviewsTable = new JTable(interviewsTableModel);
        Theme.styleTable(interviewsTable);
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
        selectedAppLabel.setText("Select an application above to inspect interview schedules.");

        String statusFilter = (String) filterComboBox.getSelectedItem();
        String query = searchField.getText().trim().toLowerCase();

        List<Application> apps = DataManager.getInstance().getApplications();
        int matched = 0;
        for (Application a : apps) {
            if (a.getStudent() == null || a.getDrive() == null) continue;

            if (!"All Statuses".equalsIgnoreCase(statusFilter) && !a.getStatus().equalsIgnoreCase(statusFilter)) {
                continue;
            }

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
                    String.format("%.2f", a.getStudent().getCgpa()),
                    cName,
                    a.getDrive().getRole(),
                    String.format("%.1f", a.getDrive().getPackageOffered()),
                    a.getStatus(),
                    a.getAppliedDate()
            });
            matched++;
        }

        if (matched == 0) {
            applicationsTableModel.addRow(new Object[]{"-", "No applications match criteria", "-", "-", "-", "-", "-", "-", "-", "-"});
        }
    }

    private void onApplicationSelected() {
        int row = applicationsTable.getSelectedRow();
        interviewsTableModel.setRowCount(0);
        if (row == -1) {
            selectedAppLabel.setText("Select an application above to inspect interview schedules.");
            return;
        }

        String appId = (String) applicationsTableModel.getValueAt(row, 0);
        if ("-".equals(appId)) {
            selectedAppLabel.setText("Select an application above to inspect interview schedules.");
            return;
        }

        String sName = (String) applicationsTableModel.getValueAt(row, 1);
        String cName = (String) applicationsTableModel.getValueAt(row, 5);
        String role = (String) applicationsTableModel.getValueAt(row, 6);

        selectedAppLabel.setText("Interview rounds for: " + sName + " - " + role + " at " + cName + " (" + appId + ")");

        List<Interview> allInterviews = DataManager.getInstance().getInterviews();
        int count = 0;
        for (Interview inv : allInterviews) {
            if (inv.getApplication() != null && inv.getApplication().getApplicationId().equalsIgnoreCase(appId)) {
                interviewsTableModel.addRow(new Object[]{
                        inv.getInterviewId(),
                        appId,
                        inv.getRoundName(),
                        inv.getResult(),
                        inv.getInterviewDate()
                });
                count++;
            }
        }

        if (count == 0) {
            selectedAppLabel.setText("No interview rounds scheduled yet for: " + sName + " (" + appId + ")");
        }
    }

    public void clearSession() {
        if (applicationsTableModel != null) applicationsTableModel.setRowCount(0);
        if (interviewsTableModel != null) interviewsTableModel.setRowCount(0);
        if (searchField != null) searchField.setText("");
        if (selectedAppLabel != null) selectedAppLabel.setText("Select an application above to inspect interview schedules.");
    }

    private void exportPipelineToCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Pipeline CSV");
        fileChooser.setSelectedFile(new java.io.File("placement_pipeline.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File fileToSave = fileChooser.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
            fileToSave = new java.io.File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
        }

        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(fileToSave))) {
            writer.println("App ID,Student Name,USN,Department,CGPA,Company,Role,CTC (LPA),Status,Applied On");
            for (int r = 0; r < applicationsTableModel.getRowCount(); r++) {
                String firstVal = String.valueOf(applicationsTableModel.getValueAt(r, 0));
                if ("-".equals(firstVal)) continue;

                StringBuilder line = new StringBuilder();
                for (int c = 0; c < applicationsTableModel.getColumnCount(); c++) {
                    String val = String.valueOf(applicationsTableModel.getValueAt(r, c));
                    if (val.contains(",") || val.contains("\"")) {
                        val = "\"" + val.replace("\"", "\"\"") + "\"";
                    }
                    line.append(val);
                    if (c < applicationsTableModel.getColumnCount() - 1) {
                        line.append(",");
                    }
                }
                writer.println(line.toString());
            }
            JOptionPane.showMessageDialog(this, "Pipeline CSV exported successfully to:\n" + fileToSave.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to export CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
