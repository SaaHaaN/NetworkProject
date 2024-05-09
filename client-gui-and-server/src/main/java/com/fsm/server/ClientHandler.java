package com.fsm.server;

import com.fsm.client.gui.LoginPage;
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
    
    private static ArrayList<Project> projects;
    private static ArrayList<User> users;
    private static Authenticator authenticator;
    
    private String username;
    
    public ClientHandler(Socket socket, ArrayList<User> users, Authenticator authenticator, ArrayList<Project> projects) throws IOException{
        this.client = socket;
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(client.getOutputStream(), true);
        this.users = users;
        this.authenticator = authenticator;
        this.projects = projects;
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
                        String canLogin = authenticator.checkIfUserCanLogin(username, password);
                        if (canLogin.equals("ok")) {
                            this.username = username;
                            isAuthenticated = true;
                            out.println("ok");
                        }
                        else if(canLogin.equals("already-connected")){
                            out.println("Kullanıcı zaten giriş yapmış.");
                        }
                        else{
                            out.println("Kullanıcı adı veya şifre yanlış!");
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
                
                while(true){
                    req = in.readLine();
                    
                    parts = req.split("\\$");
                    cmd = parts[0];
                    
                    if(req.startsWith("PROJECTS")){
                        String response = "";
                        
                        for (Project project : this.projects) {
                            if(project.creator.equals(this.username)){
                                response += project.projectName + "*" + project.projectId + "$";
                            }
                        }
                        
                        out.println(response);
                    }
                    
                    else if(req.startsWith("DISCONNECT")){
                        for (User user : users) {
                            if(this.username.equals(user.username)){
                                user.setConnectedStatus(false);
                            }
                        } 
                    }
                    
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
