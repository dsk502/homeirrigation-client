package com.simon.homeirrigationclient.model;


import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.Base64;

//class for AES-256
public class AESUtils {

    private static final String AES = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256; // AES-256
    private static final int IV_SIZE = 16; // 128 bits

    //Extract AES key and IV from Base64
    public static SecretKey extractKeyFromBase64(String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(keyBytes, AES);
    }

    public static IvParameterSpec extractIVFromBase64(String base64IV) throws Exception {
        byte[] ivBytes = Base64.getDecoder().decode(base64IV);
        return new IvParameterSpec(ivBytes);
    }


    //Decrypt the file
    public static void decryptFile(File inputFile, File outputFile, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] decryptedBytes = cipher.update(buffer, 0, bytesRead);
                if (decryptedBytes != null) {
                    fos.write(decryptedBytes);
                }
            }
            byte[] finalDecryptedBytes = cipher.doFinal();
            if (finalDecryptedBytes != null) {
                fos.write(finalDecryptedBytes);
            }
        }

    }

}
