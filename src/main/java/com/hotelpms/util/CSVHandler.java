/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.util;

import com.hotelpms.enums.RoomStatus;
import com.hotelpms.model.DeluxeRoom;
import com.hotelpms.model.FolioItem;
import com.hotelpms.model.Guest;
import com.hotelpms.model.Reservation;
import com.hotelpms.model.Room;
import com.hotelpms.model.StandardRoom;
import com.hotelpms.model.VIPRoom;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class untuk menangani semua operasi File I/O dengan format CSV.
 * Bertanggung jawab membaca dan menulis data Guest, Room, dan Reservation
 * ke file CSV yang tersimpan di folder data/.
 *
 * @author Rendy & Panji
 * @version 1.0
 */
public class CSVHandler {

    // ─── Path File CSV ────────────────────────────────────────
    private static final String ROOMS_FILE        = "data/rooms.csv";
    private static final String GUESTS_FILE       = "data/guests.csv";
    private static final String RESERVATIONS_FILE = "data/reservations.csv";

    // ─── Separator ────────────────────────────────────────────
    private static final String SEPARATOR     = ",";
    private static final String PREF_SEPARATOR = ";";

    // ══════════════════════════════════════════════════════════
    //  ROOMS
    // ══════════════════════════════════════════════════════════

    /**
     * Baca semua data kamar dari rooms.csv.
     *
     * @return List berisi semua objek Room
     */
    public static List<Room> readRooms() {
        List<Room> rooms = new ArrayList<>();
        File file = new File(ROOMS_FILE);

        if (!file.exists()) {
            return rooms;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(SEPARATOR);
                if (data.length < 6) continue;

                String roomId     = data[0].trim();
                String roomNumber = data[1].trim();
                String type       = data[2].trim();
                int    floor      = Integer.parseInt(data[3].trim());
                String statusStr  = data[5].trim();

                Room room;
                switch (type.toUpperCase()) {
                    case "DELUXE" -> room = new DeluxeRoom(roomId, roomNumber, floor);
                    case "VIP"    -> room = new VIPRoom(roomId, roomNumber, floor);
                    default       -> room = new StandardRoom(roomId, roomNumber, floor);
                }

                try {
                    room.setStatus(RoomStatus.valueOf(statusStr));
                } catch (IllegalArgumentException e) {
                    room.setStatus(RoomStatus.CLEAN);
                }

                rooms.add(room);
            }
        } catch (IOException e) {
            System.err.println("Error membaca rooms.csv: " + e.getMessage());
        }

        return rooms;
    }

    /**
     * Tulis ulang seluruh data kamar ke rooms.csv.
     *
     * @param rooms List Room yang akan ditulis
     */
    public static void writeRooms(List<Room> rooms) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ROOMS_FILE))) {
            bw.write("roomId,roomNumber,type,floor,pricePerNight,status");
            bw.newLine();
            for (Room room : rooms) {
                bw.write(String.join(SEPARATOR,
                    room.getRoomId(),
                    room.getRoomNumber(),
                    room.getRoomType(),
                    String.valueOf(room.getFloor()),
                    String.valueOf(room.getPricePerNight()),
                    room.getStatus().name()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error menulis rooms.csv: " + e.getMessage());
        }
    }

    /**
     * Update status satu kamar berdasarkan roomId.
     *
     * @param roomId ID kamar yang akan diupdate
     * @param status Status baru kamar
     */
    public static void updateRoomStatus(String roomId, RoomStatus status) {
        List<Room> rooms = readRooms();
        for (Room room : rooms) {
            if (room.getRoomId().equals(roomId)) {
                room.setStatus(status);
                break;
            }
        }
        writeRooms(rooms);
    }

    // ══════════════════════════════════════════════════════════
    //  GUESTS
    // ══════════════════════════════════════════════════════════

    /**
     * Baca semua data tamu dari guests.csv.
     *
     * @return List berisi semua objek Guest
     */
    public static List<Guest> readGuests() {
        List<Guest> guests = new ArrayList<>();
        File file = new File(GUESTS_FILE);

        if (!file.exists()) {
            return guests;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(SEPARATOR);
                if (data.length < 7) continue;

                String guestId      = data[0].trim();
                String name         = data[1].trim();
                String phone        = data[2].trim();
                String email        = data[3].trim();
                String idCard       = data[4].trim();
                String prefStr      = data[5].trim();
                int    loyaltyPts   = Integer.parseInt(data[6].trim());

                Guest guest = new Guest(guestId, name, phone, email, idCard);
                guest.setLoyaltyPoints(loyaltyPts);

                // Parse preferences: key1=val1;key2=val2
                if (!prefStr.isEmpty() && !prefStr.equals("-")) {
                    Map<String, String> prefs = new HashMap<>();
                    for (String pref : prefStr.split(PREF_SEPARATOR)) {
                        String[] kv = pref.split("=");
                        if (kv.length == 2) {
                            prefs.put(kv[0].trim(), kv[1].trim());
                        }
                    }
                    guest.setPreferences(prefs);
                }

                guests.add(guest);
            }
        } catch (IOException e) {
            System.err.println("Error membaca guests.csv: " + e.getMessage());
        }

        return guests;
    }

    /**
     * Tulis ulang seluruh data tamu ke guests.csv.
     *
     * @param guests List Guest yang akan ditulis
     */
    public static void writeGuests(List<Guest> guests) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(GUESTS_FILE))) {
            bw.write("guestId,name,phone,email,idCard,preferences,loyaltyPoints");
            bw.newLine();
            for (Guest guest : guests) {
                // Serialize preferences: key1=val1;key2=val2
                String prefStr = "-";
                if (!guest.getPreferences().isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    guest.getPreferences().forEach((k, v) ->
                        sb.append(k).append("=").append(v).append(PREF_SEPARATOR)
                    );
                    prefStr = sb.toString().replaceAll(";$", "");
                }

                bw.write(String.join(SEPARATOR,
                    guest.getId(),
                    guest.getName(),
                    guest.getPhone(),
                    guest.getEmail(),
                    guest.getIdCard(),
                    prefStr,
                    String.valueOf(guest.getLoyaltyPoints())
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error menulis guests.csv: " + e.getMessage());
        }
    }

    /**
     * Tambah satu tamu baru ke guests.csv.
     *
     * @param guest objek Guest yang akan ditambahkan
     */
    public static void appendGuest(Guest guest) {
        List<Guest> guests = readGuests();
        guests.add(guest);
        writeGuests(guests);
    }

    /**
     * Update data tamu berdasarkan guestId.
     *
     * @param updatedGuest objek Guest dengan data terbaru
     */
    public static void updateGuest(Guest updatedGuest) {
        List<Guest> guests = readGuests();
        for (int i = 0; i < guests.size(); i++) {
            if (guests.get(i).getId().equals(updatedGuest.getId())) {
                guests.set(i, updatedGuest);
                break;
            }
        }
        writeGuests(guests);
    }

    // ══════════════════════════════════════════════════════════
    //  RESERVATIONS
    // ══════════════════════════════════════════════════════════

    /**
     * Baca semua data reservasi dari reservations.csv.
     * Membutuhkan daftar rooms dan guests yang sudah di-load
     * untuk meresolve referensi objek.
     *
     * @param rooms  List Room yang sudah di-load
     * @param guests List Guest yang sudah di-load
     * @return List berisi semua objek Reservation
     */
    public static List<Reservation> readReservations(List<Room> rooms, List<Guest> guests) {
        List<Reservation> reservations = new ArrayList<>();
        File file = new File(RESERVATIONS_FILE);

        if (!file.exists()) {
            return reservations;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(SEPARATOR);
                if (data.length < 7) continue;

                String reservationId = data[0].trim();
                String guestId       = data[1].trim();
                String roomId        = data[2].trim();
                LocalDate checkIn    = LocalDate.parse(data[3].trim());
                LocalDate checkOut   = LocalDate.parse(data[4].trim());
                String keyCardId     = data[5].trim();
                String statusStr     = data[6].trim();

                // Resolve referensi Guest
                Guest guest = guests.stream()
                        .filter(g -> g.getId().equals(guestId))
                        .findFirst().orElse(null);

                // Resolve referensi Room
                Room room = rooms.stream()
                        .filter(r -> r.getRoomId().equals(roomId))
                        .findFirst().orElse(null);

                if (guest == null || room == null) continue;

                Reservation reservation = new Reservation(
                        reservationId, guest, room, checkIn, checkOut);

                if (!keyCardId.equals("-")) {
                    reservation.setKeyCardId(keyCardId);
                }

                try {
                    reservation.setStatus(
                        Reservation.ReservationStatus.valueOf(statusStr));
                } catch (IllegalArgumentException e) {
                    reservation.setStatus(Reservation.ReservationStatus.RESERVED);
                }

                reservations.add(reservation);
            }
        } catch (IOException e) {
            System.err.println("Error membaca reservations.csv: " + e.getMessage());
        }

        return reservations;
    }

    /**
     * Tulis ulang seluruh data reservasi ke reservations.csv.
     *
     * @param reservations List Reservation yang akan ditulis
     */
    public static void writeReservations(List<Reservation> reservations) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RESERVATIONS_FILE))) {
            bw.write("reservationId,guestId,roomId,checkInDate,checkOutDate,keyCardId,status,totalPrice");
            bw.newLine();
            for (Reservation r : reservations) {
                bw.write(String.join(SEPARATOR,
                    r.getReservationId(),
                    r.getGuest().getId(),
                    r.getRoom().getRoomId(),
                    r.getCheckInDate().toString(),
                    r.getCheckOutDate().toString(),
                    r.getKeyCardId() != null ? r.getKeyCardId() : "-",
                    r.getStatus().name(),
                    String.valueOf(r.getSubtotal())
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error menulis reservations.csv: " + e.getMessage());
        }
    }

    /**
     * Tambah satu reservasi baru ke reservations.csv.
     *
     * @param reservation objek Reservation yang akan ditambahkan
     */
    public static void appendReservation(Reservation reservation) {
        List<Room> rooms   = readRooms();
        List<Guest> guests = readGuests();
        List<Reservation> reservations = readReservations(rooms, guests);
        reservations.add(reservation);
        writeReservations(reservations);
    }

    /**
     * Update data reservasi berdasarkan reservationId.
     *
     * @param updated objek Reservation dengan data terbaru
     */
    public static void updateReservation(Reservation updated) {
        List<Room> rooms   = readRooms();
        List<Guest> guests = readGuests();
        List<Reservation> reservations = readReservations(rooms, guests);
        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getReservationId().equals(updated.getReservationId())) {
                reservations.set(i, updated);
                break;
            }
        }
        writeReservations(reservations);
    }

    // ══════════════════════════════════════════════════════════
    //  UTILITY
    // ══════════════════════════════════════════════════════════

    /**
     * Pastikan semua file CSV dan folder data/ sudah ada.
     * Dipanggil sekali saat aplikasi pertama kali dijalankan.
     */
    public static void initializeFiles() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        createIfNotExists(ROOMS_FILE,
            "roomId,roomNumber,type,floor,pricePerNight,status");
        createIfNotExists(GUESTS_FILE,
            "guestId,name,phone,email,idCard,preferences,loyaltyPoints");
        createIfNotExists(RESERVATIONS_FILE,
            "reservationId,guestId,roomId,checkInDate,checkOutDate,keyCardId,status,totalPrice");
    }

    /**
     * Buat file CSV baru dengan header jika belum ada.
     *
     * @param path   path file CSV
     * @param header baris header CSV
     */
    private static void createIfNotExists(String path, String header) {
        File file = new File(path);
        if (!file.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(header);
                bw.newLine();
            } catch (IOException e) {
                System.err.println("Error membuat file " + path + ": " + e.getMessage());
            }
        }
    }
}