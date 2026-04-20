/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.model;

/**
 * Abstract class yang merepresentasikan semua staff hotel.
 * Extends Person dan menjadi superclass dari Receptionist dan Manager.
 * 
 * @author rendysaptra
 */
public abstract class Staff extends Person {
    
    // Atribut
    private String staffId;
    private String position;
    private String shift;
    
    // Constructor
    public Staff (String id, String name, String phone, String email, 
            String staffId, String position, String shift){
        super(id, name, phone, email);
        this.staffId = staffId;
        this.position = position;
        this.shift = shift;
    }
    
    /**
     * Setiap subclass wajib menimplementasikan tugas utama nya
     * 
     * @return String deskripsi tugas utama staff
     */
    public abstract String getMainDuty();
    
    // Implement dari Person
    @Override
    public String getDetails(){
        return String.format(
                "Staff | ID: %s | Nama: %s | Posisi: %s | Shift: %s", 
                getId(), getName(), position, shift);
    }
    
    // Getter & Setter
    public String getStaffId(){
        return staffId;
    }
    public void setStaffId(String staffId){
        this.staffId = staffId;
    }
    
    public String getPosition(){
        return position;
    }
    public void setPosition(String position){
        this.position = position;
    }
    
    public String getShift(){
        return shift;
    }
    public void setShift(String shift){
        this.shift = shift;
    }
    
    // toString
    @Override
    public String toString(){
        return String.format(
                "[%s] %s | %s | Shift: %s", 
                staffId, getName(), position, shift);
    }
}
