/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.exception;

import java.time.LocalDate;

/**
 * Exception yang dilempar ketika input tanggal tidak valid.
 * Contoh: tanggal check-out sebelum check-in,
 * atau tanggal yang sudah lewat.
 *
 * @author Rendy & Panji
 * @version 1.0
 */
public class InvalidDateException extends Exception {

    private final LocalDate checkIn;
    private final LocalDate checkOut;

    public InvalidDateException(LocalDate checkIn, LocalDate checkOut) {
        super("Tanggal tidak valid: check-in " + checkIn + " | check-out " + checkOut);
        this.checkIn  = checkIn;
        this.checkOut = checkOut;
    }

    public InvalidDateException(String message) {
        super(message);
        this.checkIn  = null;
        this.checkOut = null;
    }

    public LocalDate getCheckIn()  { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
}