/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.ui;

import com.hotelpms.enums.RoomStatus;
import com.hotelpms.model.Room;
import com.hotelpms.service.HotelService;
import com.hotelpms.service.HousekeepingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel Housekeeping — manajemen kebersihan kamar hotel.
 * Menampilkan antrian kamar kotor, update status kamar,
 * dan ringkasan kondisi semua kamar secara real-time.
 *
 * @author Rendy & Panji
 * @version 1.0
 */
public class HousekeepingPanel extends JPanel implements Refreshable {

    // ─── Service ──────────────────────────────────────────────
    private final HotelService hotelService;
    private final HousekeepingService housekeepingService;

    // ─── Komponen Tabel Antrian ───────────────────────────────
    private DefaultTableModel dirtyTableModel;
    private JTable dirtyTable;

    // ─── Komponen Tabel Semua Kamar ───────────────────────────
    private DefaultTableModel allRoomsTableModel;
    private JTable allRoomsTable;

    // ─── Komponen Summary ─────────────────────────────────────
    private JLabel cleanCount;
    private JLabel dirtyCount;
    private JLabel occupiedCount;
    private JLabel reservedCount;
    private JLabel oooCount;

    // ─── Constructor ─────────────────────────────────────────
    public HousekeepingPanel(HotelService hotelService) {
        this.hotelService        = hotelService;
        this.housekeepingService = hotelService.getHousekeepingService();
        initUI();
        refresh();
    }

    // ══════════════════════════════════════════════════════════
    //  UI INIT
    // ══════════════════════════════════════════════════════════
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(MainFrame.CONTENT_BG);

        add(MainFrame.buildPageHeader("Housekeeping",
                "Manajemen kebersihan dan status kamar"), BorderLayout.NORTH);

        // Layout utama: atas (summary) + tengah (split tabel)
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(MainFrame.CONTENT_BG);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        content.add(buildSummaryCards(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildDirtyQueuePanel(), buildAllRoomsPanel());
        split.setDividerLocation(420);
        split.setDividerSize(4);
        split.setBorder(null);

        content.add(split, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    // ── Summary Cards ─────────────────────────────────────────
    private JPanel buildSummaryCards() {
        JPanel row = new JPanel(new GridLayout(1, 5, 12, 0));
        row.setBackground(MainFrame.CONTENT_BG);
        row.setPreferredSize(new Dimension(0, 80));

        cleanCount    = new JLabel("0");
        dirtyCount    = new JLabel("0");
        occupiedCount = new JLabel("0");
        reservedCount = new JLabel("0");
        oooCount      = new JLabel("0");

        row.add(buildStatCard("Bersih",
                cleanCount,    new Color(6, 95, 70),   new Color(209, 250, 229)));
        row.add(buildStatCard("Perlu Dibersihkan",
                dirtyCount,    new Color(146, 64, 14),  new Color(254, 243, 199)));
        row.add(buildStatCard("Terisi",
                occupiedCount, new Color(30, 64, 175),  new Color(219, 234, 254)));
        row.add(buildStatCard("Dipesan",
                reservedCount, new Color(91, 33, 182),  new Color(237, 233, 254)));
        row.add(buildStatCard("Out of Order",
                oooCount,      new Color(153, 27, 27),  new Color(254, 226, 226)));

        return row;
    }

    private JPanel buildStatCard(String title, JLabel valueLabel,
                                  Color fg, Color bg) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(MainFrame.FONT_SMALL);
        titleLbl.setForeground(fg);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(fg);

        card.add(titleLbl,   BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // ── Antrian Kamar Kotor (kiri) ────────────────────────────
    private JPanel buildDirtyQueuePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(MainFrame.CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MainFrame.CONTENT_BG);

        JLabel title = new JLabel("Antrian Kamar Kotor");
        title.setFont(MainFrame.FONT_TITLE);
        title.setForeground(new Color(146, 64, 14));
        header.add(title, BorderLayout.WEST);

        JButton refreshBtn = MainFrame.buildSecondaryButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        header.add(refreshBtn, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        // Tabel
        String[] columns = {"No. Kamar", "Tipe", "Lantai", "Status"};
        dirtyTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        dirtyTable = new JTable(dirtyTableModel);
        MainFrame.styleTable(dirtyTable);
        dirtyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(dirtyTable);
        scroll.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER, 1));
        scroll.getViewport().setBackground(MainFrame.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        // Tombol aksi
        JPanel actions = new JPanel(new GridLayout(1, 2, 8, 0));
        actions.setBackground(MainFrame.CONTENT_BG);
        actions.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JButton cleanBtn = MainFrame.buildPrimaryButton("✓ Tandai CLEAN");
        cleanBtn.addActionListener(e -> handleMarkClean());
        actions.add(cleanBtn);

        JButton oooBtn = MainFrame.buildDangerButton("✗ Out of Order");
        oooBtn.addActionListener(e -> handleMarkOutOfOrder());
        actions.add(oooBtn);

        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    // ── Semua Kamar (kanan) ───────────────────────────────────
    private JPanel buildAllRoomsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(MainFrame.CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        JLabel title = new JLabel("Status Semua Kamar");
        title.setFont(MainFrame.FONT_TITLE);
        title.setForeground(MainFrame.TEXT_DARK);
        panel.add(title, BorderLayout.NORTH);

        String[] columns = {"No. Kamar", "Tipe", "Lantai", "Status"};
        allRoomsTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        allRoomsTable = new JTable(allRoomsTableModel);
        MainFrame.styleTable(allRoomsTable);
        allRoomsTable.getColumnModel().getColumn(3).setCellRenderer(
                new RoomPanel.RoomStatusCellRenderer());

        JScrollPane scroll = new JScrollPane(allRoomsTable);
        scroll.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER, 1));
        scroll.getViewport().setBackground(MainFrame.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ══════════════════════════════════════════════════════════
    //  ACTION HANDLERS
    // ══════════════════════════════════════════════════════════
    private void handleMarkClean() {
        int row = dirtyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Pilih kamar dari antrian terlebih dahulu!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String roomNumber = dirtyTableModel.getValueAt(row, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tandai kamar " + roomNumber + " sudah BERSIH?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = housekeepingService.markClean(roomNumber);
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Kamar " + roomNumber + " berhasil ditandai BERSIH!",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Gagal update status kamar " + roomNumber,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleMarkOutOfOrder() {
        int row = dirtyTable.getSelectedRow();
        if (row < 0) {
            // Coba dari tabel semua kamar
            row = allRoomsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this,
                        "Pilih kamar dari tabel terlebih dahulu!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String roomNumber = dirtyTableModel.getRowCount() > 0 && row >= 0
                ? dirtyTableModel.getValueAt(row, 0).toString()
                : allRoomsTableModel.getValueAt(
                        allRoomsTable.getSelectedRow(), 0).toString();

        String reason = JOptionPane.showInputDialog(this,
                "Alasan kamar " + roomNumber + " Out of Order:",
                "Out of Order", JOptionPane.PLAIN_MESSAGE);

        if (reason == null || reason.trim().isEmpty()) return;

        boolean success = housekeepingService.markOutOfOrder(
                roomNumber, reason.trim());
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Kamar " + roomNumber + " ditandai OUT OF ORDER.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Gagal update status kamar " + roomNumber,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  REFRESH
    // ══════════════════════════════════════════════════════════
    @Override
    public void refresh() {
        hotelService.refreshData();
        housekeepingService.refreshRooms();

        List<Room> allRooms = hotelService.getRooms();
        List<Room> dirty    = housekeepingService.getDirtyQueue();

        // Update summary cards
        long clean    = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.CLEAN).count();
        long dirtyC   = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.DIRTY).count();
        long occupied = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count();
        long reserved = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.RESERVED).count();
        long ooo      = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.OUT_OF_ORDER).count();

        cleanCount.setText(String.valueOf(clean));
        dirtyCount.setText(String.valueOf(dirtyC));
        occupiedCount.setText(String.valueOf(occupied));
        reservedCount.setText(String.valueOf(reserved));
        oooCount.setText(String.valueOf(ooo));

        // Update tabel antrian kotor
        dirtyTableModel.setRowCount(0);
        for (Room room : dirty) {
            dirtyTableModel.addRow(new Object[]{
                room.getRoomNumber(),
                room.getRoomType(),
                "Lantai " + room.getFloor(),
                room.getStatus().getDisplayName()
            });
        }

        // Update tabel semua kamar
        allRoomsTableModel.setRowCount(0);
        for (Room room : allRooms) {
            allRoomsTableModel.addRow(new Object[]{
                room.getRoomNumber(),
                room.getRoomType(),
                "Lantai " + room.getFloor(),
                room.getStatus().getDisplayName()
            });
        }
    }
}