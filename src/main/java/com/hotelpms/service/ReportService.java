/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.service;

import com.hotelpms.enums.RoomStatus;
import com.hotelpms.model.Reservation;
import com.hotelpms.model.Room;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service untuk menghasilkan laporan dan analitik hotel.
 * Menghitung occupancy rate, revenue, average stay,
 * dan breakdown per tipe kamar.
 *
 * @author Rendy & Panji
 * @version 1.0
 */
public class ReportService {

    // ─── Konstanta ───────────────────────────────────────────
    private static final String REPORT_DIR    = "data/reports/";
    private static final String SEPARATOR     =
            "─────────────────────────────────────────────\n";

    // ─── Atribut ─────────────────────────────────────────────
    private List<Room> rooms;
    private List<Reservation> reservations;

    // ─── Constructor ─────────────────────────────────────────
    public ReportService(List<Room> rooms, List<Reservation> reservations) {
        this.rooms        = rooms;
        this.reservations = reservations;
    }

    // ══════════════════════════════════════════════════════════
    //  OCCUPANCY
    // ══════════════════════════════════════════════════════════

    /**
     * Hitung occupancy rate saat ini.
     * Occupancy = jumlah kamar OCCUPIED / total kamar * 100
     *
     * @return occupancy rate dalam persen (0.0 - 100.0)
     */
    public double getOccupancyRate() {
        if (rooms.isEmpty()) return 0.0;
        long occupied = rooms.stream()
                .filter(r -> r.getStatus() == RoomStatus.OCCUPIED)
                .count();
        return (double) occupied / rooms.size() * 100;
    }

    /**
     * Hitung occupancy rate per tipe kamar.
     *
     * @return Map dengan key tipe kamar dan value occupancy rate
     */
    public Map<String, Double> getOccupancyRateByType() {
        Map<String, Integer> totalByType    = new HashMap<>();
        Map<String, Integer> occupiedByType = new HashMap<>();

        for (Room room : rooms) {
            String type = room.getRoomType();
            totalByType.put(type, totalByType.getOrDefault(type, 0) + 1);
            if (room.getStatus() == RoomStatus.OCCUPIED) {
                occupiedByType.put(type, occupiedByType.getOrDefault(type, 0) + 1);
            }
        }

        Map<String, Double> result = new HashMap<>();
        for (String type : totalByType.keySet()) {
            int total    = totalByType.get(type);
            int occupied = occupiedByType.getOrDefault(type, 0);
            result.put(type, (double) occupied / total * 100);
        }
        return result;
    }

    // ══════════════════════════════════════════════════════════
    //  REVENUE
    // ══════════════════════════════════════════════════════════

    /**
     * Hitung total revenue dari semua reservasi CHECKED_OUT
     * dalam rentang tanggal tertentu.
     *
     * @param from tanggal awal periode
     * @param to   tanggal akhir periode
     * @return total revenue periode tersebut
     */
    public double getRevenue(LocalDate from, LocalDate to) {
        return reservations.stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.CHECKED_OUT)
                .filter(r -> !r.getCheckOutDate().isBefore(from)
                          && !r.getCheckOutDate().isAfter(to))
                .mapToDouble(Reservation::getSubtotal)
                .sum();
    }

    /**
     * Hitung revenue per tipe kamar dalam rentang tanggal.
     *
     * @param from tanggal awal periode
     * @param to   tanggal akhir periode
     * @return Map dengan key tipe kamar dan value total revenue
     */
    public Map<String, Double> getRevenueByRoomType(LocalDate from, LocalDate to) {
        Map<String, Double> result = new HashMap<>();
        for (Reservation r : reservations) {
            if (r.getStatus() != Reservation.ReservationStatus.CHECKED_OUT) continue;
            if (r.getCheckOutDate().isBefore(from)) continue;
            if (r.getCheckOutDate().isAfter(to)) continue;

            String type   = r.getRoom().getRoomType();
            double amount = r.getSubtotal();
            result.put(type, result.getOrDefault(type, 0.0) + amount);
        }
        return result;
    }

    /**
     * Hitung revenue bulan ini.
     *
     * @return total revenue bulan berjalan
     */
    public double getRevenueThisMonth() {
        LocalDate now   = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end   = now.withDayOfMonth(now.lengthOfMonth());
        return getRevenue(start, end);
    }

    // ══════════════════════════════════════════════════════════
    //  AVERAGE STAY
    // ══════════════════════════════════════════════════════════

    /**
     * Hitung rata-rata lama menginap dari semua reservasi selesai.
     *
     * @return rata-rata jumlah malam menginap
     */
    public double getAverageStayDuration() {
        List<Reservation> completed = new ArrayList<>();
        for (Reservation r : reservations) {
            if (r.getStatus() == Reservation.ReservationStatus.CHECKED_OUT) {
                completed.add(r);
            }
        }
        if (completed.isEmpty()) return 0.0;
        double totalNights = completed.stream()
                .mapToLong(Reservation::getTotalNights)
                .sum();
        return totalNights / completed.size();
    }

    /**
     * Hitung rata-rata lama menginap per tipe kamar.
     *
     * @return Map dengan key tipe kamar dan value rata-rata malam
     */
    public Map<String, Double> getAvgStayByRoomType() {
        Map<String, List<Long>> nightsByType = new HashMap<>();

        for (Reservation r : reservations) {
            if (r.getStatus() != Reservation.ReservationStatus.CHECKED_OUT) continue;
            String type = r.getRoom().getRoomType();
            nightsByType.computeIfAbsent(type, k -> new ArrayList<>())
                        .add(r.getTotalNights());
        }

        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, List<Long>> entry : nightsByType.entrySet()) {
            double avg = entry.getValue().stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            result.put(entry.getKey(), avg);
        }
        return result;
    }

    // ══════════════════════════════════════════════════════════
    //  GENERATE REPORT
    // ══════════════════════════════════════════════════════════

    /**
     * Generate laporan occupancy lengkap.
     *
     * @return String laporan occupancy
     */
    public String generateOccupancyReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════╗\n");
        sb.append("║        LAPORAN OCCUPANCY KAMAR             ║\n");
        sb.append("╚════════════════════════════════════════════╝\n");
        sb.append(String.format("Tanggal : %s\n",
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
        sb.append(SEPARATOR);
        sb.append(String.format("Total Kamar        : %d kamar\n", rooms.size()));
        sb.append(String.format("Occupancy Rate     : %.1f%%\n", getOccupancyRate()));
        sb.append(SEPARATOR);
        sb.append("Occupancy per Tipe Kamar:\n");

        Map<String, Double> byType = getOccupancyRateByType();
        for (Map.Entry<String, Double> entry : byType.entrySet()) {
            sb.append(String.format("  %-12s : %.1f%%\n",
                    entry.getKey(), entry.getValue()));
        }

        sb.append(SEPARATOR);
        sb.append("Detail Status Kamar:\n");
        for (Room room : rooms) {
            sb.append(String.format("  Kamar %-5s [%-8s] %s\n",
                    room.getRoomNumber(),
                    room.getRoomType(),
                    room.getStatus().getDisplayName()));
        }
        return sb.toString();
    }

    /**
     * Generate laporan revenue dalam rentang tanggal.
     *
     * @param from tanggal awal
     * @param to   tanggal akhir
     * @return String laporan revenue
     */
    public String generateRevenueReport(LocalDate from, LocalDate to) {
        double totalRevenue = getRevenue(from, to);
        Map<String, Double> byType = getRevenueByRoomType(from, to);

        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════╗\n");
        sb.append("║           LAPORAN REVENUE                  ║\n");
        sb.append("╚════════════════════════════════════════════╝\n");
        sb.append(String.format("Periode : %s s/d %s\n",
                from.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                to.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
        sb.append(SEPARATOR);
        sb.append(String.format("Total Revenue      : Rp %,.0f\n", totalRevenue));
        sb.append(String.format("Rata-rata Stay     : %.1f malam\n",
                getAverageStayDuration()));
        sb.append(SEPARATOR);
        sb.append("Revenue per Tipe Kamar:\n");
        for (Map.Entry<String, Double> entry : byType.entrySet()) {
            sb.append(String.format("  %-12s : Rp %,.0f\n",
                    entry.getKey(), entry.getValue()));
        }
        sb.append(SEPARATOR);
        sb.append("Rata-rata Stay per Tipe Kamar:\n");
        Map<String, Double> avgStay = getAvgStayByRoomType();
        for (Map.Entry<String, Double> entry : avgStay.entrySet()) {
            sb.append(String.format("  %-12s : %.1f malam\n",
                    entry.getKey(), entry.getValue()));
        }
        return sb.toString();
    }

    /**
     * Export laporan revenue ke file CSV.
     *
     * @param from tanggal awal
     * @param to   tanggal akhir
     */
    public void exportRevenueToCSV(LocalDate from, LocalDate to) {
        new java.io.File(REPORT_DIR).mkdirs();

        String fileName = REPORT_DIR + "revenue-"
                + from.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-to-"
                + to.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + ".csv";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write("reservationId,guestName,roomType,checkIn,checkOut,nights,subtotal");
            bw.newLine();

            for (Reservation r : reservations) {
                if (r.getStatus() != Reservation.ReservationStatus.CHECKED_OUT) continue;
                if (r.getCheckOutDate().isBefore(from)) continue;
                if (r.getCheckOutDate().isAfter(to)) continue;

                bw.write(String.join(",",
                    r.getReservationId(),
                    r.getGuest().getName(),
                    r.getRoom().getRoomType(),
                    r.getCheckInDate().toString(),
                    r.getCheckOutDate().toString(),
                    String.valueOf(r.getTotalNights()),
                    String.valueOf(r.getSubtotal())
                ));
                bw.newLine();
            }
            System.out.println("Report diekspor ke: " + fileName);
        } catch (IOException e) {
            System.err.println("Gagal export report: " + e.getMessage());
        }
    }

    // ─── Setter untuk refresh data ────────────────────────────
    public void setRooms(List<Room> rooms)                   { this.rooms = rooms; }
    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}