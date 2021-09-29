package com.example.hometaskforaes;


import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class JavaTest {


    @Test
    public void testing() {


        try {
            String str = "Hell world!";
            Cipher cipher = Cipher.getInstance("AES");
            KeyGenerator kGen = KeyGenerator.getInstance("AES");
            kGen.init(128);
            SecretKey key = kGen.generateKey();
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] bytes = cipher.doFinal(str.getBytes());

            for (byte k : bytes) {
                System.out.print(k);
            }

        } catch (Exception ex) {

        }



    }


}
