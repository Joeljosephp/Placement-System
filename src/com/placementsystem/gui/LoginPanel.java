package com.placementsystem.gui;

import com.placementsystem.MainFrame;
import com.placementsystem.model.Company;
import com.placementsystem.model.PlacementOfficer;
import com.placementsystem.model.Student;
import com.placementsystem.util.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final MainFrame mainFrame;
    private final boolean showDemoHints;

    private JComboBox<String> roleComboBox;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel emailErrorLabel;
    private JLabel passwordErrorLabel;

    private JButton loginButton;
    private JButton registerStudentButton;
    private JButton registerCompanyButton;

    public LoginPanel(MainFrame mainFrame) {
        this(mainFrame, false);
    }

    public LoginPanel(MainFrame mainFrame, boolean showDemoHints) {
        this.mainFrame = mainFrame;
        this.showDemoHints = showDemoHints;
        setLayout(new GridBagLayout());
        setBackground(Theme.COLOR_BG);
        initComponents();
    }

    private void initComponents() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Theme.COLOR_SURFACE);
        card.setBorder(Theme.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel titleLabel = new JLabel("Placement Management System", SwingConstants.CENTER);
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.COLOR_TEXT_PRIMARY);
        card.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        // Role selection
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(Theme.FONT_BODY_BOLD);
        roleLabel.setForeground(Theme.COLOR_TEXT_PRIMARY);
        card.add(roleLabel, gbc);

        gbc.gridx = 1;
        String[] roles = {"Student", "Company", "Placement Officer"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(Theme.FONT_BODY);
        card.add(roleComboBox, gbc);

        // Email
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(Theme.FONT_BODY_BOLD);
        emailLabel.setForeground(Theme.COLOR_TEXT_PRIMARY);
        card.add(emailLabel, gbc);

        gbc.gridx = 1;
        emailField = new JTextField(20);
        emailField.setFont(Theme.FONT_BODY);
        card.add(emailField, gbc);

        // Email error inline
        gbc.gridy++;
        gbc.gridx = 1;
        emailErrorLabel = Theme.createInlineValidationLabel();
        card.add(emailErrorLabel, gbc);

        // Password
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(Theme.FONT_BODY_BOLD);
        passLabel.setForeground(Theme.COLOR_TEXT_PRIMARY);
        card.add(passLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setFont(Theme.FONT_BODY);
        card.add(passwordField, gbc);

        // Password error inline
        gbc.gridy++;
        gbc.gridx = 1;
        passwordErrorLabel = Theme.createInlineValidationLabel();
        card.add(passwordErrorLabel, gbc);

        // Login Button (single primary button on screen)
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        loginButton = new JButton("Login");
        Theme.stylePrimaryButton(loginButton);
        loginButton.addActionListener(this::handleLogin);
        card.add(loginButton, gbc);

        // Registration buttons (secondary actions)
        gbc.gridy++;
        JPanel regPanel = new JPanel(new GridLayout(1, 2, Theme.SPACING_SMALL, 0));
        regPanel.setOpaque(false);

        registerStudentButton = new JButton("Register Student");
        Theme.styleSecondaryButton(registerStudentButton);
        registerStudentButton.addActionListener(e -> showStudentRegistrationDialog());
        regPanel.add(registerStudentButton);

        registerCompanyButton = new JButton("Register Company");
        Theme.styleSecondaryButton(registerCompanyButton);
        registerCompanyButton.addActionListener(e -> showCompanyRegistrationDialog());
        regPanel.add(registerCompanyButton);

        card.add(regPanel, gbc);

        // Optional demo credentials box via --demo flag only
        if (showDemoHints) {
            gbc.gridy++;
            JPanel demoBox = new JPanel(new GridLayout(3, 1, 2, 2));
            demoBox.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.COLOR_BORDER, 1),
                    new EmptyBorder(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL)
            ));
            demoBox.setBackground(Theme.COLOR_SUBTLE_BG);
            JLabel officerHint = new JLabel("Officer: admin@ktu.edu / admin123");
            officerHint.setFont(Theme.FONT_SMALL);
            officerHint.setForeground(Theme.COLOR_TEXT_MUTED);
            JLabel studentHint = new JLabel("Student: arun@ktu.edu / arun123");
            studentHint.setFont(Theme.FONT_SMALL);
            studentHint.setForeground(Theme.COLOR_TEXT_MUTED);
            JLabel companyHint = new JLabel("Company: hr@infosys.com / info123");
            companyHint.setFont(Theme.FONT_SMALL);
            companyHint.setForeground(Theme.COLOR_TEXT_MUTED);
            demoBox.add(officerHint);
            demoBox.add(studentHint);
            demoBox.add(companyHint);
            card.add(demoBox, gbc);
        }

        GridBagConstraints outerGbc = new GridBagConstraints();
        add(card, outerGbc);
    }

    private void clearErrors() {
        emailErrorLabel.setText(" ");
        passwordErrorLabel.setText(" ");
    }

    private void handleLogin(ActionEvent e) {
        clearErrors();

        String role = (String) roleComboBox.getSelectedItem();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        boolean hasError = false;
        if (email.isEmpty()) {
            emailErrorLabel.setText("Email is required.");
            hasError = true;
        }
        if (password.isEmpty()) {
            passwordErrorLabel.setText("Password is required.");
            hasError = true;
        }
        if (hasError) {
            return;
        }

        DataManager dm = DataManager.getInstance();

        if ("Student".equals(role)) {
            Student student = dm.authenticateStudent(email, password);
            if (student != null) {
                clearFields();
                mainFrame.loginUser("Student", student);
            } else {
                passwordErrorLabel.setText("Invalid student credentials.");
            }
        } else if ("Company".equals(role)) {
            Company company = dm.authenticateCompany(email, password);
            if (company != null) {
                clearFields();
                mainFrame.loginUser("Company", company);
            } else {
                passwordErrorLabel.setText("Invalid company credentials.");
            }
        } else if ("Placement Officer".equals(role)) {
            PlacementOfficer officer = dm.authenticateOfficer(email, password);
            if (officer != null) {
                clearFields();
                mainFrame.loginUser("Placement Officer", officer);
            } else {
                passwordErrorLabel.setText("Invalid officer credentials.");
            }
        }
    }

    private void clearFields() {
        emailField.setText("");
        passwordField.setText("");
        clearErrors();
    }

    private void showStudentRegistrationDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Student Registration", true);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Theme.COLOR_SURFACE);
        dialog.setSize(440, 520);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 10, 4, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField usnF = new JTextField(15);
        JTextField nameF = new JTextField(15);
        JTextField emailF = new JTextField(15);
        JTextField phoneF = new JTextField(15);
        JTextField deptF = new JTextField("Computer Science", 15);
        JTextField cgpaF = new JTextField("8.0", 15);
        JTextField backlogsF = new JTextField("0", 15);
        JPasswordField passF = new JPasswordField(15);

        JLabel usnErr = Theme.createInlineValidationLabel();
        JLabel nameErr = Theme.createInlineValidationLabel();
        JLabel emailErr = Theme.createInlineValidationLabel();
        JLabel cgpaErr = Theme.createInlineValidationLabel();
        JLabel backlogsErr = Theme.createInlineValidationLabel();
        JLabel passErr = Theme.createInlineValidationLabel();

        int row = 0;
        addFormRowWithValidation(dialog, g, row++, "USN:", usnF, usnErr);
        addFormRowWithValidation(dialog, g, row++, "Full Name:", nameF, nameErr);
        addFormRowWithValidation(dialog, g, row++, "Email:", emailF, emailErr);
        addFormRow(dialog, g, row++, "Phone:", phoneF);
        addFormRow(dialog, g, row++, "Department:", deptF);
        addFormRowWithValidation(dialog, g, row++, "CGPA (0.0 to 10.0):", cgpaF, cgpaErr);
        addFormRowWithValidation(dialog, g, row++, "Backlogs:", backlogsF, backlogsErr);
        addFormRowWithValidation(dialog, g, row++, "Password:", passF, passErr);

        JButton submitBtn = new JButton("Register");
        Theme.stylePrimaryButton(submitBtn);
        submitBtn.addActionListener(ev -> {
            usnErr.setText(" ");
            nameErr.setText(" ");
            emailErr.setText(" ");
            cgpaErr.setText(" ");
            backlogsErr.setText(" ");
            passErr.setText(" ");

            String usn = usnF.getText().trim();
            String name = nameF.getText().trim();
            String email = emailF.getText().trim();
            String phone = phoneF.getText().trim();
            String dept = deptF.getText().trim();
            String pass = new String(passF.getPassword()).trim();

            boolean invalid = false;
            if (usn.isEmpty()) {
                usnErr.setText("USN is required.");
                invalid = true;
            }
            if (name.isEmpty()) {
                nameErr.setText("Name is required.");
                invalid = true;
            }
            if (email.isEmpty()) {
                emailErr.setText("Email is required.");
                invalid = true;
            }
            if (pass.isEmpty()) {
                passErr.setText("Password is required.");
                invalid = true;
            }

            double cgpa = 0.0;
            try {
                cgpa = Double.parseDouble(cgpaF.getText().trim());
                if (cgpa < 0.0 || cgpa > 10.0) {
                    cgpaErr.setText("CGPA must be between 0.0 and 10.0.");
                    invalid = true;
                }
            } catch (NumberFormatException nfe) {
                cgpaErr.setText("Invalid numeric format for CGPA.");
                invalid = true;
            }

            int backlogs = 0;
            try {
                backlogs = Integer.parseInt(backlogsF.getText().trim());
                if (backlogs < 0) {
                    backlogsErr.setText("Backlogs cannot be negative.");
                    invalid = true;
                }
            } catch (NumberFormatException nfe) {
                backlogsErr.setText("Invalid numeric format for backlogs.");
                invalid = true;
            }

            if (invalid) {
                return;
            }

            try {
                Student s = new Student(usn, name, email, phone, dept, cgpa, backlogs, pass);
                DataManager.getInstance().addStudent(s);
                JOptionPane.showMessageDialog(dialog, "Student registered successfully. You may now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (IllegalArgumentException iae) {
                usnErr.setText(iae.getMessage());
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
        dialog.getContentPane().setBackground(Theme.COLOR_SURFACE);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 10, 4, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField compNameF = new JTextField(15);
        JTextField industryF = new JTextField("Software / IT", 15);
        JTextField emailF = new JTextField(15);
        JPasswordField passF = new JPasswordField(15);

        JLabel nameErr = Theme.createInlineValidationLabel();
        JLabel emailErr = Theme.createInlineValidationLabel();
        JLabel passErr = Theme.createInlineValidationLabel();

        int row = 0;
        addFormRowWithValidation(dialog, g, row++, "Company Name:", compNameF, nameErr);
        addFormRow(dialog, g, row++, "Industry Type:", industryF);
        addFormRowWithValidation(dialog, g, row++, "Email:", emailF, emailErr);
        addFormRowWithValidation(dialog, g, row++, "Password:", passF, passErr);

        JButton submitBtn = new JButton("Submit Registration");
        Theme.stylePrimaryButton(submitBtn);
        submitBtn.addActionListener(ev -> {
            nameErr.setText(" ");
            emailErr.setText(" ");
            passErr.setText(" ");

            String name = compNameF.getText().trim();
            String ind = industryF.getText().trim();
            String email = emailF.getText().trim();
            String pass = new String(passF.getPassword()).trim();

            boolean invalid = false;
            if (name.isEmpty()) {
                nameErr.setText("Company name is required.");
                invalid = true;
            }
            if (email.isEmpty()) {
                emailErr.setText("Email is required.");
                invalid = true;
            }
            if (pass.isEmpty()) {
                passErr.setText("Password is required.");
                invalid = true;
            }

            if (invalid) {
                return;
            }

            try {
                String compId = "CMP" + (DataManager.getInstance().getCompanies().size() + 101);
                Company c = new Company(compId, name, ind, email, pass);
                DataManager.getInstance().addCompany(c);
                JOptionPane.showMessageDialog(dialog,
                        "Company registered successfully.\nStatus: Pending verification by Placement Officer.",
                        "Registration Submitted",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                emailErr.setText(ex.getMessage());
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
        lbl.setFont(Theme.FONT_BODY_BOLD);
        lbl.setForeground(Theme.COLOR_TEXT_PRIMARY);
        container.add(lbl, g);

        g.gridx = 1;
        field.setFont(Theme.FONT_BODY);
        container.add(field, g);
    }

    private void addFormRowWithValidation(Container container, GridBagConstraints g, int row, String label, JComponent field, JLabel errorLabel) {
        addFormRow(container, g, row, label, field);
        g.gridx = 1;
        g.gridy = row + 1;
        container.add(errorLabel, g);
    }
}
