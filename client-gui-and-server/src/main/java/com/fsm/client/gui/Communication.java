package com.fsm.client.gui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Şahan
 */
public class Communication {
   
        public static void SendMessage(String message, DataOutputStream dos) {
        try {
            // String ifadenin UTF-8'e göre byte kodlarını alıyoruz.
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);

            // Socket'in bağlı olduğu DataOutputStream'i alıp yolluyoruz.
            dos.write(bytes);
            dos.flush();

            System.out.println("Message sent: " + message);

        } catch (IOException err) { // Herhangi bir problem olursa kontrol etmek için
            err.printStackTrace();
        }
    }

    public static String ReadMessage(DataInputStream dis) {
        try {
            // Mesajı alabilmek için yeni byte dizisi oluşturuyoruz.
            byte[] messageByte = new byte[1024];

            /* Bu fonksiyon, içine aldığı byte dizisini doldurup
               okunan byte sayısını döndürüyor. */
            int bytesRead = dis.read(messageByte);

            // En sonunda okuduğumuz byte'ları UTF-8'e göre String'e geri dönüştürüyoruz.
            String message = new String(messageByte, 0, bytesRead, StandardCharsets.UTF_8);
            System.out.println("Message received: " + message);
            return message;

        } catch (IOException err) { // Herhangi bir problem olursa kontrol etmek için
            err.printStackTrace();
        }

        // Eğer bir hata oluştuysa boş bir String döndürüyorum
        // ve gelecekte onu kontrol ettiğim yerler oluyor.
        return "";
    }
    
    
    public static void SendFile(File fileToSend, DataOutputStream out) throws Exception {
        int bytes = 0;

        FileInputStream fis = new FileInputStream(fileToSend);

        out.writeLong(fileToSend.length());

        byte[] buffer = new byte[1024];
        while ((bytes = fis.read(buffer))
                != -1) {

            out.write(buffer, 0, bytes);
            out.flush();
        }

        fis.close();
    }

    public static void ReceiveFile(String fileName, DataInputStream in) throws Exception {
        int bytes = 0;

        FileOutputStream fos = new FileOutputStream(fileName);

        long size = in.readLong();

        byte[] buffer = new byte[1024];

        while (size > 0
                && (bytes = in.read(buffer, 0, (int) Math.min(buffer.length, size))) != 1) {
            fos.write(buffer, 0, bytes);
            size -= bytes;
        }

        fos.flush();
        fos.close();
    }

}
