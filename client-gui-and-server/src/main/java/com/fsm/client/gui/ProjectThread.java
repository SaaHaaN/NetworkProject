/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fsm.client.gui;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.swing.JLabel;

public class ProjectThread extends Thread{
    
    private final Socket client;
    private final DataInputStream in;
    private final DataOutputStream out;
    
    private final String projectKey;
    
    private final JLabel usersLabel;
    
    public ProjectThread(String key, Socket socket, DataInputStream in, DataOutputStream out, JLabel usersLabel){
        this.client = socket;
        this.in = in;
        this.out = out;
        this.usersLabel = usersLabel;
        this.projectKey = "A";
    }
    
    @Override
    public void run(){        
        
    }
    
    
    
    
}
