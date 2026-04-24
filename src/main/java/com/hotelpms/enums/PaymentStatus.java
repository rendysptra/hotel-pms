/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.hotelpms.enums;
 
/**
 * Enum yang merepresentasikan status pembayaran reservasi.
 * Digunakan oleh BillingService dan Reservation.
 *
 * @author Rendy & Panji
 * @version 1.0
 */
public enum PaymentStatus {
 
    /** Belum ada pembayaran */
    UNPAID("Belum Dibayar"),
 
    /** Pembayaran sebagian / uang muka */
    PARTIAL("Bayar Sebagian"),
 
    /** Pembayaran lunas */
    PAID("Lunas"),
 
    /** Dana dikembalikan setelah pembatalan */
    REFUNDED("Dana Dikembalikan");
 
    // ─── Atribut ─────────────────────────────────────────────
    private final String displayName;
 
    // ─── Constructor ─────────────────────────────────────────
    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
 
    // ─── Getter ──────────────────────────────────────────────
    public String getDisplayName() {
        return displayName;
    }
 
    // ─── toString ────────────────────────────────────────────
    @Override
    public String toString() {
        return displayName;
    }
}
