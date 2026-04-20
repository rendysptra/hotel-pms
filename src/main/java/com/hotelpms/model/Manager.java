/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Merepresentasikan manager hotel. 
 * Extends Staff dan bertanggung jawab atas
 * pelaporan, monitoring operasional, dan manajemen staff.
 * 
 * @author rendysaptra
 */
public class Manager extends Staff {
    
    // atribut
    private String department;
    private int totalReportsGenerated;
    
    // Constructor
    public Manager (String id, String nama, String phone, String email, String staffId, String department){
        super(id, nama, phone, email, staffId, "Manager", "FULL");
        this.department = department;
        this.totalReportsGenerated = 0;
    }
    
    // Abstact Method Implementation
    @Override
    public String getMainDuty(){
        return "Mengelola operasional hotel, monitoring performa, dan generating laporan";
    }
    
    /**
    * Generate laporan harian hotel.
    * Mencatat jumlah laporan yang sudah digenerate manager ini.
    *
    * @param reportType jenis laporan: OCCUPANCY, REVENUE, HOUSEKEEPING
    * @return String header laporan dengan timestamp
    */
    public String generateReport(String reportType){
        totalReportsGenerated++;
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        return String.format("=== LAPORAN %s ===\nTanggal : %s\nDibuat oleh : %s (%s)\n", 
                reportType.toUpperCase(), date, getName(), department);
    }
    
    /**
    * Approve perubahan status kamar yang dibuat housekeeping.
    *
    * @param roomNumber nomor kamar
    * @param newStatus  status baru kamar
    * @return String konfirmasi approval
    */
    public String approveRoomStatusChange(String roomNumber, String newStatus){
        return String.format(
                "Manager %s menyetujui perubahan status kamar %s menjadi %s", 
                getName(), roomNumber, newStatus);
    }
    
    // Getter & Setter
    public String getDepartment(){
        return department;
    }
    public void setDepartment(String department){
        this.department = department;
    }
    
    public int getTotalReportsGenerated(){
        return totalReportsGenerated;
    }
    
    // toString
    @Override
    public String toString(){
        return String.format(
                "[%s] %s | Manager | Dept: %s | Laporan: %d", 
                getStaffId(), getName(), department, totalReportsGenerated);
    }
}
