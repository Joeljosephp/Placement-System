package com.placementsystem;

import com.placementsystem.util.DataManager;

import javax.swing.*;

/**
 * System Launcher & Entry Point for Placement Management System.
 * Initializes storage, sets up Look-and-Feel, registers JVM shutdown hooks for autosave,
 * and launches the GUI on the Swing Event Dispatch Thread (EDT).
 */
public class Main {
    public static void main(String[] args) {
        // 1. Configure Modern System Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Default fallback
            }
        }

        // 2. Initialize DataManager and persistence engine
        System.out.println("==================================================");
        System.out.println("   KTU S3 CS - Placement Management System");
        System.out.println("==================================================");
        DataManager dataManager = DataManager.getInstance();

        // 3. Register JVM Shutdown Hook for automatic disk persistence on application exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Shutdown Hook] Autosaving Placement Management System data before termination...");
            dataManager.saveAll();
            System.out.println("[Shutdown Hook] Persistence finished cleanly.");
        }, "PlacementSystem-Autosave-Hook"));

        // 4. Launch Top-Level GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                System.out.println("[GUI] MainFrame initialized and rendered successfully.");
            } catch (Exception ex) {
                System.err.println("[GUI Error] Failed to initialize MainFrame: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }
}
