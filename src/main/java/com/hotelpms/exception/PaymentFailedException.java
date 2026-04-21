/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.exception;

/**
 * Exception yang dilempar ketika terjadi kesalahan
 * dalam proses perhitungan atau pembayaran tagihan.
 *
 * @author Rendy & Panji
 * @version 1.0
 */
public class PaymentFailedException extends Exception {

    private final String reservationId;
    private final double amount;

    public PaymentFailedException(String reservationId, double amount) {
        super("Pembayaran gagal untuk reservasi " + reservationId
                + " sejumlah Rp " + String.format("%,.0f", amount));
        this.reservationId = reservationId;
        this.amount        = amount;
    }

    public PaymentFailedException(String reservationId, double amount, String message) {
        super(message);
        this.reservationId = reservationId;
        this.amount        = amount;
    }

    public String getReservationId() { return reservationId; }
    public double getAmount()        { return amount; }
}