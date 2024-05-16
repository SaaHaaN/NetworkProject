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
public class Project {

    public String projectId;

    public String creator;

    public String projectName;

    public ArrayList<String> connectedUsers;

    public Project(String creator, String projectName) {
        this.projectId = generateRandomId(5);
        this.creator = creator;
        this.projectName = projectName;
        this.connectedUsers = new ArrayList<>();
    }

    public void addConnectedUserToProject(String username) {
        this.connectedUsers.add(username);
    }

    public void disconnectUserFromProject(String username) {
        this.connectedUsers.remove(username);
    }

    public String getConnectedUsers() {
        String names = "";

        for (String usernames : connectedUsers) {
            names += usernames + ", ";
        }

        return names.substring(0, names.length() - 2);
    }

    private String generateRandomId(int n) {
        // choose a Character random from this String 
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString 
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between 
            // 0 to AlphaNumericString variable length 
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb 
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }
}
