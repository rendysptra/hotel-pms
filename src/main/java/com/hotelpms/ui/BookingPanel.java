/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.ui;

import com.hotelpms.exception.InvalidDateException;
import com.hotelpms.exception.RoomUnavailableException;
import com.hotelpms.model.Guest;
import com.hotelpms.model.Reservation;
import com.hotelpms.service.HotelService;
 
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Panel Booking — manajemen reservasi hotel.
 * Menangani pembuatan, modifikasi, dan pembatalan reservasi.
 *
 * @author rendysaptra
 */
public class BookingPanel extends JPanel implements Refreshable{

    // ─── Service ──────────────────────────────────────────────
    private final HotelService hotelService;

    // ─── Komponen Form ────────────────────────────────────────
    private JTextField guestIdField;
    private JComboBox<String> roomTypeCombo;
    private JTextField checkInField;
    private JTextField checkOutField;

    // ─── Komponen Tabel ───────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable reservationTable;

    // ─── Filter ───────────────────────────────────────────────
    private JComboBox<String> filterCombo;

    // ─── Format Tanggal ───────────────────────────────────────
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ─── Constructor ─────────────────────────────────────────
    public BookingPanel(HotelService hotelService) {
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

        add(MainFrame.buildPageHeader("Reservasi",
                "Kelola pemesanan kamar hotel"), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildFormPanel(), buildTablePanel());
        split.setDividerLocation(320);
        split.setDividerSize(4);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);
    }

    // ── Form Panel (kiri) ─────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MainFrame.WHITE);
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, MainFrame.BORDER));

        // Form header
        JPanel formHeader = new JPanel(new BorderLayout());
        formHeader.setBackground(MainFrame.WHITE);
        formHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MainFrame.BORDER),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        JLabel title = new JLabel("Reservasi Baru");
        title.setFont(MainFrame.FONT_TITLE);
        title.setForeground(MainFrame.TEXT_DARK);
        formHeader.add(title, BorderLayout.WEST);
        panel.add(formHeader, BorderLayout.NORTH);

        // Form fields
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(MainFrame.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Guest ID
        form.add(buildFieldLabel("ID Tamu *"));
        guestIdField = buildTextField("Contoh: GST-001");
        form.add(guestIdField);
        form.add(Box.createVerticalStrut(12));

        // Tipe Kamar
        form.add(buildFieldLabel("Tipe Kamar *"));
        roomTypeCombo = new JComboBox<>(new String[]{"STANDARD", "DELUXE", "VIP"});
        roomTypeCombo.setFont(MainFrame.FONT_BODY);
        roomTypeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        form.add(roomTypeCombo);
        form.add(Box.createVerticalStrut(12));

        // Check-in
        form.add(buildFieldLabel("Tanggal Check-in * (dd-MM-yyyy)"));
        checkInField = buildTextField("Contoh: 01-06-2025");
        form.add(checkInField);
        form.add(Box.createVerticalStrut(12));

        // Check-out
        form.add(buildFieldLabel("Tanggal Check-out * (dd-MM-yyyy)"));
        checkOutField = buildTextField("Contoh: 04-06-2025");
        form.add(checkOutField);
        form.add(Box.createVerticalStrut(20));

        // Tombol buat reservasi
        JButton createBtn = MainFrame.buildPrimaryButton("Buat Reservasi");
        createBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        createBtn.addActionListener(e -> handleCreateReservation());
        form.add(createBtn);
        form.add(Box.createVerticalStrut(8));

        // Tombol reset form
        JButton resetBtn = MainFrame.buildSecondaryButton("Reset Form");
        resetBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        resetBtn.addActionListener(e -> resetForm());
        form.add(resetBtn);
        form.add(Box.createVerticalStrut(20));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(MainFrame.BORDER);
        form.add(sep);
        form.add(Box.createVerticalStrut(16));

        // Section modifikasi
        JLabel modTitle = new JLabel("Modifikasi / Batalkan");
        modTitle.setFont(MainFrame.FONT_TITLE);
        modTitle.setForeground(MainFrame.TEXT_DARK);
        modTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(modTitle);
        form.add(Box.createVerticalStrut(12));

        // Info pilih dari tabel
        JLabel hint = new JLabel("<html><i>Pilih reservasi dari tabel<br>lalu klik tombol di bawah</i></html>");
        hint.setFont(MainFrame.FONT_SMALL);
        hint.setForeground(new Color(130, 130, 130));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(hint);
        form.add(Box.createVerticalStrut(12));

        // Tombol modify
        JButton modifyBtn = MainFrame.buildSecondaryButton("Ubah Tanggal");
        modifyBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        modifyBtn.addActionListener(e -> handleModifyReservation());
        form.add(modifyBtn);
        form.add(Box.createVerticalStrut(8));

        // Tombol cancel
        JButton cancelBtn = MainFrame.buildDangerButton("Batalkan Reservasi");
        cancelBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cancelBtn.addActionListener(e -> handleCancelReservation());
        form.add(cancelBtn);

        panel.add(new JScrollPane(form,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        return panel;
    }

    // ── Table Panel (kanan) ───────────────────────────────────
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(MainFrame.CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(MainFrame.CONTENT_BG);

        JLabel title = new JLabel("Semua Reservasi");
        title.setFont(MainFrame.FONT_TITLE);
        title.setForeground(MainFrame.TEXT_DARK);
        toolbar.add(title, BorderLayout.WEST);

        JPanel toolbarRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbarRight.setBackground(MainFrame.CONTENT_BG);

        filterCombo = new JComboBox<>(new String[]{
            "Semua", "Dipesan", "Check-in", "Check-out", "Dibatalkan"});
        filterCombo.setFont(MainFrame.FONT_BODY);
        filterCombo.addActionListener(e -> applyFilter());
        toolbarRight.add(new JLabel("Filter:"));
        toolbarRight.add(filterCombo);

        JButton refreshBtn = MainFrame.buildSecondaryButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        toolbarRight.add(refreshBtn);
        toolbar.add(toolbarRight, BorderLayout.EAST);

        panel.add(toolbar, BorderLayout.NORTH);

        // Tabel
        String[] columns = {"ID Reservasi", "Tamu", "Kamar",
                "Tipe", "Check-in", "Check-out", "Malam", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        reservationTable = new JTable(tableModel);
        MainFrame.styleTable(reservationTable);
        reservationTable.getColumnModel().getColumn(7).setCellRenderer(
                new DashboardPanel.StatusCellRenderer());

        // Set lebar kolom
        int[] widths = {150, 140, 60, 80, 100, 100, 60, 90};
        for (int i = 0; i < widths.length; i++) {
            reservationTable.getColumnModel().getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(reservationTable);
        scroll.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER, 1));
        scroll.getViewport().setBackground(MainFrame.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ══════════════════════════════════════════════════════════
    //  ACTION HANDLERS
    // ══════════════════════════════════════════════════════════
    private void handleCreateReservation() {
        String guestId   = guestIdField.getText().trim();
        String roomType  = (String) roomTypeCombo.getSelectedItem();
        String ciStr     = checkInField.getText().trim();
        String coStr     = checkOutField.getText().trim();

        // Validasi input kosong
        if (guestId.isEmpty() || ciStr.isEmpty() || coStr.isEmpty()) {
            showError("Semua field wajib diisi!");
            return;
        }

        try {
            LocalDate checkIn  = LocalDate.parse(ciStr, DATE_FMT);
            LocalDate checkOut = LocalDate.parse(coStr, DATE_FMT);

            Guest guest = hotelService.findGuestById(guestId);
            Reservation rsv = hotelService.createReservation(
                    guest, roomType, checkIn, checkOut);

            showSuccess("Reservasi berhasil dibuat!\nID: " + rsv.getReservationId()
                    + "\nKamar: " + rsv.getRoom().getRoomNumber());
            resetForm();
            refresh();

        } catch (DateTimeParseException e) {
            showError("Format tanggal tidak valid!\nGunakan format: dd-MM-yyyy");
        } catch (InvalidDateException e) {
            showError("Tanggal tidak valid:\n" + e.getMessage());
        } catch (RoomUnavailableException e) {
            showError("Kamar tidak tersedia:\n" + e.getMessage());
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void handleModifyReservation() {
        int row = reservationTable.getSelectedRow();
        if (row < 0) {
            showError("Pilih reservasi dari tabel terlebih dahulu!");
            return;
        }

        String reservationId = tableModel.getValueAt(row, 0).toString();
        String status        = tableModel.getValueAt(row, 7).toString();

        if (!status.equals("Dipesan")) {
            showError("Hanya reservasi berstatus 'Dipesan' yang bisa diubah!");
            return;
        }

        // Dialog input tanggal baru
        JTextField newCiField = buildTextField("dd-MM-yyyy");
        JTextField newCoField = buildTextField("dd-MM-yyyy");

        JPanel dlgPanel = new JPanel(new GridLayout(4, 1, 0, 8));
        dlgPanel.add(new JLabel("Check-in baru (dd-MM-yyyy):"));
        dlgPanel.add(newCiField);
        dlgPanel.add(new JLabel("Check-out baru (dd-MM-yyyy):"));
        dlgPanel.add(newCoField);

        int result = JOptionPane.showConfirmDialog(this, dlgPanel,
                "Ubah Tanggal Reservasi", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        try {
            LocalDate newCi = LocalDate.parse(newCiField.getText().trim(), DATE_FMT);
            LocalDate newCo = LocalDate.parse(newCoField.getText().trim(), DATE_FMT);

            hotelService.modifyReservation(reservationId, newCi, newCo);
            showSuccess("Reservasi berhasil diubah!");
            refresh();

        } catch (DateTimeParseException e) {
            showError("Format tanggal tidak valid!\nGunakan format: dd-MM-yyyy");
        } catch (Exception e) {
            showError("Gagal mengubah reservasi:\n" + e.getMessage());
        }
    }

    private void handleCancelReservation() {
        int row = reservationTable.getSelectedRow();
        if (row < 0) {
            showError("Pilih reservasi dari tabel terlebih dahulu!");
            return;
        }

        String reservationId = tableModel.getValueAt(row, 0).toString();
        String status        = tableModel.getValueAt(row, 7).toString();

        if (!status.equals("Dipesan")) {
            showError("Hanya reservasi berstatus 'Dipesan' yang bisa dibatalkan!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Yakin ingin membatalkan reservasi " + reservationId + "?",
                "Konfirmasi Pembatalan",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            hotelService.cancelReservation(reservationId);
            showSuccess("Reservasi " + reservationId + " berhasil dibatalkan!");
            refresh();
        } catch (Exception e) {
            showError("Gagal membatalkan reservasi:\n" + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  REFRESH & FILTER
    // ══════════════════════════════════════════════════════════
    @Override
    public void refresh() {
        hotelService.refreshData();
        populateTable(hotelService.getReservations());
    }

    private void applyFilter() {
        String filter = (String) filterCombo.getSelectedItem();
        List<Reservation> all = hotelService.getReservations();

        if ("Semua".equals(filter)) {
            populateTable(all);
            return;
        }

        List<Reservation> filtered = all.stream()
                .filter(r -> r.getStatus().getDisplayName().equals(filter))
                .toList();
        populateTable(filtered);
    }

    private void populateTable(List<Reservation> reservations) {
        tableModel.setRowCount(0);
        for (Reservation r : reservations) {
            tableModel.addRow(new Object[]{
                r.getReservationId(),
                r.getGuest().getName(),
                r.getRoom().getRoomNumber(),
                r.getRoom().getRoomType(),
                r.getCheckInDate().format(DATE_FMT),
                r.getCheckOutDate().format(DATE_FMT),
                r.getTotalNights() + " malam",
                r.getStatus().getDisplayName()
            });
        }
    }

    // ══════════════════════════════════════════════════════════
    //  HELPER
    // ══════════════════════════════════════════════════════════
    private void resetForm() {
        guestIdField.setText("");
        checkInField.setText("");
        checkOutField.setText("");
        roomTypeCombo.setSelectedIndex(0);
    }

    private JLabel buildFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(MainFrame.FONT_SMALL);
        lbl.setForeground(new Color(80, 80, 80));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    private JTextField buildTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(MainFrame.FONT_BODY);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        field.setToolTipText(placeholder);
        return field;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message,
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);
    }
}
