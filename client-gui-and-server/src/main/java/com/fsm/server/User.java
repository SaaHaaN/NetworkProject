/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fsm.server;

import java.util.ArrayList;

/**
 *
 * @author Şahan
 */
public class User {

    public String username;
    public String password;

    public ArrayList<Project> projects = new ArrayList<>();

    private Boolean isConnected = false;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Boolean getConnectedStatus() {
        return this.isConnected;
    }

    public void setConnectedStatus(Boolean value) {
        this.isConnected = value;
    }

    public void addProjectToUser(Project project) {
        this.projects.add(project);
    }
}
