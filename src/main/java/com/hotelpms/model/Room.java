/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.model;

import com.hotelpms.enums.RoomStatus;
import com.hotelpms.interfaces.Bookable;

/**
 * Abstract class yang merepresentasikan kamar hotel.
 * Superclass dari StandardRoom, DeluxeRoom, dan VIPRoom.
 * Mengimplementasikan interface Bookable.
 *
 * @author rendysaptra
 */
public abstract class Room implements Bookable {
    
    // Atribut
    private String roomId;
    private String roomNumber;
    private int floor;
    private double pricePerNight;
    private RoomStatus status;
    
    // Constructor
    public Room (String roomId, String roomNumber, int floor, double pricePerNight) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.pricePerNight = pricePerNight;
        this.status = RoomStatus.CLEAN;
    }
    
    /**
    * Setiap subclass wajib mengembalikan tipe kamar.
    *
    * @return String tipe kamar: STANDARD, DELUXE, atau VIP
    */
    public abstract String getRoomType();
    
    /**
     * Setiap subclass wajib mengembalikan fasilitas kamar.
     *
     * @return String deskripsi fasilitas kamar
     */
    public abstract String getFacilities();
    
    /**
    * Cek apakah kamar tersedia untuk dipesan.
    * Kamar tersedia hanya jika statusnya CLEAN.
    *
    * @return true jika kamar tersedia
    */
    public boolean isAvailableForBooking() {
        return this.status == RoomStatus.CLEAN;
    }
    
    /**
    * Hitung total harga menginap berdasarkan jumlah malam.
    *
    * @param nights jumlah malam menginap
    * @return total harga sebelum pajak
    */
    public double calculateTotalPrice(int nights) {
        return pricePerNight * nights;
    }
    
    /**
    * Format harga per malam ke format Rupiah.
    *
    * @return String harga dalam format Rp xxx.xxx
    */
    public String getFormattedPrice() {
        return String.format("Rp. %,.0f / malam", pricePerNight);
    }
    
    // Getter & Setter
    public String getRoomId(){
        return roomId;
    }
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public String getRoomNumber() {
        return roomNumber;
    }
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
    
    public int getFloor() {
        return floor;
    }
    public void setFloor(int floor) {
        this.floor = floor;
    }
    
    public double getPricePerNight() {
        return pricePerNight;
    }
    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
    
    public RoomStatus getStatus() {
        return status;
    }
    public void setStatus(RoomStatus status) {
        this.status = status;
    }
    
    // toString
    @Override
    public String toString() {
        return String.format(
                "[%s] Kamar %s | Lantai %d | %s | %s | %s", 
                roomId, roomNumber, floor, getRoomType(), getFormattedPrice(), status.getDisplayName());
    }
}
