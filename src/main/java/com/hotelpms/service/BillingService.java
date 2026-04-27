/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.service;

import com.hotelpms.exception.PaymentFailedException;
import com.hotelpms.model.FolioItem;
import com.hotelpms.model.Reservation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service untuk menangani semua operasi billing dan pembayaran.
 * Mengelola folio itemized, kalkulasi pajak, dan cetak tagihan.
 *
 * @author Rendy & Panji
 * @version 1.0
 */
public class BillingService {

    // ─── Konstanta ───────────────────────────────────────────
    private static final double TAX_RATE       = 0.11; // 11%
    private static final String FOLIO_DIR      = "data/folio/";
    private static final String SEPARATOR_LINE =
            "─────────────────────────────────────────────\n";

    // ══════════════════════════════════════════════════════════
    //  CHARGE MANAGEMENT
    // ══════════════════════════════════════════════════════════

    /**
     * Tambahkan item tagihan ke folio reservasi.
     *
     * @param reservation reservasi yang akan ditambah tagihannya
     * @param description deskripsi item tagihan
     * @param chargeType  tipe tagihan
     * @param amount      nominal tagihan
     */
    public void addCharge(Reservation reservation, String description,
                          FolioItem.ChargeType chargeType, double amount) {
        if (reservation == null) return;
        if (amount <= 0) return;

        String itemId = String.format("FI-%s-%03d",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                reservation.getFolioItems().size() + 1);

        FolioItem item = new FolioItem(itemId, description, chargeType,
                amount, LocalDate.now());
        reservation.addFolioItem(item);
    }

    /**
     * Tambah biaya kamar otomatis berdasarkan jumlah malam.
     * Dipanggil saat tamu check-in.
     *
     * @param reservation reservasi yang akan ditagih biaya kamar
     */
    public void addRoomCharge(Reservation reservation) {
        if (reservation == null) return;

        long nights     = reservation.getTotalNights();
        double price    = reservation.getRoom().getPricePerNight();
        double total    = price * nights;
        String desc     = String.format("Biaya Kamar %s (%d malam x %s)",
                reservation.getRoom().getRoomType(),
                nights,
                reservation.getRoom().getFormattedPrice());

        addCharge(reservation, desc, FolioItem.ChargeType.ROOM, total);
    }

    // ══════════════════════════════════════════════════════════
    //  KALKULASI
    // ══════════════════════════════════════════════════════════

    /**
     * Hitung subtotal semua item tagihan sebelum pajak.
     *
     * @param reservation reservasi yang akan dihitung
     * @return subtotal sebelum pajak
     */
    public double calculateSubtotal(Reservation reservation) {
        return reservation.getSubtotal();
    }

    /**
     * Hitung nominal pajak 11% dari subtotal.
     *
     * @param subtotal nominal sebelum pajak
     * @return nominal pajak
     */
    public double calculateTax(double subtotal) {
        return subtotal * TAX_RATE;
    }

    /**
     * Hitung grand total termasuk pajak 11%.
     *
     * @param reservation reservasi yang akan dihitung
     * @return grand total termasuk pajak
     * @throws PaymentFailedException jika terjadi error kalkulasi
     */
    public double calculateTotal(Reservation reservation)
            throws PaymentFailedException {
        try {
            double subtotal = calculateSubtotal(reservation);
            double tax      = calculateTax(subtotal);
            return subtotal + tax;
        } catch (Exception e) {
            throw new PaymentFailedException(
                    reservation.getReservationId(), 0,
                    "Gagal menghitung total tagihan: " + e.getMessage());
        }
    }

    /**
     * Format nominal ke format Rupiah.
     *
     * @param amount nominal yang akan diformat
     * @return String dalam format Rp xxx.xxx
     */
    public String formatRupiah(double amount) {
        return String.format("Rp %,.0f", amount);
    }

    // ══════════════════════════════════════════════════════════
    //  PRINT FOLIO
    // ══════════════════════════════════════════════════════════

    /**
     * Cetak folio lengkap ke layar (console).
     *
     * @param reservation reservasi yang akan dicetak folionya
     * @throws PaymentFailedException jika kalkulasi gagal
     */
    public void printFolioToConsole(Reservation reservation)
            throws PaymentFailedException {
        System.out.println(buildFolioString(reservation));
    }

    /**
     * Simpan folio ke file .txt di folder data/folio/.
     * Nama file: folio-[reservationId].txt
     *
     * @param reservation reservasi yang akan disimpan folionya
     * @throws PaymentFailedException jika kalkulasi atau penulisan file gagal
     */
    public void printFolioToFile(Reservation reservation)
            throws PaymentFailedException {

        // Buat folder data/folio/ kalau belum ada
        new java.io.File(FOLIO_DIR).mkdirs();

        String fileName = FOLIO_DIR + "folio-" +
                reservation.getReservationId() + ".txt";
        String content  = buildFolioString(reservation);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(content);
            System.out.println("Folio disimpan ke: " + fileName);
        } catch (IOException e) {
            throw new PaymentFailedException(
                    reservation.getReservationId(), 0,
                    "Gagal menyimpan folio ke file: " + e.getMessage());
        }
    }

    /**
     * Build string folio lengkap dengan itemized charges, subtotal, pajak, total.
     *
     * @param reservation reservasi yang akan di-build folionya
     * @return String folio lengkap
     * @throws PaymentFailedException jika kalkulasi gagal
     */
    private String buildFolioString(Reservation reservation)
            throws PaymentFailedException {

        double subtotal = calculateSubtotal(reservation);
        double tax      = calculateTax(subtotal);
        double total    = subtotal + tax;

        String date = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy",
                        new java.util.Locale("id", "ID")));

        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════╗\n");
        sb.append("║          HOTEL PMS — FOLIO TAMU            ║\n");
        sb.append("╚════════════════════════════════════════════╝\n");
        sb.append(String.format("Tanggal Cetak : %s\n", date));
        sb.append(SEPARATOR_LINE);
        sb.append(String.format("No. Reservasi : %s\n", reservation.getReservationId()));
        sb.append(String.format("Tamu          : %s\n", reservation.getGuest().getName()));
        sb.append(String.format("No. KTP       : %s\n", reservation.getGuest().getIdCard()));
        sb.append(String.format("Kamar         : %s (%s)\n",
                reservation.getRoom().getRoomNumber(),
                reservation.getRoom().getRoomType()));
        sb.append(String.format("Check-in      : %s\n",
                reservation.getCheckInDate()
                        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
        sb.append(String.format("Check-out     : %s\n",
                reservation.getCheckOutDate()
                        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
        sb.append(String.format("Lama Menginap : %d malam\n",
                reservation.getTotalNights()));
        sb.append(SEPARATOR_LINE);
        sb.append(String.format("%-4s %-30s %15s\n", "No.", "Deskripsi", "Jumlah"));
        sb.append(SEPARATOR_LINE);

        List<FolioItem> items = reservation.getFolioItems();
        for (int i = 0; i < items.size(); i++) {
            FolioItem item = items.get(i);
            sb.append(String.format("%-4d %-30s %15s\n",
                    i + 1,
                    item.getDescription(),
                    item.getFormattedAmount()));
        }

        sb.append(SEPARATOR_LINE);
        sb.append(String.format("%-34s %15s\n", "Subtotal", formatRupiah(subtotal)));
        sb.append(String.format("%-34s %15s\n",
                String.format("Pajak (%.0f%%)", TAX_RATE * 100),
                formatRupiah(tax)));
        sb.append(SEPARATOR_LINE);
        sb.append(String.format("%-34s %15s\n", "TOTAL", formatRupiah(total)));
        sb.append(SEPARATOR_LINE);
        sb.append("\nTerima kasih telah menginap di Hotel PMS!\n");

        return sb.toString();
    }

    // ══════════════════════════════════════════════════════════
    //  GETTER
    // ══════════════════════════════════════════════════════════

    public double getTaxRate() {
        return TAX_RATE;
    }
}