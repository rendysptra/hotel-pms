/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.hotelpms.enums;

/**
 * Enum yang merepresentasikan status kamar hotel.
 * Digunakan oleh class Room dan HousekeepingService.
 * 
 * @author rendysaptra
 */
public enum RoomStatus {
    
    // Kamar bersih dan siap dipesan
    CLEAN("Bersih"),
    
    // Kamar perlu dibersihkan — otomatis setelah checkout
    DIRTY("Perlu Dibersihkan"),
    
    // Tamu sedang menginap
    OCCUPIED("Terisi"),
    
    // Sudah dipesan, tamu belum check-in
    RESERVED("Dipesan"),
    
    // Kamar tidak dapat digunakan — sedang maintenance
    OUT_OF_ORDER("Tidak Tersedia");
    
    // Atribut
    private final String displayName;
    
    // Constructor
    RoomStatus(String displayName) {
        this.displayName = displayName;
    }
    
    // Getter
    public String getDisplayName() {
        return displayName;
    }
    
    // toString
    @Override
    public String toString() {
        return displayName;
    }
    
}
