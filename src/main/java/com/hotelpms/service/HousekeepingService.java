/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.service;

import com.hotelpms.enums.RoomStatus;
import com.hotelpms.model.Room;
import com.hotelpms.util.CSVHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service untuk menangani operasional housekeeping hotel.
 * Mengelola status kamar secara real-time, antrian pembersihan,
 * dan workflow auto-dirty setelah checkout.
 *
 * @author Rendy & Panji
 * @version 1.0
 */
public class HousekeepingService {

    // ─── Atribut ─────────────────────────────────────────────
    private List<Room> rooms;

    // ─── Constructor ─────────────────────────────────────────
    public HousekeepingService() {
        this.rooms = CSVHandler.readRooms();
    }

    public HousekeepingService(List<Room> rooms) {
        this.rooms = rooms;
    }

    // ══════════════════════════════════════════════════════════
    //  WORKFLOW UTAMA
    // ══════════════════════════════════════════════════════════

    /**
     * Auto-set status kamar menjadi DIRTY setelah tamu checkout.
     * Dipanggil otomatis oleh HotelService saat checkout.
     *
     * @param room kamar yang baru saja ditinggalkan tamu
     */
    public void autoDirtyOnCheckout(Room room) {
        if (room == null) return;
        room.setStatus(RoomStatus.DIRTY);
        CSVHandler.updateRoomStatus(room.getRoomId(), RoomStatus.DIRTY);
        log("Kamar " + room.getRoomNumber() + " otomatis DIRTY setelah checkout");
    }

    /**
     * Tandai kamar sudah dibersihkan — ubah status ke CLEAN.
     * Dipanggil oleh staff housekeeping setelah selesai membersihkan.
     *
     * @param roomNumber nomor kamar yang sudah dibersihkan
     * @return true jika berhasil, false jika kamar tidak ditemukan
     */
    public boolean markClean(String roomNumber) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null) {
            log("Kamar " + roomNumber + " tidak ditemukan");
            return false;
        }
        room.setStatus(RoomStatus.CLEAN);
        CSVHandler.updateRoomStatus(room.getRoomId(), RoomStatus.CLEAN);
        log("Kamar " + roomNumber + " sudah CLEAN dan siap dipesan");
        return true;
    }

    /**
     * Tandai kamar tidak bisa digunakan — ubah status ke OUT_OF_ORDER.
     * Digunakan saat kamar sedang dalam perbaikan atau maintenance.
     *
     * @param roomNumber nomor kamar
     * @param reason     alasan kamar tidak bisa digunakan
     * @return true jika berhasil
     */
    public boolean markOutOfOrder(String roomNumber, String reason) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null) {
            log("Kamar " + roomNumber + " tidak ditemukan");
            return false;
        }
        room.setStatus(RoomStatus.OUT_OF_ORDER);
        CSVHandler.updateRoomStatus(room.getRoomId(), RoomStatus.OUT_OF_ORDER);
        log("Kamar " + roomNumber + " OUT OF ORDER — " + reason);
        return true;
    }

    /**
     * Set status kamar ke OCCUPIED saat tamu check-in.
     *
     * @param roomNumber nomor kamar
     * @return true jika berhasil
     */
    public boolean markOccupied(String roomNumber) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null) return false;
        room.setStatus(RoomStatus.OCCUPIED);
        CSVHandler.updateRoomStatus(room.getRoomId(), RoomStatus.OCCUPIED);
        log("Kamar " + roomNumber + " OCCUPIED");
        return true;
    }

    /**
     * Set status kamar ke RESERVED saat reservasi dibuat.
     *
     * @param roomNumber nomor kamar
     * @return true jika berhasil
     */
    public boolean markReserved(String roomNumber) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null) return false;
        room.setStatus(RoomStatus.RESERVED);
        CSVHandler.updateRoomStatus(room.getRoomId(), RoomStatus.RESERVED);
        log("Kamar " + roomNumber + " RESERVED");
        return true;
    }

    // ══════════════════════════════════════════════════════════
    //  ANTRIAN & QUERY
    // ══════════════════════════════════════════════════════════

    /**
     * Ambil daftar kamar yang perlu dibersihkan (status DIRTY).
     *
     * @return List kamar dengan status DIRTY
     */
    public List<Room> getDirtyQueue() {
        List<Room> dirtyRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (room.getStatus() == RoomStatus.DIRTY) {
                dirtyRooms.add(room);
            }
        }
        return dirtyRooms;
    }

    /**
     * Ambil daftar kamar yang sedang OUT_OF_ORDER.
     *
     * @return List kamar dengan status OUT_OF_ORDER
     */
    public List<Room> getOutOfOrderRooms() {
        List<Room> oooRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (room.getStatus() == RoomStatus.OUT_OF_ORDER) {
                oooRooms.add(room);
            }
        }
        return oooRooms;
    }

    /**
     * Ambil daftar kamar yang tersedia (status CLEAN).
     *
     * @return List kamar dengan status CLEAN
     */
    public List<Room> getAvailableRooms() {
        List<Room> available = new ArrayList<>();
        for (Room room : rooms) {
            if (room.getStatus() == RoomStatus.CLEAN) {
                available.add(room);
            }
        }
        return available;
    }

    /**
     * Ambil ringkasan status semua kamar.
     *
     * @return String ringkasan jumlah kamar per status
     */
    public String getRoomStatusSummary() {
        int clean      = 0, dirty    = 0;
        int occupied   = 0, reserved = 0, outOfOrder = 0;

        for (Room room : rooms) {
            switch (room.getStatus()) {
                case CLEAN       -> clean++;
                case DIRTY       -> dirty++;
                case OCCUPIED    -> occupied++;
                case RESERVED    -> reserved++;
                case OUT_OF_ORDER-> outOfOrder++;
            }
        }

        return String.format("""
                             === STATUS KAMAR ===
                             Bersih (CLEAN)       : %d kamar
                             Kotor (DIRTY)        : %d kamar
                             Terisi (OCCUPIED)    : %d kamar
                             Dipesan (RESERVED)   : %d kamar
                             Out of Order         : %d kamar
                             \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500
                             Total                : %d kamar
                             """,
            clean, dirty, occupied, reserved, outOfOrder,
            rooms.size()
        );
    }

    // ══════════════════════════════════════════════════════════
    //  UTILITY
    // ══════════════════════════════════════════════════════════

    /**
     * Cari kamar berdasarkan nomor kamar.
     *
     * @param roomNumber nomor kamar yang dicari
     * @return Room jika ditemukan, null jika tidak
     */
    private Room findRoomByNumber(String roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber().equals(roomNumber)) {
                return room;
            }
        }
        return null;
    }

    /**
     * Refresh data kamar dari CSV.
     * Dipanggil untuk sync data terbaru.
     */
    public void refreshRooms() {
        this.rooms = CSVHandler.readRooms();
    }

    /**
     * Log aktivitas housekeeping dengan timestamp.
     *
     * @param message pesan yang akan di-log
     */
    private void log(String message) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        System.out.println("[HOUSEKEEPING " + timestamp + "] " + message);
    }

    // ─── Getter ──────────────────────────────────────────────
    public List<Room> getRooms() {
        return rooms;
    }
}