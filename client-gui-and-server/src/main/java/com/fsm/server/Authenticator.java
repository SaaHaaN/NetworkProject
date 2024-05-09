/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fsm.server;

import java.util.ArrayList;
import java.util.List;

public class Authenticator {
    private static ArrayList<User> users;
        
    public Authenticator(ArrayList<User> users){
        this.users = users;
    }
    
    public Boolean checkIfUserCanLogin(String username, String password){
        
        for (User user : users) {
            
            if(user.username.equals(username) && user.password.equals(password)){
                if(user.getConnectedStatus()){
                    return false;
                }
                user.setConnectedStatus(Boolean.TRUE);
                return true;
            }
        }
        
        return false;
    }
    
    public Boolean checkIfUserCanRegister(String username, String password){
        
        for (User user : users) {
            
            if(user.username.equals(username) && user.password.equals(password)){
                return false;
            }
        }
        
        User newUser = new User(username, password);
        users.add(newUser);
        
        return true;
    }
}
