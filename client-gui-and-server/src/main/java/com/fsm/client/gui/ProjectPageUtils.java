package com.fsm.client.gui;

import javax.swing.JComboBox;

/**
 *
 * @author Şahan
 */
public class ProjectPageUtils {

    public static Boolean doesUserExistInCombobox(JComboBox<String> usersCombobox, String username) {

        for (int i = 0; i < usersCombobox.getItemCount(); i++) {
            if (usersCombobox.getItemAt(i).equals(username)) {
                return true;
            }
        }

        return false;
    }

    public static void removeUserFromCombobox(JComboBox<String> usersCombobox, String disconnectedUser) {
        int i = 0;

        for (; i < usersCombobox.getItemCount(); i++) {
            if (usersCombobox.getItemAt(i).equals(disconnectedUser)) {
                break;
            }
        }

        usersCombobox.remove(i);
    }

}
