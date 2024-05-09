package com.fsm.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler extends Thread{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private Boolean isAuthenticated = false;
    
    private static ArrayList<User> users;
    private static Authenticator authenticator;
    
    public ClientHandler(Socket socket, ArrayList<User> users, Authenticator authenticator) throws IOException{
        this.client = socket;
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(client.getOutputStream(), true);
        this.users = users;
        this.authenticator = authenticator;
    }
    
    @Override
    public void run(){
        try {
            while (true) {                
                String req = in.readLine();
                
                String[] parts = req.split("\\$");
                String cmd = parts[0];
                String username = parts[1];
                String password = parts[2];
                
                while(!isAuthenticated){
                    if(cmd.equals("LOGIN")){ // eğer LOGIN ise:
                        Boolean canLogin = authenticator.checkIfUserCanLogin(username, password);
                        if (canLogin) {
                            out.println("Giriş yapabilirsin");
                        }
                        else{
                            out.println("Giriş yapamazsın");
                        }
                        
                    }

                    else if(cmd.equals("REGISTER")){ // eğer REGISTER ise:
                        Boolean canRegister = authenticator.checkIfUserCanRegister(username, password);
                        if (canRegister) {
                            out.println("Kaydedildin!");
                        }
                        else{
                            out.println("Kullanıcı adı alınmış.");
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }
}
