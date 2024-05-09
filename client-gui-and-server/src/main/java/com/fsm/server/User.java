/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fsm.server;

/**
 *
 * @author Yakup
 */
public class User {
    public String username;
    public String password;
    private Boolean isConnected = false;
    
    public User(String username, String password){
        this.username = username;
        this.password = password;
    }
    
    public Boolean getConnectedStatus(){
        return this.isConnected;
    }
    
    public void setConnectedStatus(Boolean value){
        this.isConnected = value;
    }
}
