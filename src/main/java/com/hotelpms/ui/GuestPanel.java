/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.ui;

import com.hotelpms.model.Guest;
import com.hotelpms.model.Reservation;
import com.hotelpms.service.HotelService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel Tamu — manajemen profil dan riwayat tamu hotel.
 * Menampilkan daftar tamu, detail profil, preferensi,
 * loyalty points, dan riwayat reservasi.
 *
 * @author Rendy & Panji
 * @version 1.0
 */
public class GuestPanel extends JPanel implements Refreshable {

    // ─── Service ──────────────────────────────────────────────
    private final HotelService hotelService;

    // ─── Komponen Tabel ───────────────────────────────────────
    private DefaultTableModel guestTableModel;
    private JTable guestTable;

    // ─── Komponen Form ────────────────────────────────────────
    private JTextField guestIdField;
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField idCardField;

    // ─── Komponen Detail ──────────────────────────────────────
    private JPanel detailPanel;
    private JTextArea preferenceArea;
    private DefaultTableModel historyTableModel;

    // ─── Komponen Search ──────────────────────────────────────
    private JTextField searchField;

    // ─── Format ───────────────────────────────────────────────
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ─── Constructor ─────────────────────────────────────────
    public GuestPanel(HotelService hotelService) {
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

        add(MainFrame.buildPageHeader("Tamu",
                "Manajemen profil dan riwayat tamu hotel"), BorderLayout.NORTH);

        // Split utama: kiri (tabel+form) | kanan (detail)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftPanel(), buildDetailPanel());
        mainSplit.setDividerLocation(520);
        mainSplit.setDividerSize(4);
        mainSplit.setBorder(null);

        add(mainSplit, BorderLayout.CENTER);
    }

    // ── Panel Kiri ────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(MainFrame.CONTENT_BG);

        // Split vertikal: atas (tabel) | bawah (form)
        JSplitPane vertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                buildGuestTablePanel(), buildAddGuestForm());
        vertSplit.setDividerLocation(320);
        vertSplit.setDividerSize(4);
        vertSplit.setBorder(null);

        panel.add(vertSplit, BorderLayout.CENTER);
        return panel;
    }

    // ── Tabel Tamu ────────────────────────────────────────────
    private JPanel buildGuestTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(MainFrame.CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 8));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setBackground(MainFrame.CONTENT_BG);

        JLabel title = new JLabel("Daftar Tamu");
        title.setFont(MainFrame.FONT_TITLE);
        toolbar.add(title, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setBackground(MainFrame.CONTENT_BG);

        searchField = new JTextField(16);
        searchField.setFont(MainFrame.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        searchField.setToolTipText("Cari by nama atau ID");

        JButton searchBtn = MainFrame.buildPrimaryButton("Cari");
        searchBtn.addActionListener(e -> handleSearch());
        searchField.addActionListener(e -> handleSearch());

        JButton refreshBtn = MainFrame.buildSecondaryButton("↻");
        refreshBtn.addActionListener(e -> refresh());

        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);
        toolbar.add(searchPanel, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);

        // Tabel
        String[] columns = {"ID Tamu", "Nama", "Telepon",
                "Email", "Poin"};
        guestTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        guestTable = new JTable(guestTableModel);
        MainFrame.styleTable(guestTable);
        guestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {90, 150, 110, 160, 60};
        for (int i = 0; i < widths.length; i++) {
            guestTable.getColumnModel().getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        // Listener pilih baris
        guestTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = guestTable.getSelectedRow();
                if (row >= 0) {
                    String guestId = guestTableModel.getValueAt(row, 0).toString();
                    try {
                        Guest guest = hotelService.findGuestById(guestId);
                        updateDetailPanel(guest);
                    } catch (Exception ex) {
                        System.err.println("Guest tidak ditemukan: " + ex.getMessage());
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(guestTable);
        scroll.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER, 1));
        scroll.getViewport().setBackground(MainFrame.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ── Form Tambah Tamu ──────────────────────────────────────
    private JPanel buildAddGuestForm() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MainFrame.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, MainFrame.BORDER));

        // Header form
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MainFrame.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MainFrame.BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel title = new JLabel("Tambah Tamu Baru");
        title.setFont(MainFrame.FONT_TITLE);
        header.add(title, BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        // Form fields — 2 kolom
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(MainFrame.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(buildLabel("ID Tamu *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        guestIdField = buildFormField("Contoh: GST-001");
        form.add(guestIdField, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        form.add(buildLabel("Nama Lengkap *"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        nameField = buildFormField("Nama tamu");
        form.add(nameField, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(buildLabel("No. Telepon *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        phoneField = buildFormField("08xxxxxxxxxx");
        form.add(phoneField, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        form.add(buildLabel("Email"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        emailField = buildFormField("email@contoh.com");
        form.add(emailField, gbc);

        // Row 3
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        form.add(buildLabel("No. KTP *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        idCardField = buildFormField("Nomor KTP/Paspor");
        form.add(idCardField, gbc);

        // Tombol
        gbc.gridx = 2; gbc.gridy = 2;
        gbc.gridwidth = 1; gbc.weightx = 0;
        JButton addBtn = MainFrame.buildPrimaryButton("Tambah Tamu");
        form.add(addBtn, gbc);
        addBtn.addActionListener(e -> handleAddGuest());

        gbc.gridx = 3; gbc.weightx = 0;
        JButton resetBtn = MainFrame.buildSecondaryButton("Reset");
        form.add(resetBtn, gbc);
        resetBtn.addActionListener(e -> resetForm());

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    // ── Panel Detail (kanan) ──────────────────────────────────
    private JPanel buildDetailPanel() {
        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBackground(MainFrame.WHITE);
        detailPanel.setBorder(BorderFactory.createMatteBorder(
                0, 1, 0, 0, MainFrame.BORDER));
        showEmptyDetail();
        return detailPanel;
    }

    private void showEmptyDetail() {
        detailPanel.removeAll();

        JLabel hint = new JLabel("<html><div style='text-align:center'>" +
                "<b>Pilih Tamu</b><br><br>" +
                "Klik nama tamu di tabel<br>" +
                "untuk melihat profil,<br>" +
                "preferensi, dan riwayat" +
                "</div></html>");
        hint.setFont(MainFrame.FONT_BODY);
        hint.setForeground(new Color(180, 180, 180));
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        detailPanel.add(hint, BorderLayout.CENTER);

        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void updateDetailPanel(Guest guest) {
        detailPanel.removeAll();
        detailPanel.setLayout(new BorderLayout(0, 0));

        // Header nama
        JPanel nameHeader = new JPanel(new BorderLayout());
        nameHeader.setBackground(MainFrame.PRIMARY);
        nameHeader.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel nameLbl = new JLabel(guest.getName());
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLbl.setForeground(MainFrame.WHITE);

        JLabel idLbl = new JLabel(guest.getId());
        idLbl.setFont(MainFrame.FONT_SMALL);
        idLbl.setForeground(new Color(180, 230, 210));

        JPanel nameText = new JPanel(new BorderLayout(0, 4));
        nameText.setBackground(MainFrame.PRIMARY);
        nameText.add(nameLbl, BorderLayout.NORTH);
        nameText.add(idLbl, BorderLayout.SOUTH);
        nameHeader.add(nameText, BorderLayout.CENTER);

        // Loyalty points badge
        JLabel pointsBadge = new JLabel(guest.getLoyaltyPoints() + " pts");
        pointsBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pointsBadge.setForeground(MainFrame.PRIMARY);
        pointsBadge.setBackground(MainFrame.WHITE);
        pointsBadge.setOpaque(true);
        pointsBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.WHITE, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        nameHeader.add(pointsBadge, BorderLayout.EAST);
        detailPanel.add(nameHeader, BorderLayout.NORTH);

        // Tab pane: Info | Preferensi | Riwayat
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(MainFrame.FONT_BODY);
        tabs.setBackground(MainFrame.WHITE);

        tabs.addTab("Info", buildInfoTab(guest));
        tabs.addTab("Preferensi", buildPreferenceTab(guest));
        tabs.addTab("Riwayat", buildHistoryTab(guest));

        detailPanel.add(tabs, BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private JPanel buildInfoTab(Guest guest) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 12));
        panel.setBackground(MainFrame.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        addInfoRow(panel, "ID Tamu",      guest.getId());
        addInfoRow(panel, "Nama",         guest.getName());
        addInfoRow(panel, "No. Telepon",  guest.getPhone());
        addInfoRow(panel, "Email",        guest.getEmail());
        addInfoRow(panel, "No. KTP",      guest.getIdCard());
        addInfoRow(panel, "Loyalty Pts",  guest.getLoyaltyPoints() + " poin");
        addInfoRow(panel, "Total Reservasi",
                String.valueOf(guest.getReservationHistory().size()));

        return panel;
    }

    private JPanel buildPreferenceTab(Guest guest) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(MainFrame.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        if (guest.getPreferences().isEmpty()) {
            JLabel noPrefs = new JLabel("Tidak ada preferensi tersimpan.");
            noPrefs.setFont(MainFrame.FONT_BODY);
            noPrefs.setForeground(new Color(150, 150, 150));
            noPrefs.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(noPrefs, BorderLayout.CENTER);
        } else {
            JPanel prefList = new JPanel(new GridLayout(0, 2, 8, 10));
            prefList.setBackground(MainFrame.WHITE);
            guest.getPreferences().forEach((k, v) ->
                addInfoRow(prefList, k, v));
            panel.add(prefList, BorderLayout.NORTH);
        }

        return panel;
    }

    private JPanel buildHistoryTab(Guest guest) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(MainFrame.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] columns = {"ID Reservasi", "Kamar", "Check-in",
                "Check-out", "Status"};
        historyTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // Load riwayat reservasi tamu ini
        List<Reservation> guestReservations =
                hotelService.getReservationsByGuest(guest.getId());
        for (Reservation r : guestReservations) {
            historyTableModel.addRow(new Object[]{
                r.getReservationId(),
                r.getRoom().getRoomNumber(),
                r.getCheckInDate().format(DATE_FMT),
                r.getCheckOutDate().format(DATE_FMT),
                r.getStatus().getDisplayName()
            });
        }

        JTable historyTable = new JTable(historyTableModel);
        MainFrame.styleTable(historyTable);
        historyTable.getColumnModel().getColumn(4).setCellRenderer(
                new DashboardPanel.StatusCellRenderer());

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void addInfoRow(JPanel panel, String label, String value) {
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
    private void handleAddGuest() {
        String id     = guestIdField.getText().trim();
        String name   = nameField.getText().trim();
        String phone  = phoneField.getText().trim();
        String email  = emailField.getText().trim();
        String idCard = idCardField.getText().trim();

        if (id.isEmpty() || name.isEmpty() ||
                phone.isEmpty() || idCard.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "ID, Nama, Telepon, dan No. KTP wajib diisi!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Cek duplikat
        boolean exists = hotelService.getGuests().stream()
                .anyMatch(g -> g.getId().equals(id));
        if (exists) {
            JOptionPane.showMessageDialog(this,
                    "ID Tamu " + id + " sudah ada!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Guest guest = new Guest(id, name, phone,
                email.isEmpty() ? "-" : email, idCard);
        hotelService.addGuest(guest);

        JOptionPane.showMessageDialog(this,
                "Tamu " + name + " berhasil ditambahkan!",
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        resetForm();
        refresh();
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { refresh(); return; }

        List<Guest> results = hotelService.findGuestsByName(keyword);
        populateTable(results);
    }

    // ══════════════════════════════════════════════════════════
    //  REFRESH
    // ══════════════════════════════════════════════════════════
    @Override
    public void refresh() {
        hotelService.refreshData();
        populateTable(hotelService.getGuests());
    }

    private void populateTable(List<Guest> guests) {
        guestTableModel.setRowCount(0);
        for (Guest g : guests) {
            guestTableModel.addRow(new Object[]{
                g.getId(),
                g.getName(),
                g.getPhone(),
                g.getEmail(),
                g.getLoyaltyPoints()
            });
        }
    }

    // ══════════════════════════════════════════════════════════
    //  HELPER
    // ══════════════════════════════════════════════════════════
    private void resetForm() {
        guestIdField.setText("");
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        idCardField.setText("");
    }

    private JLabel buildLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(MainFrame.FONT_SMALL);
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private JTextField buildFormField(String tooltip) {
        JTextField field = new JTextField();
        field.setFont(MainFrame.FONT_BODY);
        field.setPreferredSize(new Dimension(0, 32));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        field.setToolTipText(tooltip);
        return field;
    }
}