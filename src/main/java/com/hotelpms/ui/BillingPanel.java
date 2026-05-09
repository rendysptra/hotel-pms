/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.ui;

import com.hotelpms.exception.PaymentFailedException;
import com.hotelpms.model.FolioItem;
import com.hotelpms.model.Reservation;
import com.hotelpms.service.BillingService;
import com.hotelpms.service.HotelService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel Billing — manajemen tagihan dan folio tamu.
 * Menampilkan folio itemized, tambah charge,
 * kalkulasi pajak, dan cetak tagihan.
 *
 * @author Rendy & Panji
 * @version 1.0
 */
public class BillingPanel extends JPanel implements Refreshable {

    // ─── Service ──────────────────────────────────────────────
    private final HotelService hotelService;
    private final BillingService billingService;

    // ─── Komponen ─────────────────────────────────────────────
    private JComboBox<String> reservationCombo;
    private DefaultTableModel folioTableModel;
    private JLabel subtotalLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;

    // ─── Form Tambah Charge ───────────────────────────────────
    private JTextField descField;
    private JTextField amountField;
    private JComboBox<String> chargeTypeCombo;

    // ─── State ────────────────────────────────────────────────
    private Reservation selectedReservation;

    // ─── Constructor ─────────────────────────────────────────
    public BillingPanel(HotelService hotelService) {
        this.hotelService   = hotelService;
        this.billingService = new BillingService();
        initUI();
        refresh();
    }

    // ══════════════════════════════════════════════════════════
    //  UI INIT
    // ══════════════════════════════════════════════════════════
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(MainFrame.CONTENT_BG);

        add(MainFrame.buildPageHeader("Billing",
                "Manajemen tagihan dan folio tamu"), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftPanel(), buildRightPanel());
        split.setDividerLocation(520);
        split.setDividerSize(4);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);
    }

    // ── Panel Kiri: Pilih Reservasi + Folio ───────────────────
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(MainFrame.CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 8));

        // Pilih reservasi
        JPanel selectPanel = new JPanel(new BorderLayout(8, 0));
        selectPanel.setBackground(MainFrame.WHITE);
        selectPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel selectLbl = new JLabel("Reservasi:");
        selectLbl.setFont(MainFrame.FONT_BODY);
        selectPanel.add(selectLbl, BorderLayout.WEST);

        reservationCombo = new JComboBox<>();
        reservationCombo.setFont(MainFrame.FONT_BODY);
        reservationCombo.addActionListener(e -> handleReservationSelected());
        selectPanel.add(reservationCombo, BorderLayout.CENTER);

        JButton loadBtn = MainFrame.buildPrimaryButton("Load Folio");
        loadBtn.addActionListener(e -> handleReservationSelected());
        selectPanel.add(loadBtn, BorderLayout.EAST);

        panel.add(selectPanel, BorderLayout.NORTH);

        // Tabel folio
        JPanel folioPanel = new JPanel(new BorderLayout(0, 8));
        folioPanel.setBackground(MainFrame.CONTENT_BG);

        JLabel folioTitle = new JLabel("Detail Folio");
        folioTitle.setFont(MainFrame.FONT_TITLE);
        folioTitle.setForeground(MainFrame.TEXT_DARK);
        folioPanel.add(folioTitle, BorderLayout.NORTH);

        String[] columns = {"No", "Tanggal", "Tipe", "Deskripsi", "Jumlah"};
        folioTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable folioTable = new JTable(folioTableModel);
        MainFrame.styleTable(folioTable);

        int[] widths = {40, 100, 100, 200, 110};
        for (int i = 0; i < widths.length; i++) {
            folioTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Right-align kolom jumlah
        DefaultTableCellRenderer rightRenderer =
                new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        folioTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scroll = new JScrollPane(folioTable);
        scroll.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER, 1));
        scroll.getViewport().setBackground(MainFrame.WHITE);
        folioPanel.add(scroll, BorderLayout.CENTER);

        // Summary tagihan
        folioPanel.add(buildBillingSummary(), BorderLayout.SOUTH);

        panel.add(folioPanel, BorderLayout.CENTER);
        return panel;
    }

    // ── Billing Summary ───────────────────────────────────────
    private JPanel buildBillingSummary() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 0, 4));
        panel.setBackground(MainFrame.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, MainFrame.BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel subLbl = new JLabel("Subtotal:");
        subLbl.setFont(MainFrame.FONT_BODY);
        subtotalLabel = new JLabel("Rp 0");
        subtotalLabel.setFont(MainFrame.FONT_BODY);
        subtotalLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel taxLbl = new JLabel("Pajak (11%):");
        taxLbl.setFont(MainFrame.FONT_BODY);
        taxLabel = new JLabel("Rp 0");
        taxLabel.setFont(MainFrame.FONT_BODY);
        taxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel totalLbl = new JLabel("TOTAL:");
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLbl.setForeground(MainFrame.PRIMARY);
        totalLabel = new JLabel("Rp 0");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(MainFrame.PRIMARY);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(subLbl);   panel.add(subtotalLabel);
        panel.add(taxLbl);   panel.add(taxLabel);
        panel.add(totalLbl); panel.add(totalLabel);

        return panel;
    }

    // ── Panel Kanan: Tambah Charge + Aksi ────────────────────
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MainFrame.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(
                0, 1, 0, 0, MainFrame.BORDER));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MainFrame.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MainFrame.BORDER),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        JLabel title = new JLabel("Tambah Tagihan");
        title.setFont(MainFrame.FONT_TITLE);
        header.add(title, BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(MainFrame.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        form.add(buildFieldLabel("Tipe Tagihan"));
        chargeTypeCombo = new JComboBox<>(new String[]{
            "ROOM", "ROOM_SERVICE", "LAUNDRY", "MINIBAR", "OTHER"});
        chargeTypeCombo.setFont(MainFrame.FONT_BODY);
        chargeTypeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        form.add(chargeTypeCombo);
        form.add(Box.createVerticalStrut(12));

        form.add(buildFieldLabel("Deskripsi *"));
        descField = buildTextField("Contoh: Laundry 3 potong");
        form.add(descField);
        form.add(Box.createVerticalStrut(12));

        form.add(buildFieldLabel("Jumlah (Rp) *"));
        amountField = buildTextField("Contoh: 50000");
        form.add(amountField);
        form.add(Box.createVerticalStrut(20));

        JButton addChargeBtn = MainFrame.buildPrimaryButton("Tambah Tagihan");
        addChargeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        addChargeBtn.addActionListener(e -> handleAddCharge());
        form.add(addChargeBtn);
        form.add(Box.createVerticalStrut(20));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(MainFrame.BORDER);
        form.add(sep);
        form.add(Box.createVerticalStrut(16));

        // Aksi cetak
        JLabel printTitle = new JLabel("Cetak Folio");
        printTitle.setFont(MainFrame.FONT_TITLE);
        printTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(printTitle);
        form.add(Box.createVerticalStrut(12));

        JButton printConsoleBtn = MainFrame.buildSecondaryButton(
                "Tampilkan di Console");
        printConsoleBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        printConsoleBtn.addActionListener(e -> handlePrintConsole());
        form.add(printConsoleBtn);
        form.add(Box.createVerticalStrut(8));

        JButton printFileBtn = MainFrame.buildPrimaryButton(
                "Simpan Folio ke File (.txt)");
        printFileBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        printFileBtn.addActionListener(e -> handlePrintFile());
        form.add(printFileBtn);

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    // ══════════════════════════════════════════════════════════
    //  ACTION HANDLERS
    // ══════════════════════════════════════════════════════════
    private void handleReservationSelected() {
        String selected = (String) reservationCombo.getSelectedItem();
        if (selected == null || selected.isEmpty()) return;

        String reservationId = selected.split(" ")[0];
        try {
            selectedReservation = hotelService.findReservationById(reservationId);
            loadFolioItems();
        } catch (Exception e) {
            System.err.println("Reservasi tidak ditemukan: " + e.getMessage());
        }
    }

    private void loadFolioItems() {
        if (selectedReservation == null) return;

        folioTableModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        int no = 1;

        for (FolioItem item : selectedReservation.getFolioItems()) {
            folioTableModel.addRow(new Object[]{
                no++,
                item.getDate().format(fmt),
                item.getChargeType().getDisplayName(),
                item.getDescription(),
                item.getFormattedAmount()
            });
        }

        updateBillingSummary();
    }

    private void updateBillingSummary() {
        if (selectedReservation == null) return;

        try {
            double subtotal = billingService.calculateSubtotal(selectedReservation);
            double tax      = billingService.calculateTax(subtotal);
            double total    = subtotal + tax;

            subtotalLabel.setText(billingService.formatRupiah(subtotal));
            taxLabel.setText(billingService.formatRupiah(tax));
            totalLabel.setText(billingService.formatRupiah(total));
        } catch (Exception e) {
            System.err.println("Error kalkulasi: " + e.getMessage());
        }
    }

    private void handleAddCharge() {
        if (selectedReservation == null) {
            JOptionPane.showMessageDialog(this,
                    "Pilih reservasi terlebih dahulu!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String desc      = descField.getText().trim();
        String amountStr = amountField.getText().trim();
        String typeStr   = (String) chargeTypeCombo.getSelectedItem();

        if (desc.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Deskripsi dan jumlah wajib diisi!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            FolioItem.ChargeType chargeType =
                    FolioItem.ChargeType.valueOf(typeStr);

            billingService.addCharge(selectedReservation,
                    desc, chargeType, amount);

            descField.setText("");
            amountField.setText("");

            loadFolioItems();
            JOptionPane.showMessageDialog(this,
                    "Tagihan berhasil ditambahkan!",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Jumlah harus berupa angka!",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePrintConsole() {
        if (selectedReservation == null) {
            JOptionPane.showMessageDialog(this,
                    "Pilih reservasi terlebih dahulu!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            billingService.printFolioToConsole(selectedReservation);
            JOptionPane.showMessageDialog(this,
                    "Folio sudah ditampilkan di console (Output window).",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
        } catch (PaymentFailedException e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal mencetak folio: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePrintFile() {
        if (selectedReservation == null) {
            JOptionPane.showMessageDialog(this,
                    "Pilih reservasi terlebih dahulu!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            billingService.printFolioToFile(selectedReservation);
            JOptionPane.showMessageDialog(this,
                    "Folio berhasil disimpan ke:\ndata/folio/folio-"
                    + selectedReservation.getReservationId() + ".txt",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } catch (PaymentFailedException e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal menyimpan folio: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  REFRESH
    // ══════════════════════════════════════════════════════════
    @Override
    public void refresh() {
        hotelService.refreshData();

        // Update dropdown reservasi
        reservationCombo.removeAllItems();
        List<Reservation> active = hotelService.getActiveReservations();
        for (Reservation r : active) {
            reservationCombo.addItem(r.getReservationId()
                    + " — " + r.getGuest().getName()
                    + " (Kamar " + r.getRoom().getRoomNumber() + ")");
        }

        // Juga tambah yang sudah checkout (untuk lihat folio lama)
        List<Reservation> all = hotelService.getReservations();
        for (Reservation r : all) {
            if (r.getStatus() ==
                    Reservation.ReservationStatus.CHECKED_OUT) {
                reservationCombo.addItem(r.getReservationId()
                        + " — " + r.getGuest().getName()
                        + " [Selesai]");
            }
        }

        if (selectedReservation != null) {
            loadFolioItems();
        }
    }

    // ══════════════════════════════════════════════════════════
    //  HELPER
    // ══════════════════════════════════════════════════════════
    private JLabel buildFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(MainFrame.FONT_SMALL);
        lbl.setForeground(new Color(80, 80, 80));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    private JTextField buildTextField(String tooltip) {
        JTextField field = new JTextField();
        field.setFont(MainFrame.FONT_BODY);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        field.setToolTipText(tooltip);
        return field;
    }
}