/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.hotelpms.enums.RoomStatus;
import com.hotelpms.exception.GuestNotFoundException;
import com.hotelpms.exception.InvalidDateException;
import com.hotelpms.exception.KeyCardException;
import com.hotelpms.exception.RoomUnavailableException;
import com.hotelpms.model.Guest;
import com.hotelpms.model.Reservation;
import com.hotelpms.model.Room;
import com.hotelpms.util.CSVHandler;

/**
 *
 * Service utama Hotel PMS.
 * Mengorkestrasi semua operasi Hotel.
 * 
 * @author rendysaptra
 */
public class HotelService {
    
    // Atribut
    private List<Room> rooms;
    private List<Guest> guests;
    private List<Reservation> reservations;

    private final BillingService billingService;
    private final HousekeepingService housekeepingService;

    // Constructor
    public HotelService() {
        CSVHandler.initializeFiles();
        this.rooms = CSVHandler.readRooms();
        this.guests = CSVHandler.readGuests();
        this.reservations = CSVHandler.readReservations(rooms, guests);

        this.billingService = new BillingService();
        this.housekeepingService = new HousekeepingService(rooms);
    }

    // ══════════════════════════════════════════════════════════
    // Guest Management
    // ══════════════════════════════════════════════════════════
    /**
     * 
     * Tambah tamu baru ke sistem dan simpan ke CSV.
     * 
     * @param guest objek Guest yang akan ditambahkan
     */
    public void addGuest(Guest guest) {
        guests.add(guest);
        CSVHandler.writeGuests(guests);
        log("Tamu baru ditambahkan: " + guest.getName());
    }

    /**
     * 
     * Cari tamu berdasarkan ID.
     * 
     * @param guestId ID tamu yang dicari
     * @return objek Guest
     * @throws GuestNotFoundException jika tamu tidak ditemukan
     */
    public Guest findGuestById(String guestId) throws GuestNotFoundException {
        return guests.stream()
                .filter(g -> g.getId().equals(guestId))
                .findFirst()
                .orElseThrow(() -> new GuestNotFoundException(guestId));
    }

    /**
     * cari tamu berdasarkan nama(case-insensitive, partial match).
     * 
     * @param name nama atau sebagian nama tamu yang dicari.
     * @return List tamu yang cocok dengan kriteria pencarian.
     */
    public List<Guest> findGuestsByName(String name) {
        String keyword = name.toLowerCase();
        return guests.stream()
                .filter(g -> g.getName().toLowerCase().contains(keyword))
                .collect(Collectors.toList());
    }

    /**
     * Update data tamu dan simpan ke CSV.
     * 
     * @param guest objek Guest dengan data baru.
     */
    public void updateGuest(Guest guest) {
        for (int i = 0; i < guests.size(); i++) {
            if (guests.get(i).getId().equals(guest.getId())) {
                guests.set(i, guest);
            }
        }
        CSVHandler.updateGuest(guest);
        log("Data tamu diperbarui: " + guest.getName());
    }

    // ══════════════════════════════════════════════════════════
    // ROOM MANAGEMENT
    // ══════════════════════════════════════════════════════════
    /**
     * Tambah kamar baru ke sistem dan simpan ke CSV.
     * 
     * @param room objek Room yang akan ditambahkan
    */
   public void addRoom(Room room) {
        rooms.add(room);
        CSVHandler.writeRooms(rooms);
        log("Kamar baru ditambahkan: " + room.getRoomNumber());
    }

    /**
     * Cari kamar tersedia berdasarkan tipe dan rentang tanggal.
     * 
     * @param roomType tipe kamar: STANDARD, DELUXE, atau VIP
     * @param checkIn tanggal check-in
     * @param checkOut tanggal check-out
     * @return List kamar tersedia
     */
    public List<Room> findAvailableRooms(String roomType, LocalDate checkIn, LocalDate checkOut) {
        List<Room> available = new ArrayList<>();
        for (Room room : rooms) {
            if(!room.getRoomType().equalsIgnoreCase(roomType)) continue;
            if(room.getStatus() != RoomStatus.CLEAN) continue;
            if(!isRoomAvailable(room, checkIn, checkOut)) continue;
            available.add(room);
        }
        return available;
    }

    /**
     * Cek apakah kamar tersedia untuk rentang tanggal tertentu.
     * Memvalidasi tidak ada reservasi yang tumpang tindih.
     * 
     * @param room kamar yang akan dicek
     * @param checkIn tanggal check-in
     * @param checkOut tanggal check-out
     * @return true jika kamar tersedia.
     */
    public boolean isRoomAvailable(Room room, LocalDate checkIn, LocalDate checkOut) {
        for (Reservation r : reservations) {
            if(!r.getRoom().getRoomId().equals(room.getRoomId())) continue;
            if(r.getStatus() == Reservation.ReservationStatus.CANCELLED) continue;
            if(r.getStatus() == Reservation.ReservationStatus.CHECKED_OUT) continue;

            // Cek tumpang tindih tanggal
            boolean overlap = checkIn.isBefore(r.getCheckOutDate())
                    && checkOut.isAfter(r.getCheckInDate());
            if (overlap) return false;
        }
        return true;
    }

    // ══════════════════════════════════════════════════════════
    // BOOKING MANAGEMENT
    // ══════════════════════════════════════════════════════════
    /** 
     * Buat reservasi baru dengan auto room assignment.
     * 
     * @param guest tamu yang memesan
     * @param roomType tipe kamar yang diinginkan
     * @param checkIn tanggal check-in
     * @param checkOut tanggal check-out
     * @return objek Reservation yang dibuat
     * @throws InvalidDateException jika tanggal tidak valid
     * @throws RoomUnavailableException jika tidak ada kamar yang tersedia
     */
    public Reservation createReservation(Guest guest, String roomType, LocalDate checkIn, LocalDate checkOut) throws InvalidDateException, RoomUnavailableException {

        // Validasi tanggal
        if (checkIn == null || checkOut == null){
            throw new InvalidDateException("Tanggal tidak boleh kosong");
        }
        if  (!checkOut.isAfter(checkIn)){
            throw new InvalidDateException(checkIn, checkOut);
        }
        if (checkIn.isBefore(LocalDate.now())){
            throw new InvalidDateException("Tanggal check-in tidak boleh di masa lalu");
        }

        // Cari kamar tersedia
        List<Room> available = findAvailableRooms(roomType, checkIn, checkOut);
        if (available.isEmpty()) {
            throw new RoomUnavailableException(roomType, "Tidak ada kamar " + roomType + " tersedia pada tanggal tersebut");
        }

        // assign kamar pertama yang tersedia
        Room assignedRoom = available.get(0);

        // generate ID reservasi
        String reservationId = generateReservationId();

        // buat reservasi
        Reservation reservation = new Reservation(reservationId, guest, assignedRoom, checkIn, checkOut);

        // update status kamar
        assignedRoom.setStatus(RoomStatus.RESERVED);
        housekeepingService.markReserved(assignedRoom.getRoomNumber());

        // Tambah ke riwayat tamu
        guest.addReservationHistory(reservationId);
        CSVHandler.updateGuest(guest);

        // simpan reservasi 
        reservations.add(reservation);
        CSVHandler.appendReservation(reservation);

        log("Reservasi baru " + reservationId + " | Tamu: " + guest.getName() + " | Kamar: " + assignedRoom.getRoomNumber());
        return reservation;
    }

    /** 
     * Ubah tanggal reservasi yang sudah ada.
     * 
     * @param reservationId ID reservasi yang akan diubah
     * @param newCheckIn tanggal check-in baru
     * @param newCheckOut tanggal check-out baru
     * @throws InvalidDateException jika tanggal tidak valid
     * @throws RoomUnavailableException jika kamar tidak tersedia untuk tanggal baru
     */
    public void modifyReservation(String reservationId, LocalDate newCheckIn, LocalDate newCheckOut) throws InvalidDateException, RoomUnavailableException {
        Reservation reservation = findReservationById(reservationId);

        if(reservation.getStatus() != Reservation.ReservationStatus.RESERVED){
            throw new InvalidDateException("Reservasi hanya bisa diubah jika masih berstatus RESERVED");
        }
        if(!newCheckOut.isAfter(newCheckIn)) {
            throw new InvalidDateException(newCheckIn, newCheckOut);
        }

        // Cek ketersediaan kamar di tanggal baru (exclude reservasi ini)
        Room room = reservation.getRoom();
        for (Reservation r : reservations) {
            if(r.getReservationId().equals(reservationId)) continue;
            if(!r.getRoom().getRoomId().equals(room.getRoomId())) continue;
            if(r.getStatus() == Reservation.ReservationStatus.CANCELLED) continue;
            if(r.getStatus() == Reservation.ReservationStatus.CHECKED_OUT) continue;

            boolean overlap = newCheckIn.isBefore(r.getCheckOutDate())
                    && newCheckOut.isAfter(r.getCheckInDate());
            if (overlap) {
                throw new RoomUnavailableException("Kamar tidak tersedia di tanggal yang baru dipilih");
            }
        }

        reservation.setCheckInDate(newCheckIn);
        reservation.setCheckOutDate(newCheckOut);
        CSVHandler.updateReservation(reservation);
        log("Reservasi " + reservationId + " diubah: " + newCheckIn + " s/d " + newCheckOut);
    }
    
    /** 
     * Batalkan reservasi
     * 
     * @param reservationId ID reservasi yang akan dibatalkan
     * @throws InvalidDateException jika reservasi sudah tidak bisa dibatalkan
     */
    public void cancelReservation(String reservationId) throws InvalidDateException {
        Reservation reservation = findReservationById(reservationId);

        if(reservation.getStatus() != Reservation.ReservationStatus.RESERVED){
            throw new InvalidDateException("Hanya reservasi berstatus RESERVED yang bisa dibatalkan");
        }

        reservation.cancel();

        // Kembalikan status kamar ke CLEAN
        Room room = reservation.getRoom();
        room.setStatus(RoomStatus.CLEAN);
        housekeepingService.markClean(room.getRoomNumber());

        CSVHandler.updateReservation(reservation);
        log("Reservasi " + reservationId + " dibatalkan");
    }

    // ══════════════════════════════════════════════════════════
    // Check-in & Check-out
    // ══════════════════════════════════════════════════════════
    /**
     * Proses check-in tamu
     * Menerbitkan keycard dan mengubah status kamar ke OCCUPIED.
     * 
     * @param reservationId ID reservasi yang akan check-in
     * @return key card ID yang diterbitkan
     * @throws KeyCardException jika penerbitan key card gagal
     * @throws RoomUnavailableException jika kamar tidak dalam status RESERVED
     */
    public String checkIn(String reservationId) throws KeyCardException, RoomUnavailableException {
        
        Reservation reservation = findReservationById(reservationId);

        if (reservation.getStatus() != Reservation.ReservationStatus.RESERVED) {
            throw new RoomUnavailableException(reservation.getRoom().getRoomNumber(),"Reservasi tidak dalam status RESERVED");
        }

        // Generate dan issue key card
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String keyCardId = String.format("KC-%s-%s", date, reservation.getRoom().getRoomNumber());

        if(keyCardId.isEmpty()) {
            throw new KeyCardException("Gagal generate key card");
        }

        reservation.issueKeyCard(keyCardId);
        reservation.checkIn();

        // Update status kamar ke OCCUPIED
        reservation.getRoom().setStatus(RoomStatus.OCCUPIED);
        housekeepingService.markOccupied(reservation.getRoom().getRoomNumber());

        // Tambah biaya kamar ke folio
        billingService.addRoomCharge(reservation);

        CSVHandler.updateReservation(reservation);
        log("Check-in: " + reservationId + " | KeyCard: " + keyCardId + " | Kamar: " + reservation.getRoom().getRoomNumber());

        return keyCardId;
    }

    /** 
     * Proses check-out tamu
     * Trigger auto-dirty pada kamar dan finalkan tagihan.
     * 
     * @param reservationId ID reservasi
     * @return total tagihan termasuk pajak
     * @throws Exeception jika checkout gagal
     */
    public double checkOut(String reservationId) throws Exception {
        Reservation reservation = findReservationById(reservationId);

        if (reservation.getStatus() != Reservation.ReservationStatus.CHECKED_IN) {
            throw new Exception("Reservasi tidak dalam status CHECKED_IN");
        }

        // Hitung total tagihan
        double total = billingService.calculateTotal(reservation);

        // Eksekusi check-out
        reservation.checkOut();

        // Auto dirty kamar setelah check-out
        housekeepingService.autoDirtyOnCheckout(reservation.getRoom());

        // Tambah loyalty points (1 point per Rp 100.000)
        int points = (int) (total / 100000);
        reservation.getGuest().addLoyaltyPoints(points);
        CSVHandler.updateGuest(reservation.getGuest());

        // Cetak folio ke filereservation
        billingService.printFolioToFile(reservation);
        log("Check-out: " + reservationId + " | Total: Rp " + String.format("%,.0f", total) + " | Poin: " + points);

        return total;
    }

    // ══════════════════════════════════════════════════════════
    // FIND / QUERY
    // ══════════════════════════════════════════════════════════
    /** 
     * Cari reservasi berdasarkan ID.
     * 
     * @param reservationId ID reservasi
     * @return objek Reservation
     * @throws IllegalArgumentException jika tidak ditemukan
     */
    public Reservation findReservationById(String reservationId) {
        return reservations.stream()
                .filter(r -> r.getReservationId().equals(reservationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reservasi tidak ditemukan: " + reservationId));
    }

    /**
     * Ambil semua reservasi yang masih aktif (RESERVED atau CHECKED_IN).
     * 
     * @return List reservasi aktif
     */
    public List<Reservation> getActiveReservations() {
        return reservations.stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.RESERVED || r.getStatus() == Reservation.ReservationStatus.CHECKED_IN)
                .collect(Collectors.toList());
    }

    /**
     * Ambil semua reservasi milik satu tamu
     * 
     * @param guestId ID tamu
     * @return List reservasi milik tamu
     */
    public List<Reservation> getReservationsByGuest(String guestId) {
        return reservations.stream()
                .filter(r -> r.getGuest().getId().equals(guestId))
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════
    // Utility
    // ══════════════════════════════════════════════════════════
    /**
     * Generate ID reservasi unik
     * Format: RSV-YYYYMMDD-XXX (XXX = urutan hari ini)
     * 
     * @return String reservation ID
     */
    private String generateReservationId() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = reservations.stream()
                .filter(r -> r.getReservationId().contains(date))
                .count();
        return String.format("RSV-%s-%03d", date, count + 1);
    }

    /**
     * Refresh semua data dari CSV
     * Dipanggil untuk sync data baru
     */
    public void refreshData() {
        this.rooms = CSVHandler.readRooms();
        this.guests = CSVHandler.readGuests();
        this.reservations = CSVHandler.readReservations(rooms, guests);
        housekeepingService.refreshRooms();
        log("Data di-refresh dari CSV");
    }

    /**
     * Log aktivitas dengan timestamp
     * 
     * @param message pesan yang akan di-log
     */
    private void log(String message) {
        String timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        System.out.println("[HOTEL SERVICE " + timestamp + "] " + message);
    }

    // Getter
    public List<Room> getRooms() {
        return rooms;
    }

    public List<Guest> getGuests() {
        return guests;
    } 

    public List<Reservation> getReservations() {
        return reservations;
    }

    public BillingService getBillingService() {
        return billingService;
    }

    public HousekeepingService getHousekeepingService() {
        return housekeepingService;
    }
}
