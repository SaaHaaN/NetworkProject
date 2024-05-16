package com.fsm.server;

import com.fsm.client.gui.Communication;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Şahan
 */
public class ClientHandler extends Thread {

    private String username;
    private Socket client;
    private DataInputStream in;
    private DataOutputStream out;
    private Boolean isAuthenticated = false;

    private static CopyOnWriteArrayList<Project> projects;
    private static CopyOnWriteArrayList<User> users;
    private static CopyOnWriteArrayList<ClientHandler> clientHandlers;
    private static Authenticator authenticator;

    public ClientHandler(Socket socket, CopyOnWriteArrayList<User> users, Authenticator authenticator, CopyOnWriteArrayList<Project> projects, CopyOnWriteArrayList<ClientHandler> clients) throws IOException {
        this.client = socket;
        in = new DataInputStream(client.getInputStream());
        out = new DataOutputStream(client.getOutputStream());
        this.users = users;
        this.authenticator = authenticator;
        this.projects = projects;
        this.clientHandlers = clients;
        clientHandlers.add(this);
    }

    private String getProjectsCreatedByUser() {
        String response = "yok";

        for (Project project : this.projects) {
            if (project.creator.equals(this.getUsername())) {
                response += project.projectName + "*" + project.projectId + "$";
            }
        }

        return response;
    }

    private Project FindProjectById(String id) {

        Project tmp = null;

        for (Project project : projects) {
            if (project.projectId.equals(id)) {
                return project;
            }
        }

        return tmp;
    }

    @Override
    public void run() {
        try {
            while (!client.isClosed()) {

                Authenticator.HandleAuthentication(this);

                while (!client.isClosed()) {
                    String req = Communication.ReadMessage(getIn());

                    String[] parts = req.split("\\$");
                    String cmd = parts[0];

                    if (req == null) {
                        continue;
                    }

                    if (cmd.equals("PROJECTS")) {
                        Communication.SendMessage(getProjectsCreatedByUser(), getOut());
                    } else if (cmd.equals("CREATE")) {
                        String author = parts[1];
                        String projectName = parts[2];

                        Project created = new Project(author, projectName);

                        projects.add(created);
                    } else if (cmd.equals("CONNECT")) {
                        // CONNECT$ANAHTAR

                        String key = parts[1];

                        Project project = FindProjectById(key);
                        project.addConnectedUserToProject(getUsername());

                        BroadcastConnectedUsers(project);

                        //BroadcastToProjectMembers(key, "adlı kullanıcı sohbete katıldı!");
                    } else if (cmd.equals("GENERAL")) {

                        String key = parts[1];
                        String type = parts[2];

                        //GENERAL${ANAHTAR}$MESSAGE${MESAJIN KENDİSİ}
                        if (type.equals("MESSAGE")) {
                            String message = parts[3];
                            BroadcastToProjectMembers(key, message);
                        } //GENERAL${ANAHTAR}$FILE${DOSYA ADI}
                        else if (type.equals("FILE")) {
                            String fileName = parts[3];

                            Communication.ReceiveFile("sunucuya_gelen_dosyalar\\" + fileName, this.getIn());

                            String report = String.format("********\n"
                                    + "Sunucuya dosya geldi!\n"
                                    + "Proje Anahtarı: %s\n"
                                    + "Gönderen: %s\n"
                                    + "Kaydedilen konum: sunucuya_gelen_dosyalar/%s",
                                    key, this.getUsername(), fileName);

                            System.out.println(report);

                            BroadcastFileToProjectMembers(key, fileName);

                            BroadcastToProjectMembers(key, fileName + "adlı dosyayı gönderdi!");
                        }

                    } else if (cmd.equals("DISCONNECT")) {
                        // DISCONNECT$PROJECTID

                        String key = parts[1];

                        DisconnectUserFromProject(key);

                        BroadcastDisconnectedUser(FindProjectById(key), username);
                        //BroadcastToProjectMembers(key, "adlı kullanıcı projeden ayrıldı");
                    } else if (cmd.equals("EXIT")) {
                        for (User user : users) {
                            if (this.getUsername().equals(user.username)) {
                                user.setConnectedStatus(false);
                            }
                        }
                        CloseEverything();
                    }

                }
            }
        } catch (Exception e) {
            interrupt();
        }
    }

    private void RemoveClientHandler() {
        clientHandlers.remove(this);
    }

    private void CloseEverything() {
        RemoveClientHandler();
        try {

            if (getIn() != null) {
                getIn().close();
            }

            if (getOut() != null) {
                getOut().close();
            }

            if (client != null) {
                client.close();
            }

            this.interrupt();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void DisconnectUserFromProject(String key) {
        for (Project project : projects) {
            if (project.projectId.equals(key)) {
                project.connectedUsers.remove(getUsername());
            }
        }

        BroadcastDisconnectedUser(FindProjectById(key), username);
    }

    private void BroadcastConnectedUsers(Project project) {
        // Bütün client handlerlar arasında:
        for (ClientHandler handler : clientHandlers) {

            // Projeye bağlanmış kullanıcıları bul
            for (String connectedUser : project.connectedUsers) {
                if (handler.getUsername() == null) {
                    continue;
                } else if (handler.getUsername().equals(connectedUser)) {
                    String response = String.format("NEW-USER-CONNECTED$%s", project.getConnectedUsers());
                    Communication.SendMessage(response, handler.getOut());
                }
            }

        }
    }

    private void BroadcastDisconnectedUser(Project project, String disconnectedUsername) {

        // Bütün client handlerlar arasında:
        for (ClientHandler handler : clientHandlers) {

            // Projeye bağlanmış kullanıcıları bul
            for (String connectedUser : project.connectedUsers) {
                if (handler.getUsername() == null) {
                    continue;
                } else if (handler.getUsername().equals(connectedUser)) {

                    String response = String.format("USER-DISCONNECTED$%s$%s",
                            disconnectedUsername, project.getConnectedUsers());

                    Communication.SendMessage(response, handler.getOut());
                }
            }

        }
    }

    private void BroadcastToProjectMembers(String key, String msg) {

        //GENERAL$PROJECTKEY$MESSAGE$MESSAGE_DATA
        Project project = FindProjectById(key);

        for (ClientHandler handler : clientHandlers) {

            // Projeye bağlanmış kullanıcıları bul
            for (String connectedUser : project.connectedUsers) {
                if (handler.getUsername().equals(connectedUser)) {
                    String toSend = String.format("GENERAL$MESSAGE$%s: %s", getUsername(), msg);
                    Communication.SendMessage(toSend, handler.getOut());
                }
            }

        }
    }

    private void BroadcastFileToProjectMembers(String key, String fileName) throws IOException, Exception {
        Project project = FindProjectById(key);
        File fileToSend = new File("sunucuya_gelen_dosyalar\\" + fileName);

        for (ClientHandler handler : clientHandlers) {

            // Projeye bağlanmış kullanıcıları bul
            for (String connectedUser : project.connectedUsers) {
                if (handler.getUsername().equals(connectedUser) && !handler.username.equals(this.username)) {
                    try {

                        // Kullanıcılara genelden dosya geleceği bilgisini yolluyorum.
                        Communication.SendMessage("GENERAL$FILE$" + fileName, handler.getOut());

                        Communication.SendFile(fileToSend, handler.getOut());

                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }

        fileToSend.delete();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public Boolean getIsAuthenticated() {
        return isAuthenticated;
    }

    public void setIsAuthenticated(Boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }
}
