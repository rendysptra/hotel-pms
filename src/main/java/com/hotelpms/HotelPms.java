/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.hotelpms;

import com.hotelpms.enums.RoomStatus;
import com.hotelpms.exception.*;
import com.hotelpms.model.*;
import com.hotelpms.service.*;
import com.hotelpms.util.CSVHandler;

import java.time.LocalDate;

/**
 * File testing untuk memvalidasi service layer Minggu 2.
 * File ini TIDAK di-commit ke Git setelah testing selesai.
 *
 * @author rendysaptra
 */
public class HotelPms {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║     HOTEL PMS — UNIT TEST MINGGU 2   ║");
        System.out.println("╚══════════════════════════════════════╝\n");

        setupTestData();

        testBillingService();
        testHousekeepingService();
        testReportService();
        testHotelService();
        testEdgeCases();

        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.printf( "║  HASIL: %d PASSED | %d FAILED%s║%n",
                passed, failed, " ".repeat(Math.max(1, 13 - String.valueOf(passed).length()
                        - String.valueOf(failed).length())));
        System.out.println("╚══════════════════════════════════════╝");

        if (failed == 0) {
            System.out.println("\n✅ Semua test PASSED! Siap lanjut ke Minggu 3.");
        } else {
            System.out.println("\n❌ Ada " + failed + " test FAILED. Cek output di atas.");
        }

        cleanupTestData();
    }

    // ══════════════════════════════════════════════════════════
    //  SETUP & CLEANUP
    // ══════════════════════════════════════════════════════════
    static void setupTestData() {
        CSVHandler.initializeFiles();

        // Sample rooms
        java.util.List<Room> rooms = new java.util.ArrayList<>();
        rooms.add(new StandardRoom("RM-101", "101", 1));
        rooms.add(new StandardRoom("RM-102", "102", 1));
        rooms.add(new DeluxeRoom("RM-201", "201", 2));
        rooms.add(new VIPRoom("RM-301", "301", 3));
        CSVHandler.writeRooms(rooms);

        // Sample guests
        Guest g1 = new Guest("GST-001", "Budi Santoso", "08111", "budi@mail.com", "KTP001");
        Guest g2 = new Guest("GST-002", "Sari Dewi",    "08222", "sari@mail.com", "KTP002");
        g1.setPreference("floor", "high");
        g1.setPreference("bed", "king");
        CSVHandler.appendGuest(g1);
        CSVHandler.appendGuest(g2);

        System.out.println("✔ Test data berhasil disiapkan\n");
    }

    static void cleanupTestData() {
        // Reset semua CSV ke header saja
        try {
            java.io.PrintWriter pw;
            pw = new java.io.PrintWriter("data/rooms.csv");
            pw.println("roomId,roomNumber,type,floor,pricePerNight,status");
            pw.close();

            pw = new java.io.PrintWriter("data/guests.csv");
            pw.println("guestId,name,phone,email,idCard,preferences,loyaltyPoints");
            pw.close();

            pw = new java.io.PrintWriter("data/reservations.csv");
            pw.println("reservationId,guestId,roomId,checkInDate,checkOutDate,keyCardId,status,totalPrice");
            pw.close();

            System.out.println("\n✔ Test data berhasil dibersihkan");
        } catch (Exception e) {
            System.out.println("Warning: gagal cleanup — " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TEST 1 — BILLING SERVICE
    // ══════════════════════════════════════════════════════════
    static void testBillingService() {
        System.out.println("─── TEST 1: BillingService ─────────────");

        // Test kalkulasi pajak
        try {
            BillingService billing = new BillingService();

            double subtotal = 600_000;
            double tax      = billing.calculateTax(subtotal);
            double expected = 600_000 * 0.11;

            assert tax == expected : "Kalkulasi pajak salah";
            assert billing.getTaxRate() == 0.11 : "Tax rate salah";

            System.out.println("  ✅ Kalkulasi pajak 11% OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ Kalkulasi pajak — " + e.getMessage());
            failed++;
        }

        // Test addCharge dan calculateTotal
        try {
            BillingService billing = new BillingService();
            Guest guest = new Guest("GST-001", "Budi", "081", "b@mail.com", "KTP001");
            Room  room  = new StandardRoom("RM-101", "101", 1);
            Reservation rsv = new Reservation("RSV-T01", guest, room,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3));

            billing.addCharge(rsv, "Laundry", FolioItem.ChargeType.LAUNDRY, 50_000);
            billing.addCharge(rsv, "Mini Bar", FolioItem.ChargeType.MINIBAR, 75_000);

            assert rsv.getFolioItems().size() == 2 : "Jumlah folio item salah";
            assert rsv.getSubtotal() == 125_000    : "Subtotal salah";

            double total = billing.calculateTotal(rsv);
            double expectedTotal = 125_000 + (125_000 * 0.11);
            assert Math.abs(total - expectedTotal) < 0.01 : "Total dengan pajak salah";

            System.out.println("  ✅ addCharge + calculateTotal OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ addCharge/calculateTotal — " + e.getMessage());
            failed++;
        }

        // Test addRoomCharge
        try {
            BillingService billing = new BillingService();
            Guest guest = new Guest("GST-001", "Budi", "081", "b@mail.com", "KTP001");
            Room  room  = new DeluxeRoom("RM-201", "201", 2);
            Reservation rsv = new Reservation("RSV-T02", guest, room,
                    LocalDate.of(2025, 6, 1),
                    LocalDate.of(2025, 6, 4)); // 3 malam

            billing.addRoomCharge(rsv);

            assert rsv.getFolioItems().size() == 1        : "Room charge tidak ditambahkan";
            assert rsv.getSubtotal() == 600_000 * 3       : "Room charge salah (3 malam deluxe)";
            assert rsv.getFolioItems().get(0)
                    .getChargeType() == FolioItem.ChargeType.ROOM : "ChargeType bukan ROOM";

            System.out.println("  ✅ addRoomCharge (3 malam Deluxe = Rp 1.800.000) OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ addRoomCharge — " + e.getMessage());
            failed++;
        }

        // Test printFolioToFile
        try {
            BillingService billing = new BillingService();
            Guest guest = new Guest("GST-001", "Budi", "081", "b@mail.com", "KTP001");
            Room  room  = new StandardRoom("RM-101", "101", 1);
            Reservation rsv = new Reservation("RSV-T03", guest, room,
                    LocalDate.of(2025, 6, 1),
                    LocalDate.of(2025, 6, 2));

            billing.addRoomCharge(rsv);
            billing.printFolioToFile(rsv);

            java.io.File folioFile = new java.io.File("data/folio/folio-RSV-T03.txt");
            assert folioFile.exists() : "File folio tidak dibuat";

            System.out.println("  ✅ printFolioToFile — file folio terbuat OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ printFolioToFile — " + e.getMessage());
            failed++;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TEST 2 — HOUSEKEEPING SERVICE
    // ══════════════════════════════════════════════════════════
    static void testHousekeepingService() {
        System.out.println("\n─── TEST 2: HousekeepingService ────────");

        try {
            java.util.List<Room> rooms = new java.util.ArrayList<>();
            Room r1 = new StandardRoom("RM-101", "101", 1);
            Room r2 = new DeluxeRoom("RM-201", "201", 2);
            Room r3 = new VIPRoom("RM-301", "301", 3);
            rooms.add(r1); rooms.add(r2); rooms.add(r3);

            HousekeepingService hk = new HousekeepingService(rooms);

            // Test autoDirtyOnCheckout
            hk.autoDirtyOnCheckout(r1);
            assert r1.getStatus() == RoomStatus.DIRTY : "autoDirty gagal";
            System.out.println("  ✅ autoDirtyOnCheckout OK");
            passed++;

            // Test markClean
            boolean cleaned = hk.markClean("101");
            assert cleaned                              : "markClean return false";
            assert r1.getStatus() == RoomStatus.CLEAN  : "Status bukan CLEAN setelah markClean";
            System.out.println("  ✅ markClean OK");
            passed++;

            // Test markOutOfOrder
            boolean ooo = hk.markOutOfOrder("201", "Pipa bocor");
            assert ooo                                         : "markOutOfOrder return false";
            assert r2.getStatus() == RoomStatus.OUT_OF_ORDER  : "Status bukan OUT_OF_ORDER";
            System.out.println("  ✅ markOutOfOrder OK");
            passed++;

            // Test getDirtyQueue
            hk.autoDirtyOnCheckout(r3);
            java.util.List<Room> dirty = hk.getDirtyQueue();
            assert dirty.size() == 1        : "Dirty queue size salah";
            assert dirty.get(0).getRoomNumber().equals("301") : "Kamar dirty salah";
            System.out.println("  ✅ getDirtyQueue OK");
            passed++;

            // Test getRoomStatusSummary
            String summary = hk.getRoomStatusSummary();
            assert summary.contains("CLEAN")       : "Summary tidak ada CLEAN";
            assert summary.contains("Total")       : "Summary tidak ada Total";
            System.out.println("  ✅ getRoomStatusSummary OK");
            passed++;

        } catch (Exception e) {
            System.out.println("  ❌ HousekeepingService — " + e.getMessage());
            failed++;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TEST 3 — REPORT SERVICE
    // ══════════════════════════════════════════════════════════
    static void testReportService() {
        System.out.println("\n─── TEST 3: ReportService ──────────────");

        try {
            // Setup data untuk report
            java.util.List<Room> rooms = new java.util.ArrayList<>();
            Room r1 = new StandardRoom("RM-101", "101", 1);
            Room r2 = new DeluxeRoom("RM-201", "201", 2);
            r1.setStatus(RoomStatus.OCCUPIED);
            rooms.add(r1); rooms.add(r2);

            java.util.List<Reservation> reservations = new java.util.ArrayList<>();
            Guest guest = new Guest("GST-001", "Budi", "081", "b@mail.com", "KTP001");

            Reservation rsv = new Reservation("RSV-001", guest, r1,
                    LocalDate.of(2025, 6, 1),
                    LocalDate.of(2025, 6, 4));
            rsv.checkIn();
            rsv.checkOut();
            rsv.addFolioItem(new FolioItem("FI-001", "Biaya Kamar",
                    FolioItem.ChargeType.ROOM, 900_000, LocalDate.of(2025, 6, 4)));
            reservations.add(rsv);

            ReportService report = new ReportService(rooms, reservations);

            // Test occupancy rate
            double occupancy = report.getOccupancyRate();
            assert occupancy == 50.0 : "Occupancy rate salah (harusnya 50%)";
            System.out.println("  ✅ getOccupancyRate (50%) OK");
            passed++;

            // Test revenue
            double revenue = report.getRevenue(
                    LocalDate.of(2025, 6, 1),
                    LocalDate.of(2025, 6, 30));
            assert revenue == 900_000 : "Revenue salah";
            System.out.println("  ✅ getRevenue OK");
            passed++;

            // Test avg stay
            double avg = report.getAverageStayDuration();
            assert avg == 3.0 : "Avg stay salah (harusnya 3 malam)";
            System.out.println("  ✅ getAverageStayDuration (3 malam) OK");
            passed++;

            // Test generate report string
            String occReport = report.generateOccupancyReport();
            assert occReport.contains("50") : "Occupancy report tidak ada angka 50";
            System.out.println("  ✅ generateOccupancyReport OK");
            passed++;

            // Test export CSV
            report.exportRevenueToCSV(
                    LocalDate.of(2025, 6, 1),
                    LocalDate.of(2025, 6, 30));
            java.io.File reportFile = new java.io.File("data/reports");
            assert reportFile.exists() : "Folder reports tidak dibuat";
            System.out.println("  ✅ exportRevenueToCSV OK");
            passed++;

        } catch (Exception e) {
            System.out.println("  ❌ ReportService — " + e.getMessage());
            failed++;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TEST 4 — HOTEL SERVICE (INTEGRASI)
    // ══════════════════════════════════════════════════════════
    static void testHotelService() {
        System.out.println("\n─── TEST 4: HotelService (Integrasi) ───");

        HotelService hotel = new HotelService();

        // Test addGuest
        try {
            Guest guest = new Guest("GST-TEST", "Test Tamu",
                    "08999", "test@mail.com", "KTP-TEST");
            hotel.addGuest(guest);

            Guest found = hotel.findGuestById("GST-TEST");
            assert found.getName().equals("Test Tamu") : "Nama guest salah setelah addGuest";
            System.out.println("  ✅ addGuest + findGuestById OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ addGuest — " + e.getMessage());
            failed++;
        }

        // Test findGuestByName
        try {
            java.util.List<Guest> results = hotel.findGuestsByName("Budi");
            assert !results.isEmpty() : "findGuestByName tidak menemukan hasil";
            System.out.println("  ✅ findGuestByName OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ findGuestByName — " + e.getMessage());
            failed++;
        }

        // Test createReservation
        String reservationId = null;
        try {
            Guest guest = hotel.findGuestById("GST-001");
            Reservation rsv = hotel.createReservation(
                    guest, "STANDARD",
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3));

            reservationId = rsv.getReservationId();
            assert rsv != null                                           : "Reservasi null";
            assert rsv.getStatus() == Reservation.ReservationStatus.RESERVED : "Status bukan RESERVED";
            assert rsv.getRoom().getRoomType().equals("STANDARD")        : "Tipe kamar salah";
            assert rsv.getRoom().getStatus() == RoomStatus.RESERVED      : "Status kamar bukan RESERVED";

            System.out.println("  ✅ createReservation + auto room assignment OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ createReservation — " + e.getMessage());
            failed++;
        }

        // Test invalid date
        try {
            Guest guest = hotel.findGuestById("GST-001");
            hotel.createReservation(guest, "STANDARD",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(2)); // checkOut sebelum checkIn
            System.out.println("  ❌ Harusnya throw InvalidDateException");
            failed++;
        } catch (InvalidDateException e) {
            System.out.println("  ✅ InvalidDateException untuk tanggal tidak valid OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ Exception salah: " + e.getMessage());
            failed++;
        }

        // Test checkIn
        String keyCardId = null;
        if (reservationId != null) {
            try {
                keyCardId = hotel.checkIn(reservationId);
                Reservation rsv = hotel.findReservationById(reservationId);

                assert keyCardId != null                                              : "KeyCard null";
                assert keyCardId.startsWith("KC-")                                   : "Format keyCard salah";
                assert rsv.getStatus() == Reservation.ReservationStatus.CHECKED_IN   : "Status bukan CHECKED_IN";
                assert rsv.getRoom().getStatus() == RoomStatus.OCCUPIED              : "Status kamar bukan OCCUPIED";
                assert !rsv.getFolioItems().isEmpty()                                : "Room charge tidak ditambahkan";

                System.out.println("  ✅ checkIn + issueKeyCard + addRoomCharge OK");
                passed++;
            } catch (Exception e) {
                System.out.println("  ❌ checkIn — " + e.getMessage());
                failed++;
            }
        }

        // Test checkOut
        if (reservationId != null) {
            try {
                double total = hotel.checkOut(reservationId);
                Reservation rsv = hotel.findReservationById(reservationId);

                assert total > 0                                                       : "Total checkout 0";
                assert rsv.getStatus() == Reservation.ReservationStatus.CHECKED_OUT   : "Status bukan CHECKED_OUT";
                assert rsv.getRoom().getStatus() == RoomStatus.DIRTY                  : "Kamar tidak DIRTY setelah checkout";
                assert rsv.getGuest().getLoyaltyPoints() > 0                          : "Loyalty points tidak bertambah";

                System.out.println("  ✅ checkOut + autoDirty + loyaltyPoints OK");
                System.out.println("     Total tagihan: Rp " + String.format("%,.0f", total));
                passed++;
            } catch (Exception e) {
                System.out.println("  ❌ checkOut — " + e.getMessage());
                failed++;
            }
        }

        // Test cancelReservation
        try {
            // Reset status kamar Deluxe ke CLEAN dulu di CSV
            CSVHandler.updateRoomStatus("RM-201", RoomStatus.CLEAN);
            hotel.refreshData();

            Guest guest = hotel.findGuestById("GST-002");
            Reservation rsv = hotel.createReservation(
                    guest, "DELUXE",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(7));

            hotel.cancelReservation(rsv.getReservationId());
            assert rsv.getStatus() == Reservation.ReservationStatus.CANCELLED  : "Status bukan CANCELLED";
            assert rsv.getRoom().getStatus() == RoomStatus.CLEAN               : "Kamar tidak CLEAN setelah cancel";

            System.out.println("  ✅ cancelReservation + restore CLEAN OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ cancelReservation — " + e.getMessage());
            failed++;
        }

        // Test getActiveReservations
        try {
            java.util.List<Reservation> active = hotel.getActiveReservations();
            assert active != null : "getActiveReservations null";
            System.out.println("  ✅ getActiveReservations OK (" + active.size() + " aktif)");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ getActiveReservations — " + e.getMessage());
            failed++;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TEST 5 — EDGE CASES
    // ══════════════════════════════════════════════════════════
    static void testEdgeCases() {
        System.out.println("\n─── TEST 5: Edge Cases ─────────────────");

        HotelService hotel = new HotelService();

        // EC-01: checkIn reservasi yang sudah CANCELLED
        try {
            Guest guest = hotel.findGuestById("GST-001");
            Reservation rsv = hotel.createReservation(
                    guest, "STANDARD",
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3));

            hotel.cancelReservation(rsv.getReservationId());

            // Coba checkIn setelah cancel — harusnya throw exception
            hotel.checkIn(rsv.getReservationId());
            System.out.println("  ❌ EC-01: Harusnya throw exception saat checkIn reservasi CANCELLED");
            failed++;
        } catch (RoomUnavailableException e) {
            System.out.println("  ✅ EC-01: checkIn reservasi CANCELLED throw RoomUnavailableException OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ✅ EC-01: checkIn reservasi CANCELLED throw exception OK (" 
                    + e.getClass().getSimpleName() + ")");
            passed++;
        }

        // EC-02: checkOut reservasi yang belum checkIn (masih RESERVED)
        try {
            Guest guest = hotel.findGuestById("GST-002");
            Reservation rsv = hotel.createReservation(
                    guest, "STANDARD",
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3));

            // Coba checkOut tanpa checkIn dulu — harusnya throw exception
            hotel.checkOut(rsv.getReservationId());
            System.out.println("  ❌ EC-02: Harusnya throw exception saat checkOut tanpa checkIn");
            failed++;

            // cleanup — cancel reservasi ini
            hotel.cancelReservation(rsv.getReservationId());
        } catch (Exception e) {
            System.out.println("  ✅ EC-02: checkOut tanpa checkIn throw exception OK ("
                    + e.getClass().getSimpleName() + ")");
            passed++;
        }

        // EC-03: modifyReservation dengan tanggal overlap
        try {
            // Buat 2 reservasi di kamar yang sama dengan tanggal berbeda
            Guest g1 = hotel.findGuestById("GST-001");

            // Reservasi 1: 10-15
            Reservation rsv1 = hotel.createReservation(
                    g1, "DELUXE",
                    LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(15));

            // Reservasi 2: 16-20 (kamar berbeda karena hanya 1 deluxe)
            // Coba modify rsv1 ke tanggal 12-18 yang overlap dengan dirinya sendiri
            // Seharusnya tetap bisa karena exclude reservasi sendiri
            hotel.modifyReservation(rsv1.getReservationId(),
                    LocalDate.now().plusDays(11),
                    LocalDate.now().plusDays(14));

            assert rsv1.getCheckInDate().equals(LocalDate.now().plusDays(11))  : "CheckIn tidak berubah";
            assert rsv1.getCheckOutDate().equals(LocalDate.now().plusDays(14)) : "CheckOut tidak berubah";

            System.out.println("  ✅ EC-03: modifyReservation tanggal valid OK");
            passed++;

            // Cleanup
            hotel.cancelReservation(rsv1.getReservationId());
        } catch (Exception e) {
            System.out.println("  ❌ EC-03: modifyReservation — " + e.getMessage());
            failed++;
        }

        // EC-04: modifyReservation dengan checkOut sebelum checkIn
        try {
            Guest guest = hotel.findGuestById("GST-001");
            Reservation rsv = hotel.createReservation(
                    guest, "STANDARD",
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusDays(8));

            // Coba modify dengan tanggal tidak valid
            hotel.modifyReservation(rsv.getReservationId(),
                    LocalDate.now().plusDays(8),   // checkIn
                    LocalDate.now().plusDays(5));   // checkOut sebelum checkIn

            System.out.println("  ❌ EC-04: Harusnya throw InvalidDateException");
            failed++;
        } catch (InvalidDateException e) {
            System.out.println("  ✅ EC-04: modifyReservation tanggal tidak valid throw InvalidDateException OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ✅ EC-04: modifyReservation tanggal tidak valid throw exception OK ("
                    + e.getClass().getSimpleName() + ")");
            passed++;
        }

        // EC-05: cancelReservation yang sudah CHECKED_IN
        try {
            Guest guest = hotel.findGuestById("GST-002");
            Reservation rsv = hotel.createReservation(
                    guest, "STANDARD",
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3));

            hotel.checkIn(rsv.getReservationId());

            // Coba cancel setelah checkIn — harusnya throw exception
            hotel.cancelReservation(rsv.getReservationId());
            System.out.println("  ❌ EC-05: Harusnya throw exception saat cancel reservasi CHECKED_IN");
            failed++;
        } catch (InvalidDateException e) {
            System.out.println("  ✅ EC-05: cancelReservation setelah checkIn throw InvalidDateException OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ✅ EC-05: cancelReservation setelah checkIn throw exception OK ("
                    + e.getClass().getSimpleName() + ")");
            passed++;
        }

        // EC-06: createReservation dengan tanggal checkIn di masa lalu
        try {
            Guest guest = hotel.findGuestById("GST-001");
            hotel.createReservation(
                    guest, "STANDARD",
                    LocalDate.now().minusDays(1),  // kemarin
                    LocalDate.now().plusDays(2));

            System.out.println("  ❌ EC-06: Harusnya throw InvalidDateException untuk tanggal masa lalu");
            failed++;
        } catch (InvalidDateException e) {
            System.out.println("  ✅ EC-06: checkIn tanggal masa lalu throw InvalidDateException OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ✅ EC-06: checkIn tanggal masa lalu throw exception OK ("
                    + e.getClass().getSimpleName() + ")");
            passed++;
        }

        // EC-07: BillingService addCharge dengan amount negatif
        try {
            BillingService billing = new BillingService();
            Guest guest = new Guest("GST-EC", "EC Tamu", "089", "ec@mail.com", "KTP-EC");
            Room  room  = new StandardRoom("RM-101", "101", 1);
            Reservation rsv = new Reservation("RSV-EC", guest, room,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(2));

            billing.addCharge(rsv, "Invalid Charge",
                    FolioItem.ChargeType.OTHER, -50_000);

            // Amount negatif seharusnya diabaikan
            assert rsv.getFolioItems().isEmpty() : "Charge negatif seharusnya tidak ditambahkan";

            System.out.println("  ✅ EC-07: addCharge dengan amount negatif diabaikan OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ EC-07: addCharge negatif — " + e.getMessage());
            failed++;
        }

        // EC-08: findGuestById dengan ID yang tidak ada
        try {
            hotel.findGuestById("GST-TIDAK-ADA");
            System.out.println("  ❌ EC-08: Harusnya throw GuestNotFoundException");
            failed++;
        } catch (GuestNotFoundException e) {
            System.out.println("  ✅ EC-08: findGuestById ID tidak ada throw GuestNotFoundException OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ EC-08: Exception salah — " + e.getMessage());
            failed++;
        }
    }
}