package com.placementsystem.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class Theme {
    // Spacing constants
    public static final int SPACING_SMALL = 8;
    public static final int SPACING_MEDIUM = 16;
    public static final int SPACING_LARGE = 24;

    // Palette
    public static final Color COLOR_PRIMARY = new Color(0x2C, 0x52, 0x82);
    public static final Color COLOR_PRIMARY_HOVER = new Color(0x1A, 0x36, 0x5D);

    public static final Color COLOR_BG = new Color(0xFA, 0xFA, 0xFA);
    public static final Color COLOR_SURFACE = new Color(0xFF, 0xFF, 0xFF);
    public static final Color COLOR_BORDER = new Color(0xDC, 0xE1, 0xE7);
    public static final Color COLOR_SUBTLE_BG = new Color(0xF1, 0xF3, 0xF5);

    public static final Color COLOR_TEXT_PRIMARY = new Color(0x21, 0x25, 0x29);
    public static final Color COLOR_TEXT_MUTED = new Color(0x6C, 0x75, 0x7D);

    public static final Color COLOR_ERROR = new Color(0xC6, 0x28, 0x28);
    public static final Color COLOR_SUCCESS = new Color(0x2E, 0x7D, 0x32);

    // Typography: one family, 3 sizes
    private static final String FONT_FAMILY = Font.SANS_SERIF;
    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 16);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font(FONT_FAMILY, Font.BOLD, 11);

    private Theme() {
    }

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(SPACING_MEDIUM, SPACING_MEDIUM, SPACING_MEDIUM, SPACING_MEDIUM)
        );
    }

    public static Border createFieldBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(4, 8, 4, 8)
        );
    }

    public static void stylePrimaryButton(JButton button) {
        button.setFont(FONT_BODY_BOLD);
        button.setBackground(COLOR_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(6, 14, 6, 14));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(COLOR_PRIMARY_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(COLOR_PRIMARY);
                }
            }
        });
    }

    public static void styleSecondaryButton(JButton button) {
        button.setFont(FONT_BODY);
        button.setBackground(COLOR_SURFACE);
        button.setForeground(COLOR_TEXT_PRIMARY);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(5, 12, 5, 12)
        ));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(COLOR_SUBTLE_BG);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(COLOR_SURFACE);
                }
            }
        });
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setFont(FONT_BODY);
        table.setForeground(COLOR_TEXT_PRIMARY);
        table.setBackground(COLOR_SURFACE);
        table.setSelectionBackground(new Color(0xE2, 0xE8, 0xF0));
        table.setSelectionForeground(COLOR_TEXT_PRIMARY);
        table.setGridColor(COLOR_BORDER);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);

        table.getTableHeader().setFont(FONT_BODY_BOLD);
        table.getTableHeader().setBackground(COLOR_SUBTLE_BG);
        table.getTableHeader().setForeground(COLOR_TEXT_PRIMARY);
        table.getTableHeader().setReorderingAllowed(false);
    }

    public static DefaultTableCellRenderer getRightAlignRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        return renderer;
    }

    public static DefaultTableCellRenderer getCenterAlignRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        return renderer;
    }

    public static JLabel createInlineValidationLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(FONT_SMALL);
        label.setForeground(COLOR_ERROR);
        return label;
    }
}
