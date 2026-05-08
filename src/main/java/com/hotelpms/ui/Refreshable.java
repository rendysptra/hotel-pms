/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.ui;

/**
 * Interface untuk komponen yang dapat di-refresh
 * Saat panel dibuka di sidebar
 *
 * @author rendysaptra
 */
public interface Refreshable {
    
    /**
     * Refresh data panel dari HotelService.
     * Dipanggil otomatis oleh MainFrame saat panel dibuka.
     */
    void refresh();
}
