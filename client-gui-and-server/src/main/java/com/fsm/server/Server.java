/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.fsm.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Şahan
 */
public class Server {

    private static final Integer PORT = 9090;

    private static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static CopyOnWriteArrayList<Project> projects = new CopyOnWriteArrayList<>();
    private static CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<User>();
    private static Authenticator authenticator = new Authenticator(users);

    public static void main(String[] args) throws IOException {
        ServerSocket listener = new ServerSocket(PORT);

        Project project1 = new Project("Yusuf", "Sistem Programlama");
        Project project2 = new Project("Yusuf", "Web Programlama");

        Project project3 = new Project("Orhan", "Veri Yapıları");

        User yusuf = new User("Yusuf", "1");
        User orhan = new User("Orhan", "1");
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
            ClientHandler newClient = new ClientHandler(client, users, authenticator, projects, clients);
            Thread clientThread = new Thread(newClient);
            clientThread.start();
        }
    }
}