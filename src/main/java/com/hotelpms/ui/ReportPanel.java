/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.ui;

import com.hotelpms.service.HotelService;
import com.hotelpms.service.ReportService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Panel Laporan — analitik dan pelaporan operasional hotel.
 * Menampilkan occupancy rate, revenue, rata-rata stay,
 * dan breakdown per tipe kamar dengan fitur export CSV.
 *
 * @author Rendy & Panji
 * @version 1.0
 */
public class ReportPanel extends JPanel implements Refreshable {

    // ─── Service ──────────────────────────────────────────────
    private final HotelService hotelService;
    private ReportService reportService;

    // ─── Komponen Filter ──────────────────────────────────────
    private JTextField fromDateField;
    private JTextField toDateField;

    // ─── Komponen Occupancy ───────────────────────────────────
    private JLabel occupancyValue;
    private DefaultTableModel occupancyByTypeModel;

    // ─── Komponen Revenue ─────────────────────────────────────
    private JLabel revenueValue;
    private JLabel avgStayValue;
    private DefaultTableModel revenueByTypeModel;

    // ─── Format ───────────────────────────────────────────────
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ─── Constructor ─────────────────────────────────────────
    public ReportPanel(HotelService hotelService) {
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

        add(MainFrame.buildPageHeader("Laporan",
                "Analitik dan pelaporan operasional hotel"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(MainFrame.CONTENT_BG);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        content.add(buildFilterBar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildOccupancyPanel(), buildRevenuePanel());
        split.setDividerLocation(400);
        split.setDividerSize(4);
        split.setBorder(null);

        content.add(split, BorderLayout.CENTER);
        content.add(buildActionBar(), BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);
    }

    // ── Filter Bar ────────────────────────────────────────────
    private JPanel buildFilterBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        panel.setBackground(MainFrame.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        panel.add(new JLabel("Periode:"));

        fromDateField = new JTextField(10);
        fromDateField.setFont(MainFrame.FONT_BODY);
        fromDateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        fromDateField.setToolTipText("Format: dd-MM-yyyy");

        toDateField = new JTextField(10);
        toDateField.setFont(MainFrame.FONT_BODY);
        toDateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        toDateField.setToolTipText("Format: dd-MM-yyyy");

        panel.add(fromDateField);
        panel.add(new JLabel("s/d"));
        panel.add(toDateField);

        // Shortcut buttons
        JButton thisMonthBtn = MainFrame.buildSecondaryButton("Bulan Ini");
        thisMonthBtn.addActionListener(e -> {
            LocalDate now   = LocalDate.now();
            LocalDate start = now.withDayOfMonth(1);
            LocalDate end   = now.withDayOfMonth(now.lengthOfMonth());
            fromDateField.setText(start.format(DATE_FMT));
            toDateField.setText(end.format(DATE_FMT));
            generateReport();
        });

        JButton thisYearBtn = MainFrame.buildSecondaryButton("Tahun Ini");
        thisYearBtn.addActionListener(e -> {
            LocalDate now   = LocalDate.now();
            fromDateField.setText(
                    LocalDate.of(now.getYear(), 1, 1).format(DATE_FMT));
            toDateField.setText(
                    LocalDate.of(now.getYear(), 12, 31).format(DATE_FMT));
            generateReport();
        });

        JButton generateBtn = MainFrame.buildPrimaryButton("Generate Laporan");
        generateBtn.addActionListener(e -> generateReport());

        panel.add(thisMonthBtn);
        panel.add(thisYearBtn);
        panel.add(generateBtn);

        return panel;
    }

    // ── Occupancy Panel (kiri) ────────────────────────────────
    private JPanel buildOccupancyPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(MainFrame.CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        JLabel title = new JLabel("Occupancy");
        title.setFont(MainFrame.FONT_TITLE);
        title.setForeground(MainFrame.TEXT_DARK);
        panel.add(title, BorderLayout.NORTH);

        // Card occupancy rate
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(MainFrame.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        JLabel rateLbl = new JLabel("Occupancy Rate Saat Ini");
        rateLbl.setFont(MainFrame.FONT_SMALL);
        rateLbl.setForeground(new Color(130, 130, 130));

        occupancyValue = new JLabel("0%");
        occupancyValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        occupancyValue.setForeground(MainFrame.PRIMARY);

        card.add(rateLbl,        BorderLayout.NORTH);
        card.add(occupancyValue, BorderLayout.CENTER);
        panel.add(card, BorderLayout.NORTH);

        // Tabel per tipe kamar
        JLabel byTypeTitle = new JLabel("Occupancy per Tipe Kamar");
        byTypeTitle.setFont(MainFrame.FONT_SMALL);
        byTypeTitle.setForeground(new Color(100, 100, 100));
        panel.add(byTypeTitle, BorderLayout.CENTER);

        String[] columns = {"Tipe Kamar", "Occupancy Rate"};
        occupancyByTypeModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable occTable = new JTable(occupancyByTypeModel);
        MainFrame.styleTable(occTable);

        // Right-align kolom rate
        DefaultTableCellRenderer rightRenderer =
                new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        occTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);

        JScrollPane scroll = new JScrollPane(occTable);
        scroll.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER, 1));
        scroll.setPreferredSize(new Dimension(0, 160));
        panel.add(scroll, BorderLayout.SOUTH);

        return panel;
    }

    // ── Revenue Panel (kanan) ─────────────────────────────────
    private JPanel buildRevenuePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(MainFrame.CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        JLabel title = new JLabel("Revenue & Statistik");
        title.setFont(MainFrame.FONT_TITLE);
        title.setForeground(MainFrame.TEXT_DARK);
        panel.add(title, BorderLayout.NORTH);

        // Cards row
        JPanel cardsRow = new JPanel(new GridLayout(1, 2, 12, 0));
        cardsRow.setBackground(MainFrame.CONTENT_BG);

        // Revenue card
        JPanel revCard = new JPanel(new BorderLayout(0, 8));
        revCard.setBackground(MainFrame.WHITE);
        revCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        JLabel revLbl = new JLabel("Total Revenue Periode");
        revLbl.setFont(MainFrame.FONT_SMALL);
        revLbl.setForeground(new Color(130, 130, 130));

        revenueValue = new JLabel("Rp 0");
        revenueValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        revenueValue.setForeground(new Color(186, 117, 23));

        revCard.add(revLbl,     BorderLayout.NORTH);
        revCard.add(revenueValue, BorderLayout.CENTER);
        cardsRow.add(revCard);

        // Avg stay card
        JPanel avgCard = new JPanel(new BorderLayout(0, 8));
        avgCard.setBackground(MainFrame.WHITE);
        avgCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainFrame.BORDER, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        JLabel avgLbl = new JLabel("Rata-rata Lama Menginap");
        avgLbl.setFont(MainFrame.FONT_SMALL);
        avgLbl.setForeground(new Color(130, 130, 130));

        avgStayValue = new JLabel("0 malam");
        avgStayValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        avgStayValue.setForeground(new Color(30, 64, 175));

        avgCard.add(avgLbl,      BorderLayout.NORTH);
        avgCard.add(avgStayValue, BorderLayout.CENTER);
        cardsRow.add(avgCard);

        panel.add(cardsRow, BorderLayout.NORTH);

        // Tabel revenue per tipe
        JLabel byTypeTitle = new JLabel("Revenue per Tipe Kamar");
        byTypeTitle.setFont(MainFrame.FONT_SMALL);
        byTypeTitle.setForeground(new Color(100, 100, 100));
        panel.add(byTypeTitle, BorderLayout.CENTER);

        String[] columns = {"Tipe Kamar", "Revenue", "Avg Stay"};
        revenueByTypeModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable revTable = new JTable(revenueByTypeModel);
        MainFrame.styleTable(revTable);

        DefaultTableCellRenderer rightRenderer =
                new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        revTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        revTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);

        JScrollPane scroll = new JScrollPane(revTable);
        scroll.setBorder(BorderFactory.createLineBorder(MainFrame.BORDER, 1));
        scroll.setPreferredSize(new Dimension(0, 160));
        panel.add(scroll, BorderLayout.SOUTH);

        return panel;
    }

    // ── Action Bar ────────────────────────────────────────────
    private JPanel buildActionBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setBackground(MainFrame.CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JButton exportBtn = MainFrame.buildPrimaryButton(
                "Export Laporan ke CSV");
        exportBtn.addActionListener(e -> handleExportCSV());
        panel.add(exportBtn);

        JButton printBtn = MainFrame.buildSecondaryButton(
                "Cetak ke Console");
        printBtn.addActionListener(e -> handlePrintConsole());
        panel.add(printBtn);

        return panel;
    }

    // ══════════════════════════════════════════════════════════
    //  ACTION HANDLERS
    // ══════════════════════════════════════════════════════════
    private void generateReport() {
        try {
            LocalDate from = LocalDate.parse(
                    fromDateField.getText().trim(), DATE_FMT);
            LocalDate to   = LocalDate.parse(
                    toDateField.getText().trim(), DATE_FMT);

            if (to.isBefore(from)) {
                JOptionPane.showMessageDialog(this,
                        "Tanggal akhir tidak boleh sebelum tanggal awal!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            updateReportData(from, to);

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Format tanggal tidak valid!\nGunakan format: dd-MM-yyyy",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateReportData(LocalDate from, LocalDate to) {
        // Occupancy rate saat ini
        double occupancy = reportService.getOccupancyRate();
        occupancyValue.setText(String.format("%.1f%%", occupancy));

        // Occupancy per tipe
        occupancyByTypeModel.setRowCount(0);
        Map<String, Double> occByType =
                reportService.getOccupancyRateByType();
        for (Map.Entry<String, Double> entry : occByType.entrySet()) {
            occupancyByTypeModel.addRow(new Object[]{
                entry.getKey(),
                String.format("%.1f%%", entry.getValue())
            });
        }

        // Revenue
        double revenue = reportService.getRevenue(from, to);
        revenueValue.setText(String.format("Rp %,.0f", revenue));

        // Avg stay
        double avgStay = reportService.getAverageStayDuration();
        avgStayValue.setText(String.format("%.1f malam", avgStay));

        // Revenue + avg stay per tipe
        revenueByTypeModel.setRowCount(0);
        Map<String, Double> revByType =
                reportService.getRevenueByRoomType(from, to);
        Map<String, Double> avgByType =
                reportService.getAvgStayByRoomType();

        for (String type : revByType.keySet()) {
            double rev = revByType.getOrDefault(type, 0.0);
            double avg = avgByType.getOrDefault(type, 0.0);
            revenueByTypeModel.addRow(new Object[]{
                type,
                String.format("Rp %,.0f", rev),
                String.format("%.1f malam", avg)
            });
        }
    }

    private void handleExportCSV() {
        if (fromDateField.getText().trim().isEmpty() ||
                toDateField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Tentukan periode laporan terlebih dahulu!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            LocalDate from = LocalDate.parse(
                    fromDateField.getText().trim(), DATE_FMT);
            LocalDate to   = LocalDate.parse(
                    toDateField.getText().trim(), DATE_FMT);

            reportService.exportRevenueToCSV(from, to);

            JOptionPane.showMessageDialog(this,
                    "Laporan berhasil diekspor ke:\ndata/reports/",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Format tanggal tidak valid!\nGunakan format: dd-MM-yyyy",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePrintConsole() {
        if (fromDateField.getText().trim().isEmpty() ||
                toDateField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Tentukan periode laporan terlebih dahulu!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            LocalDate from = LocalDate.parse(
                    fromDateField.getText().trim(), DATE_FMT);
            LocalDate to   = LocalDate.parse(
                    toDateField.getText().trim(), DATE_FMT);

            System.out.println(reportService.generateOccupancyReport());
            System.out.println(reportService.generateRevenueReport(from, to));

            JOptionPane.showMessageDialog(this,
                    "Laporan sudah dicetak ke console (Output window).",
                    "Info", JOptionPane.INFORMATION_MESSAGE);

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Format tanggal tidak valid!\nGunakan format: dd-MM-yyyy",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  REFRESH
    // ══════════════════════════════════════════════════════════
    @Override
    public void refresh() {
        hotelService.refreshData();
        reportService = new ReportService(
                hotelService.getRooms(),
                hotelService.getReservations());

        // Set default periode bulan ini
        LocalDate now   = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end   = now.withDayOfMonth(now.lengthOfMonth());

        if (fromDateField.getText().isEmpty()) {
            fromDateField.setText(start.format(DATE_FMT));
        }
        if (toDateField.getText().isEmpty()) {
            toDateField.setText(end.format(DATE_FMT));
        }

        // Update occupancy saat ini
        double occupancy = reportService.getOccupancyRate();
        occupancyValue.setText(String.format("%.1f%%", occupancy));

        // Update occupancy per tipe
        occupancyByTypeModel.setRowCount(0);
        Map<String, Double> occByType =
                reportService.getOccupancyRateByType();
        for (Map.Entry<String, Double> entry : occByType.entrySet()) {
            occupancyByTypeModel.addRow(new Object[]{
                entry.getKey(),
                String.format("%.1f%%", entry.getValue())
            });
        }

        // Update revenue bulan ini
        double revenue = reportService.getRevenue(start, end);
        revenueValue.setText(String.format("Rp %,.0f", revenue));

        double avgStay = reportService.getAverageStayDuration();
        avgStayValue.setText(String.format("%.1f malam", avgStay));
    }
}
