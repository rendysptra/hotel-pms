/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.model;

import java.time.LocalDate;

/**
 * Merepresentasikan kamar tipe Deluxe.
 * Harga: Rp 600.000 per malam.
 *
 * @author rendysaptra
 */
public class DeluxeRoom extends Room {
    
    // Konstanta
    public static final double PRICE_PER_NIGHT = 600_000;
    
    // Constructor
    public DeluxeRoom (String roomId, String roomNumber, int floor) {
        super(roomId, roomNumber, floor, PRICE_PER_NIGHT);
    }
    
    // Abstract Method Implementation
    @Override
    public String getRoomType(){
        return "DELUXE";
    }
    
    @Override
    public String getFacilities(){
        return "AC, TV 42\", WiFi, Kamar Mandi Dalam, Bathtub, Mini Bar";
    }
    
    // Bookable Implementation
    @Override
    public boolean isAvailable(LocalDate checkIn, LocalDate checkOut){
        return isAvailableForBooking();
    }
    
    @Override
    public String book(String guestId, LocalDate checkIn, LocalDate checkOut) {
        return String.format(
                "Kamar Deluxe %s dipesan oleh tamu %s | %s s/d %s", 
                getRoomNumber(), guestId, checkIn, checkOut);
    }
    
    @Override
    public String cancel(String reservationId) {
        return String.format(
                "Pemesanan %s untuk kamar Deluxe %s dibatalkan", 
                reservationId, getRoomNumber());
    }
    
    // toString
    @Override
    public String toString() {
        return String.format("[Deluxe] %s", super.toString());
    }
    
}
