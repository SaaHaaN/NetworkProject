package com.fsm.client.gui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Communication {
    
    public static void SendMessage(String message, DataOutputStream dos){
        try {
            // String ifadenin UTF-8'e göre byte kodlarını alıyoruz.
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            
            // Socket'in bağlı olduğu DataOutputStream'i alıp yolluyoruz.
            dos.write(bytes); 
            
        } catch (IOException err) { // Herhangi bir problem olursa kontrol etmek için
            System.out.println("Bir hata oluştu: " + err);
            err.printStackTrace();
        }
    }
    
    public static String ReadMessage(DataInputStream dis){
        try {
            // Mesajı alabilmek için yeni byte dizisi oluşturuyoruz.
            byte[] messageByte = new byte[1024];
            
            /* Bu fonksiyon, içine aldığı byte dizisini doldurup
               okunan byte sayısını döndürüyor. */
            int bytesRead = dis.read(messageByte); 
            
            // En sonunda okuduğumuz byte'ları UTF-8'e göre String'e geri dönüştürüyoruz.
            return new String(messageByte, 0, bytesRead, Charset.forName("UTF-8")); 
            
        } catch (IOException err) { // Herhangi bir problem olursa kontrol etmek için
            System.out.println("Bir hata oluştu: " + err);
            err.printStackTrace();
        }
        
        // Eğer bir hata oluştuysa boş bir String döndürüyorum
        // ve gelecekte onu kontrol ettiğim yerler oluyor.
        return "";
    }
    
}
