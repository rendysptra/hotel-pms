/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.exception;

/**
 * Exception yang dilempar ketika terjadi kesalahan
 * dalam penerbitan atau validasi key card.
 *
 * @author Rendy & Panji
 * @version 1.0
 */
public class KeyCardException extends Exception {

    private final String keyCardId;

    public KeyCardException(String keyCardId) {
        super("Gagal memproses key card: " + keyCardId);
        this.keyCardId = keyCardId;
    }

    public KeyCardException(String keyCardId, String message) {
        super(message);
        this.keyCardId = keyCardId;
    }

    public String getKeyCardId() {
        return keyCardId;
    }
}