/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fsm.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Authenticator {
    private static CopyOnWriteArrayList<User> users;
        
    public Authenticator(CopyOnWriteArrayList<User> users){
        this.users = users;
    }
    
    public String checkIfUserCanLogin(String username, String password){
        
        for (User user : users) {
            
            if(user.username.equals(username) && user.password.equals(password)){
                if(user.getConnectedStatus()){
                    return "already-connected";
                }
                user.setConnectedStatus(Boolean.TRUE);
                return "ok";
            }
        }
        
        return "pw-user-wrong";
    }
    
    public Boolean checkIfUserCanRegister(String username, String password){
        
        for (User user : users) {
            
            if(user.username.equals(username)){
                return false;
            }
        }
        
        User newUser = new User(username, password);
        users.add(newUser);
        
        return true;
    }
}
