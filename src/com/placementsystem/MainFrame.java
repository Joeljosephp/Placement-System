package com.placementsystem;

import com.placementsystem.gui.*;
import com.placementsystem.model.Company;
import com.placementsystem.model.PlacementOfficer;
import com.placementsystem.model.Student;
import com.placementsystem.util.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Top-level application window and navigation controller.
 * Configured at 1200x800, centered, managing CardLayout switching between all 5 panels.
 * Demonstrates CardLayout, Event Dispatch Thread handling, and Controller patterns.
 */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    // Card Names
    public static final String CARD_LOGIN = "LOGIN";
    public static final String CARD_STUDENT = "STUDENT_PROFILE";
    public static final String CARD_COMPANY = "COMPANY_DASHBOARD";
    public static final String CARD_ADMIN = "ADMIN_PANEL";
    public static final String CARD_TRACKING = "APP_TRACKING";

    private CardLayout cardLayout;
    private JPanel cardsPanel;

    // Sub-panels
    private LoginPanel loginPanel;
    private StudentProfilePanel studentProfilePanel;
    private CompanyDashboard companyDashboard;
    private AdminDashboard adminDashboard;
    private ApplicationTrackingPanel trackingPanel;

    // Header Components
    private JPanel headerPanel;
    private JLabel userStatusLabel;
    private JButton navPrimaryButton;
    private JButton navTrackingButton;
    private JButton logoutButton;

    // Current Session State
    private String currentRole = null;
    private Object currentUser = null;

    public MainFrame() {
        super("Placement Management System - KTU S3 CS OOP Mini Project");
        initUI();
    }

    private void initUI() {
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null); // Center on screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. Top Navigation Header
        headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // 2. CardLayout Central Container
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        loginPanel = new LoginPanel(this);
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

        // Start on Login Card
        showCard(CARD_LOGIN);
        updateHeaderForSession(null, null);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(24, 43, 73));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Left Branding
        JLabel brandLabel = new JLabel("🎓 Placement Management System");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandLabel.setForeground(Color.WHITE);
        header.add(brandLabel, BorderLayout.WEST);

        // Right Navigation & User Controls
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightControls.setOpaque(false);

        userStatusLabel = new JLabel("Not Logged In");
        userStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userStatusLabel.setForeground(new Color(200, 220, 245));
        rightControls.add(userStatusLabel);

        navPrimaryButton = new JButton("Home");
        navPrimaryButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        navPrimaryButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        navPrimaryButton.addActionListener(e -> navigateToPrimaryPortal());
        navPrimaryButton.setVisible(false);
        rightControls.add(navPrimaryButton);

        navTrackingButton = new JButton("Pipeline Tracker");
        navTrackingButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        navTrackingButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        navTrackingButton.addActionListener(e -> {
            trackingPanel.refreshData();
            showCard(CARD_TRACKING);
        });
        navTrackingButton.setVisible(false);
        rightControls.add(navTrackingButton);

        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
            navPrimaryButton.setText("My Profile & Drives");
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
        logoutButton.setVisible(true);
    }

    public void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to log out of the Placement Management System?", 
                "Confirm Logout", 
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Autosave latest data
            DataManager.getInstance().saveAll();

            this.currentRole = null;
            this.currentUser = null;

            updateHeaderForSession(null, null);
            showCard(CARD_LOGIN);

            JOptionPane.showMessageDialog(this, "You have been logged out safely.", "Logged Out", JOptionPane.INFORMATION_MESSAGE);
        }
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
            userStatusLabel.setText("Please Log In");
            navPrimaryButton.setVisible(false);
            navTrackingButton.setVisible(false);
            logoutButton.setVisible(false);
        } else {
            String displayName = "";
            if (userObject instanceof Student) {
                Student s = (Student) userObject;
                displayName = "👤 " + s.getName() + " [Student - " + s.getUsn() + "]";
            } else if (userObject instanceof Company) {
                Company c = (Company) userObject;
                displayName = "🏢 " + c.getCompanyName() + " [Company]";
            } else if (userObject instanceof PlacementOfficer) {
                PlacementOfficer po = (PlacementOfficer) userObject;
                displayName = "⭐ " + po.getName() + " [Officer]";
            }
            userStatusLabel.setText(displayName);
        }
    }

    public void updateActiveUserDisplay(String text) {
        userStatusLabel.setText(text);
    }
}
