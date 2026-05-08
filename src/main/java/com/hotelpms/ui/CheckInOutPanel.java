/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.ui;

import com.hotelpms.model.Reservation;
import com.hotelpms.service.HotelService;
 
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *
 * @author rendysaptra
 */
public class CheckInOutPanel extends JPanel implements Refreshable {
    
    // ─── Service ──────────────────────────────────────────────
    private final HotelService hotelService;

    // ─── Komponen ─────────────────────────────────────────────
    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JTable reservationTable;
    private JPanel detailPanel;

    // ─── State ────────────────────────────────────────────────
    private Reservation selectedReservation;

    // ─── Format ───────────────────────────────────────────────
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ─── Constructor ─────────────────────────────────────────
    public CheckInOutPanel(HotelService hotelService) {
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

        add(MainFrame.buildPageHeader("Check-in / Check-out",
                "Proses kedatangan dan keberangkatan tamu"), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftPanel(), buildDetailPanel());
        split.setDividerLocation(480);
        split.setDividerSize(4);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);
    }

    // ── Panel Kiri: Search + Tabel ────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(MainFrame.CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 8));

        // Search bar
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setBackground(MainFrame.CONTENT_BG);

        searchField = new JTextField();
        searchField.setFont(MainFrame.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        searchField.setToolTipText("Cari by ID reservasi atau nama tamu");

        JButton searchBtn = MainFrame.buildPrimaryButton("Cari");
        searchBtn.addActionListener(e -> handleSearch());
        searchField.addActionListener(e -> handleSearch());

        JButton showAllBtn = MainFrame.buildSecondaryButton("Tampilkan Semua");
        showAllBtn.addActionListener(e -> refresh());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(MainFrame.CONTENT_BG);
        btnPanel.add(showAllBtn);
        btnPanel.add(searchBtn);

        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(btnPanel, BorderLayout.EAST);
        panel.add(searchBar, BorderLayout.NORTH);

        // Tabel reservasi aktif
        String[] columns = {"ID Reservasi", "Nama Tamu", "Kamar",
                "Check-in", "Check-out", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        reservationTable = new JTable(tableModel);
        MainFrame.styleTable(reservationTable);
        reservationTable.getColumnModel().getColumn(5).setCellRenderer(
                new DashboardPanel.StatusCellRenderer());
        reservationTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        // Listener saat baris dipilih
        reservationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = reservationTable.getSelectedRow();
                if (row >= 0) {
                    String id = tableModel.getValueAt(row, 0).toString();
                    selectedReservation = hotelService.findReservationById(id);
                    updateDetailPanel();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(reservationTable);
        scroll.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER, 1));
        scroll.getViewport().setBackground(MainFrame.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ── Panel Kanan: Detail + Aksi ────────────────────────────
    private JPanel buildDetailPanel() {
        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBackground(MainFrame.WHITE);
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, MainFrame.BORDER),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        showEmptyDetail();
        return detailPanel;
    }

    private void showEmptyDetail() {
        detailPanel.removeAll();

        JLabel hint = new JLabel("<html><div style='text-align:center'>" +
                "<b>Pilih Reservasi</b><br><br>" +
                "Klik baris reservasi di tabel<br>" +
                "untuk melihat detail dan<br>" +
                "melakukan aksi check-in/out" +
                "</div></html>");
        hint.setFont(MainFrame.FONT_BODY);
        hint.setForeground(new Color(180, 180, 180));
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        detailPanel.add(hint, BorderLayout.CENTER);

        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void updateDetailPanel() {
        if (selectedReservation == null) {
            showEmptyDetail();
            return;
        }

        detailPanel.removeAll();
        detailPanel.setLayout(new BorderLayout(0, 16));

        // ── Detail Info ──
        JPanel info = new JPanel(new GridLayout(0, 2, 8, 10));
        info.setBackground(MainFrame.WHITE);
        info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        addDetailRow(info, "ID Reservasi",
                selectedReservation.getReservationId());
        addDetailRow(info, "Nama Tamu",
                selectedReservation.getGuest().getName());
        addDetailRow(info, "No. KTP",
                selectedReservation.getGuest().getIdCard());
        addDetailRow(info, "Nomor Kamar",
                selectedReservation.getRoom().getRoomNumber());
        addDetailRow(info, "Tipe Kamar",
                selectedReservation.getRoom().getRoomType());
        addDetailRow(info, "Check-in",
                selectedReservation.getCheckInDate().format(DATE_FMT));
        addDetailRow(info, "Check-out",
                selectedReservation.getCheckOutDate().format(DATE_FMT));
        addDetailRow(info, "Total Malam",
                selectedReservation.getTotalNights() + " malam");
        addDetailRow(info, "Status",
                selectedReservation.getStatus().getDisplayName());

        String keyCard = selectedReservation.getKeyCardId();
        addDetailRow(info, "Key Card",
                keyCard != null ? keyCard : "Belum diterbitkan");

        detailPanel.add(info, BorderLayout.NORTH);

        // ── Tombol Aksi ──
        JPanel actions = new JPanel(new GridLayout(0, 1, 0, 10));
        actions.setBackground(MainFrame.WHITE);
        actions.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        Reservation.ReservationStatus status = selectedReservation.getStatus();

        if (status == Reservation.ReservationStatus.RESERVED) {
            JButton checkInBtn = MainFrame.buildPrimaryButton(
                    "✓  Proses Check-in");
            checkInBtn.setPreferredSize(new Dimension(0, 44));
            checkInBtn.addActionListener(e -> handleCheckIn());
            actions.add(checkInBtn);

        } else if (status == Reservation.ReservationStatus.CHECKED_IN) {
            // Info key card
            JPanel keyCardPanel = new JPanel(new BorderLayout());
            keyCardPanel.setBackground(new Color(225, 245, 237));
            keyCardPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            new Color(134, 200, 170), 1),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)));

            JLabel keyLbl = new JLabel("Key Card: " +
                    selectedReservation.getKeyCardId());
            keyLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            keyLbl.setForeground(new Color(6, 95, 70));
            keyCardPanel.add(keyLbl, BorderLayout.CENTER);
            actions.add(keyCardPanel);

            JButton checkOutBtn = MainFrame.buildDangerButton(
                    "✗  Proses Check-out");
            checkOutBtn.setPreferredSize(new Dimension(0, 44));
            checkOutBtn.addActionListener(e -> handleCheckOut());
            actions.add(checkOutBtn);

        } else {
            JLabel doneLbl = new JLabel("Reservasi ini sudah selesai.");
            doneLbl.setFont(MainFrame.FONT_BODY);
            doneLbl.setForeground(new Color(130, 130, 130));
            doneLbl.setHorizontalAlignment(SwingConstants.CENTER);
            actions.add(doneLbl);
        }

        detailPanel.add(actions, BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(MainFrame.FONT_SMALL);
        lbl.setForeground(new Color(100, 100, 100));
        panel.add(lbl);

        JLabel val = new JLabel(value);
        val.setFont(MainFrame.FONT_BODY);
        val.setForeground(MainFrame.TEXT_DARK);
        panel.add(val);
    }

    // ══════════════════════════════════════════════════════════
    //  ACTION HANDLERS
    // ══════════════════════════════════════════════════════════
    private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) { refresh(); return; }

        List<Reservation> all = hotelService.getReservations();
        List<Reservation> results = all.stream()
                .filter(r -> r.getReservationId().toLowerCase().contains(keyword)
                          || r.getGuest().getName().toLowerCase().contains(keyword))
                .toList();
        populateTable(results);
    }

    private void handleCheckIn() {
        if (selectedReservation == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Proses check-in untuk:\n" +
                "Tamu  : " + selectedReservation.getGuest().getName() + "\n" +
                "Kamar : " + selectedReservation.getRoom().getRoomNumber() + "\n" +
                "Tanggal: " + selectedReservation.getCheckInDate().format(DATE_FMT),
                "Konfirmasi Check-in",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            String keyCardId = hotelService.checkIn(
                    selectedReservation.getReservationId());

            // Tampilkan key card
            JPanel successPanel = new JPanel(new BorderLayout(0, 12));
            JLabel msg = new JLabel("<html><b>Check-in berhasil!</b><br><br>" +
                    "Key Card telah diterbitkan:</html>");
            msg.setFont(MainFrame.FONT_BODY);

            JLabel keyCard = new JLabel(keyCardId);
            keyCard.setFont(new Font("Courier New", Font.BOLD, 18));
            keyCard.setForeground(MainFrame.PRIMARY);
            keyCard.setHorizontalAlignment(SwingConstants.CENTER);
            keyCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(MainFrame.PRIMARY, 2),
                    BorderFactory.createEmptyBorder(12, 24, 12, 24)));

            successPanel.add(msg, BorderLayout.NORTH);
            successPanel.add(keyCard, BorderLayout.CENTER);

            JOptionPane.showMessageDialog(this, successPanel,
                    "Check-in Berhasil", JOptionPane.INFORMATION_MESSAGE);

            refresh();
            selectedReservation = hotelService.findReservationById(
                    selectedReservation.getReservationId());
            updateDetailPanel();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal check-in:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCheckOut() {
        if (selectedReservation == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Proses check-out untuk:\n" +
                "Tamu  : " + selectedReservation.getGuest().getName() + "\n" +
                "Kamar : " + selectedReservation.getRoom().getRoomNumber() + "\n\n" +
                "Tagihan akan dikalkulasi dan folio akan dicetak.",
                "Konfirmasi Check-out",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            double total = hotelService.checkOut(
                    selectedReservation.getReservationId());

            JOptionPane.showMessageDialog(this,
                    "<html><b>Check-out berhasil!</b><br><br>" +
                    "Total tagihan: <b>Rp " +
                    String.format("%,.0f", total) + "</b><br><br>" +
                    "Folio telah disimpan ke folder data/folio/</html>",
                    "Check-out Berhasil",
                    JOptionPane.INFORMATION_MESSAGE);

            refresh();
            showEmptyDetail();
            selectedReservation = null;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal check-out:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  REFRESH
    // ══════════════════════════════════════════════════════════
    @Override
    public void refresh() {
        hotelService.refreshData();
        List<Reservation> active = hotelService.getActiveReservations();
        populateTable(active);
    }

    private void populateTable(List<Reservation> reservations) {
        tableModel.setRowCount(0);
        for (Reservation r : reservations) {
            tableModel.addRow(new Object[]{
                r.getReservationId(),
                r.getGuest().getName(),
                r.getRoom().getRoomNumber(),
                r.getCheckInDate().format(DATE_FMT),
                r.getCheckOutDate().format(DATE_FMT),
                r.getStatus().getDisplayName()
            });
        }
    }

}
