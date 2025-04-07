package com.simon.homeirrigationclient.model;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

    private static RSAUtils instance;
    public static final String CLIENT_KEYS_DIR = "keys";
    public static final String CLIENT_PUBLIC_KEY_FILENAME = "client_pubkey.der";
    public static final String CLIENT_PRIVATE_KEY_FILENAME = "client_prikey.der";

    //public static final String AES_KEY_FILENAME = "aes_key.txt";
    private final Context context;

    public RSAUtils(Context context) {
        instance = this;
        this.context = context;
    }

    public static RSAUtils getInstance() {
        return instance;
    }
    //Determine whether keypair is on the disk
    public boolean isKeypairFileExist() {

        File prikeyFile = new File(context.getFilesDir(), CLIENT_KEYS_DIR + "/" + CLIENT_PRIVATE_KEY_FILENAME);
        File pubkeyFile = new File(context.getFilesDir(), CLIENT_KEYS_DIR + "/" + CLIENT_PUBLIC_KEY_FILENAME);
        return (prikeyFile.exists() && pubkeyFile.exists());
    }

    //Generate Keypair
    //Public key is keyPair.getPublic
    //Private key is keyPair.getPrivate
    public void generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        String encodedPrikey = encodeKey(privateKeyBytes);
        String encodedPubkey = encodeKey(publicKeyBytes);


        writeKeyToFile(false, encodedPrikey);
        writeKeyToFile(true, encodedPubkey);
    }

    //Encode the key to string
    private String encodeKey(byte[] keyBytes) {
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
        /*
        privateKeyStr = privateKeyStr.trim().replaceAll("\\s", "");

        // 确保Base64字符串长度是4的倍数
        while (privateKeyStr.length() % 4 != 0) {
            privateKeyStr += "=";
        }

         */
        //boolean isNewLine = (privateKeyStr.charAt(privateKeyStr.length() - 1) == '\n');
        //Log.println(Log.ERROR, "Does private key has new line", String.valueOf(isNewLine));
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
    public String readKeyFromFile(boolean isPubkey) {
        FileInputStream fis = null;
        StringBuilder content = new StringBuilder();
        try {
            // 打开文件输入流，文件存储在内部存储的私有目录下
            File keysDir = new File(context.getFilesDir(), CLIENT_KEYS_DIR);
            if(!isPubkey) {   //Private key
                File privateKeyFullPath = new File(keysDir, CLIENT_PRIVATE_KEY_FILENAME);
                fis = new FileInputStream(privateKeyFullPath);
            } else {    //Public key
                File publicKeyFullPath = new File(keysDir, CLIENT_PUBLIC_KEY_FILENAME);
                fis = new FileInputStream(publicKeyFullPath);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line = reader.readLine();
            content.append(line);
            /*
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

             */
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
        //Delete the last char (\n)
        //content.deleteCharAt(content.length() - 1);
        return content.toString();
    }

    // 写入内部存储私有文件
    private void writeKeyToFile(boolean isPubkey, String content) {
        FileOutputStream fos = null;
        try {
            File keysDir = new File(context.getFilesDir(), CLIENT_KEYS_DIR);
            if(!keysDir.exists()) {
                keysDir.mkdirs();
            }
            if(!isPubkey) {   //private key
                File privateKeyFullPath = new File(keysDir, CLIENT_PRIVATE_KEY_FILENAME);
                fos = new FileOutputStream(privateKeyFullPath);
                Log.println(Log.ERROR, "Private key generated", content);

            } else {    //public key
                File publicKeyFullPath = new File(keysDir, CLIENT_PUBLIC_KEY_FILENAME);
                fos = new FileOutputStream(publicKeyFullPath);

            }
            fos.write(content.getBytes(StandardCharsets.US_ASCII));
            fos.flush();
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
