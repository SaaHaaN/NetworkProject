package com.fsm.server;

import com.fsm.client.gui.Communication;
import com.mysql.cj.conf.PropertyKey;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    
    private String getProjectsCreatedByUser(){
        String response = "";
        
        for (Project project : this.projects) {
            if(project.creator.equals(this.username)){
                response += project.projectName + "*" + project.projectId + "$";
            }
        }
        
        return response;
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
    
    @Override
    public void run(){
        try {
            while (!client.isClosed()) {        
                
                while(!isAuthenticated){
                    
                    String req = Communication.ReadMessage(in);
                    
                    if(req.equals(""))
                    {
                        continue;
                    }

                    String[] parts = req.split("\\$");
                    String cmd = parts[0];
                    String username = parts[1];
                    String password = parts[2];
                    
                    if(cmd.equals("LOGIN")){ // eğer LOGIN ise:
                        String canLogin = authenticator.checkIfUserCanLogin(username, password);
                        if (canLogin.equals("ok")) {
                            this.username = username;
                            isAuthenticated = true;
                            Communication.SendMessage("ok", out);
                        }
                        
                        else if(canLogin.equals("already-connected")){
                            Communication.SendMessage("Kullanıcı zaten giriş yapmış.", out);
                        }
                        else{
                            Communication.SendMessage("Kullanıcı adı veya şifre yanlış!", out);
                        }
                        
                    }

                    else if(cmd.equals("REGISTER")){ // eğer REGISTER ise:
                        Boolean canRegister = authenticator.checkIfUserCanRegister(username, password);
                        if (canRegister) {
                            authenticator.RegisterUser(username, password);
                            Communication.SendMessage("Kaydedildin!", out);
                        }
                        else{
                            Communication.SendMessage("Kullanıcı adı alınmış.", out);
                        }
                    }
                }
                
                while(!client.isClosed()){
                    String req = Communication.ReadMessage(in);

                    String[] parts = req.split("\\$");
                    String cmd = parts[0];
                    
                    if(req == null){
                        continue;
                    }
                    
                    parts = req.split("\\$");
                    cmd = parts[0];
                    
                    if(cmd.equals("PROJECTS")){
                        Communication.SendMessage(getProjectsCreatedByUser(), out);
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
                        
                        String messageToSend = "USERS$" + project.getConnectedUsers();
                        Communication.SendMessage(messageToSend, out);
                    }
                    
                    else if(cmd.equals("GENERAL")){
                        //GENERAL$PROJECTKEY$MESSAGE$MESSAGE_DATA
                        
                        //GENERAL$PROJECTKEY$FILE${DOSYA ADI VE UZANTISI}
                        
                        String key = parts[1];
                        String type = parts[2];
                        
                        if(type.equals("MESSAGE")){
                            String message = parts[3];
                            BroadcastToProjectMembers(key, message, this.username);
                        }
                        else if(type.equals("FILE")){
                            String fileProperties = parts[3];
                            
                            FileOutputStream fout = new FileOutputStream("sunucuya_gelen_dosyalar\\" + fileProperties);
                            
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            
                            while((bytesRead = in.read(buffer)) != 1){
                                fout.write(buffer, 0, bytesRead);
                                System.out.println(bytesRead);
                            }
                            
                            fout.close();
                            
                            BroadcastFileToProjectMembers(key, fileProperties, this.username);
                        }
                        
                        
                    }
                    
                    else if(cmd.equals("DISCONNECT")){
                        // DISCONNECT$PROJECTID
                        
                        String projectKey = parts[1];
                        
                        DisconnectUserFromProject(projectKey);
                    }
                    
                    else if(cmd.equals("EXIT")){
                        for (User user : users) {
                            if(this.username.equals(user.username)){
                                user.setConnectedStatus(false);
                            }
                        }
                        CloseEverything();
                    }
                    
                }
            }
        } catch (Exception e) {
            CloseEverything();
        }
    }
    
    private void RemoveClientHandler(){
        clientHandlers.remove(this);
    }
    
    private void CloseEverything(){
        RemoveClientHandler();
        try{
            
            if(in != null){
                in.close();
            }
            
            if(out != null){
                out.close();
            }
            
            if(client != null){
                client.close();
            }
            
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    private void DisconnectUserFromProject(String key){
        for(Project project : projects){
            if(project.projectId.equals(key)){
                project.connectedUsers.remove(username);
            }
        }
        
        BroadcastConnectedUsers(FindProjectById(key));
    }
    
    private void SendProjectKey(Project project){
        if(this.username.equals(project.creator)){
            Communication.SendMessage("KEY$"+project.projectId, out);
        }
        else{
            Communication.SendMessage("KEY$"+"<Proje sahibi olmadığınız için Key'i göremezsiniz.>", out);
        }
    }
    
    private void BroadcastConnectedUsers(Project project){
        // Bütün client handlerlar arasında:
        for(ClientHandler handler: clientHandlers){
            
            // Projeye bağlanmış kullanıcıları bul
            for(String connectedUser : project.connectedUsers){
                if(handler.username == null){
                    continue;
                }
                else if(handler.username.equals(connectedUser)){
                    Communication.SendMessage("USERS$" + project.getConnectedUsers(), handler.out);
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
                    Communication.SendMessage("GENERAL$" + user + ": " + msg, handler.out);
                }
            }

        }
    }
    
    private void BroadcastFileToProjectMembers(String key, String fileProperties, String user) throws IOException{
        Project project = FindProjectById(key);
        
        for(ClientHandler handler: clientHandlers){
            
            // Projeye bağlanmış kullanıcıları bul
            for(String connectedUser : project.connectedUsers){
                if(handler.username.equals(connectedUser) && !handler.username.equals(this.username)){
                    
                    FileInputStream fin = null;
                    try {
                        
                        Communication.SendMessage("GENERAL$FILE$" + fileProperties, out);
                        
                        fin = new FileInputStream("sunucuya_gelen_dosyalar\\" + fileProperties);
                        
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while((bytesRead = fin.read(buffer)) != 1){
                            handler.out.write(buffer, 0, bytesRead);
                        }
                        
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        try {
                            fin.close();
                        } catch (IOException ex) {
                            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }

        }
    }
}
