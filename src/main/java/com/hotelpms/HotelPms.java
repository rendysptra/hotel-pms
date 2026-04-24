/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.hotelpms;

import com.hotelpms.enums.PaymentStatus;
import com.hotelpms.enums.RoomStatus;
import com.hotelpms.exception.*;
import com.hotelpms.model.*;
import com.hotelpms.util.CSVHandler;
 
import java.time.LocalDate;

/**
 * File testing sementara untuk memvalidasi model layer dan CSVHandler.
 * File ini TIDAK di-commit ke Git setelah testing selesai.
 *
 * @author rendysaptra
 */
public class HotelPms {

    // ─── Counter ──────────────────────────────────────────────
    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║     HOTEL PMS — UNIT TEST MINGGU 1   ║");
        System.out.println("╚══════════════════════════════════════╝\n");

        testPerson();
        testRoom();
        testReservation();
        testFolioItem();
        testEnums();
        testExceptions();
        testCSVHandler();

        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.printf( "║  HASIL: %d PASSED | %d FAILED%s║%n",
                passed, failed, " ".repeat(Math.max(1, 13 - String.valueOf(passed).length() - String.valueOf(failed).length())));
        System.out.println("╚══════════════════════════════════════╝");

        if (failed == 0) {
            System.out.println("\n✅ Semua test PASSED! Siap lanjut ke Minggu 2.");
        } else {
            System.out.println("\n❌ Ada " + failed + " test yang FAILED. Cek output di atas.");
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TEST 1 — PERSON (Guest, Staff, Receptionist, Manager)
    // ══════════════════════════════════════════════════════════
    static void testPerson() {
        System.out.println("─── TEST 1: Person Layer ───────────────");

        // Test Guest
        try {
            Guest guest = new Guest("GST-001", "Budi Santoso",
                    "08123456789", "budi@email.com", "1234567890");
            guest.setPreference("floor", "high");
            guest.setPreference("bed", "king");
            guest.addLoyaltyPoints(100);
            guest.addReservationHistory("RSV-001");

            assert guest.getName().equals("Budi Santoso")      : "Nama guest salah";
            assert guest.getPreference("floor").equals("high") : "Preferensi floor salah";
            assert guest.getLoyaltyPoints() == 100             : "Loyalty points salah";
            assert guest.getReservationHistory().size() == 1   : "History size salah";
            assert guest.getPreference("unknown").equals("Tidak ada") : "Default preference salah";

            System.out.println("  ✅ Guest — semua atribut dan method OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ Guest — " + e.getMessage());
            failed++;
        }

        // Test Receptionist
        try {
            Receptionist rcpt = new Receptionist("P-001", "Sari",
                    "08111111111", "sari@hotel.com", "STF-001", "PAGI");

            String keyCard = rcpt.issueKeyCard("101");
            assert keyCard.startsWith("KC-")         : "Format keyCard salah";
            assert rcpt.getMainDuty().contains("check-in") : "MainDuty salah";

            String ci = rcpt.processCheckIn("RSV-001");
            assert rcpt.getTotalCheckInsHandled() == 1 : "Counter checkIn salah";

            String co = rcpt.processCheckOut("RSV-001");
            assert rcpt.getTotalCheckOutsHandled() == 1 : "Counter checkOut salah";

            System.out.println("  ✅ Receptionist — issueKeyCard, processCheckIn/Out OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ Receptionist — " + e.getMessage());
            failed++;
        }

        // Test Manager
        try {
            Manager mgr = new Manager("P-002", "Budi Manajer",
                    "08222222222", "budi@hotel.com", "STF-002", "Operasional");

            String report = mgr.generateReport("OCCUPANCY");
            assert report.contains("OCCUPANCY")          : "Report type salah";
            assert mgr.getTotalReportsGenerated() == 1   : "Counter report salah";
            assert !mgr.getMainDuty().equals(
                new Receptionist("x","x","x","x","x","x").getMainDuty()) 
                : "Polymorphism getMainDuty gagal";

            System.out.println("  ✅ Manager — generateReport, polymorphism OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ Manager — " + e.getMessage());
            failed++;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TEST 2 — ROOM
    // ══════════════════════════════════════════════════════════
    static void testRoom() {
        System.out.println("\n─── TEST 2: Room Layer ─────────────────");

        try {
            StandardRoom std = new StandardRoom("RM-101", "101", 1);
            DeluxeRoom    dlx = new DeluxeRoom("RM-201", "201", 2);
            VIPRoom       vip = new VIPRoom("RM-301", "301", 3);

            // Tipe kamar
            assert std.getRoomType().equals("STANDARD") : "Tipe standard salah";
            assert dlx.getRoomType().equals("DELUXE")   : "Tipe deluxe salah";
            assert vip.getRoomType().equals("VIP")      : "Tipe VIP salah";

            // Harga
            assert std.getPricePerNight() == 300_000  : "Harga standard salah";
            assert dlx.getPricePerNight() == 600_000  : "Harga deluxe salah";
            assert vip.getPricePerNight() == 1_200_000: "Harga VIP salah";

            // Status default
            assert std.getStatus() == RoomStatus.CLEAN : "Status default bukan CLEAN";
            assert std.isAvailableForBooking()          : "Kamar baru harusnya available";

            // Hitung total harga
            assert std.calculateTotalPrice(3) == 900_000 : "Total harga 3 malam salah";

            // Ubah status
            std.setStatus(RoomStatus.OCCUPIED);
            assert !std.isAvailableForBooking() : "Kamar OCCUPIED harusnya tidak available";

            // Polymorphism — panggil getRoomType() dari referensi Room
            Room room = new DeluxeRoom("RM-202", "202", 2);
            assert room.getRoomType().equals("DELUXE") : "Polymorphism Room gagal";

            System.out.println("  ✅ Room subtypes — harga, status, polymorphism OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ Room — " + e.getMessage());
            failed++;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TEST 3 — RESERVATION
    // ══════════════════════════════════════════════════════════
    static void testReservation() {
        System.out.println("\n─── TEST 3: Reservation ────────────────");

        try {
            Guest guest = new Guest("GST-001", "Andi", "081", "andi@mail.com", "KTP001");
            Room  room  = new StandardRoom("RM-101", "101", 1);

            LocalDate checkIn  = LocalDate.of(2025, 6, 1);
            LocalDate checkOut = LocalDate.of(2025, 6, 4);

            Reservation rsv = new Reservation("RSV-001", guest, room, checkIn, checkOut);

            // Status awal
            assert rsv.getStatus() == Reservation.ReservationStatus.RESERVED : "Status awal bukan RESERVED";
            assert rsv.getTotalNights() == 3                                  : "Total malam salah";
            assert rsv.getKeyCardId() == null                                 : "KeyCard harusnya null";

            // Issue key card
            rsv.issueKeyCard("KC-20250601-101");
            assert rsv.getKeyCardId().equals("KC-20250601-101") : "KeyCard tidak tersimpan";

            // Check-in
            rsv.checkIn();
            assert rsv.getStatus() == Reservation.ReservationStatus.CHECKED_IN : "Status setelah checkIn salah";

            // Tidak bisa cancel setelah check-in
            rsv.cancel();
            assert rsv.getStatus() == Reservation.ReservationStatus.CHECKED_IN : "Harusnya tidak bisa cancel setelah checkIn";

            // Check-out
            rsv.checkOut();
            assert rsv.getStatus() == Reservation.ReservationStatus.CHECKED_OUT : "Status setelah checkOut salah";

            System.out.println("  ✅ Reservation — status flow, keyCard, totalNights OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ Reservation — " + e.getMessage());
            failed++;
        }

        // Test cancel flow
        try {
            Guest guest = new Guest("GST-002", "Citra", "082", "citra@mail.com", "KTP002");
            Room  room  = new DeluxeRoom("RM-201", "201", 2);
            Reservation rsv = new Reservation("RSV-002", guest, room,
                    LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 3));

            rsv.cancel();
            assert rsv.getStatus() == Reservation.ReservationStatus.CANCELLED : "Status cancel salah";

            System.out.println("  ✅ Reservation cancel flow OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ Reservation cancel — " + e.getMessage());
            failed++;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TEST 4 — FOLIO ITEM
    // ══════════════════════════════════════════════════════════
    static void testFolioItem() {
        System.out.println("\n─── TEST 4: FolioItem ──────────────────");

        try {
            Guest guest = new Guest("GST-001", "Andi", "081", "andi@mail.com", "KTP001");
            Room  room  = new StandardRoom("RM-101", "101", 1);
            Reservation rsv = new Reservation("RSV-001", guest, room,
                    LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 3));

            FolioItem roomCharge = new FolioItem(
                    "FI-001", "Biaya Kamar 2 Malam",
                    FolioItem.ChargeType.ROOM, 600_000, LocalDate.of(2025, 6, 3));
            FolioItem laundry = new FolioItem(
                    "FI-002", "Laundry Baju",
                    FolioItem.ChargeType.LAUNDRY, 50_000, LocalDate.of(2025, 6, 2));

            rsv.addFolioItem(roomCharge);
            rsv.addFolioItem(laundry);

            assert rsv.getFolioItems().size() == 2   : "Jumlah folio item salah";
            assert rsv.getSubtotal() == 650_000      : "Subtotal salah";
            assert roomCharge.getFormattedAmount().contains("600") : "Format amount salah";

            System.out.println("  ✅ FolioItem — addFolioItem, getSubtotal, format OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ FolioItem — " + e.getMessage());
            failed++;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TEST 5 — ENUMS
    // ══════════════════════════════════════════════════════════
    static void testEnums() {
        System.out.println("\n─── TEST 5: Enums ──────────────────────");

        try {
            assert RoomStatus.CLEAN.getDisplayName().equals("Bersih")             : "RoomStatus CLEAN salah";
            assert RoomStatus.DIRTY.getDisplayName().equals("Perlu Dibersihkan")  : "RoomStatus DIRTY salah";
            assert RoomStatus.OCCUPIED.getDisplayName().equals("Terisi")          : "RoomStatus OCCUPIED salah";
            assert RoomStatus.RESERVED.getDisplayName().equals("Dipesan")         : "RoomStatus RESERVED salah";
            assert RoomStatus.OUT_OF_ORDER.getDisplayName().equals("Tidak Tersedia") : "RoomStatus OOO salah";

            assert PaymentStatus.UNPAID.getDisplayName().equals("Belum Dibayar")  : "PaymentStatus UNPAID salah";
            assert PaymentStatus.PAID.getDisplayName().equals("Lunas")            : "PaymentStatus PAID salah";
            assert PaymentStatus.REFUNDED.getDisplayName().equals("Dana Dikembalikan") : "PaymentStatus REFUNDED salah";

            System.out.println("  ✅ RoomStatus & PaymentStatus — semua nilai OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ Enums — " + e.getMessage());
            failed++;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TEST 6 — EXCEPTIONS
    // ══════════════════════════════════════════════════════════
    static void testExceptions() {
        System.out.println("\n─── TEST 6: Custom Exceptions ──────────");

        try {
            // RoomUnavailableException
            try {
                throw new RoomUnavailableException("101");
            } catch (RoomUnavailableException e) {
                assert e.getRoomNumber().equals("101")    : "RoomNumber salah";
                assert e.getMessage().contains("101")     : "Message tidak mengandung roomNumber";
            }

            // GuestNotFoundException
            try {
                throw new GuestNotFoundException("Budi");
            } catch (GuestNotFoundException e) {
                assert e.getSearchKey().equals("Budi")   : "SearchKey salah";
            }

            // InvalidDateException
            try {
                throw new InvalidDateException(
                        LocalDate.of(2025, 6, 5), LocalDate.of(2025, 6, 1));
            } catch (InvalidDateException e) {
                assert e.getCheckIn().equals(LocalDate.of(2025, 6, 5)) : "CheckIn date salah";
            }

            // PaymentFailedException
            try {
                throw new PaymentFailedException("RSV-001", 500_000);
            } catch (PaymentFailedException e) {
                assert e.getReservationId().equals("RSV-001") : "ReservationId salah";
                assert e.getAmount() == 500_000               : "Amount salah";
            }

            // KeyCardException
            try {
                throw new KeyCardException("KC-001");
            } catch (KeyCardException e) {
                assert e.getKeyCardId().equals("KC-001")  : "KeyCardId salah";
            }

            System.out.println("  ✅ Semua custom exception — throw & catch OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ Exceptions — " + e.getMessage());
            failed++;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TEST 7 — CSV HANDLER
    // ══════════════════════════════════════════════════════════
    static void testCSVHandler() {
        System.out.println("\n─── TEST 7: CSVHandler ─────────────────");

        // Test initializeFiles
        try {
            CSVHandler.initializeFiles();
            assert new java.io.File("data/rooms.csv").exists()        : "rooms.csv tidak dibuat";
            assert new java.io.File("data/guests.csv").exists()       : "guests.csv tidak dibuat";
            assert new java.io.File("data/reservations.csv").exists() : "reservations.csv tidak dibuat";

            System.out.println("  ✅ initializeFiles — folder & file CSV terbuat OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ initializeFiles — " + e.getMessage());
            failed++;
        }

        // Test write & read Guest
        try {
            Guest guest = new Guest("GST-TEST", "Test User",
                    "08999", "test@mail.com", "KTP-TEST");
            guest.setPreference("floor", "high");
            guest.setPreference("bed", "king");
            guest.addLoyaltyPoints(50);

            CSVHandler.appendGuest(guest);

            java.util.List<Guest> guests = CSVHandler.readGuests();
            Guest loaded = guests.stream()
                    .filter(g -> g.getId().equals("GST-TEST"))
                    .findFirst().orElse(null);

            assert loaded != null                                   : "Guest tidak ditemukan setelah write";
            assert loaded.getName().equals("Test User")            : "Nama guest berubah setelah CSV";
            assert loaded.getLoyaltyPoints() == 50                 : "LoyaltyPoints berubah setelah CSV";
            assert loaded.getPreference("floor").equals("high")    : "Preferensi floor berubah setelah CSV";

            System.out.println("  ✅ CSVHandler Guest — write & read konsisten OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ CSVHandler Guest — " + e.getMessage());
            failed++;
        }

        // Test write & read Room
        try {
            StandardRoom room = new StandardRoom("RM-TEST", "999", 9);
            java.util.List<Room> rooms = CSVHandler.readRooms();
            rooms.add(room);
            CSVHandler.writeRooms(rooms);

            java.util.List<Room> loaded = CSVHandler.readRooms();
            Room loadedRoom = loaded.stream()
                    .filter(r -> r.getRoomId().equals("RM-TEST"))
                    .findFirst().orElse(null);

            assert loadedRoom != null                              : "Room tidak ditemukan setelah write";
            assert loadedRoom.getRoomType().equals("STANDARD")    : "Tipe room berubah setelah CSV";
            assert loadedRoom.getStatus() == RoomStatus.CLEAN     : "Status room berubah setelah CSV";

            System.out.println("  ✅ CSVHandler Room — write & read konsisten OK");
            passed++;
        } catch (Exception e) {
            System.out.println("  ❌ CSVHandler Room — " + e.getMessage());
            failed++;
        }
    }
}
