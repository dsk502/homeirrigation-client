package com.simon.homeirrigationclient.model;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPClient {

    public String serverHost = "172.20.10.6";
    public int serverPort = 7400;
    //use \n to end the data
    //Ask the server (Raspberry Pi) to generate server id
    public static String generateServerId() {
        return null;
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

    //Pack (and encrypt) the sending message
    private byte[] packMessage(String message, boolean isEncrypted) {
        byte[] messageBytes;
        byte[] packedMessage = new byte[message.length() + 2];
        if(isEncrypted) {
            //String encryptedMessage =     //Encrypt the message
            //messageBytes = encryptedMessage.getBytes();   //Convert the encrypted message to byte array
            packedMessage[0] = 0x02;    //Set the first byte of the packed byte array
        } else {
            messageBytes = message.getBytes(StandardCharsets.US_ASCII);
            packedMessage[0] = 0x01;
        }
        System.arraycopy(messageBytes, 0, packedMessage, 1, messageBytes.length);   //Copy the message block to the packed array
        packedMessage[packedMessage.length - 1] = (byte)10;   //10 = '\n', set the last byte of the packed array
        return packedMessage;
    }

    //Unpack (and decrypt) the received message from the receive buffer
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

        //messagePT =   //Decrypt the message
        messagePT = null;

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
