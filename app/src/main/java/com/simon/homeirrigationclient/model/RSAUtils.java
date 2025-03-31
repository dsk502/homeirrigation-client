package com.simon.homeirrigationclient.model;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

//The RSA public and private keys are in Base64-encoded DER format

public class RSAUtils {

    public static final String CLIENT_PUBLIC_KEY_FILENAME = "client_pubkey.der";
    public static final String CLIENT_PRIVATE_KEY_FILENAME = "client_prikey.der";

    //public static final String AES_KEY_FILENAME = "aes_key.txt";
    private final Context context;

    public RSAUtils(Context context) {
        this.context = context;
    }

    //Generate Keypair
    //Public key is keyPair.getPublic
    //Private key is keyPair.getPrivate
    public KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    //Encode the key to string
    public String encodeKey(byte[] keyBytes) {
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    //Load public key from key string
    public PublicKey loadPublicKey(String publicKeyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    //Load private key from key string
    public PrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    //Encrypt data
    public String rsaEncrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    //Decrypt data
    public String rsaDecrypt(String encryptedData, PrivateKey privateKey) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

    //Read the key from file
    //false = public key, true = private key
    public String readKeyFromFile(boolean keyType) {
        FileInputStream fis = null;
        StringBuilder content = new StringBuilder();
        try {
            // 打开文件输入流，文件存储在内部存储的私有目录下
            if(keyType) {   //Private key
                fis = context.openFileInput(CLIENT_PRIVATE_KEY_FILENAME);
            } else {    //Public key
                fis = context.openFileInput(CLIENT_PUBLIC_KEY_FILENAME);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    // 关闭文件输入流
                    fis.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }
        return content.toString();
    }

    // 写入内部存储私有文件
    //false = public key, true = private key
    public void writeKeyToFile(boolean keyType, String content) {
        FileOutputStream fos = null;
        try {
            if(keyType) {   //private key
                fos = context.openFileOutput(CLIENT_PRIVATE_KEY_FILENAME, Context.MODE_PRIVATE);
            } else {    //public key
                fos = context.openFileOutput(CLIENT_PUBLIC_KEY_FILENAME, Context.MODE_PRIVATE);
            }
            fos.write(content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
