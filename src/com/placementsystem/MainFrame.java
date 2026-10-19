package com.placementsystem;

import com.placementsystem.gui.*;
import com.placementsystem.model.Company;
import com.placementsystem.model.PlacementOfficer;
import com.placementsystem.model.Student;
import com.placementsystem.util.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    public static final String CARD_LOGIN = "LOGIN";
    public static final String CARD_STUDENT = "STUDENT_PROFILE";
    public static final String CARD_COMPANY = "COMPANY_DASHBOARD";
    public static final String CARD_ADMIN = "ADMIN_PANEL";
    public static final String CARD_TRACKING = "APP_TRACKING";

    private CardLayout cardLayout;
    private JPanel cardsPanel;

    private LoginPanel loginPanel;
    private StudentProfilePanel studentProfilePanel;
    private CompanyDashboard companyDashboard;
    private AdminDashboard adminDashboard;
    private ApplicationTrackingPanel trackingPanel;

    private JLabel userStatusLabel;
    private JButton navPrimaryButton;
    private JButton navTrackingButton;
    private JButton changePasswordButton;
    private JButton logoutButton;

    private String currentRole = null;
    private Object currentUser = null;
    private final boolean showDemoHints;

    public MainFrame() {
        this(false);
    }

    public MainFrame(boolean showDemoHints) {
        super("Placement Management System");
        this.showDemoHints = showDemoHints;
        initUI();
    }

    private void initUI() {
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClose();
            }
        });

        headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(Theme.COLOR_BG);

        loginPanel = new LoginPanel(this, showDemoHints);
        studentProfilePanel = new StudentProfilePanel(this);
        companyDashboard = new CompanyDashboard(this);
        adminDashboard = new AdminDashboard(this);
        trackingPanel = new ApplicationTrackingPanel(this);

        cardsPanel.add(loginPanel, CARD_LOGIN);
        cardsPanel.add(studentProfilePanel, CARD_STUDENT);
        cardsPanel.add(companyDashboard, CARD_COMPANY);
        cardsPanel.add(adminDashboard, CARD_ADMIN);
        cardsPanel.add(trackingPanel, CARD_TRACKING);

        add(cardsPanel, BorderLayout.CENTER);

        showCard(CARD_LOGIN);
        updateHeaderForSession(null, null);
    }

    private void handleWindowClose() {
        if ("Student".equals(currentRole) && studentProfilePanel != null && studentProfilePanel.hasUnsavedChanges()) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "You have unsaved changes in your profile. Are you sure you want to exit without saving?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        DataManager.getInstance().shutdown();
        dispose();
        System.exit(0);
    }

    private JPanel headerPanel;

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.COLOR_PRIMARY);
        header.setBorder(new EmptyBorder(Theme.SPACING_SMALL, Theme.SPACING_MEDIUM, Theme.SPACING_SMALL, Theme.SPACING_MEDIUM));

        JLabel brandLabel = new JLabel("Placement Management System");
        brandLabel.setFont(Theme.FONT_TITLE);
        brandLabel.setForeground(Color.WHITE);
        header.add(brandLabel, BorderLayout.WEST);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SPACING_SMALL, 0));
        rightControls.setOpaque(false);

        userStatusLabel = new JLabel("");
        userStatusLabel.setFont(Theme.FONT_BODY);
        userStatusLabel.setForeground(Color.WHITE);
        rightControls.add(userStatusLabel);

        navPrimaryButton = new JButton("Home");
        Theme.styleSecondaryButton(navPrimaryButton);
        navPrimaryButton.addActionListener(e -> navigateToPrimaryPortal());
        navPrimaryButton.setVisible(false);
        rightControls.add(navPrimaryButton);

        navTrackingButton = new JButton("Pipeline Tracker");
        Theme.styleSecondaryButton(navTrackingButton);
        navTrackingButton.addActionListener(e -> {
            trackingPanel.refreshData();
            showCard(CARD_TRACKING);
        });
        navTrackingButton.setVisible(false);
        rightControls.add(navTrackingButton);

        changePasswordButton = new JButton("Change Password");
        Theme.styleSecondaryButton(changePasswordButton);
        changePasswordButton.addActionListener(e -> {
            if (currentUser != null) {
                ChangePasswordDialog dlg = new ChangePasswordDialog(this, currentUser);
                dlg.setVisible(true);
            }
        });
        changePasswordButton.setVisible(false);
        rightControls.add(changePasswordButton);

        logoutButton = new JButton("Logout");
        Theme.styleSecondaryButton(logoutButton);
        logoutButton.addActionListener(e -> logout());
        logoutButton.setVisible(false);
        rightControls.add(logoutButton);

        header.add(rightControls, BorderLayout.EAST);
        return header;
    }

    public void showCard(String cardName) {
        cardLayout.show(cardsPanel, cardName);
    }

    public void loginUser(String role, Object userObject) {
        this.currentRole = role;
        this.currentUser = userObject;

        updateHeaderForSession(role, userObject);

        if ("Student".equals(role)) {
            Student s = (Student) userObject;
            studentProfilePanel.setCurrentStudent(s);
            navPrimaryButton.setText("Profile and Drives");
            showCard(CARD_STUDENT);
        } else if ("Company".equals(role)) {
            Company c = (Company) userObject;
            companyDashboard.setCurrentCompany(c);
            navPrimaryButton.setText("Company Dashboard");
            showCard(CARD_COMPANY);
        } else if ("Placement Officer".equals(role)) {
            PlacementOfficer po = (PlacementOfficer) userObject;
            adminDashboard.setCurrentOfficer(po);
            navPrimaryButton.setText("Admin Console");
            showCard(CARD_ADMIN);
        }

        navPrimaryButton.setVisible(true);
        navTrackingButton.setVisible(true);
        changePasswordButton.setVisible(true);
        logoutButton.setVisible(true);
    }

    public void logout() {
        if ("Student".equals(currentRole) && studentProfilePanel != null && studentProfilePanel.hasUnsavedChanges()) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "You have unsaved changes in your profile. Are you sure you want to log out without saving?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        DataManager.getInstance().saveAllAsync();

        if (studentProfilePanel != null) studentProfilePanel.clearSession();
        if (companyDashboard != null) companyDashboard.clearSession();
        if (adminDashboard != null) adminDashboard.clearSession();
        if (trackingPanel != null) trackingPanel.clearSession();
        if (loginPanel != null) loginPanel.clearFields();

        this.currentRole = null;
        this.currentUser = null;

        updateHeaderForSession(null, null);
        showCard(CARD_LOGIN);
    }

    private void navigateToPrimaryPortal() {
        if ("Student".equals(currentRole)) {
            studentProfilePanel.refreshData();
            showCard(CARD_STUDENT);
        } else if ("Company".equals(currentRole)) {
            companyDashboard.refreshData();
            showCard(CARD_COMPANY);
        } else if ("Placement Officer".equals(currentRole)) {
            adminDashboard.refreshData();
            showCard(CARD_ADMIN);
        }
    }

    private void updateHeaderForSession(String role, Object userObject) {
        if (role == null || userObject == null) {
            userStatusLabel.setText("");
            navPrimaryButton.setVisible(false);
            navTrackingButton.setVisible(false);
            changePasswordButton.setVisible(false);
            logoutButton.setVisible(false);
        } else {
            String displayName = "";
            if (userObject instanceof Student) {
                Student s = (Student) userObject;
                displayName = s.getName() + " (Student)";
            } else if (userObject instanceof Company) {
                Company c = (Company) userObject;
                displayName = c.getCompanyName() + " (Company)";
            } else if (userObject instanceof PlacementOfficer) {
                PlacementOfficer po = (PlacementOfficer) userObject;
                displayName = po.getName() + " (Placement Officer)";
            }
            userStatusLabel.setText(displayName);
            changePasswordButton.setVisible(true);
        }
    }

    public void updateActiveUserDisplay(String text) {
        userStatusLabel.setText(text);
    }
}
