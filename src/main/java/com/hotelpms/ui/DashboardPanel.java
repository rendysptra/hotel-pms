/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.ui;

import com.hotelpms.enums.RoomStatus;
import com.hotelpms.model.Reservation;
import com.hotelpms.model.Room;
import com.hotelpms.service.HotelService;
import com.hotelpms.service.ReportService;
 
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *
 * @author rendysaptra
 */
public class DashboardPanel extends JPanel implements Refreshable {
    // ─── Service ──────────────────────────────────────────────
    private final HotelService hotelService;

    // ─── Komponen ─────────────────────────────────────────────
    private JLabel occupancyValue;
    private JLabel totalRoomsValue;
    private JLabel activeGuestsValue;
    private JLabel revenueValue;
    private DefaultTableModel reservationTableModel;
    private JLabel lastUpdatedLabel;

    // ─── Constructor ─────────────────────────────────────────
    public DashboardPanel(HotelService hotelService) {
        this.hotelService = hotelService;
        initUI();
        refresh();
    }

    // ══════════════════════════════════════════════════════════
    //  UI INIT
    // ══════════════════════════════════════════════════════════
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(MainFrame.CONTENT_BG);

        // Header
        add(MainFrame.buildPageHeader("Dashboard",
                "Ringkasan operasional hotel hari ini"), BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(MainFrame.CONTENT_BG);
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        content.add(buildStatsRow(), BorderLayout.NORTH);
        content.add(buildReservationTable(), BorderLayout.CENTER);
        content.add(buildFooter(), BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);
    }

    // ── Stats Row ─────────────────────────────────────────────
    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setBackground(MainFrame.CONTENT_BG);
        row.setPreferredSize(new Dimension(0, 100));

        // Card 1: Occupancy Rate
        occupancyValue = new JLabel("0%");
        JPanel card1 = buildStatCard("Occupancy Rate", occupancyValue,
                MainFrame.PRIMARY);
        row.add(card1);

        // Card 2: Total Kamar
        totalRoomsValue = new JLabel("0");
        JPanel card2 = buildStatCard("Total Kamar", totalRoomsValue,
                new Color(41, 128, 185));
        row.add(card2);

        // Card 3: Tamu Aktif
        activeGuestsValue = new JLabel("0");
        JPanel card3 = buildStatCard("Tamu Aktif", activeGuestsValue,
                new Color(142, 68, 173));
        row.add(card3);

        // Card 4: Revenue Bulan Ini
        revenueValue = new JLabel("Rp 0");
        JPanel card4 = buildStatCard("Revenue Bulan Ini", revenueValue,
                new Color(186, 117, 23));
        row.add(card4);

        return row;
    }

    private JPanel buildStatCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(MainFrame.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        // Accent bar kiri
        JPanel bar = new JPanel();
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(4, 0));
        card.add(bar, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new BorderLayout(0, 6));
        textPanel.setBackground(MainFrame.WHITE);
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(MainFrame.FONT_SMALL);
        titleLbl.setForeground(new Color(130, 130, 130));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accent);

        textPanel.add(titleLbl, BorderLayout.NORTH);
        textPanel.add(valueLabel, BorderLayout.CENTER);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    // ── Reservation Table ─────────────────────────────────────
    private JPanel buildReservationTable() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(MainFrame.CONTENT_BG);

        // Sub header
        JPanel subHeader = new JPanel(new BorderLayout());
        subHeader.setBackground(MainFrame.CONTENT_BG);

        JLabel title = new JLabel("Reservasi Aktif");
        title.setFont(MainFrame.FONT_TITLE);
        title.setForeground(MainFrame.TEXT_DARK);
        subHeader.add(title, BorderLayout.WEST);

        JButton refreshBtn = MainFrame.buildSecondaryButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        subHeader.add(refreshBtn, BorderLayout.EAST);

        panel.add(subHeader, BorderLayout.NORTH);

        // Tabel
        String[] columns = {"ID Reservasi", "Nama Tamu", "Kamar",
                "Tipe", "Check-in", "Check-out", "Status"};
        reservationTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(reservationTableModel);
        MainFrame.styleTable(table);

        // Custom renderer untuk kolom Status
        table.getColumnModel().getColumn(6).setCellRenderer(
                new StatusCellRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER, 1));
        scroll.getViewport().setBackground(MainFrame.WHITE);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ── Footer ────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(MainFrame.CONTENT_BG);
        footer.setPreferredSize(new Dimension(0, 28));

        lastUpdatedLabel = new JLabel("Terakhir diupdate: -");
        lastUpdatedLabel.setFont(MainFrame.FONT_SMALL);
        lastUpdatedLabel.setForeground(new Color(150, 150, 150));
        footer.add(lastUpdatedLabel, BorderLayout.EAST);

        return footer;
    }

    // ══════════════════════════════════════════════════════════
    //  REFRESH
    // ══════════════════════════════════════════════════════════
    @Override
    public void refresh() {
        hotelService.refreshData();

        List<Room> rooms             = hotelService.getRooms();
        List<Reservation> active    = hotelService.getActiveReservations();

        // Update stat cards
        long occupied = rooms.stream()
                .filter(r -> r.getStatus() == RoomStatus.OCCUPIED)
                .count();
        double occupancy = rooms.isEmpty() ? 0 :
                (double) occupied / rooms.size() * 100;

        long checkedIn = active.stream()
                .filter(r -> r.getStatus() ==
                        Reservation.ReservationStatus.CHECKED_IN)
                .count();

        ReportService report = new ReportService(rooms,
                hotelService.getReservations());
        double revenue = report.getRevenueThisMonth();

        occupancyValue.setText(String.format("%.0f%%", occupancy));
        totalRoomsValue.setText(String.valueOf(rooms.size()));
        activeGuestsValue.setText(String.valueOf(checkedIn));
        revenueValue.setText(String.format("Rp %,.0f", revenue));

        // Update tabel reservasi
        reservationTableModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        for (Reservation r : active) {
            reservationTableModel.addRow(new Object[]{
                r.getReservationId(),
                r.getGuest().getName(),
                r.getRoom().getRoomNumber(),
                r.getRoom().getRoomType(),
                r.getCheckInDate().format(fmt),
                r.getCheckOutDate().format(fmt),
                r.getStatus().getDisplayName()
            });
        }

        // Update timestamp
        lastUpdatedLabel.setText("Terakhir diupdate: " +
                java.time.LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
    }

    // ══════════════════════════════════════════════════════════
    //  INNER CLASS — Status Cell Renderer
    // ══════════════════════════════════════════════════════════
    static class StatusCellRenderer extends
            javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

            if (!isSelected) {
                String status = value != null ? value.toString() : "";
                switch (status) {
                    case "Dipesan"  -> { setBackground(new Color(254, 243, 199));
                                        setForeground(new Color(146, 64, 14)); }
                    case "Check-in" -> { setBackground(new Color(209, 250, 229));
                                        setForeground(new Color(6, 95, 70)); }
                    default         -> { setBackground(MainFrame.WHITE);
                                        setForeground(MainFrame.TEXT_DARK); }
                }
            }
            return this;
        }
    }    
}
