/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.ui;

import com.hotelpms.enums.RoomStatus;
import com.hotelpms.model.DeluxeRoom;
import com.hotelpms.model.Room;
import com.hotelpms.model.StandardRoom;
import com.hotelpms.model.VIPRoom;
import com.hotelpms.service.HotelService;
 
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 *
 * @author rendysaptra
 */
public class RoomPanel extends JPanel implements Refreshable{

    // ─── Service ──────────────────────────────────────────────
    private final HotelService hotelService;

    // ─── Komponen ─────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable roomTable;
    private JComboBox<String> filterTypeCombo;
    private JComboBox<String> filterStatusCombo;

    // ─── Form Tambah Kamar ────────────────────────────────────
    private JTextField roomIdField;
    private JTextField roomNumberField;
    private JTextField floorField;
    private JComboBox<String> roomTypeCombo;

    // ─── Constructor ─────────────────────────────────────────
    public RoomPanel(HotelService hotelService) {
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

        add(MainFrame.buildPageHeader("Kamar",
                "Manajemen data dan status kamar hotel"), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildTablePanel(), buildFormPanel());
        split.setDividerLocation(720);
        split.setDividerSize(4);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);
    }

    // ── Table Panel (kiri) ────────────────────────────────────
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(MainFrame.CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 8));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(MainFrame.CONTENT_BG);

        JLabel title = new JLabel("Daftar Kamar");
        title.setFont(MainFrame.FONT_TITLE);
        title.setForeground(MainFrame.TEXT_DARK);
        toolbar.add(title, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setBackground(MainFrame.CONTENT_BG);

        filterPanel.add(new JLabel("Tipe:"));
        filterTypeCombo = new JComboBox<>(new String[]{
            "Semua", "STANDARD", "DELUXE", "VIP"});
        filterTypeCombo.setFont(MainFrame.FONT_BODY);
        filterTypeCombo.addActionListener(e -> applyFilter());
        filterPanel.add(filterTypeCombo);

        filterPanel.add(new JLabel("Status:"));
        filterStatusCombo = new JComboBox<>(new String[]{
            "Semua", "Bersih", "Perlu Dibersihkan",
            "Terisi", "Dipesan", "Tidak Tersedia"});
        filterStatusCombo.setFont(MainFrame.FONT_BODY);
        filterStatusCombo.addActionListener(e -> applyFilter());
        filterPanel.add(filterStatusCombo);

        JButton refreshBtn = MainFrame.buildSecondaryButton("↻ Refresh");
        refreshBtn.addActionListener(e -> refresh());
        filterPanel.add(refreshBtn);

        toolbar.add(filterPanel, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);

        // Tabel
        String[] columns = {"ID Kamar", "No. Kamar", "Tipe",
                "Lantai", "Harga/Malam", "Fasilitas", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        roomTable = new JTable(tableModel);
        MainFrame.styleTable(roomTable);
        roomTable.getColumnModel().getColumn(6).setCellRenderer(
                new RoomStatusCellRenderer());

        // Lebar kolom
        int[] widths = {90, 80, 80, 60, 120, 200, 110};
        for (int i = 0; i < widths.length; i++) {
            roomTable.getColumnModel().getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(roomTable);
        scroll.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER, 1));
        scroll.getViewport().setBackground(MainFrame.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        // Summary bar
        panel.add(buildSummaryBar(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildSummaryBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        bar.setBackground(new Color(240, 240, 240));
        bar.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, MainFrame.BORDER));

        List<Room> rooms = hotelService.getRooms();
        long clean    = rooms.stream().filter(r -> r.getStatus() == RoomStatus.CLEAN).count();
        long dirty    = rooms.stream().filter(r -> r.getStatus() == RoomStatus.DIRTY).count();
        long occupied = rooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count();
        long reserved = rooms.stream().filter(r -> r.getStatus() == RoomStatus.RESERVED).count();
        long ooo      = rooms.stream().filter(r -> r.getStatus() == RoomStatus.OUT_OF_ORDER).count();

        bar.add(buildBadge("Bersih: " + clean,       new Color(209, 250, 229), new Color(6, 95, 70)));
        bar.add(buildBadge("Kotor: " + dirty,         new Color(254, 243, 199), new Color(146, 64, 14)));
        bar.add(buildBadge("Terisi: " + occupied,     new Color(219, 234, 254), new Color(30, 64, 175)));
        bar.add(buildBadge("Dipesan: " + reserved,    new Color(237, 233, 254), new Color(91, 33, 182)));
        bar.add(buildBadge("Out of Order: " + ooo,    new Color(254, 226, 226), new Color(153, 27, 27)));

        return bar;
    }

    private JLabel buildBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text);
        badge.setFont(MainFrame.FONT_SMALL);
        badge.setForeground(fg);
        badge.setBackground(bg);
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        return badge;
    }

    // ── Form Panel (kanan) ────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MainFrame.WHITE);
        panel.setPreferredSize(new Dimension(260, 0));
        panel.setBorder(BorderFactory.createMatteBorder(
                0, 1, 0, 0, MainFrame.BORDER));

        // Form header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MainFrame.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, MainFrame.BORDER),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        JLabel title = new JLabel("Tambah Kamar Baru");
        title.setFont(MainFrame.FONT_TITLE);
        header.add(title, BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        // Form fields
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(MainFrame.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        form.add(buildFieldLabel("ID Kamar *"));
        roomIdField = buildTextField("Contoh: RM-101");
        form.add(roomIdField);
        form.add(Box.createVerticalStrut(12));

        form.add(buildFieldLabel("Nomor Kamar *"));
        roomNumberField = buildTextField("Contoh: 101");
        form.add(roomNumberField);
        form.add(Box.createVerticalStrut(12));

        form.add(buildFieldLabel("Lantai *"));
        floorField = buildTextField("Contoh: 1");
        form.add(floorField);
        form.add(Box.createVerticalStrut(12));

        form.add(buildFieldLabel("Tipe Kamar *"));
        roomTypeCombo = new JComboBox<>(
                new String[]{"STANDARD", "DELUXE", "VIP"});
        roomTypeCombo.setFont(MainFrame.FONT_BODY);
        roomTypeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        form.add(roomTypeCombo);
        form.add(Box.createVerticalStrut(8));

        // Info harga
        JLabel priceInfo = new JLabel(
                "<html><i>Standard: Rp 300.000<br>" +
                "Deluxe: Rp 600.000<br>" +
                "VIP: Rp 1.200.000</i></html>");
        priceInfo.setFont(MainFrame.FONT_SMALL);
        priceInfo.setForeground(new Color(130, 130, 130));
        priceInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(priceInfo);
        form.add(Box.createVerticalStrut(20));

        JButton addBtn = MainFrame.buildPrimaryButton("Tambah Kamar");
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        addBtn.addActionListener(e -> handleAddRoom());
        form.add(addBtn);
        form.add(Box.createVerticalStrut(8));

        JButton resetBtn = MainFrame.buildSecondaryButton("Reset Form");
        resetBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        resetBtn.addActionListener(e -> resetForm());
        form.add(resetBtn);

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    // ══════════════════════════════════════════════════════════
    //  ACTION HANDLERS
    // ══════════════════════════════════════════════════════════
    private void handleAddRoom() {
        String roomId     = roomIdField.getText().trim();
        String roomNumber = roomNumberField.getText().trim();
        String floorStr   = floorField.getText().trim();
        String roomType   = (String) roomTypeCombo.getSelectedItem();

        if (roomId.isEmpty() || roomNumber.isEmpty() || floorStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field wajib diisi!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int floor = Integer.parseInt(floorStr);

            // Cek duplikat ID
            boolean exists = hotelService.getRooms().stream()
                    .anyMatch(r -> r.getRoomId().equals(roomId));
            if (exists) {
                JOptionPane.showMessageDialog(this,
                        "ID Kamar " + roomId + " sudah ada!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Room room = switch (roomType) {
                case "DELUXE" -> new DeluxeRoom(roomId, roomNumber, floor);
                case "VIP"    -> new VIPRoom(roomId, roomNumber, floor);
                default       -> new StandardRoom(roomId, roomNumber, floor);
            };

            hotelService.addRoom(room);
            JOptionPane.showMessageDialog(this,
                    "Kamar " + roomNumber + " (" + roomType + ") berhasil ditambahkan!",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
            refresh();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Lantai harus berupa angka!",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  REFRESH & FILTER
    // ══════════════════════════════════════════════════════════
    @Override
    public void refresh() {
        hotelService.refreshData();
        populateTable(hotelService.getRooms());
    }

    private void applyFilter() {
        String typeFilter   = (String) filterTypeCombo.getSelectedItem();
        String statusFilter = (String) filterStatusCombo.getSelectedItem();

        List<Room> filtered = hotelService.getRooms().stream()
                .filter(r -> "Semua".equals(typeFilter) ||
                        r.getRoomType().equals(typeFilter))
                .filter(r -> "Semua".equals(statusFilter) ||
                        r.getStatus().getDisplayName().equals(statusFilter))
                .toList();

        populateTable(filtered);
    }

    private void populateTable(List<Room> rooms) {
        tableModel.setRowCount(0);
        for (Room room : rooms) {
            tableModel.addRow(new Object[]{
                room.getRoomId(),
                room.getRoomNumber(),
                room.getRoomType(),
                "Lantai " + room.getFloor(),
                String.format("Rp %,.0f", room.getPricePerNight()),
                room.getFacilities(),
                room.getStatus().getDisplayName()
            });
        }
    }

    // ══════════════════════════════════════════════════════════
    //  HELPER
    // ══════════════════════════════════════════════════════════
    private void resetForm() {
        roomIdField.setText("");
        roomNumberField.setText("");
        floorField.setText("");
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

    // ══════════════════════════════════════════════════════════
    //  INNER CLASS — Room Status Cell Renderer
    // ══════════════════════════════════════════════════════════
    static class RoomStatusCellRenderer extends DefaultTableCellRenderer {
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
                    case "Bersih"            -> { setBackground(new Color(209, 250, 229));
                                                  setForeground(new Color(6, 95, 70)); }
                    case "Perlu Dibersihkan" -> { setBackground(new Color(254, 243, 199));
                                                  setForeground(new Color(146, 64, 14)); }
                    case "Terisi"            -> { setBackground(new Color(219, 234, 254));
                                                  setForeground(new Color(30, 64, 175)); }
                    case "Dipesan"           -> { setBackground(new Color(237, 233, 254));
                                                  setForeground(new Color(91, 33, 182)); }
                    case "Tidak Tersedia"    -> { setBackground(new Color(254, 226, 226));
                                                  setForeground(new Color(153, 27, 27)); }
                    default                  -> { setBackground(MainFrame.WHITE);
                                                  setForeground(MainFrame.TEXT_DARK); }
                }
            }
            return this;
        }
    }
}
