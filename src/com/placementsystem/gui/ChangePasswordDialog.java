package com.placementsystem.gui;

import com.placementsystem.model.Company;
import com.placementsystem.model.PlacementOfficer;
import com.placementsystem.model.Student;
import com.placementsystem.util.DataManager;
import com.placementsystem.util.Validator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class ChangePasswordDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final transient Object currentUser;

    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    private JLabel currentPassErr;
    private JLabel newPassErr;
    private JLabel confirmPassErr;

    @SuppressWarnings("this-escape")
    public ChangePasswordDialog(Frame parent, Object currentUser) {
        super(parent, "Change Account Password", true);
        this.currentUser = currentUser;

        initComponents();
    }

    private void initComponents() {
        setSize(440, 360);
        setLocationRelativeTo(getParent());
        setLayout(new GridBagLayout());
        getContentPane().setBackground(Theme.COLOR_SURFACE);

        // Escape key to dismiss dialog
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 12, 6, 12);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;

        JLabel title = new JLabel("Update Account Password");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.COLOR_TEXT_PRIMARY);

        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        add(title, g);

        currentPasswordField = new JPasswordField(15);
        currentPasswordField.setFont(Theme.FONT_BODY);
        newPasswordField = new JPasswordField(15);
        newPasswordField.setFont(Theme.FONT_BODY);
        confirmPasswordField = new JPasswordField(15);
        confirmPasswordField.setFont(Theme.FONT_BODY);

        currentPassErr = Theme.createInlineValidationLabel();
        newPassErr = Theme.createInlineValidationLabel();
        confirmPassErr = Theme.createInlineValidationLabel();

        int row = 1;
        addRow(g, row++, "Current Password:", currentPasswordField, currentPassErr);
        addRow(g, row++, "New Password (min 6 chars):", newPasswordField, newPassErr);
        addRow(g, row++, "Confirm New Password:", confirmPasswordField, confirmPassErr);

        JButton saveBtn = new JButton("Update Password");
        Theme.stylePrimaryButton(saveBtn);
        getRootPane().setDefaultButton(saveBtn);
        saveBtn.addActionListener(e -> handleChangePassword());

        g.gridx = 0; g.gridy = row; g.gridwidth = 2;
        add(saveBtn, g);
    }

    private void addRow(GridBagConstraints g, int row, String label, JComponent field, JLabel errorLabel) {
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_BODY_BOLD);
        lbl.setForeground(Theme.COLOR_TEXT_PRIMARY);
        add(lbl, g);

        g.gridx = 1;
        add(Theme.createFieldWithValidation(field, errorLabel), g);
    }

    private void handleChangePassword() {
        currentPassErr.setText(" ");
        newPassErr.setText(" ");
        confirmPassErr.setText(" ");

        String currentPass = new String(currentPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        boolean invalid = false;

        boolean currentMatches = false;
        if (currentUser instanceof Student) {
            currentMatches = ((Student) currentUser).checkPassword(currentPass);
        } else if (currentUser instanceof Company) {
            currentMatches = ((Company) currentUser).checkPassword(currentPass);
        } else if (currentUser instanceof PlacementOfficer) {
            currentMatches = ((PlacementOfficer) currentUser).checkPassword(currentPass);
        }

        if (!currentMatches) {
            currentPassErr.setText("Incorrect current password.");
            invalid = true;
        }

        if (newPass.isEmpty()) {
            newPassErr.setText("New password is required.");
            invalid = true;
        } else if (!Validator.isValidPassword(newPass)) {
            newPassErr.setText("Password must be at least 6 characters.");
            invalid = true;
        }

        if (!newPass.equals(confirmPass)) {
            confirmPassErr.setText("Passwords do not match.");
            invalid = true;
        }

        if (invalid) {
            revalidate();
            repaint();
            return;
        }

        if (currentUser instanceof Student) {
            Student s = (Student) currentUser;
            s.setPassword(newPass);
            DataManager.getInstance().updateStudent(s);
        } else if (currentUser instanceof Company) {
            Company c = (Company) currentUser;
            c.setPassword(newPass);
            DataManager.getInstance().updateCompany(c);
        } else if (currentUser instanceof PlacementOfficer) {
            PlacementOfficer po = (PlacementOfficer) currentUser;
            po.setPassword(newPass);
            DataManager.getInstance().saveOfficers();
        }

        JOptionPane.showMessageDialog(this, "Password updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
