package com.fsm.client.gui;

import java.awt.event.ItemEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;

/**
 *
 * @author Şahan
 */
public class ProjectPage extends javax.swing.JFrame {

    private DataInputStream in;
    private DataOutputStream out;
    private String key;
    private DefaultListModel<String> model = new DefaultListModel<>();
    private Map<String, JList> chats = new HashMap<String, JList>();
    private String username;

    Thread threadForThis;

    private volatile boolean isActive;

    /**
     * Creates new form ProjectPage
     */
    public ProjectPage() {
        initComponents();
        setDefaultCloseOperation(ProjectPage.DO_NOTHING_ON_CLOSE);
    }

    public void initialize(String key, String username, DataInputStream dis, DataOutputStream dos) {
        this.in = dis;
        this.out = dos;
        this.key = key;
        this.username = username;
        this.projectKeyLabel.setText(key);
        this.usersCombobox.addItemListener(this::comboBoxItemStateChanged);
        generalList.setModel(model);
        this.isActive = true;
        ListenForMessage();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void HandleNewConnectedUser(String[] allUsernames) {
        if (allUsernames == null || usersCombobox == null || chats == null) {
            System.err.println("One of the required components is null.");
            return;
        }

        for (String currentUsername : allUsernames) {
            if (currentUsername != null && !currentUsername.equals(this.username)) {
                // Aktif olan kullanıcılar arayüzde görüntülenecek
                System.out.println("Adding active user to combobox: " + currentUsername);
                if (!ProjectPageUtils.doesUserExistInCombobox(usersCombobox, currentUsername)) {
                    usersCombobox.addItem(currentUsername);

                    JList<String> jlist = new JList<>();
                    DefaultListModel<String> listModel = new DefaultListModel<>();
                    jlist.setModel(listModel);
                    chats.put(currentUsername, jlist);
                }
            }
        }
    }

// Kullanıcının projenin sahibi olup olmadığını kontrol eden bir yardımcı metod
    private boolean isProjectOwner(String username) {
        // Kullanıcı adınız ile eşleşiyorsa, o kullanıcı projenin sahibidir
        return username.equals(this.username);
    }

    private void removeUserChat(String disconnectedUser) {
        chats.remove(disconnectedUser);
    }

    public void updateChatList(String username, String message) {
        if (chats.containsKey(username)) {
            DefaultListModel<String> model = (DefaultListModel<String>) chats.get(username).getModel();
            model.addElement(message);
        }
    }

    public void ListenForMessage() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isActive) {
                        String response = Communication.ReadMessage(in);

                        String[] parts = response.split("\\$");
                        System.out.println(parts.toString());
                        String command = parts[0];

                        if (command.equals("NEW-USER-CONNECTED")) {
                            String users = parts[1];
                            activeUsersLabel.setText(users);
                            String[] allUsernames = users.split(", ");
                            HandleNewConnectedUser(allUsernames);

                        } else if (command.equals("USER-DISCONNECTED")) {
                            String disconnectedUser = parts[1];
                            String remainingUsers = parts[2];
                            activeUsersLabel.setText(remainingUsers);
                            ProjectPageUtils.removeUserFromCombobox(usersCombobox, disconnectedUser);
                            removeUserChat(disconnectedUser);

                        } else if (command.equals("GENERAL")) {
                            // Genel Mesajlaşma için:
                            // GENERAL$MESSAGE${MESSAGE DATA}
                            String type = parts[1];

                            if (type.equals("MESSAGE")) {
                                String message = parts[2];
                                String senderUsername = ""; // Varsayılan olarak boş bir kullanıcı adı atayın
                                if (parts.length > 3) {
                                    senderUsername = parts[3];
                                }

                                updateChatList(senderUsername, message);
                            } // Dosya indirmek için:
                            // GENERAL$FILE{DOSYA ADI VE EKLENTİSİ}
                            // Sonra da DATA gelecek;
                            else if (type.equals("FILE")) {
                                String fileName = parts[2];

                                Communication.ReceiveFile(fileName, in);
                            }

                        }
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        this.threadForThis = thread;

    }

    public void comboBoxItemStateChanged(ItemEvent e) {
//        if (e.getStateChange() == ItemEvent.SELECTED) {
//            String selectedItem = (String) e.getItem();
//            personalList.setModel(chats.get(selectedItem).getModel());
//        }
        if (e.getStateChange() == ItemEvent.SELECTED) {
            String selectedItem = (String) e.getItem();
            if (chats.containsKey(selectedItem) && chats.get(selectedItem) != null) {
                personalList.setModel(chats.get(selectedItem).getModel());
            } else {
                System.err.println("Selected item is not present in the chats map or its value is null.");
                // Here you could also set an empty model or take another appropriate action
                personalList.setModel(new DefaultListModel<>());
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectKeyIndicatorLabel = new javax.swing.JLabel();
        projectKeyLabel = new javax.swing.JLabel();
        projectKeyIndicatorLabel1 = new javax.swing.JLabel();
        activeUsersLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        generalList = new javax.swing.JList<>();
        generalMessageFieldTxt = new javax.swing.JTextField();
        generalSendMessageBtn = new javax.swing.JButton();
        disconnectBtn = new javax.swing.JButton();
        chooseFileButton = new javax.swing.JButton();
        chooseFileButton2 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        personalList = new javax.swing.JList<>();
        personalMessageFieldTxt = new javax.swing.JTextField();
        personalSendMessageBtn = new javax.swing.JButton();
        usersCombobox = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        projectKeyIndicatorLabel.setFont(new java.awt.Font("Dialog", 0, 13)); // NOI18N
        projectKeyIndicatorLabel.setText("Proje Anahtarı:");

        projectKeyLabel.setFont(new java.awt.Font("Dialog", 0, 13)); // NOI18N
        projectKeyLabel.setText("<>");

        projectKeyIndicatorLabel1.setFont(new java.awt.Font("Dialog", 0, 13)); // NOI18N
        projectKeyIndicatorLabel1.setText("Aktif Kullanıcılar:");

        activeUsersLabel.setFont(new java.awt.Font("Dialog", 0, 13)); // NOI18N
        activeUsersLabel.setText("<>");

        jLabel1.setText("Genel Mesajlaşma");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jScrollPane1.setViewportView(generalList);

        generalSendMessageBtn.setText("Genel Mesaj GÖNDER");
        generalSendMessageBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generalSendMessageBtnActionPerformed(evt);
            }
        });

        disconnectBtn.setText("Projeden ÇIK");
        disconnectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectBtnActionPerformed(evt);
            }
        });

        chooseFileButton.setText("Dosya SEÇ");
        chooseFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseFileButtonActionPerformed(evt);
            }
        });

        chooseFileButton2.setText("Dosya SEÇ");
        chooseFileButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseFileButton2ActionPerformed(evt);
            }
        });

        jLabel2.setText("Kişiler Arası Mesajlaşma");
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jScrollPane3.setViewportView(personalList);

        personalSendMessageBtn.setText("Kişisel Mesaj GÖNDER");
        personalSendMessageBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                personalSendMessageBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(generalMessageFieldTxt)
                            .addComponent(jScrollPane1)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(projectKeyIndicatorLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(projectKeyLabel))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(projectKeyIndicatorLabel1)
                                .addGap(9, 9, 9)
                                .addComponent(activeUsersLabel))
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(generalSendMessageBtn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chooseFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(46, 46, 46)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(personalMessageFieldTxt)
                            .addComponent(jScrollPane3)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(usersCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(personalSendMessageBtn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chooseFileButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(242, 242, 242)
                        .addComponent(disconnectBtn)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectKeyIndicatorLabel)
                    .addComponent(projectKeyLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(projectKeyIndicatorLabel1)
                    .addComponent(activeUsersLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(44, 44, 44)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(generalMessageFieldTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(generalSendMessageBtn)
                            .addComponent(chooseFileButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(usersCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(personalMessageFieldTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(personalSendMessageBtn)
                            .addComponent(chooseFileButton2))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                .addComponent(disconnectBtn)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void generalSendMessageBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generalSendMessageBtnActionPerformed
        String messageToSend = generalMessageFieldTxt.getText();
        String request = String.format("GENERAL$%s$MESSAGE$%s", key, messageToSend);
        System.out.println("Attempting to send message: " + request);
        Communication.SendMessage(request, out);
        generalMessageFieldTxt.setText("");

    }//GEN-LAST:event_generalSendMessageBtnActionPerformed

    private void disconnectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectBtnActionPerformed
        String messageToSend = String.format("DISCONNECT$%s", key);
        Communication.SendMessage(messageToSend, out);
        this.isActive = false;
        threadForThis.interrupt();
        this.dispose();
    }//GEN-LAST:event_disconnectBtnActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        String messageToSend = String.format("DISCONNECT$%s", key);
        Communication.SendMessage(messageToSend, out);
        this.isActive = false;
        threadForThis.interrupt();
    }//GEN-LAST:event_formWindowClosing

    private void chooseFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseFileButtonActionPerformed
        JFileChooser fileChooser = new JFileChooser();

        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            FileInputStream fin = null;
            try {
                File selectedFile = fileChooser.getSelectedFile();

                String fileName = selectedFile.getName();

                String req = String.format("GENERAL$%s$FILE$%s", key, fileName);

                Communication.SendMessage(req, out);

                Communication.SendFile(selectedFile, out);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }//GEN-LAST:event_chooseFileButtonActionPerformed

    private void chooseFileButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseFileButton2ActionPerformed
        // TODO add your handling code here:
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);

        String selectedUser = (String) usersCombobox.getSelectedItem();
        if (selectedUser != null) {
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                FileInputStream fin = null;
                try {
                    File selectedFile = fileChooser.getSelectedFile();

                    String fileName = selectedFile.getName();

                    String req = String.format("GENERAL$%s$FILE$%s", key, fileName);

                    Communication.SendMessage(req, out);

                    Communication.SendFile(selectedFile, out);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            System.err.println("No user selected!");
        }
    }//GEN-LAST:event_chooseFileButton2ActionPerformed

    private void personalSendMessageBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_personalSendMessageBtnActionPerformed
        // TODO add your handling code here:
        String selectedUser = (String) usersCombobox.getSelectedItem();
        if (selectedUser != null) {
            String messageToSend = personalMessageFieldTxt.getText();
            String request = String.format("GENERAL$%s$MESSAGE$%s", key, messageToSend);
            Communication.SendMessage(messageToSend, out);
        } else {
            System.err.println("No user selected!");
        }
    }//GEN-LAST:event_personalSendMessageBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ProjectPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ProjectPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ProjectPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ProjectPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ProjectPage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel activeUsersLabel;
    private javax.swing.JButton chooseFileButton;
    private javax.swing.JButton chooseFileButton2;
    private javax.swing.JButton disconnectBtn;
    private javax.swing.JList<String> generalList;
    private javax.swing.JTextField generalMessageFieldTxt;
    private javax.swing.JButton generalSendMessageBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList<String> personalList;
    private javax.swing.JTextField personalMessageFieldTxt;
    private javax.swing.JButton personalSendMessageBtn;
    private javax.swing.JLabel projectKeyIndicatorLabel;
    private javax.swing.JLabel projectKeyIndicatorLabel1;
    private javax.swing.JLabel projectKeyLabel;
    private javax.swing.JComboBox<String> usersCombobox;
    // End of variables declaration//GEN-END:variables
}
