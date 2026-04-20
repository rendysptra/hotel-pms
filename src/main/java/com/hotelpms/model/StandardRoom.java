/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.model;

import java.time.LocalDate;

/**
 * Merepresentasikan kamar tipe Standard.
 * Harga: Rp 300.000 per malam.
 *
 * @author rendysaptra
 */
public class StandardRoom extends Room{
    
    // Konstanta
    public static final double PRICE_PER_NIGHT = 300_000;
    
    // Constructor
    public StandardRoom (String roomId, String roomNumber, int floor) {
        super(roomId, roomNumber, floor, PRICE_PER_NIGHT);
    }
    
    // Abstract Method Implementation
    @Override
    public String getRoomType(){
        return "STANDARD";
    }
    
    @Override
    public String getFacilities(){
        return "AC, TV, WiFi, Kamar Mandi Dalam";
    }
    
    // Bookable Implementation
    @Override
    public boolean isAvailable(LocalDate checkIn, LocalDate checkOut){
        return isAvailableForBooking();
    }
    
    @Override
    public String book(String guestId, LocalDate checkIn, LocalDate checkOut) {
        return String.format(
                "Kamar Standard %s dipesan oleh tamu %s | %s s/d %s", 
                getRoomNumber(), guestId, checkIn, checkOut);
    }
    
    @Override
    public String cancel(String reservationId) {
        return String.format(
                "Pemesanan %s untuk kamar Standard %s dibatalkan", 
                reservationId, getRoomNumber());
    }
    
    // toString
    @Override
    public String toString() {
        return String.format("[Standard] %s", super.toString());
    }
}
