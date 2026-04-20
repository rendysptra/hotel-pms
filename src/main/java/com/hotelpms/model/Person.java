/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hotelpms.model;

/**
 * Abstact class yang mempresentasikan semua orang dalam sistem Hotel PMS
 * Superclass dari Guest dan Staff
 * 
 * 
 * @author rendysaptra
 */
public abstract class Person {
    
    // atribut
    private String id;
    private String name;
    private String phone;
    private String email;
    
    // constructor
    public Person (String id, String name, String phone, String email){
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }
    
    // Abstract method, untuk menampilkan informasi setiap person
    public abstract String getDetails();
    
    // Getter & Setter
    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }
    
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    
    public String getPhone(){
        return phone;
    }
    public void setPhone(String phone){
        this.phone = phone;
    }
    
    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email = email;
    }
    
    // toString
    @Override
    public String toString(){
        return String.format("[%s] %s | %s | %s", id, name, phone, email);
    } 
}
