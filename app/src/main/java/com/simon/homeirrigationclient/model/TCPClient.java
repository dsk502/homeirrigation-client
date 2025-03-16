package com.simon.homeirrigationclient.model;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPClient {

    public String serverHost = "172.20.10.6";
    public int serverPort = 7400;
    //use \n to end the data
    //Ask the server (Raspberry Pi) to generate server id
    public static String generateServerId() {
        return null;
    }

    public int addDeviceRequest(long timeStamp, String clientPubkey) {

        try (Socket socket = new Socket(serverHost, serverPort)) {
            //InputStream is used for reading messages coming from server
            InputStream in = socket.getInputStream();

            //OutputStream is used for sending messages to the server
            OutputStream out = socket.getOutputStream();

            //Variables for sending and receiving
            byte[] sendingBytes;
            byte[] sendingMessageBytes;
            String sendingMessage;

            byte[] receiveBuffer = new byte[1024];
            int receiveLen;
            byte isEncrypted;
            byte[] receivedMessageBytes;
            String receivedMessage;
            String recvCommand;
            String[] recvArguments;

            //Send add_device(timestamp)
            sendingMessage = "add_device(" + timeStamp + ")\n";
            sendingMessageBytes = sendingMessage.getBytes();
            sendingBytes = new byte[sendingMessageBytes.length + 1];
            sendingBytes[0] = 0x01;   //Unencrypted
            System.arraycopy(sendingMessageBytes, 0, sendingBytes, 1, sendingMessageBytes.length);
            out.write(sendingBytes);
            out.flush();

            //Receive key_exchange_server(server_pubkey)
            receiveLen = in.read(receiveBuffer);

            isEncrypted = receiveBuffer[0];
            //The received message should not be encrypted
            if(isEncrypted == 0x02) {
                return -1;
            }
            receivedMessageBytes = new byte[receiveLen - 1];
            System.arraycopy(receiveBuffer, 1, receivedMessageBytes, 0, receivedMessageBytes.length);
            receivedMessage = new String(receivedMessageBytes);

            //Retrieve command and arguments
            recvCommand = receivedMessage.substring(0, receivedMessage.indexOf('('));
            recvArguments = receivedMessage.substring(receivedMessage.indexOf('(') + 1, receivedMessage.indexOf(')')).split(",");
            if(!(recvCommand.equals("key_exchange_server") && recvArguments.length == 1)) {
                return -1;
            }

            //Store the server_pubkey


            //Reply key_exchange_client(client_pubkey)
            sendingMessage = "key_exchange_client(" + clientPubkey + ")";
            sendingBytes = packMessage(sendingMessage, false);
            out.write(sendingBytes);
            out.flush();

            //Receive request_add_param(server_id)

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Pack (and encrypt) the sending message
    private byte[] packMessage(String message, boolean isEncrypted) {
        byte[] messageBytes;
        byte[] packedMessage = new byte[messageBytes.length + 2];
        if(isEncrypted) {
            //String encryptedMessage =     //Encrypt the message
            //messageBytes = encryptedMessage.getBytes();   //Convert the encrypted message to byte array
            packedMessage[0] = 0x02;    //Set the first byte of the packed byte array
        } else {
            messageBytes = message.getBytes();
            packedMessage[0] = 0x01;
        }
        System.arraycopy(messageBytes, 0, packedMessage, 1, messageBytes.length);   //Copy the message block to the packed array
        packedMessage[packedMessage.length - 1] = (byte)10;   //10 = '\n', set the last byte of the packed array
        return packedMessage;
    }

    //Unpack (and decrypt) the received message from the receive buffer
    private String unpackMessage(byte[] receiveBuffer, int receiveLen) {
        byte encryptedTag = receiveBuffer[0];
        byte endTag = receiveBuffer[receiveLen - 1];
        if(endTag != (byte)10) {
            return null;    //The end of the message is not '\n', which is an error
        }
        //copy the message block to the array messageBlock
        byte[] messageBlock = new byte[receiveLen - 2];
        System.arraycopy(receiveBuffer, 1, messageBlock, 0, messageBlock.length);
        String messageBlockStr = new String(messageBlock);
        String messagePT;
        if(encryptedTag == (byte)0x01) {    //unencrypted
            messagePT = messageBlockStr;
        } else {
            //messagePT =   //Decrypt the message
            messagePT = null;
        }
        return messagePT;
    }
}
