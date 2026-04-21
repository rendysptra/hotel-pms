/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.exception;
 
/**
 * Exception yang dilempar ketika kamar tidak tersedia
 * pada tanggal yang diminta.
 *
 * @author Rendy & Panji
 * @version 1.0
 */
public class RoomUnavailableException extends Exception {
 
    private final String roomNumber;
 
    public RoomUnavailableException(String roomNumber) {
        super("Kamar " + roomNumber + " tidak tersedia pada tanggal yang dipilih.");
        this.roomNumber = roomNumber;
    }
 
    public RoomUnavailableException(String roomNumber, String message) {
        super(message);
        this.roomNumber = roomNumber;
    }
 
    public String getRoomNumber() {
        return roomNumber;
    }
}
 
