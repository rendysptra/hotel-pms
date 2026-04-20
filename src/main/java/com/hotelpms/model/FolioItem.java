/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.model;

import java.time.LocalDate;

/**
 * Merepresentasikan satu item tagihan dalam folio tamu.
 * Setiap reservasi memiliki daftar FolioItem yang mencatat
 * semua biaya selama menginap secara itemized.
 *
 * @author rendysaptra
 */
public class FolioItem {
    
    // Enum ChargeType
    public enum ChargeType {
        ROOM("Biaya Kamar"),
        ROOM_SERVICE("Room Service"),
        LAUNDRY("Laundry"),
        MINIBAR("Mini Bar"),
        OTHER("Lainnya");
        
        private final String displayName;
        
        ChargeType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName(){
            return displayName;
        }
        
        @Override
        public String toString(){
            return displayName;
        }
    }
    
    // Atribut
    private String itemId;
    private String description;
    private ChargeType chargeType;
    private double amount;
    private LocalDate date;
    
    // Constructor
    public FolioItem(String itemId, String description, ChargeType chargeType, double amount, LocalDate date){
        this.itemId = itemId;
        this.description = description;
        this.chargeType = chargeType;
        this.amount = amount;
        this.date = date;
    }
    
    /**
    * Format nominal tagihan ke format Rupiah.
    *
    * @return String nominal dalam format Rp xxx.xxx
    */
    public String getFormattedAmount(){
        return String.format("Rp. %,.0f", amount);
    }
    
    // Getter & Setter
    public String getItemId(){
        return itemId;
    }
    public void setItemId(String itemId){
        this.itemId = itemId;
    }
    
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }
    
    public ChargeType getChargeType(){
        return chargeType;
    }
    public void setChargeType(ChargeType chargeType){
        this.chargeType = chargeType;
    }
    
    public double getAmount(){
        return amount;
    }
    public void setAmount(double amount){
        this.amount = amount;
    }
    
    public LocalDate getDate(){
        return date;
    }
    public void setDate(LocalDate date){
        this.date = date;
    }
    
    // toString
    @Override
    public String toString(){
        return String.format(
                "[%s] %s | %s | %s | %s", 
                itemId, date, chargeType.getDisplayName(), description, getFormattedAmount());
    }
}
