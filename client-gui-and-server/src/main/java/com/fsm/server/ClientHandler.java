package com.fsm.server;

import com.mysql.cj.conf.PropertyKey;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler extends Thread{
    
    private String username;
    private Socket client;
    private DataInputStream in;
    private DataOutputStream out;
    private Boolean isAuthenticated = false;
    
    private static CopyOnWriteArrayList<Project> projects;
    private static CopyOnWriteArrayList<User> users;
    private static CopyOnWriteArrayList<ClientHandler> clientHandlers;
    private static Authenticator authenticator;
    
    
    public ClientHandler(Socket socket, CopyOnWriteArrayList<User> users, Authenticator authenticator, CopyOnWriteArrayList<Project> projects, CopyOnWriteArrayList<ClientHandler> clients) throws IOException{
        this.client = socket;
        in = new DataInputStream(client.getInputStream());
        out = new DataOutputStream(client.getOutputStream());
        this.users = users;
        this.authenticator = authenticator;
        this.projects = projects;
        this.clientHandlers = clients;
        clientHandlers.add(this);
    }
    
    private String ReturnAuthoredProjects(){
        String response = "";
        
        for (Project project : this.projects) {
            if(project.creator.equals(this.username)){
                response += project.projectName + "*" + project.projectId + "$";
            }
        }
        
        return response;
    }
    
    private void SendMessage(String msg){
        try {
            byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
            this.out.write(bytes);  
        } catch (IOException err) {
            
        }
    }
    
    private Project FindProjectById(String id){
        
        Project tmp = null;
        
        for(Project project: projects){
            if(project.projectId.equals(id)){
                return project;
            }
        }
        
        return tmp;
    }
        
    private String ReadMessage(){
        try {
            byte[] messageByte = new byte[1024];
            int bytesRead = in.read(messageByte); 
            return new String(messageByte, 0, bytesRead, Charset.forName("UTF-8")); 
        } catch (IOException err) {
            return "";
        } catch (StringIndexOutOfBoundsException excp){
            return "";
        }
    }
    
    @Override
    public void run(){
        try {
            while (true) {        
                String req = ReadMessage();
                
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
                            SendMessage("ok");
                        }
                        
                        else if(canLogin.equals("already-connected")){
                            SendMessage("Kullanıcı zaten giriş yapmış.");
                        }
                        else{
                            SendMessage("Kullanıcı adı veya şifre yanlış!");
                        }
                        
                    }

                    else if(cmd.equals("REGISTER")){ // eğer REGISTER ise:
                        Boolean canRegister = authenticator.checkIfUserCanRegister(username, password);
                        if (canRegister) {
                            SendMessage("Kaydedildin!");
                        }
                        else{
                            SendMessage("Kullanıcı adı alınmış.");
                        }
                    }
                }
                
                while(true){
                    String reqAfterLogin = ReadMessage();
                    
                    if(reqAfterLogin == null){
                        continue;
                    }
                    
                    parts = reqAfterLogin.split("\\$");
                    cmd = parts[0];
                    
                    if(cmd.equals("PROJECTS")){
                        SendMessage(ReturnAuthoredProjects());
                    }
                    
                    else if(cmd.equals("CREATE")){
                        String author = parts[1];
                        String projectName = parts[2];
                        
                        Project created = new Project(author, projectName);
                        
                        projects.add(created);
                    }
                    
                    else if(cmd.equals("CONNECT")){
                        // CONNECT$KULLANICIADI$PROJEID
                        
                        String user = parts[1];
                        String id = parts[2];
                        
                        Project project = FindProjectById(id);
                        project.addConnectedUserToProject(user);
                        
                        SendProjectKey(project);
                        BroadcastConnectedUsers(project);
                    }
                    
                    else if(cmd.equals("USERS")){
                        String pId = parts[1];
                        
                        Project project = FindProjectById(pId);
                        
                        if(project == null){
                            continue;
                        }
                        
                        SendMessage("USERS$" + project.getConnectedUsers());
                    }
                    
                    else if(cmd.equals("GENERAL")){
                        //GENERAL$PROJECTKEY$MESSAGE
                        
                        String key = parts[1];
                        String message = parts[2];
                        
                        BroadcastToProjectMembers(key, message, this.username);
                    }
                    
                    else if(cmd.equals("EXIT")){
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
    
    private void SendProjectKey(Project project){
        if(this.username.equals(project.creator)){
            SendMessage("KEY$"+project.projectId);
        }
        else{
            SendMessage("KEY$"+"<Proje sahibi olmadığınız için Key'i göremezsiniz.>");
        }
    }
    
    
    private void BroadcastConnectedUsers(Project project){
        // Bütün client handlerlar arasında:
        for(ClientHandler handler: clientHandlers){
            
            // Projeye bağlanmış kullanıcıları bul
            for(String connectedUser : project.connectedUsers){
                if(handler.username.equals(connectedUser)){
                    handler.SendMessage("USERS$" + project.getConnectedUsers());
                }
            }

        }
    }
    
    private void BroadcastToProjectMembers(String key, String msg, String user){
        Project project = FindProjectById(key);
        
        for(ClientHandler handler: clientHandlers){
            
            // Projeye bağlanmış kullanıcıları bul
            for(String connectedUser : project.connectedUsers){
                if(handler.username.equals(connectedUser)){
                    handler.SendMessage("GENERAL$" + user + ": " + msg);
                }
            }

        }
    }
}
