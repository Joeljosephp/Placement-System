package com.placementsystem.gui;

import com.placementsystem.MainFrame;
import com.placementsystem.model.Company;
import com.placementsystem.model.PlacementOfficer;
import com.placementsystem.model.Student;
import com.placementsystem.util.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Authentication & Registration panel.
 * Demonstrates GUI Form Handling, Event Listeners, and DataManager integration.
 */
public class LoginPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private MainFrame mainFrame;
    private JComboBox<String> roleComboBox;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerStudentButton;
    private JButton registerCompanyButton;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 247, 250));
        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 215, 225), 1),
                new EmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints cGbc = new GridBagConstraints();
        cGbc.insets = new Insets(8, 8, 8, 8);
        cGbc.fill = GridBagConstraints.HORIZONTAL;
        cGbc.gridx = 0;
        cGbc.gridy = 0;
        cGbc.gridwidth = 2;

        // Title
        JLabel titleLabel = new JLabel("Placement Management System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(24, 43, 73));
        card.add(titleLabel, cGbc);

        cGbc.gridy++;
        JLabel subtitleLabel = new JLabel("Login to Access Your Portal", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(Color.GRAY);
        card.add(subtitleLabel, cGbc);

        cGbc.gridwidth = 1;

        // Role
        cGbc.gridy++;
        cGbc.gridx = 0;
        JLabel roleLabel = new JLabel("Select Role:");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(roleLabel, cGbc);

        cGbc.gridx = 1;
        String[] roles = {"Student", "Company", "Placement Officer"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(roleComboBox, cGbc);

        // Email
        cGbc.gridy++;
        cGbc.gridx = 0;
        JLabel emailLabel = new JLabel("Email Address:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(emailLabel, cGbc);

        cGbc.gridx = 1;
        emailField = new JTextField(20);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(emailField, cGbc);

        // Password
        cGbc.gridy++;
        cGbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(passLabel, cGbc);

        cGbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(passwordField, cGbc);

        // Login Button
        cGbc.gridy++;
        cGbc.gridx = 0;
        cGbc.gridwidth = 2;
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(new Color(30, 100, 200));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(this::handleLogin);
        card.add(loginButton, cGbc);

        // Divider & Registration
        cGbc.gridy++;
        JSeparator sep = new JSeparator();
        card.add(sep, cGbc);

        cGbc.gridy++;
        JPanel regPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        regPanel.setOpaque(false);

        registerStudentButton = new JButton("Register as Student");
        registerStudentButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        registerStudentButton.addActionListener(e -> showStudentRegistrationDialog());
        regPanel.add(registerStudentButton);

        registerCompanyButton = new JButton("Register Company");
        registerCompanyButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        registerCompanyButton.addActionListener(e -> showCompanyRegistrationDialog());
        regPanel.add(registerCompanyButton);

        card.add(regPanel, cGbc);

        // Demo credentials helper
        cGbc.gridy++;
        JPanel demoBox = new JPanel(new GridLayout(3, 1, 3, 3));
        demoBox.setBorder(new TitledBorder("Demo Accounts"));
        demoBox.setBackground(new Color(248, 250, 252));
        JLabel adminHint = new JLabel("• Officer: admin@ktu.edu / admin123");
        adminHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        JLabel studentHint = new JLabel("• Student: arun@ktu.edu / arun123");
        studentHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        JLabel companyHint = new JLabel("• Company: hr@infosys.com / info123");
        companyHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        demoBox.add(adminHint);
        demoBox.add(studentHint);
        demoBox.add(companyHint);
        card.add(demoBox, cGbc);

        add(card, gbc);
    }

    private void handleLogin(ActionEvent e) {
        String role = (String) roleComboBox.getSelectedItem();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter both Email and Password.", 
                    "Validation Error", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        DataManager dm = DataManager.getInstance();

        if ("Student".equals(role)) {
            Student student = dm.authenticateStudent(email, password);
            if (student != null) {
                clearFields();
                mainFrame.loginUser("Student", student);
            } else {
                showAuthError("Student account not found with provided credentials.");
            }
        } else if ("Company".equals(role)) {
            Company company = dm.authenticateCompany(email, password);
            if (company != null) {
                clearFields();
                mainFrame.loginUser("Company", company);
            } else {
                showAuthError("Company account not found with provided credentials.");
            }
        } else if ("Placement Officer".equals(role)) {
            PlacementOfficer officer = dm.authenticateOfficer(email, password);
            if (officer != null) {
                clearFields();
                mainFrame.loginUser("Placement Officer", officer);
            } else {
                showAuthError("Placement Officer account not found or invalid password.");
            }
        }
    }

    private void showAuthError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Authentication Failed", JOptionPane.ERROR_MESSAGE);
    }

    private void clearFields() {
        emailField.setText("");
        passwordField.setText("");
    }

    private void showStudentRegistrationDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Student Registration", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(420, 480);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField usnF = new JTextField(15);
        JTextField nameF = new JTextField(15);
        JTextField emailF = new JTextField(15);
        JTextField phoneF = new JTextField(15);
        JTextField deptF = new JTextField("Computer Science", 15);
        JTextField cgpaF = new JTextField("8.0", 15);
        JTextField backlogsF = new JTextField("0", 15);
        JPasswordField passF = new JPasswordField(15);

        int row = 0;
        addFormRow(dialog, g, row++, "USN / Roll No:", usnF);
        addFormRow(dialog, g, row++, "Full Name:", nameF);
        addFormRow(dialog, g, row++, "Email:", emailF);
        addFormRow(dialog, g, row++, "Phone:", phoneF);
        addFormRow(dialog, g, row++, "Department:", deptF);
        addFormRow(dialog, g, row++, "CGPA (0.0 - 10.0):", cgpaF);
        addFormRow(dialog, g, row++, "Active Backlogs:", backlogsF);
        addFormRow(dialog, g, row++, "Password:", passF);

        JButton submitBtn = new JButton("Create Student Account");
        submitBtn.setBackground(new Color(30, 100, 200));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.addActionListener(ev -> {
            try {
                String usn = usnF.getText().trim();
                String name = nameF.getText().trim();
                String email = emailF.getText().trim();
                String phone = phoneF.getText().trim();
                String dept = deptF.getText().trim();
                double cgpa = Double.parseDouble(cgpaF.getText().trim());
                int backlogs = Integer.parseInt(backlogsF.getText().trim());
                String pass = new String(passF.getPassword()).trim();

                if (usn.isEmpty() || name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Student s = new Student(usn, name, email, phone, dept, cgpa, backlogs, pass);
                DataManager.getInstance().addStudent(s);
                JOptionPane.showMessageDialog(dialog, "Student registered successfully! You may now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numeric values for CGPA and Backlogs.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException iae) {
                JOptionPane.showMessageDialog(dialog, iae.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 2;
        dialog.add(submitBtn, g);

        dialog.setVisible(true);
    }

    private void showCompanyRegistrationDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Company Registration", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(420, 360);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField compNameF = new JTextField(15);
        JTextField industryF = new JTextField("Software / IT", 15);
        JTextField emailF = new JTextField(15);
        JPasswordField passF = new JPasswordField(15);

        int row = 0;
        addFormRow(dialog, g, row++, "Company Name:", compNameF);
        addFormRow(dialog, g, row++, "Industry Type:", industryF);
        addFormRow(dialog, g, row++, "Email:", emailF);
        addFormRow(dialog, g, row++, "Password:", passF);

        JButton submitBtn = new JButton("Submit for Officer Approval");
        submitBtn.setBackground(new Color(30, 100, 200));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.addActionListener(ev -> {
            try {
                String name = compNameF.getText().trim();
                String ind = industryF.getText().trim();
                String email = emailF.getText().trim();
                String pass = new String(passF.getPassword()).trim();

                if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String compId = "CMP" + (DataManager.getInstance().getCompanies().size() + 101);
                Company c = new Company(compId, name, ind, email, pass);
                DataManager.getInstance().addCompany(c);
                JOptionPane.showMessageDialog(dialog, 
                        "Company registered successfully!\nStatus: PENDING APPROVAL by Placement Officer.", 
                        "Registration Submitted", 
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 2;
        dialog.add(submitBtn, g);

        dialog.setVisible(true);
    }

    private void addFormRow(Container container, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        container.add(lbl, g);

        g.gridx = 1;
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        container.add(field, g);
    }
}
