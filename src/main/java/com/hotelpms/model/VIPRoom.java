/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.model;

import java.time.LocalDate;

/**
 * Merepresentasikan kamar tipe VIP.
 * Harga: Rp 1.200.000 per malam.
 *
 * @author rendysaptra
 */
public class VIPRoom extends Room {
    
    // Konstanta
    public static final double PRICE_PER_NIGHT = 600_000;
    
    // Atribut tambahan
    private boolean hasPrivatePool;
    private boolean hasButlerService;
    
    // Constructor
    public VIPRoom (String roomId, String roomNumber, int floor) {
        super(roomId, roomNumber, floor, PRICE_PER_NIGHT);
        this.hasPrivatePool = true;
        this.hasButlerService = true;
    }
    
    // Abstract Method Implementation
    @Override
    public String getRoomType(){
        return "VIP";
    }
    
    @Override
    public String getFacilities(){
        return "AC, TV 55\\\" 4K, WiFi, Kamar Mandi Dalam, Jacuzzi, "
                + "Mini Bar, Ruang Tamu, "
                + (hasPrivatePool    ? "Private Pool, " : "")
                + (hasButlerService  ? "Butler Service" : "");
    }
    
    // Bookable Implementation
    @Override
    public boolean isAvailable(LocalDate checkIn, LocalDate checkOut){
        return isAvailableForBooking();
    }
    
    @Override
    public String book(String guestId, LocalDate checkIn, LocalDate checkOut) {
        return String.format(
                "Kamar VIP %s dipesan oleh tamu %s | %s s/d %s", 
                getRoomNumber(), guestId, checkIn, checkOut);
    }
    
    @Override
    public String cancel(String reservationId) {
        return String.format(
                "Pemesanan %s untuk kamar VIP %s dibatalkan", 
                reservationId, getRoomNumber());
    }
    
    // Getter & Setter
    public boolean isHasPrivatePool(){
        return hasPrivatePool;
    }
    public void setHasPrivatePool(boolean hasPrivatePool){
        this.hasPrivatePool = hasPrivatePool;
    }
    
    public boolean isHasButlerService(){
        return hasButlerService;
    }
    public void setHasButlerService(boolean hasButlerService){
        this.hasButlerService = hasButlerService;
    }
    
    // toString
    @Override
    public String toString() {
        return String.format("[VIP] %s | Pool: %s | Butler: %s", 
                super.toString(),
                hasPrivatePool   ? "Ya" : "Tidak",
                hasButlerService ? "Ya" : "Tidak");
    }
    
}
