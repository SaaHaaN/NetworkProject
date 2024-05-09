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
 
    private static ArrayList<Project> projects = new ArrayList<>();
    private static ArrayList<User> users = new ArrayList<User>();
    private static Authenticator authenticator = new Authenticator(users);

    public static void main(String[] args) throws IOException{
        ServerSocket listener = new ServerSocket(PORT);
        
        Project project1 = new Project("Yusuf", "Sistem Programlama");
        Project project2 = new Project("Yusuf", "Web Programlama");
        
        Project project3 = new Project("Orhan", "Veri Yapıları");
        
        User yusuf = new User("Yusuf", "12345");
        User orhan = new User("Orhan", "qwerty");
        users.add(yusuf);
        users.add(orhan);
        
        yusuf.addProjectToUser(project1);
        yusuf.addProjectToUser(project2);
        orhan.addProjectToUser(project3);
        
        projects.add(project1);
        projects.add(project2);
        projects.add(project3);
        
        while (true) {            
            Socket client = listener.accept();
            ClientHandler newClient = new ClientHandler(client, users, authenticator, projects);
            Thread clientThread = new Thread(newClient);
            clientThread.start();
        }
    }
}
