package com.placementsystem;

import com.placementsystem.util.DataManager;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        boolean showDemoHints = false;
        for (String arg : args) {
            if ("--demo".equalsIgnoreCase(arg.trim())) {
                showDemoHints = true;
                break;
            }
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Default swing fallback
        }

        DataManager dataManager = DataManager.getInstance();

        // Register shutdown hook for clean data save on exit
        Runtime.getRuntime().addShutdownHook(new Thread(dataManager::shutdown, "PlacementSystem-Shutdown-Hook"));

        final boolean demoFlag = showDemoHints;
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame(demoFlag);
                frame.setVisible(true);
            } catch (Exception ex) {
                System.err.println("Failed to start application: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }
}
