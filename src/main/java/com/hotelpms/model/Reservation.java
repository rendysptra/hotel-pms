/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Merepresentasikan reservasi tamu hotel.
 * Menyimpan data pemesanan kamar, key card,
 * dan daftar tagihan itemized (folio).
 *
 * @author rendysaptra
 */
public class Reservation {
    
    // Enum ReservationStatus
    public enum ReservationStatus {
        RESERVED("Dipesan"),
        CHECKED_IN("Check-in"),
        CHECKED_OUT("Check-out"),
        CANCELLED("Dibatalkan");
        
        private final String displayName;
        
        ReservationStatus(String displayName){
            this.displayName = displayName;
        }
        
        public String getDisplayName(){
            return displayName;
        }
        
        @Override
        public String toString(){
            return displayName;
        }
    }
    
    // Atribut
    private String reservationId;
    private Guest guest;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String keyCardId;
    private ReservationStatus status;
    private final List<FolioItem> folioItems;
    
    // Constructor
    public Reservation(String reservationId, Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        this.reservationId = reservationId;
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.keyCardId = null;
        this.status = ReservationStatus.RESERVED;
        this.folioItems = new ArrayList<>();
    }
    
    // Hitung jumlah malam menginap
    public long getTotalNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }
    
    // Hitung subtotal semua FolioItem sebelum pajak
    public double getSubtotal(){
        return folioItems.stream()
                .mapToDouble(FolioItem::getAmount)
                .sum();
    }
    
    // Tambahkan item tagihan ke folio
    public void addFolioItem(FolioItem item) {
        if (item != null) {
            folioItems.add(item);
        }
    }
    
    // Terbitkan key card - hanya saat status RESERVED
    public void issueKeyCard(String keyCardId) {
        if (this.status == ReservationStatus.RESERVED) {
            this.keyCardId = keyCardId;
        }
    }
    
    // Eksekusi check-in
    public void checkIn() {
        if (this.status == ReservationStatus.RESERVED) {
            this.status = ReservationStatus.CHECKED_IN;
        }
    }
    
    // Eksekusi check-out
    public void checkOut() {
        if (this.status == ReservationStatus.CHECKED_IN) {
            this.status = ReservationStatus.CHECKED_OUT;
        }
    }
    
    // Batalkan reservasi
    public void cancel() {
        if (this.status == ReservationStatus.RESERVED) {
            this.status = ReservationStatus.CANCELLED;
        }
    }
    
    // Tampilkan ringkasan folio
    public String getFolioSummary(){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== FOLIO RESERVASI %s ===\n", reservationId));
        sb.append(String.format("Tamu  : %s\n", guest.getName()));
        sb.append(String.format("Kamar : %s (%s)\n", room.getRoomNumber(), room.getRoomType()));
        sb.append(String.format("CI/CO : %s s/d %s (%d malam)\n", checkInDate, checkOutDate, getTotalNights()));
        sb.append("---------------------------------\n");
        for (FolioItem item : folioItems) {
            sb.append(String.format("  %-30s %s\n", item.getDescription(), item.getFormattedAmount()));
        }
        sb.append("---------------------------------\n");
        sb.append(String.format("Subtotal : Rp %,.0f\n", getSubtotal()));
        return sb.toString();
    }
    
    // Getter & Setter
    public String getReservationId(){
        return reservationId;
    }
    public void setReservationId(String reservationId){
        this.reservationId = reservationId;
    }
    
    public Guest getGuest(){
        return guest;
    }
    public void setGuest(Guest guest){
        this.guest = guest;
    }
    
    public Room getRoom(){
        return room;
    }
    public void setRoom(Room room){
        this.room = room;
    }
    
    public LocalDate getCheckInDate(){
        return checkInDate;
    }
    public void setCheckInDate(LocalDate checkInDate){
        this.checkInDate = checkInDate;
    }
    
    public LocalDate getCheckOutDate(){
        return checkOutDate;
    }
    public void setCheckOutDate(LocalDate checkOutDate){
        this.checkOutDate = checkOutDate;
    }
    
    public String getKeyCardId(){
        return keyCardId;
    }
    public void setKeyCardId(String keyCardId){
        this.keyCardId = keyCardId;
    }
    
    public ReservationStatus getStatus(){
        return status;
    }
    public void setStatus(ReservationStatus status){
        this.status = status;
    }
    
    public List<FolioItem> getFolioItems(){
        return folioItems;
    }
    
    // toString
    @Override
    public String toString(){
        return String.format(
                "[%s] %s | Kamar %s | %s s/d %s | %s | KeyCard: %s", 
                reservationId,
                guest.getName(),
                room.getRoomNumber(),
                checkInDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                checkOutDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                status.getDisplayName(),
                keyCardId != null ? keyCardId : "Belum Diterbitkan"
        );
    }
}
