/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fsm.server;

import com.fsm.client.gui.Communication;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Şahan
 */
public class Authenticator {

    private static CopyOnWriteArrayList<User> users;

    public Authenticator(CopyOnWriteArrayList<User> users) {
        this.users = users;
    }

    private static String checkIfUserCanLogin(String username, String password) {

        for (User user : users) {

            if (user.username.equals(username) && user.password.equals(password)) {
                if (user.getConnectedStatus()) {
                    return "already-connected";
                }
                user.setConnectedStatus(Boolean.TRUE);
                return "ok";
            }
        }

        return "pw-user-wrong";
    }

    private static Boolean checkIfUserCanRegister(String username, String password) {

        for (User user : users) {

            if (user.username.equals(username)) {
                return false;
            }
        }

        return true;
    }

    private static void RegisterUser(String username, String password) {
        User created = new User(username, password);
        users.add(created);
    }

    private static void HandleLoginAttempt(ClientHandler handler, String username, String password) {
        // Giriş yapabilir mi diye bak
        String canLogin = checkIfUserCanLogin(username, password);

        // Eğer yapabiliyorsa
        if (canLogin.equals("ok")) {
            handler.setUsername(username);
            handler.setIsAuthenticated(true);
            Communication.SendMessage("ok", handler.getOut());
        } // Eğer kullanıcı zaten giriş yapmış hâldeyse
        else if (canLogin.equals("already-connected")) {
            Communication.SendMessage("Kullanıcı zaten giriş yapmış.", handler.getOut());
        } // Eğer hiçbirisi değilse yanlış şifre veya kullanıcı adı girilmiştir
        else {
            Communication.SendMessage("Kullanıcı adı veya şifre yanlış!", handler.getOut());
        }

    }

    private static void HandleRegisterAttempt(ClientHandler handler, String username, String password) {
        // Kullanıcı kaydolabiliyor mu diye bak
        Boolean canRegister = checkIfUserCanRegister(username, password);

        if (canRegister) {
            RegisterUser(username, password);
            Communication.SendMessage("Kaydedildin!", handler.getOut());
        } else {
            Communication.SendMessage("Kullanıcı adı alınmış.", handler.getOut());
        }
    }

    public static void HandleAuthentication(ClientHandler handler) {
        while (!handler.getIsAuthenticated()) {

            String req = Communication.ReadMessage(handler.getIn());

            String[] parts = req.split("\\$");

            String command = parts[0];
            String username = parts[1];
            String password = parts[2];

            // Eğer GİRİŞ yapmaya çalışıyorsa:
            if (command.equals("LOGIN")) {
                HandleLoginAttempt(handler, username, password);
            } // Eğer KAYIT OLMAYA çalışıyorsa:
            else if (command.equals("REGISTER")) {
                HandleRegisterAttempt(handler, username, password);
            }
        }
    }
}
