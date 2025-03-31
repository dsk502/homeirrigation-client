package com.simon.homeirrigationclient.model;


import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;
import java.util.Base64;

//class for AES-256
public class AESUtils {

    private static final String AES = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256; // AES-256
    private static final int IV_SIZE = 16; // 128 bits

    // 从Base64编码的字符串中提取密钥和IV
    public static SecretKey extractKeyFromBase64(String base64Key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(keyBytes, AES);
    }

    public static IvParameterSpec extractIVFromBase64(String base64IV) throws Exception {
        byte[] ivBytes = Base64.getDecoder().decode(base64IV);
        return new IvParameterSpec(ivBytes);
    }


    // 解密文件
    public static void decryptFile(String inputFilePath, String outputFilePath, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        try (FileInputStream fis = new FileInputStream(inputFilePath);
             FileOutputStream fos = new FileOutputStream(outputFilePath)) {
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

        System.out.println("文件解密成功: " + outputFilePath);
    }

    /*
    public static void main(String[] args) {
        try {
            // 假设密钥和IV已经以Base64编码的形式存储
            String base64Key = "yourBase64EncodedKeyHere";
            String base64IV = "yourBase64EncodedIVHere";

            // 提取密钥和IV
            SecretKey key = extractKeyFromBase64(base64Key);
            IvParameterSpec iv = extractIVFromBase64(base64IV);

            // 解密文件
            String inputFilePath = "path/to/encrypted/file.enc";
            String outputFilePath = "path/to/decrypted/file.dec";
            decryptFile(inputFilePath, outputFilePath, key, iv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}
