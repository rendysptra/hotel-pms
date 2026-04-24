/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.exception;

/**
 * Exception yang dilempar ketika tamu tidak ditemukan
 * berdasarkan ID atau nama.
 *
 * @author Rendy & Panji
 * @version 1.0
 */
public class GuestNotFoundException extends Exception {

    private final String searchKey;

    public GuestNotFoundException(String searchKey) {
        super("Tamu dengan ID/nama '" + searchKey + "' tidak ditemukan.");
        this.searchKey = searchKey;
    }

    public GuestNotFoundException(String searchKey, String message) {
        super(message);
        this.searchKey = searchKey;
    }

    public String getSearchKey() {
        return searchKey;
    }
}
