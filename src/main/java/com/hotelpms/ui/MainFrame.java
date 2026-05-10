/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.ui;

import com.hotelpms.service.HotelService;

import javax.swing.*;
import java.awt.*;

/**
 * JFrame utama aplikasi Hotel PMS.
 * Mengelola navigasi sidebar dan menampilkan panel
 * menggunakan CardLayout.
 *
 * @author rendysaptra
 */
public class MainFrame extends JFrame {
    // ─── Konstanta Warna ──────────────────────────────────────
    public static final Color PRIMARY    = new Color(15, 110, 86);   // Teal gelap
    public static final Color PRIMARY_LIGHT = new Color(29, 158, 117); // Teal terang
    public static final Color SIDEBAR_BG = new Color(30, 30, 30);   // Sidebar gelap
    public static final Color SIDEBAR_TEXT = new Color(200, 200, 200);
    public static final Color SIDEBAR_ACTIVE = new Color(29, 158, 117);
    public static final Color CONTENT_BG = new Color(245, 245, 245);
    public static final Color WHITE      = Color.WHITE;
    public static final Color TEXT_DARK  = new Color(44, 44, 42);
    public static final Color BORDER     = new Color(220, 220, 220);

    // ─── Konstanta Font ───────────────────────────────────────
    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 18);

    // ─── Komponen ─────────────────────────────────────────────
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JLabel titleLabel;

    // ─── Service ──────────────────────────────────────────────
    private final HotelService hotelService;

    // ─── Panel Names ──────────────────────────────────────────
    public static final String PANEL_DASHBOARD    = "Dashboard";
    public static final String PANEL_BOOKING      = "Reservasi";
    public static final String PANEL_CHECKINOUT   = "Check-in/Out";
    public static final String PANEL_ROOM         = "Kamar";
    public static final String PANEL_GUEST        = "Tamu";
    public static final String PANEL_BILLING      = "Billing";
    public static final String PANEL_HOUSEKEEPING = "Housekeeping";
    public static final String PANEL_REPORT       = "Laporan";

    // ─── Constructor ─────────────────────────────────────────
    public MainFrame() {
        this.hotelService = new HotelService();
        initFrame();
        initComponents();
        showPanel(PANEL_DASHBOARD);
    }

    // ══════════════════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════════════════

    private void initFrame() {
        setTitle("Hotel PMS — Property Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CONTENT_BG);
    }

    private void initComponents() {
        // Top bar
        add(buildTopBar(), BorderLayout.NORTH);

        // Sidebar + content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildSidebar(), buildContentArea());
        splitPane.setDividerSize(0);
        splitPane.setDividerLocation(220);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
    }

    // ── Top Bar ───────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PRIMARY);
        bar.setPreferredSize(new Dimension(0, 52));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel logo = new JLabel("Hotel PMS");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(WHITE);
        bar.add(logo, BorderLayout.WEST);

        titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(180, 230, 210));
        bar.add(titleLabel, BorderLayout.EAST);

        return bar;
    }

    // ── Sidebar ───────────────────────────────────────────────
    private JPanel buildSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(220, 0));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));

        String[][] menus = {
            {"Dashboard",     PANEL_DASHBOARD},
            {"Reservasi",     PANEL_BOOKING},
            {"Check-in/Out",  PANEL_CHECKINOUT},
            {"Kamar",         PANEL_ROOM},
            {"Tamu",          PANEL_GUEST},
            {"Billing",       PANEL_BILLING},
            {"Housekeeping",  PANEL_HOUSEKEEPING},
            {"Laporan",       PANEL_REPORT},
        };

        for (String[] menu : menus) {
            sidebarPanel.add(buildMenuButton(menu[0], menu[1]));
        }

        sidebarPanel.add(Box.createVerticalGlue());

        // Footer sidebar
        JLabel footer = new JLabel("  v1.0 — Cyber University");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(new Color(100, 100, 100));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(footer);

        return sidebarPanel;
    }

    private JButton buildMenuButton(String label, String panelName) {
        JButton btn = new JButton(label);
        btn.setFont(FONT_BODY);
        btn.setForeground(SIDEBAR_TEXT);
        btn.setBackground(SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!btn.getBackground().equals(SIDEBAR_ACTIVE)) {
                    btn.setBackground(new Color(50, 50, 50));
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!btn.getBackground().equals(SIDEBAR_ACTIVE)) {
                    btn.setBackground(SIDEBAR_BG);
                }
            }
        });

        btn.addActionListener(e -> {
            // Reset semua tombol
            for (Component c : sidebarPanel.getComponents()) {
                if (c instanceof JButton b) {
                    b.setBackground(SIDEBAR_BG);
                    b.setForeground(SIDEBAR_TEXT);
                }
            }
            // Aktifkan tombol ini
            btn.setBackground(SIDEBAR_ACTIVE);
            btn.setForeground(WHITE);
            showPanel(panelName);
        });

        return btn;
    }

    // ── Content Area ──────────────────────────────────────────
    private JPanel buildContentArea() {
        cardLayout  = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CONTENT_BG);

        // Daftarkan semua panel
        contentPanel.add(new DashboardPanel(hotelService),    PANEL_DASHBOARD);
        contentPanel.add(new BookingPanel(hotelService),      PANEL_BOOKING);
        contentPanel.add(new CheckInOutPanel(hotelService),   PANEL_CHECKINOUT);
        contentPanel.add(new RoomPanel(hotelService),         PANEL_ROOM);
        contentPanel.add(new GuestPanel(hotelService),        PANEL_GUEST);
        contentPanel.add(new BillingPanel(hotelService),      PANEL_BILLING);
        contentPanel.add(new HousekeepingPanel(hotelService), PANEL_HOUSEKEEPING);
        contentPanel.add(new ReportPanel(hotelService),       PANEL_REPORT);

        return contentPanel;
    }

    // ══════════════════════════════════════════════════════════
    //  NAVIGASI
    // ══════════════════════════════════════════════════════════

    /**
     * Tampilkan panel berdasarkan nama.
     *
     * @param panelName nama panel yang akan ditampilkan
     */
    public void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
        titleLabel.setText(panelName);

        // Refresh panel saat dibuka
        Component current = null;
        for (Component c : contentPanel.getComponents()) {
            if (c.isVisible()) { current = c; break; }
        }
        if (current instanceof Refreshable r) {
            r.refresh();
        }
    }

    // ══════════════════════════════════════════════════════════
    //  GETTER
    // ══════════════════════════════════════════════════════════

    public HotelService getHotelService() {
        return hotelService;
    }

    // ══════════════════════════════════════════════════════════
    //  HELPER METHODS (dipakai semua panel)
    // ══════════════════════════════════════════════════════════

    /**
     * Buat panel header dengan judul dan subjudul.
     */
    public static JPanel buildPageHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_HEADER);
        titleLbl.setForeground(TEXT_DARK);
        header.add(titleLbl, BorderLayout.WEST);

        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel subLbl = new JLabel(subtitle);
            subLbl.setFont(FONT_SMALL);
            subLbl.setForeground(new Color(130, 130, 130));
            header.add(subLbl, BorderLayout.EAST);
        }

        return header;
    }

    /**
     * Buat tombol dengan warna primary (teal).
     */
    public static JButton buildPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setBackground(PRIMARY);
        btn.setForeground(WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(PRIMARY_LIGHT);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(PRIMARY);
            }
        });

        return btn;
    }

    /**
     * Buat tombol dengan warna danger (merah).
     */
    public static JButton buildDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setBackground(new Color(163, 45, 45));
        btn.setForeground(WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    /**
     * Buat tombol secondary (outline).
     */
    public static JButton buildSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setBackground(WHITE);
        btn.setForeground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(PRIMARY, 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 1),
                BorderFactory.createEmptyBorder(7, 15, 7, 15)));
        return btn;
    }

    /**
     * Style JTable agar konsisten di semua panel.
     */
    public static void styleTable(javax.swing.JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(225, 245, 237));
        table.setSelectionForeground(TEXT_DARK);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setFont(FONT_TITLE);
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getTableHeader().setForeground(TEXT_DARK);
        table.getTableHeader().setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
    }

    /**
     * Buat kartu info untuk dashboard.
     */
    public static JPanel buildInfoCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_SMALL);
        titleLbl.setForeground(new Color(130, 130, 130));

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLbl.setForeground(accent);

        JPanel accent_bar = new JPanel();
        accent_bar.setBackground(accent);
        accent_bar.setPreferredSize(new Dimension(4, 0));

        card.add(accent_bar, BorderLayout.WEST);
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);

        return card;
    }
}
