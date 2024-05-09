/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.fsm.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class Server {
    
    private static final Integer PORT = 9090;
    
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    
    private static ArrayList<User> users = new ArrayList<User>();
    
    private static Authenticator authenticator = new Authenticator(users);

    public static void main(String[] args) throws IOException{
        ServerSocket listener = new ServerSocket(PORT);
        
        User user1 = new User("Yusuf", "12345");
        User user2 = new User("Orhan", "abcdef");
        users.add(user1);
        users.add(user2);
        
        while (true) {            
            Socket client = listener.accept();
            ClientHandler newClient = new ClientHandler(client, users, authenticator);
            Thread clientThread = new Thread(newClient);
            clientThread.start();
        }
    }
}
