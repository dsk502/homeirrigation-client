package com.simon.homeirrigationclient.model;


import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class TCPClient {

    public Context context;
    public String serverHost = "172.20.10.6";
    public int serverPort = 7400;
    //use \n to end the data
    //Ask the server (Raspberry Pi) to generate server id

    public String serverPubKey;

    public RSAUtils rsaUtils;
    public TCPClient(Context context, String serverHost, int serverPort) {
        this.context = context;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }


    public int addDeviceRequest(long timeStamp, String clientPubkey, DeviceInfo deviceInfo) {

        try (Socket socket = new Socket(serverHost, serverPort)) {
            //InputStream is used for reading messages coming from server
            InputStream in = socket.getInputStream();

            //OutputStream is used for sending messages to the server
            OutputStream out = socket.getOutputStream();

            //Variables for sending and receiving
            byte[] sendingBytes;
            String sendingMessage;

            byte[] receiveBuffer = new byte[1024];
            int receiveLen;
            String receivedMessage;
            String recvCommand;
            String[] recvParams;

            //Send add_device(timestamp)
            sendingMessage = "add_device(" + timeStamp + ")";
            sendingBytes = packMessage(sendingMessage, false);
            out.write(sendingBytes);
            out.flush();

            //Receive key_exchange_server(server_pubkey)
            receiveLen = in.read(receiveBuffer);
            receivedMessage = unpackUnencryptedMessage(receiveBuffer);
            if(receivedMessage == null) {   //If encrypted or no end tag -> error
                return -1;
            }

            //Retrieve command and arguments
            recvCommand = extractCommand(receivedMessage);
            recvParams = extractParams(receivedMessage);
            if(!(recvCommand.equals("key_exchange_server") && recvParams.length == 1)) {
                return -1;
            }
            //Store the server_pubkey
            String serverPubKey = recvParams[0];

            //Reply key_exchange_client(client_pubkey)
            sendingMessage = "key_exchange_client(" + clientPubkey + ")";
            sendingBytes = packMessage(sendingMessage, false);
            out.write(sendingBytes);
            out.flush();

            //Receive and process request_add_param(server_id) -- encrypted
            receiveLen = in.read(receiveBuffer);
            receivedMessage = unpackEncryptedMessage(receiveBuffer);
            if(receivedMessage == null) {
                return -1;
            }
            recvCommand = extractCommand(receivedMessage);
            recvParams = extractParams(receivedMessage);
            if(!(recvCommand.equals("request_add_param") && recvParams.length == 1)) {
                return -1;
            }
            String serverId = recvParams[0];

            //Reply reply_add_param(mode,water_amount,scheduled_freq,scheduled_time) -- encrypted
            sendingMessage = "reply_add_param(" + deviceInfo.mode + "," + deviceInfo.waterAmount + "," + deviceInfo.scheduledFreq + "," + deviceInfo.scheduledTime + ")";
            sendingBytes = packMessage(sendingMessage, true);
            out.write(sendingBytes);
            out.flush();

            //Receive finish_add_server() -- encrypted
            receiveLen = in.read(receiveBuffer);
            receivedMessage = unpackEncryptedMessage(receiveBuffer);
            recvCommand = extractCommand(receivedMessage);
            recvParams = extractParams(receivedMessage);
            if(!(recvCommand.equals("request_add_param") && recvParams.length == 0)) {
                return -1;
            }
            //Save the data to the database

            //Reply finish_add_client()
            sendingMessage = "finish_add_client()";
            sendingBytes = packMessage(sendingMessage, true);
            out.write(sendingBytes);
            out.flush();

            //Close the input and output stream
            in.close();
            out.close();
            return 0;

        } catch (IOException e) {
            Log.println(Log.ERROR, "Error", "Network Error!");
            return -1;
        } catch (IndexOutOfBoundsException e) {
            Log.println(Log.ERROR,"Error", "Message Error!");
            return -1;
        }
    }

    public void downloadStatRequest(String serverId) {
        String serverAddress = "127.0.0.1";
        int serverPort = 8080;
        String receiveFilePath = "temp/watering_record_" + serverId + "_encrypted.db";

        try (
            Socket socket = new Socket(serverAddress, serverPort);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(receiveFilePath);
        ) {
            //For the messages
            byte[] sendingBytes;
            String sendingMessage;

            byte[] receiveBuffer = new byte[1024];
            int receiveLen;
            String receivedMessage;
            String recvCommand;
            String[] recvParams;

            //Sending download_stat()
            sendingMessage = "download_stat()";
            sendingBytes = packMessage(sendingMessage, true);
            outputStream.write(sendingBytes);
            outputStream.flush();

            //Receive stat_key(aeskey, iv)
            receiveLen = inputStream.read(receiveBuffer);
            receivedMessage = unpackEncryptedMessage(receiveBuffer);
            recvCommand = extractCommand(receivedMessage);
            recvParams = extractParams(receivedMessage);
            if(!(recvCommand.equals("stat_key") && recvParams.length == 2)) {
                return;
            }

            //Get aes key and iv in base64
            String aesKeyBase64 = recvParams[0];
            String ivBase64 = recvParams[1];

            //Reply stat_key_ok
            sendingMessage = "stat_key_ok()";
            sendingBytes = packMessage(sendingMessage, true);
            outputStream.write(sendingBytes);
            outputStream.flush();

            //For the file
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                if (bytesRead > 0 && buffer[0] == 0x03) { // 文件数据包
                    fileOutputStream.write(buffer, 1, bytesRead - 1);
                } else if (bytesRead == 1 && buffer[0] == (byte) 0xFF) { // 文件结束标记
                    System.out.println("文件接收完成，已保存到 " + receiveFilePath);
                    break;
                } else {
                    System.err.println("数据包格式错误");
                    return;
                }
            }

            //Decrypt the file
            SecretKey aesKey = AESUtils.extractKeyFromBase64(aesKeyBase64);
            IvParameterSpec iv = AESUtils.extractIVFromBase64(ivBase64);
            AESUtils.decryptFile(receiveFilePath, "databases/watering_records/watering_record_" + serverId + ".db", aesKey, iv);

            //Delete the temp file

        } catch (IOException e) {
            System.err.println("发生错误: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int deleteDeviceRequest() {
        return 0;
    }

    //Pack (and encrypt) the sending message
    private byte[] packMessage(String message, boolean isEncrypted) {
        byte[] messageBytes;
        byte[] packedMessage = new byte[message.length() + 2];
        if(isEncrypted) {
            String encryptedMessage;    //Encrypt the message
            try {
                encryptedMessage = rsaUtils.rsaEncrypt(message, rsaUtils.loadPublicKey(serverPubKey));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            messageBytes = encryptedMessage.getBytes();   //Convert the encrypted message to byte array
            packedMessage[0] = 0x02;    //Set the first byte of the packed byte array
        } else {
            messageBytes = message.getBytes(StandardCharsets.US_ASCII);
            packedMessage[0] = 0x01;
        }
        System.arraycopy(messageBytes, 0, packedMessage, 1, messageBytes.length);   //Copy the message block to the packed array
        packedMessage[packedMessage.length - 1] = (byte)10;   //10 = '\n', set the last byte of the packed array
        return packedMessage;
    }

    //Unpack the received message from the receive buffer
    private String unpackUnencryptedMessage(byte[] receiveBuffer) {
        if(receiveBuffer[0] != (byte)0x01) {    //If encrypted -> error
            return null;
        }
        //byte endTag = receiveBuffer[receiveLen - 1];
        int indexOfEndTag = findIndexOf(receiveBuffer, (byte)10);
        if(indexOfEndTag == -1) {
            return null;    //The end of the message is not '\n', which is an error
        }
        //copy the message block to the array messageBlock
        byte[] messageBlock = new byte[indexOfEndTag];
        System.arraycopy(receiveBuffer, 1, messageBlock, 0, messageBlock.length);
        return new String(messageBlock);
    }

    private String unpackEncryptedMessage(byte[] receiveBuffer) {
        if(receiveBuffer[0] != 0x02) {  //If not encrypted -> error
            return null;
        }
        int indexOfEndTag = findIndexOf(receiveBuffer, (byte)10);
        if(indexOfEndTag == -1) {
            return null;    //The end of the message is not '\n', which is an error
        }
        //copy the message block to the array messageBlock
        byte[] messageBlock = new byte[indexOfEndTag];
        System.arraycopy(receiveBuffer, 1, messageBlock, 0, messageBlock.length);
        String messageBlockStr = new String(messageBlock);
        String messagePT;
        try {
            messagePT = rsaUtils.rsaDecrypt(messageBlockStr, rsaUtils.loadPrivateKey(rsaUtils.readKeyFromFile(true)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return messagePT;
    }

    private String extractCommand(String messagePT) throws IndexOutOfBoundsException {
        return messagePT.substring(0, messagePT.indexOf('('));
    }

    private String[] extractParams(String messagePT) throws IndexOutOfBoundsException {
        //If the substring (string between two braces) is empty, the split function will return an empty string array
        return messagePT.substring(messagePT.indexOf('(') + 1, messagePT.indexOf(')')).split(",");
    }

    public static int findIndexOf(byte[] array, byte target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i; // 返回找到的索引
            }
        }
        return -1; // 如果没有找到，返回-1
    }
}
