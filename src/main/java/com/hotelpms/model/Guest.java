/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mereprentasikan tamu hotel
 * Extend Person dan menyimpan preferensi, loyal points
 * Serta riwayat reservasi tamu
 * 
 * @author rendysaptra
 */
public class Guest extends Person{
   
   // atribut
   private String idCard;
   private Map<String, String> preferences;
   private int loyaltyPoints;
   private final List<String> reservationHistory;
   
   // constructor
   public Guest(String id, String name, String phone, String email, String idCard){
       super(id,name,phone,email);
       this.idCard = idCard;
       this.preferences = new HashMap<>();
       this.loyaltyPoints = 0;
       this.reservationHistory = new ArrayList<>();
   }
   
   // Implementasi Abstact Method
   @Override
   public String getDetails(){
       return String.format("Guest | ID: %s | Nama: %s | Telp: %s | KTP: %s | Point: %d", 
               getId(), getName(), getPhone(), idCard, loyaltyPoints
       );
   }
   
   /**
   * Menambahkan Loyalty point saat guest checkout.
   * Setiap Rp 100.000 = 1 poin
   * 
   * @param points jumlah poin yang ditambahkan
   */
   public void addLoyaltyPoints(int points){
       if (points > 0){
           this.loyaltyPoints += points;
       }
   }
   
   /**
    * Ambil preferensi tamu berdasarkan key.
    * Contoh key = "floor", "bed", "smoking".
    * 
    * @param key adalah nama preferensi
    * @return nilai preferensi, atau "Tidak ada" jika tidak ditemukan
    */
   public String getPreference(String key){
       return preferences.getOrDefault(key, "Tidak ada");
   }
   
   /**
    * Set Preferensi Tamu
    * 
    * @param key nama preferensi nya (misal: "floor", "bed")
    * @param value nilai preferensi nya (misal: "high", "king")
    */
   public void setPreference(String key, String value){
       preferences.put(key, value);
   }
   
   /**
     * Tambahkan ID reservasi ke riwayat tamu.
     *
     * @param reservationId ID reservasi yang akan ditambahkan
    */
   public void addReservationHistory(String reservationId){
       if (reservationId != null && !reservationId.isEmpty()){
           reservationHistory.add(reservationId);
       }
   }
   
   // Getter & Setter
   public String getIdCard(){
       return idCard;
   }
   public void setIdCard(String idCard){
       this.idCard = idCard;
   }
   
   public Map<String, String> getPreferences(){
       return preferences;
   }
   public void setPreferences(Map<String, String> preferences){
       this.preferences = preferences;
   }
   
   public int getLoyaltyPoints(){
       return loyaltyPoints;
   }
   public void setLoyaltyPoints(int loyaltyPoints){
       this.loyaltyPoints = loyaltyPoints;
   }
   
   public List<String> getReservationHistory(){
       return reservationHistory;
   }

    // toString
   @Override
   public String toString(){
       return String.format("[%s] %s | %s | poin: %d", getId(), getName(), getPhone(), loyaltyPoints);
   }
}
