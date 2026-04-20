/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.hotelpms.interfaces;

import java.time.LocalDate;

/**
 * Interface yang mendefinisikan kontrak pemesanan kamar hotel.
 * Diimplementasikan oleh class Room dan seluruh subtype-nya.
 *
 * @author rendysaptra
 */
public interface Bookable {
    
    /**
    * Cek ketersediaan kamar pada rentang tanggal tertentu.
    *
    * @param checkIn  tanggal check-in
    * @param checkOut tanggal check-out
    * @return true jika kamar tersedia
    */
    boolean isAvailable(LocalDate checkIn, LocalDate checkOut);
    
    /**
    * Lakukan pemesanan kamar.
    *
    * @param guestId  ID tamu yang memesan
    * @param checkIn  tanggal check-in
    * @param checkOut tanggal check-out
    * @return String konfirmasi pemesanan berhasil
    */
    String book(String guestId, LocalDate checkIn, LocalDate checkOut);
    
    /**
    * Batalkan pemesanan kamar.
    *
    * @param reservationId ID reservasi yang akan dibatalkan
    * @return String konfirmasi pembatalan berhasil
    */
    String cancel(String reservationId);
}
