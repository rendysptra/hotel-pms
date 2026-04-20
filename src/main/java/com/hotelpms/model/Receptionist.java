/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Merepresentasikan receptionist hotel.
 * Extends Staff dan bertanggung jawab atas operasional
 * check-in, check-out, dan penerbitan key card.
 * 
 * @author rendysaptra
 */
public class Receptionist extends Staff {
    
    // Atribut
    private int totalCheckInsHandled;
    private int totalCheckOutsHandled;
    
    // Constructor
    public Receptionist (String id, String name, String phone, String email, String staffId, String shift){
        super(id, name, phone, email, staffId, "Receptionist", shift);
        this.totalCheckInsHandled = 0;
        this.totalCheckOutsHandled = 0;
    }
    
    // Implementasi Abstract Method Dari Staff
    @Override
    public String getMainDuty(){
        return "Menangani check-in, check-out, dan penerbitan keycard tamu";
    }
    
    /**
    * Proses check-in tamu dan terbitkan key card.
    * Mencatat jumlah check-in yang ditangani receptionist ini.
    *
    * @param reservationId ID reservasi yang akan di-check-in
    * @return String konfirmasi check-in berhasil
    */
    public String prosessCheckIn(String reservationId){
        totalCheckInsHandled++;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        return String.format(
                "Check-in di proses oleh %s | Reservasi: %s | Waktu: %s", 
                getName(), reservationId, timestamp);
    }
    
    /**
    * Proses check-out tamu.
    * Mencatat jumlah check-out yang ditangani receptionist ini.
    *
    * @param reservationId ID reservasi yang akan di-check-out
    * @return String konfirmasi check-out berhasil
    */
    public String prosessCheckOut(String reservationId){
        totalCheckOutsHandled++;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        return String.format(
                "Check-out di proses oleh %s | Reservasi: %s | Waktu: %s", 
                getName(), reservationId, timestamp);
    }
    
    /**
    * Generate key card ID unik berdasarkan nomor kamar dan tanggal.
    *
    * @param roomNumber nomor kamar
    * @return String key card ID dengan format KC-YYYYMMDD-roomNumber
    */
    public String issueKeyCard(String roomNumber){
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("KC-%s-%s", date, roomNumber);
    }
    
    // Getter & Setter
    public int getTotalCheckInsHandled(){
        return totalCheckInsHandled;
    }
    
    public int getTotalCheckOutsHandled(){
        return totalCheckOutsHandled;
    }
    
    // toString
    @Override
    public String toString(){
        return String.format(
                "[%s] %s | Receptionist | Shift: %s | CI: %d | CO: %d", 
                getStaffId(), getName(), getShift(), totalCheckInsHandled, totalCheckOutsHandled);
    }
}
